(ns my.app
  (:require [my.backend :refer [start-server stop-server]])
  (:gen-class))

(defn init
  [_]
  {})

(defn start
  [_]
  (start-server))

(defn stop
  [_]
  (stop-server))

(defn -main
  [& args]
  (start (init args)))
