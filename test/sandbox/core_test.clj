(ns sandbox.core-test
  (:require [clojure.test :refer [deftest testing is]]
            [sandbox.core :as core]))

(deftest greet-test
  (testing "greet with no args"
    (is (= (core/greet []) "Hello, World!"))
    (is (= (core/greet nil) "Hello, World!")))
  (testing "greet with args"
    (is (= (core/greet ["a" "b" "c"]) "Hello, a, b, c!"))
    (is (= (core/greet [1 2 3]) "Hello, 1, 2, 3!"))))
