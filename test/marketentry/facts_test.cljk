(ns marketentry.facts-test
  (:require [clojure.test :refer [deftest is testing]]
            [marketentry.facts :as facts]))

(deftest lux-has-spec-basis
  (let [sb (facts/spec-basis "LUX")]
    (is (some? sb))
    (is (string? (:provenance sb)))
    (is (seq (:required-evidence sb)))
    (is (some? (facts/corporate-number-spec-basis "LUX")))
    (is (some? (facts/establishment-conditions-spec-basis "LUX")))
    (is (some? (facts/restricted-procedure-threshold-spec-basis "LUX")))))

(deftest lux-rep-spec-basis-is-honestly-nil
  (testing "not investigated this session -- left nil rather than force-fit, same discipline BEN/ATG established"
    (is (nil? (facts/rep-spec-basis "LUX")))))

(deftest unknown-jurisdiction-has-no-spec-basis
  (is (nil? (facts/spec-basis "ATL")))
  (is (nil? (facts/spec-basis "ZZZ"))))

(deftest required-evidence-satisfied
  (let [sb (facts/spec-basis "LUX")
        all (:required-evidence sb)]
    (is (true? (facts/required-evidence-satisfied? "LUX" all)))
    (is (not (facts/required-evidence-satisfied? "LUX" (take 1 all))))
    (is (nil? (facts/required-evidence-satisfied? "ATL" all)))))

(deftest coverage-is-honest
  (let [c (facts/coverage ["LUX" "USA" "ATL"])]
    (is (= 3 (:requested c)))
    (is (= 2 (:covered c)))
    (is (= ["ATL"] (:missing-jurisdictions c)))))
