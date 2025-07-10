(ns my.app
  (:require [clojure.string :as s])
  (:gen-class))

(defn greet
  [xs]
  (if (empty? xs)
    "Hello, World!",
    (str "Hello, " (s/join ", " xs) "!")))

(defn init
  [args]
  (println "Init.")
  args)

(defn start
  [system]
  (println "Start.")
  (println (greet system)))

(defn stop
  [system]
  (println "Stop."))

(defn -main
  [& args]
  (start (init args)))
