;; start cljs session.
(shadow.cljs.devtools.api/repl :client)

;; exit cljs session.
:cljs/quit

(ns user
  (:require
   [clojure.string :as s]
   [reagent.dom.client :as rdc]
   [re-frame.core :as rf]
   [my.tictactoe :as my]))

(my.tictactoe/check-win-status [:x :o :o :empty :x :empty :empty :empty :x])
(my.tictactoe/check-win-status @(rf/subscribe [:board]))
@(rf/subscribe [:board])
@(rf/subscribe [:win])
