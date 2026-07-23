(ns marketentry.store
  "SSoT for the Luxembourg (LUX) market-entry compliance actor, behind a
  `Store` protocol so the backend is a swap, not a rewrite -- the same
  seam every prior cloud-itonami actor in this fleet uses.

    - `MemStore`     -- atom of EDN. The deterministic default for
                        dev/tests/demo (no deps).
    - `DatomicStore` -- backed by `langchain.db`, a Datomic-API-compatible
                        EAV store.

  Both implement the same protocol and pass the same contract
  (test/marketentry/store_contract_test.clj).

  The primary entity here is an `engagement` -- filing-draft and
  filing-submit actuation events apply SEQUENTIALLY to the SAME
  engagement record (draft first, submit later). Dedicated
  double-actuation-guard booleans (`:drafted?`/`:submitted?`, never a
  `:status` value).

  The ledger stays append-only on every backend.

  Uses `langchain-store.core` (`ls/*`) for the EDN-blob codec + event-log
  read/append seam, never a hand-rolled enc/dec* (ADR-2607141600)."
  (:require [marketentry.registry :as registry]
            [langchain.db :as d]
            [langchain-store.core :as ls]))

(defprotocol Store
  (engagement [s id])
  (all-engagements [s])
  (assessment-of [s engagement-id] "committed jurisdiction assessment, or nil")
  (ledger [s])
  (draft-history [s] "the append-only filing-draft history")
  (submit-history [s] "the append-only filing-submit history")
  (next-draft-sequence [s jurisdiction])
  (next-submit-sequence [s jurisdiction])
  (engagement-already-drafted? [s engagement-id])
  (engagement-already-submitted? [s engagement-id])
  (commit-record! [s record] "apply a committed op's record to the SSoT")
  (append-ledger! [s fact]   "append one immutable decision fact")
  (with-engagements [s engagements] "replace/seed the engagement directory"))

;; ----------------------------- demo data -----------------------------

(defn demo-data
  "A small, self-contained engagement set covering both actuation
  lifecycles (draft, submit) plus the governor's own new checks.
  `:requires-etablissement-authorization?` + the five
  `:etablissement-*-confirmed?` keys are ground truth for the FLAGSHIP
  `establishment-conditions-unmet` check (Loi du 2 septembre 2011 Arts.
  3-5); `:contract-value` / `:light-touch-procedure-claim?` are ground
  truth for the `restricted-procedure-threshold-mismatch` check (Loi du
  8 avril 2018 Art. 20 + RGD Art. 151 as amended 2024)."
  []
  {:engagements
   {"eng-1" {:id "eng-1" :operator "Kaell Digital Services SARL" :portal "pmp.lu"
             :base-fee 500000 :monthly-rate 30000 :monitoring-months 12
             :claimed-fee 860000.0
             :contract-value 45000 :light-touch-procedure-claim? true
             :requires-etablissement-authorization? true
             :etablissement-qualification-confirmed? true
             :etablissement-permanent-management-confirmed? true
             :etablissement-real-link-confirmed? true
             :etablissement-fiscal-social-clean? true
             :etablissement-fixed-place-not-domiciliation-only? true
             :drafted? false :submitted? false
             :jurisdiction "LUX" :status :intake}
    "eng-2" {:id "eng-2" :operator "Atlantis LLC" :portal "pmp.lu"
             :base-fee 500000 :monthly-rate 30000 :monitoring-months 12
             :claimed-fee 860000.0
             :contract-value 45000 :light-touch-procedure-claim? true
             :requires-etablissement-authorization? true
             :etablissement-qualification-confirmed? true
             :etablissement-permanent-management-confirmed? true
             :etablissement-real-link-confirmed? true
             :etablissement-fiscal-social-clean? true
             :etablissement-fixed-place-not-domiciliation-only? true
             :drafted? false :submitted? false
             :jurisdiction "ATL" :status :intake}
    "eng-3" {:id "eng-3" :operator "Kirchberg Consulting SA" :portal "pmp.lu"
             :base-fee 500000 :monthly-rate 30000 :monitoring-months 12
             :claimed-fee 999000.0
             :contract-value 45000 :light-touch-procedure-claim? true
             :requires-etablissement-authorization? true
             :etablissement-qualification-confirmed? true
             :etablissement-permanent-management-confirmed? true
             :etablissement-real-link-confirmed? true
             :etablissement-fiscal-social-clean? true
             :etablissement-fixed-place-not-domiciliation-only? true
             :drafted? false :submitted? false
             :jurisdiction "LUX" :status :intake}
    "eng-4" {:id "eng-4" :operator "Moselle Bau et Fournitures SARL" :portal "pmp.lu"
             :base-fee 500000 :monthly-rate 30000 :monitoring-months 12
             :claimed-fee 860000.0
             :contract-value 95000 :light-touch-procedure-claim? true
             :requires-etablissement-authorization? true
             :etablissement-qualification-confirmed? true
             :etablissement-permanent-management-confirmed? true
             :etablissement-real-link-confirmed? true
             :etablissement-fiscal-social-clean? true
             :etablissement-fixed-place-not-domiciliation-only? true
             :drafted? false :submitted? false
             :jurisdiction "LUX" :status :intake}
    "eng-5" {:id "eng-5" :operator "Gantenbein Domiciliation Only Sarl-S" :portal "pmp.lu"
             :base-fee 500000 :monthly-rate 30000 :monitoring-months 12
             :claimed-fee 860000.0
             :contract-value 45000 :light-touch-procedure-claim? true
             :requires-etablissement-authorization? true
             :etablissement-qualification-confirmed? true
             :etablissement-permanent-management-confirmed? true
             :etablissement-real-link-confirmed? true
             :etablissement-fiscal-social-clean? true
             :etablissement-fixed-place-not-domiciliation-only? false
             :drafted? false :submitted? false
             :jurisdiction "LUX" :status :intake}}})

