(ns marketentry.registry-test
  (:require [clojure.test :refer [deftest is testing]]
            [marketentry.registry :as registry]))

(deftest engagement-fee-recompute
  (let [e {:base-fee 500000 :monthly-rate 30000 :monitoring-months 12 :claimed-fee 860000.0}]
    (is (== 860000.0 (registry/compute-engagement-fee e)))
    (is (true? (registry/engagement-fee-matches-claim? e))))
  (let [bad {:base-fee 500000 :monthly-rate 30000 :monitoring-months 12 :claimed-fee 999000.0}]
    (is (false? (registry/engagement-fee-matches-claim? bad)))))

(deftest register-draft-and-submit
  (let [d (registry/register-draft "eng-1" "LUX" 0)
        s (registry/register-submit "eng-1" "LUX" 0)]
    (is (= "LUX-DFT-000000" (get d "draft_number")))
    (is (= "LUX-SUB-000000" (get s "submit_number")))
    (is (nil? (get-in d ["certificate" "proof"])))
    (is (= "draft-unsigned" (get-in s ["certificate" "status"])))))

(deftest register-requires-ids
  (is (thrown? Exception (registry/register-draft "" "LUX" 0)))
  (is (thrown? Exception (registry/register-submit "eng-1" "" 0))))

;; ------------------- FLAGSHIP: établissement conditions -------------------

(def all-conditions-met
  {:etablissement-qualification-confirmed? true
   :etablissement-permanent-management-confirmed? true
   :etablissement-real-link-confirmed? true
   :etablissement-fiscal-social-clean? true
   :etablissement-fixed-place-not-domiciliation-only? true})

(deftest establishment-conditions-satisfied-requires-all-five
  (testing "all five Art. 4 / Art. 5 conditions true -> satisfied"
    (is (true? (registry/establishment-conditions-satisfied? all-conditions-met))))
  (testing "any single condition false -> NOT satisfied (conjunction, not majority)"
    (doseq [k (keys all-conditions-met)]
      (is (false? (registry/establishment-conditions-satisfied? (assoc all-conditions-met k false)))
          (str k " being false must fail the conjunction"))))
  (testing "a missing (unset) condition is treated the same as false"
    (is (false? (registry/establishment-conditions-satisfied? (dissoc all-conditions-met :etablissement-real-link-confirmed?))))))

(deftest establishment-missing-conditions-names-the-gap
  (is (= [:etablissement-fixed-place-not-domiciliation-only?]
         (registry/establishment-missing-conditions
          (assoc all-conditions-met :etablissement-fixed-place-not-domiciliation-only? false))))
  (is (empty? (registry/establishment-missing-conditions all-conditions-met))))

(deftest establishment-conditions-unmet-is-conditional-on-requiring-it
  (testing "engagement that does NOT require autorisation d'établissement -> never unmet on this ground"
    (is (false? (registry/establishment-conditions-unmet?
                 (assoc all-conditions-met
                        :requires-etablissement-authorization? false
                        :etablissement-fixed-place-not-domiciliation-only? false)))))
  (testing "engagement that DOES require it, all conditions met -> not unmet"
    (is (false? (registry/establishment-conditions-unmet?
                 (assoc all-conditions-met :requires-etablissement-authorization? true)))))
  (testing "engagement that DOES require it, domiciliation-only (Art. 5 final alinéa carve-out) -> unmet"
    (is (true? (registry/establishment-conditions-unmet?
                (assoc all-conditions-met
                       :requires-etablissement-authorization? true
                       :etablissement-fixed-place-not-domiciliation-only? false))))))

;; ------------------- restricted-procedure threshold (non-flagship) -------------------

(deftest restricted-procedure-eligible-below-79000-eur
  (testing "below the 79 000 EUR ceiling (RGD Art. 151 as amended 2024) -> eligible"
    (is (true? (registry/restricted-procedure-eligible? {:contract-value 45000}))))
  (testing "at or above the ceiling -> not eligible on threshold grounds"
    (is (false? (registry/restricted-procedure-eligible? {:contract-value 79000})))
    (is (false? (registry/restricted-procedure-eligible? {:contract-value 95000})))))

(deftest restricted-procedure-eligible-unknown-value-is-never-eligible
  (is (false? (registry/restricted-procedure-eligible? {:contract-value nil})))
  (is (false? (registry/restricted-procedure-eligible? {}))))

(deftest restricted-procedure-claim-mismatch
  (testing "claimed eligibility matches the recompute -> no mismatch"
    (is (false? (registry/restricted-procedure-claim-mismatches?
                 {:contract-value 45000 :light-touch-procedure-claim? true}))))
  (testing "claiming light-touch eligibility an above-threshold contract does not support -> mismatch"
    (is (true? (registry/restricted-procedure-claim-mismatches?
                {:contract-value 95000 :light-touch-procedure-claim? true}))))
  (testing "an eligible engagement declared ineligible -> mismatch (catches both directions)"
    (is (true? (registry/restricted-procedure-claim-mismatches?
                {:contract-value 45000 :light-touch-procedure-claim? false})))))
