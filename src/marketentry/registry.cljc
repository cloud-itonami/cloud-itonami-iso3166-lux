(ns marketentry.registry
  "Pure-function market-entry filing-draft + filing-submit record
  construction -- an append-only market-entry book-of-record draft.

  Like every sibling actor's registry, there is no single international
  reference-number standard for a public-procurement market-entry
  filing -- every jurisdiction assigns its own format. This namespace
  does NOT invent one; it builds a jurisdiction-scoped sequence number
  and validates the record's required fields, the same honest,
  non-fabricating discipline `marketentry.facts` uses.

  `engagement-fee-matches-claim?` is an HONEST reapplication of the
  SAME ground-truth-recompute DISCIPLINE sibling actors use (verify a
  claimed monetary total against the entity's own recorded quantity x
  unit fields), reapplied to a market-entry engagement fee line.

  `establishment-conditions-satisfied?` / `establishment-conditions-
  unmet?` are the FLAGSHIP check for this vertical, grounded in a
  genuinely Luxembourg-specific mechanism: Loi modifiée du 2 septembre
  2011 réglementant l'accès aux professions d'artisan, de commerçant,
  d'industriel ainsi qu'à certaines professions libérales (curl/
  headless-Chrome-verified 2026-07-23 against legilux.public.lu's own
  hosting, cross-linked from guichet.public.lu's official 'Demande
  d'autorisation d'établissement' page), Art. 3 ('L'autorisation
  d'établissement requise au préalable pour l'exercice d'une activité
  visée par la présente loi est délivrée par le ministre si les
  conditions d'établissement, d'honorabilité et de qualification
  prévues aux articles 4 à 27 sont remplies'), Art. 4 (the designated
  dirigeant must CUMULATIVELY satisfy four conditions) and Art. 5
  (a genuine fixed place of business is required; a mere domiciliation
  under the loi modifiée du 31 mai 1999 does NOT count as an
  établissement -- read directly in Art. 5's own final alinéa).

  This is a GENUINELY DIFFERENT check SHAPE than every prior iso3166
  sibling this repo mirrors: Bulgaria's ЗОП Art. 54(5) de-minimis is a
  PERCENTAGE-OF-TURNOVER ELIGIBILITY formula, Albania's Neni 76(2)(c)
  carve-out is a FLAT-CONSTANT ELIGIBILITY threshold, Azerbaijan's/
  Armenia's flagship checks are BOOLEAN registry-membership ELIGIBILITY
  reads, Antigua and Barbuda's vendor-class check is a THREE-TIER
  ELIGIBILITY-THRESHOLD classification, Andorra's Art. 30.1 mechanism
  is a TWO-AXIS (contract type x urgency) ELIGIBILITY-THRESHOLD
  classification, Monaco's RCI-clearance mechanism is a BRANCHING gate
  that first determines WHICH evidence type applies, and Benin's Art.
  77 mechanism is a BID-EVALUATION PRICE ADJUSTMENT. Luxembourg's Art.
  3-5 mechanism is NONE of these: it is a CONJUNCTIVE
  (all-N-conditions-must-hold) legal-authorization gate over FIVE
  independently-sourced sub-conditions (qualification+honorabilité,
  permanent effective management, real link to the company, no social/
  tax evasion, and a genuine fixed établissement that is NOT a mere
  domiciliation) -- the first in this family to gate on a conjunction
  of several distinct legal sub-conditions drawn from more than one
  article of the SAME statute, rather than a single threshold, ratio,
  boolean membership read, or branching evidence-type selector. This is
  reported honestly as a seventh distinct check shape for the family,
  not treated as a variant of any prior shape.

  `restricted-procedure-eligible?` / `restricted-procedure-claim-
  mismatches?` are a second, NON-flagship, numeric-threshold check
  applying the family's familiar ground-truth-recompute discipline to
  Loi du 8 avril 2018 sur les marchés publics, Art. 20 § 1er, alinéa 3,
  lettre a) + Règlement grand-ducal du 8 avril 2018, Art. 151, AS
  AMENDED by règlement grand-ducal du 29 mai 2024 (curl/headless-Chrome
  verified 2026-07-23): the CURRENT ceiling for using procédure
  restreinte sans publication d'avis / procédure négociée instead of
  the ordinary open procedure is 79 000 EUR (HT) -- NOT the 60 000 EUR
  the original, unamended 2018 règlement grand-ducal text alone would
  suggest. Unlike Andorra's Art. 30.1, this Luxembourg mechanism does
  NOT differentiate the threshold by contract type or urgency -- Art.
  151's own text applies one uniform ceiling across travaux,
  fournitures et services alike, so this check is a SINGLE flat
  threshold, not a two-axis lookup. Only this ONE quantitative lettre
  (Art. 20 § 1er al. 3 lettre a) + Art. 151) is modeled; Art. 20's
  other lettres (b through m) each turn on a qualitative, case-by-case
  judgment call (non-conforming bids, research/experimentation,
  technical/artistic exclusivity, impérieuse urgency, standardized
  repeat works, police/army/national-protection carve-outs, etc.) that
  this governor cannot independently recompute from an engagement's own
  declared numeric fields -- an honest scope-narrowing, the same
  discipline AND applied to Art. 30.1's own lettres a, b, e, f, g, h.
  The intermediate Art. 20 § 3 band (up to 'quatorze mille euros hors
  TVA, valeur cent 1948', which requires inviting a minimum of three
  candidates) is ALSO deliberately not modeled: this iteration did not
  find a directly-verified CURRENT nominal-euro equivalent for that
  1948-indexed figure (only Art. 151's own 79 000 EUR figure is a
  directly-stated current nominal amount) -- honestly left uncomputed
  rather than guessed at via an unverified indexation ratio.

  This namespace is pure data + pure functions -- no I/O, no network
  call to any real procurement portal or business registry. It builds
  the RECORD an operator would keep, not the act of submitting a portal
  registration itself (that is `marketentry.operation`'s
  `:filing/submit`, always human-gated -- see README Actuation)."
  (:require [clojure.string :as str]))

