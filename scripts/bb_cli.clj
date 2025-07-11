#!/usr/bin/env bb

(ns my-namespace
  (:require
   [babashka.cli :refer [parse-args]]))

(def ^:private cli-options
  {:alias {:h :help}
   :coerce {:help :boolean}})

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
    (let [{:keys [args opts] :as arg-map} (parse-args args cli-options)]
      (cond
        (:help opts) (print-help arg-map)
        :else (run args opts)))
    (catch Exception _
      (println "Invalid input"))))

(apply -main *command-line-args*)
