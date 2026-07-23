(ns statute.facts-test
  (:require [clojure.string :as str]
            [clojure.test :refer [deftest is]]
            [statute.facts :as facts]))

(deftest lux-has-spec-basis
  (let [sb (facts/spec-basis "LUX")]
    (is (= 3 (count sb)))
    (is (every? #(str/starts-with? (:statute/url %) "http") sb))
    (is (every? :statute/law-number sb))))

(deftest unknown-jurisdiction-has-no-spec-basis
  (is (nil? (facts/spec-basis "ATL")))
  (is (nil? (facts/spec-basis "ZZZ"))))

(deftest coverage-is-honest
  (let [c (facts/coverage ["LUX" "JPN" "ATL"])]
    (is (= 3 (:requested c)))
    (is (= 1 (:covered c)))
    (is (= ["ATL" "JPN"] (:missing-jurisdictions c)))))

(deftest by-topic-filters
  (is (= ["lux.code-du-travail"]
         (mapv :statute/id (facts/by-topic "LUX" :labor))))
  (is (empty? (facts/by-topic "LUX" :environment)))
  (is (empty? (facts/by-topic "ATL" :labor))))