;; ----------------------------- shared commit logic -----------------------------

(defn- draft-filing!
  [s engagement-id]
  (let [e (engagement s engagement-id)
        seq-n (next-draft-sequence s (:jurisdiction e))
        result (registry/register-draft engagement-id (:jurisdiction e) seq-n)]
    {:result result
     :engagement-patch {:drafted? true
                        :draft-number (get result "draft_number")}}))

(defn- submit-filing!
  [s engagement-id]
  (let [e (engagement s engagement-id)
        seq-n (next-submit-sequence s (:jurisdiction e))
        result (registry/register-submit engagement-id (:jurisdiction e) seq-n)]
    {:result result
     :engagement-patch {:submitted? true
                        :submit-number (get result "submit_number")}}))

;; ----------------------------- MemStore (default) -----------------------------

(defrecord MemStore [a]
  Store
  (engagement [_ id] (get-in @a [:engagements id]))
  (all-engagements [_] (sort-by :id (vals (:engagements @a))))
  (assessment-of [_ engagement-id] (get-in @a [:assessments engagement-id]))
  (ledger [_] (:ledger @a))
  (draft-history [_] (:draft-records @a))
  (submit-history [_] (:submit-records @a))
  (next-draft-sequence [_ jurisdiction] (get-in @a [:draft-sequences jurisdiction] 0))
  (next-submit-sequence [_ jurisdiction] (get-in @a [:submit-sequences jurisdiction] 0))
  (engagement-already-drafted? [_ engagement-id] (boolean (get-in @a [:engagements engagement-id :drafted?])))
  (engagement-already-submitted? [_ engagement-id] (boolean (get-in @a [:engagements engagement-id :submitted?])))
  (commit-record! [s {:keys [effect path value payload]}]
    (case effect
      :engagement/upsert
      (swap! a update-in [:engagements (:id value)] merge value)

      :assessment/set
      (swap! a assoc-in [:assessments (first path)] payload)

      :engagement/mark-drafted
      (let [engagement-id (first path)
            {:keys [result engagement-patch]} (draft-filing! s engagement-id)
            jurisdiction (:jurisdiction (engagement s engagement-id))]
        (swap! a (fn [state]
                   (-> state
                       (update-in [:draft-sequences jurisdiction] (fnil inc 0))
                       (update-in [:engagements engagement-id] merge engagement-patch)
                       (update :draft-records registry/append result))))
        result)

      :engagement/mark-submitted
      (let [engagement-id (first path)
            {:keys [result engagement-patch]} (submit-filing! s engagement-id)
            jurisdiction (:jurisdiction (engagement s engagement-id))]
        (swap! a (fn [state]
                   (-> state
                       (update-in [:submit-sequences jurisdiction] (fnil inc 0))
                       (update-in [:engagements engagement-id] merge engagement-patch)
                       (update :submit-records registry/append result))))
        result)
      nil)
    s)
  (append-ledger! [_ fact] (swap! a update :ledger conj fact) fact)
  (with-engagements [s engagements] (when (seq engagements) (swap! a assoc :engagements engagements)) s))

(defn seed-db
  "A MemStore seeded with the demo engagement set."
  []
  (->MemStore (atom (assoc (demo-data)
                           :assessments {}
                           :ledger [] :draft-sequences {} :draft-records []
                           :submit-sequences {} :submit-records []))))

