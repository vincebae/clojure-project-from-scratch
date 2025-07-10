;; start cljs session.
(shadow.cljs.devtools.api/repl :client)

;; exit cljs session.
:cljs/quit

(ns user
  (:require
   [clojure.string :as s]
   [reagent.dom.client :as rdc]
   [re-frame.core :as rf]
   [my.simple :as my]))

(rf/dispatch [:time-color-change "blue"])
