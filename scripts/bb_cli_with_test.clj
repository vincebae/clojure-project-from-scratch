#!/usr/bin/env bb

(ns my-namespace
  (:require
   [babashka.cli :refer [parse-args]]
   [clojure.test :refer [run-tests deftest is testing]]))

(def ^:private cli-options
  {:alias {:h :help
           :t :test}
   :coerce {:help :boolean
            :test :boolean}})

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
    (let [{:keys [args opts] :as arg-map} (parse-args args cli-options)]
      (cond
        (:test opts) (run-tests 'my-namespace)
        (:help opts) (print-help arg-map)
        :else (run args opts)))
    (catch Exception _
      (println "Invalid input"))))

(apply -main *command-line-args*)