;; ----------------------------- DatomicStore (langchain.db) -----------------------------

(def ^:private schema
  {:engagement/id                   {:db/unique :db.unique/identity}
   :assessment/engagement-id        {:db/unique :db.unique/identity}
   :ledger/seq                      {:db/unique :db.unique/identity}
   :draft-record/seq                {:db/unique :db.unique/identity}
   :submit-record/seq               {:db/unique :db.unique/identity}
   :draft-sequence/jurisdiction     {:db/unique :db.unique/identity}
   :submit-sequence/jurisdiction    {:db/unique :db.unique/identity}})

(defn- engagement->tx [{:keys [id operator portal base-fee monthly-rate monitoring-months claimed-fee
                               contract-value light-touch-procedure-claim?
                               requires-etablissement-authorization?
                               etablissement-qualification-confirmed?
                               etablissement-permanent-management-confirmed?
                               etablissement-real-link-confirmed?
                               etablissement-fiscal-social-clean?
                               etablissement-fixed-place-not-domiciliation-only?
                               drafted? submitted?
                               jurisdiction status draft-number submit-number]}]
  (cond-> {:engagement/id id}
    operator                              (assoc :engagement/operator operator)
    portal                                (assoc :engagement/portal portal)
    base-fee                              (assoc :engagement/base-fee base-fee)
    monthly-rate                          (assoc :engagement/monthly-rate monthly-rate)
    monitoring-months                     (assoc :engagement/monitoring-months monitoring-months)
    claimed-fee                           (assoc :engagement/claimed-fee claimed-fee)
    contract-value                        (assoc :engagement/contract-value contract-value)
    (some? light-touch-procedure-claim?)  (assoc :engagement/light-touch-procedure-claim? light-touch-procedure-claim?)
    (some? requires-etablissement-authorization?)
    (assoc :engagement/requires-etablissement-authorization? requires-etablissement-authorization?)
    (some? etablissement-qualification-confirmed?)
    (assoc :engagement/etablissement-qualification-confirmed? etablissement-qualification-confirmed?)
    (some? etablissement-permanent-management-confirmed?)
    (assoc :engagement/etablissement-permanent-management-confirmed? etablissement-permanent-management-confirmed?)
    (some? etablissement-real-link-confirmed?)
    (assoc :engagement/etablissement-real-link-confirmed? etablissement-real-link-confirmed?)
    (some? etablissement-fiscal-social-clean?)
    (assoc :engagement/etablissement-fiscal-social-clean? etablissement-fiscal-social-clean?)
    (some? etablissement-fixed-place-not-domiciliation-only?)
    (assoc :engagement/etablissement-fixed-place-not-domiciliation-only? etablissement-fixed-place-not-domiciliation-only?)
    (some? drafted?)                      (assoc :engagement/drafted? drafted?)
    (some? submitted?)                    (assoc :engagement/submitted? submitted?)
    jurisdiction                          (assoc :engagement/jurisdiction jurisdiction)
    status                                (assoc :engagement/status status)
    draft-number                          (assoc :engagement/draft-number draft-number)
    submit-number                         (assoc :engagement/submit-number submit-number)))

(def ^:private engagement-pull
  [:engagement/id :engagement/operator :engagement/portal :engagement/base-fee :engagement/monthly-rate
   :engagement/monitoring-months :engagement/claimed-fee
   :engagement/contract-value :engagement/light-touch-procedure-claim?
   :engagement/requires-etablissement-authorization?
   :engagement/etablissement-qualification-confirmed?
   :engagement/etablissement-permanent-management-confirmed?
   :engagement/etablissement-real-link-confirmed?
   :engagement/etablissement-fiscal-social-clean?
   :engagement/etablissement-fixed-place-not-domiciliation-only?
   :engagement/drafted? :engagement/submitted?
   :engagement/jurisdiction :engagement/status :engagement/draft-number :engagement/submit-number])

