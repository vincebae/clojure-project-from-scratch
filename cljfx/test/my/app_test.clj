(ns my.app-test
  (:require [clojure.test :refer [deftest testing is]]))

(deftest sample
  (testing "1 is 1"
    (is (= 1 1))))
