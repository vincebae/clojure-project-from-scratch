#!/usr/bin/env bb

(ns my-namespace
  (:require
   [clojure.tools.cli :refer [parse-opts]]))

(def ^:private cli-options
  [["-h" "--help" "show help messages"]])

(defn- print-help
  [arg-map]
  (println "arg-map:")
  (println arg-map))

(defn- run
  [args opts]
  (println "args:" args)
  (println "opts:" opts))

(defn -main
  [& args]
  (try
    (let [{:keys [arguments options] :as arg-map} (parse-opts args cli-options)]
      (cond
        (:help options) (print-help arg-map)
        :else (run arguments options)))
    (catch Exception _
      (println "Invalid input"))))

(apply -main *command-line-args*)
