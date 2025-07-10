(ns user
  "Scratch pad for dev"
  (:require ;;'[clj-commons.pretty.repl :as p]
   [clojure.core.async :as a]
   [clojure.java.io :as io]
   [clojure.pprint :refer [pprint]]
   [clojure.string :as s]
   [clojure.test :as test]
   [my.backend :as backend :refer [server start-server stop-server]]))
           ; '[clojure.tools.namespace.repl :as repl])

server
(start-server)
(stop-server)

; (defn restart []
;  (stop)
;  (repl/refresh :after 'user/start))

