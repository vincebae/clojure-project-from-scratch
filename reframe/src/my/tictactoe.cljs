(ns my.tictactoe
  (:require [clojure.core.match :refer [match]]
            [reagent.dom.client :as rdc]
            [re-frame.core :as rf]))

;; Event Handlers

(rf/reg-event-db
 :initialize
 (fn [_ _]
   {:turn 0
    :win nil
    :board-history [(vec (repeat 9 :empty))]}))

(defn- get-square-value
  [turn]
  (if (even? turn) :x :o))

(def ^:private ^:const lines
  [[0 1 2] [3 4 5] [6 7 8] [0 3 6] [1 4 7] [2 5 8] [0 4 8] [2 4 6]])

(defn- check-win-status
  [board]
  (reduce
   (fn [_ indices]
     (let [line (mapv #(get board %) indices)]
       (match line
         [:x :x :x] (reduced :x)
         [:o :o :o] (reduced :o)
         _ nil)))
   nil
   lines))

(rf/reg-event-db
 :square-clicked
 (fn [{:keys [board-history turn win] :as db} [_ index]]
   (if (and (not win) (= (get-in board-history [turn index]) :empty))
     (let [curr (get-square-value turn)
           new-board (assoc (get board-history turn) index curr)
           new-turn (inc turn)
           new-history (-> board-history
                           (subvec 0 new-turn)
                           (conj new-board))]
       (-> db
           (update :turn inc)
           (assoc :board-history new-history)
           (assoc :win (check-win-status new-board))))
     db)))

(rf/reg-event-db
 :history-clicked
 (fn [db [_ turn]]
   (let [board (get-in db [:board-history turn])
         new-win (check-win-status board)]
     (assoc db :turn turn :win new-win))))

;; Query

(rf/reg-sub
 :turn
 (fn [db _] (:turn db)))

(rf/reg-sub
 :board-history
 (fn [db _] (:board-history db)))

(rf/reg-sub
 :win
 (fn [db _] (:win db)))

(rf/reg-sub
 :board
 (fn [_] [(rf/subscribe [:turn]) (rf/subscribe [:board-history])])

 (fn [[turn board-history] _] (get board-history turn)))

(rf/reg-sub
 :board-square
 (fn [_] (rf/subscribe [:board]))

 (fn [board [_ index]]
   (get board index)))

;; View Functions

(defn- square->str
  [x]
  (case x
    :x "X"
    :o "O"
    ""))

(defn square-button
  [{:keys [index]}]
  (let [board-square @(rf/subscribe [:board-square index])
        value (square->str board-square)
        click (fn [e] (rf/dispatch [:square-clicked index]))]
    [:button.square {:on-click click} value]))

(defn board
  []
  [:<>
   [:div.board-row
    [square-button {:index 0}]
    [square-button {:index 1}]
    [square-button {:index 2}]]
   [:div.board-row
    [square-button {:index 3}]
    [square-button {:index 4}]
    [square-button {:index 5}]]
   [:div.board-row
    [square-button {:index 6}]
    [square-button {:index 7}]
    [square-button {:index 8}]]])

(defn status
  []
  (let [win @(rf/subscribe [:win])
        turn @(rf/subscribe [:turn])
        square-value (get-square-value turn)]
    [:div.status (cond
                   win (str (square->str win) " won!")
                   (= turn 9) "Draw..."
                   :else (str (square->str square-value) "'s turn..."))]))

(defn go-to-button
  [{:keys [turn]}]
  (let [click (fn [e] (rf/dispatch [:history-clicked turn]))
        text (if (zero? turn)
               "Go to game start"
               (str "Go to move #" turn))]
    [:li {:key turn} [:button {:on-click click} text]]))

(defn game-info
  []
  (let [board-history @(rf/subscribe [:board-history])]
    [:div.game-info
     [:ol
      (for [x (range (count board-history))]
        (go-to-button {:turn x}))]]))

(defn ui
  []
  [:<>
   [:h2 "Tic-Tac-Toe"]
   [:div.game
    [:div
     [status]
     [:div [board]]]
    [game-info]]])

;; Entry point

(defonce root-container
  (rdc/create-root (js/document.getElementById "app")))

(defn mount-ui [] (rdc/render root-container [ui]))

(defn run
  []
  (rf/dispatch-sync [:initialize])
  (mount-ui))

(defn ^:dev/after-load clear-cache-and-render!
  []
  (rf/clear-subscription-cache!)
  (run))