(defn- pull->engagement [m]
  (when (:engagement/id m)
    {:id (:engagement/id m) :operator (:engagement/operator m) :portal (:engagement/portal m)
     :base-fee (:engagement/base-fee m) :monthly-rate (:engagement/monthly-rate m)
     :monitoring-months (:engagement/monitoring-months m) :claimed-fee (:engagement/claimed-fee m)
     :contract-value (:engagement/contract-value m)
     :light-touch-procedure-claim? (boolean (:engagement/light-touch-procedure-claim? m))
     :requires-etablissement-authorization? (boolean (:engagement/requires-etablissement-authorization? m))
     :etablissement-qualification-confirmed? (boolean (:engagement/etablissement-qualification-confirmed? m))
     :etablissement-permanent-management-confirmed? (boolean (:engagement/etablissement-permanent-management-confirmed? m))
     :etablissement-real-link-confirmed? (boolean (:engagement/etablissement-real-link-confirmed? m))
     :etablissement-fiscal-social-clean? (boolean (:engagement/etablissement-fiscal-social-clean? m))
     :etablissement-fixed-place-not-domiciliation-only? (boolean (:engagement/etablissement-fixed-place-not-domiciliation-only? m))
     :drafted? (boolean (:engagement/drafted? m)) :submitted? (boolean (:engagement/submitted? m))
     :jurisdiction (:engagement/jurisdiction m) :status (:engagement/status m)
     :draft-number (:engagement/draft-number m) :submit-number (:engagement/submit-number m)}))

(defrecord DatomicStore [conn]
  Store
  (engagement [_ id]
    (pull->engagement (d/pull (d/db conn) engagement-pull [:engagement/id id])))
  (all-engagements [_]
    (->> (d/q '[:find [?id ...] :where [?e :engagement/id ?id]] (d/db conn))
         (map #(pull->engagement (d/pull (d/db conn) engagement-pull [:engagement/id %])))
         (sort-by :id)))
  (assessment-of [_ engagement-id]
    (ls/dec* (d/q '[:find ?p . :in $ ?eid
                   :where [?a :assessment/engagement-id ?eid] [?a :assessment/payload ?p]]
                 (d/db conn) engagement-id)))
  (ledger [_] (ls/read-stream conn :ledger/seq :ledger/fact))
  (draft-history [_] (ls/read-stream conn :draft-record/seq :draft-record/record))
  (submit-history [_] (ls/read-stream conn :submit-record/seq :submit-record/record))
  (next-draft-sequence [_ jurisdiction]
    (or (d/q '[:find ?n . :in $ ?j
              :where [?e :draft-sequence/jurisdiction ?j] [?e :draft-sequence/next ?n]]
            (d/db conn) jurisdiction)
        0))
  (next-submit-sequence [_ jurisdiction]
    (or (d/q '[:find ?n . :in $ ?j
              :where [?e :submit-sequence/jurisdiction ?j] [?e :submit-sequence/next ?n]]
            (d/db conn) jurisdiction)
        0))
  (engagement-already-drafted? [s engagement-id]
    (boolean (:drafted? (engagement s engagement-id))))
  (engagement-already-submitted? [s engagement-id]
    (boolean (:submitted? (engagement s engagement-id))))
  (commit-record! [s {:keys [effect path value payload]}]
    (case effect
      :engagement/upsert
      (d/transact! conn [(engagement->tx value)])

      :assessment/set
      (d/transact! conn [{:assessment/engagement-id (first path) :assessment/payload (ls/enc payload)}])

      :engagement/mark-drafted
      (let [engagement-id (first path)
            {:keys [result engagement-patch]} (draft-filing! s engagement-id)
            jurisdiction (:jurisdiction (engagement s engagement-id))
            next-n (inc (next-draft-sequence s jurisdiction))]
        (d/transact! conn
                     [(engagement->tx (assoc engagement-patch :id engagement-id))
                      {:draft-sequence/jurisdiction jurisdiction :draft-sequence/next next-n}
                      {:draft-record/seq (count (draft-history s)) :draft-record/record (ls/enc (get result "record"))}])
        result)

      :engagement/mark-submitted
      (let [engagement-id (first path)
            {:keys [result engagement-patch]} (submit-filing! s engagement-id)
            jurisdiction (:jurisdiction (engagement s engagement-id))
            next-n (inc (next-submit-sequence s jurisdiction))]
        (d/transact! conn
                     [(engagement->tx (assoc engagement-patch :id engagement-id))
                      {:submit-sequence/jurisdiction jurisdiction :submit-sequence/next next-n}
                      {:submit-record/seq (count (submit-history s)) :submit-record/record (ls/enc (get result "record"))}])
        result)
      nil)
    s)
  (append-ledger! [s fact]
    (ls/append-blob! conn :ledger/seq :ledger/fact (count (ledger s)) fact)
    fact)
  (with-engagements [s engagements]
    (when (seq engagements) (d/transact! conn (mapv engagement->tx (vals engagements)))) s))

(defn datomic-store
  ([] (datomic-store {}))
  ([{:keys [engagements]}]
   (let [s (->DatomicStore (d/create-conn schema))]
     (with-engagements s engagements))))

(defn datomic-seed-db
  []
  (datomic-store (demo-data)))