(defn- unsigned-certificate
  "Every certificate this actor produces is UNSIGNED -- signature is
  the market-entry operator's act, not this actor's."
  [kind subject record-id]
  {"@context" ["https://www.w3.org/ns/credentials/v2"]
   "type" ["VerifiableCredential" kind]
   "credentialSubject" {"id" subject "record" record-id}
   "proof" nil
   "issued_by_registry" false
   "status" "draft-unsigned"})

(defn- zero-pad [n w]
  (let [s (str n)]
    (str (apply str (repeat (max 0 (- w (count s))) "0")) s)))

(defn compute-engagement-fee
  "The ground-truth engagement fee for `engagement`'s own `:base-fee`
  and `:monitoring-months` x `:monthly-rate` -- a single flat
  base + months x rate calculation, not a full pricing engine."
  [{:keys [base-fee monthly-rate monitoring-months]}]
  (+ (double base-fee)
     (* (double monthly-rate) (double monitoring-months))))

(defn engagement-fee-matches-claim?
  "Does `engagement`'s own `:claimed-fee` equal the independently
  recomputed `compute-engagement-fee`?"
  [{:keys [claimed-fee] :as engagement}]
  (== (double claimed-fee) (compute-engagement-fee engagement)))

;; ------------------- FLAGSHIP: établissement conditions -------------------

(def establishment-condition-keys
  "The five Art. 4 / Art. 5 sub-conditions, each independently sourced,
  that must ALL be true for `establishment-conditions-satisfied?`.
  Field names mirror the law's own vocabulary rather than a generic
  placeholder, so a governor `:detail` message can name the SPECIFIC
  missing condition."
  [:etablissement-qualification-confirmed?
   :etablissement-permanent-management-confirmed?
   :etablissement-real-link-confirmed?
   :etablissement-fiscal-social-clean?
   :etablissement-fixed-place-not-domiciliation-only?])

