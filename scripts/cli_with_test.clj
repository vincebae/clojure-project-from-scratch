#!/usr/bin/env bb

(ns my-namespace
  (:require
   [clojure.tools.cli :refer [parse-opts]]
   [clojure.test :refer [run-tests deftest is testing]]))

(def ^:private cli-options
  [["-h" "--help" "show help messages"]
   ["-t" "--test" "run tests"]])

(defn- print-help
  [arg-map]
  (println "arg-map:")
  (println arg-map))

(defn- run
  [args opts]
  (println "args:" args)
  (println "opts:" opts))

;; Test cases
(deftest test-example
  (testing "test example"
    (is (= (+ 1 2) 3))))

(defn -main
  [& args]
  (try
    (let [{:keys [arguments options] :as arg-map} (parse-opts args cli-options)]
      (cond
        (:test options) (run-tests 'my-namespace)
        (:help options) (print-help arg-map)
        :else (run arguments options)))
    (catch Exception _
      (println "Invalid input"))))

(apply -main *command-line-args*)
