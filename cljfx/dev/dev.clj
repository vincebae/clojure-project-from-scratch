;; Various dev tools especially for better REPL based development
;; Influenced by 
;; - [moon](https://github.com/damn/moon)
;; - [Clojure Workflow Reloaded](https://www.cognitect.com/blog/2013/06/04/clojure-workflow-reloaded)
(ns dev
  (:require [clj-commons.pretty.repl :as p]
            [clojure.java.io :as io]
            [clojure.tools.namespace.repl :refer [disable-reload! refresh]]
            [my.app]
            [nrepl.server :as nrepl])
  (:gen-class))

(disable-reload!)

(defonce nrepl-server (atom nil))
(defonce system (atom nil))

(def main-ns "my.app")

(defn init!
  [& args]
  (require (symbol main-ns))
  (let [init-fn (ns-resolve (symbol main-ns) 'init)]
    (reset! system (init-fn args))))

(defn start!
  []
  (require (symbol main-ns))
  (let [start-fn (ns-resolve (symbol main-ns) 'start)]
    (start-fn @system)))

(defn stop!
  []
  (require (symbol main-ns))
  (let [stop-fn (ns-resolve (symbol main-ns) 'stop)]
    (stop-fn @system)))

(defn go!
  "init! and start!"
  [& args]
  (refresh)
  (apply init! args)
  (start!))

(defn refresh!
  "stop!, refresh and go!"
  [& args]
  (try
    (stop!)
    (apply go! args)
    (catch Throwable t
      (binding [*print-level* 3]
        (p/pretty-pst t 24)))))

(defn- start-nrepl-server!
  "Start nrepl server and write the port to .nrepl-port file"
  []
  (let [server (nrepl/start-server)
        port (:port server)
        port-file (io/file ".nrepl-port")]
    (.deleteOnExit ^java.io.File port-file)
    (spit port-file port)
    server))

(defn -main [& _args]
  (println "Start dev main")
  (reset! nrepl-server (start-nrepl-server!))
  (println "Started nrepl server on port" (:port @nrepl-server))
  (go!))