(defn establishment-conditions-satisfied?
  "Does `engagement` satisfy ALL FIVE Art. 4 / Art. 5 conditions? Missing
  or false on ANY key is never satisfied -- this is a CONJUNCTION, not a
  majority vote or a weighted score. `:etablissement-fixed-place-not-
  domiciliation-only?` specifically encodes Art. 5's own final alinéa:
  a mere domiciliation (loi modifiée du 31 mai 1999) does not count as
  an établissement, so an engagement that only has a domiciliation
  service must have this key false, not merely unset."
  [engagement]
  (every? true? (map #(true? (get engagement %)) establishment-condition-keys)))

(defn establishment-missing-conditions
  "Which of the five Art. 4 / Art. 5 condition keys are NOT true on
  `engagement` -- used to build a specific, honest hold detail rather
  than a generic 'conditions unmet' message."
  [engagement]
  (vec (remove #(true? (get engagement %)) establishment-condition-keys)))

(defn establishment-conditions-unmet?
  "For an engagement that DOES require autorisation d'établissement
  (`:requires-etablissement-authorization?` true -- most public-sector
  market-entry engagements do; Loi du 2 septembre 2011 documents real,
  narrow exemptions such as EU temporary cross-border service
  providers, which this catalog does not model as a boolean flag but
  which a future iteration could), are any of the five conditions
  unsatisfied? An engagement that does NOT require the authorization is
  never 'unmet' on this ground -- this check is CONDITIONAL on the
  engagement's own declared ground truth, the same discipline AND's
  `:requires-nrt?`/`:nrt-verified?` conditional check applies."
  [{:keys [requires-etablissement-authorization?] :as engagement}]
  (boolean
   (and (true? requires-etablissement-authorization?)
        (not (establishment-conditions-satisfied? engagement)))))

;; ------------------- restricted-procedure threshold (non-flagship) -------------------

(def restricted-procedure-threshold-eur
  "Loi du 8 avril 2018 sur les marchés publics, Art. 20 § 1er, alinéa 3,
  lettre a) + Règlement grand-ducal du 8 avril 2018, Art. 151, AS
  AMENDED by règlement grand-ducal du 29 mai 2024 Art. 1er
  (curl/headless-Chrome-verified 2026-07-23 against legilux.public.lu's
  official hosting): the CURRENT EUR (hors TVA) ceiling for using
  procédure restreinte sans publication d'avis / procédure négociée
  instead of the ordinary open procedure. Single flat figure -- unlike
  AND's Art. 30.1, Luxembourg's Art. 151 does not differentiate by
  contract type."
  79000.0)

(defn restricted-procedure-eligible?
  "Does `engagement`'s own declared `:contract-value` (EUR, HT) qualify
  for the light-touch procédure restreinte sans publication d'avis /
  procédure négociée under Art. 20 § 1er al. 3 lettre a) + Art. 151 (as
  amended 2024)? Missing/unknown `:contract-value` is never eligible --
  this governor does not guess at a contract's own value."
  [{:keys [contract-value]}]
  (boolean
   (when (number? contract-value)
     (< (double contract-value) restricted-procedure-threshold-eur))))

(defn restricted-procedure-claim-mismatches?
  "Does `engagement`'s own declared `:light-touch-procedure-claim?`
  differ from the INDEPENDENTLY recomputed Art. 151 (as amended)
  eligibility? Catches BOTH directions honestly: claiming light-touch
  eligibility a non-urgent above-79 000-EUR contract does not actually
  support, and an eligible engagement being declared ineligible."
  [{:keys [light-touch-procedure-claim?] :as engagement}]
  (not= (boolean light-touch-procedure-claim?)
        (restricted-procedure-eligible? engagement)))

;; ------------------- draft / submit records -------------------

(defn register-draft
  "Validate + construct the FILING-DRAFT registration DRAFT -- the
  market-entry operator's own act of preparing a portal registration
  package. Pure function -- does not touch any real procurement
  portal."
  [engagement-id jurisdiction sequence]
  (when-not (and engagement-id (not= engagement-id ""))
    (throw (ex-info "draft: engagement_id required" {})))
  (when-not (and jurisdiction (not= jurisdiction ""))
    (throw (ex-info "draft: jurisdiction required" {})))
  (when (< sequence 0)
    (throw (ex-info "draft: sequence must be >= 0" {})))
  (let [draft-number (str (str/upper-case jurisdiction) "-DFT-" (zero-pad sequence 6))
        record {"record_id" draft-number
                "kind" "filing-draft"
                "engagement_id" engagement-id
                "jurisdiction" jurisdiction
                "immutable" true}]
    {"record" record "draft_number" draft-number
     "certificate" (unsigned-certificate "FilingDraft" draft-number draft-number)}))

(defn register-submit
  "Validate + construct the FILING-SUBMIT registration DRAFT -- the
  market-entry operator's own act of actually submitting a portal
  registration (always human-gated upstream)."
  [engagement-id jurisdiction sequence]
  (when-not (and engagement-id (not= engagement-id ""))
    (throw (ex-info "submit: engagement_id required" {})))
  (when-not (and jurisdiction (not= jurisdiction ""))
    (throw (ex-info "submit: jurisdiction required" {})))
  (when (< sequence 0)
    (throw (ex-info "submit: sequence must be >= 0" {})))
  (let [submit-number (str (str/upper-case jurisdiction) "-SUB-" (zero-pad sequence 6))
        record {"record_id" submit-number
                "kind" "filing-submit"
                "engagement_id" engagement-id
                "jurisdiction" jurisdiction
                "immutable" true}]
    {"record" record "submit_number" submit-number
     "certificate" (unsigned-certificate "FilingSubmit" submit-number submit-number)}))

(defn append [history result]
  (conj (vec history) (get result "record")))
