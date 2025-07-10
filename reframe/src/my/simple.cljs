(ns my.simple
  (:require [clojure.string :as s]
            [reagent.dom.client :as rdc]
            [re-frame.core :as rf]))

;; -- Domino 1 - Event Dispatch

(defn dispatch-timer-event
  []
  (let [now (js/Date.)]
    (rf/dispatch [:timer now])))

(defonce do-timer (js/setInterval dispatch-timer-event 1000))

;; -- Domino 2 - Event Handlers

(rf/reg-event-db              
 :initialize                 
 (fn [_ _]                   
   {:time (js/Date.)         
    :time-color "orange"}))  

(rf/reg-event-db                
 :time-color-change            
 (fn [db [_ new-color-value]]  
   (assoc db :time-color new-color-value)))   

(rf/reg-event-db                 
 :timer                         
 (fn [db [_ new-time]]          
   (assoc db :time new-time)))  

;; -- Domino 4 - Query

(rf/reg-sub
 :time
 (fn [db _]     
   (:time db))) 

(rf/reg-sub
 :time-color
 (fn [db _]
   (:time-color db)))

;; -- Domino 5 - View Functions

(defn clock
  []
  (let [colour @(rf/subscribe [:time-color])
        time   (-> @(rf/subscribe [:time])
                   .toTimeString
                   (s/split " ")
                   first)]
    [:div.example-clock {:style {:color colour}} time]))

(defn color-input
  []
  (let [gettext (fn [e] (-> e .-target .-value))
        emit    (fn [e] (rf/dispatch [:time-color-change (gettext e)]))]
    [:div.color-input
     "Display color: "
     [:input {:type "text"
              :style {:border "1px solid #CCC"}
              :value @(rf/subscribe [:time-color])        
              :on-change emit}]]))  

(defn ui
 []
 [:div
  [:h1 "The time is now:"]
  [clock]
  [color-input]])

;; -- Entry Point

(defonce root-container
  (rdc/create-root (js/document.getElementById "app")))

(defn mount-ui
  []
  (rdc/render root-container [ui]))

(defn ^:dev/after-load clear-cache-and-render!
  []
  (rf/clear-subscription-cache!)
  (mount-ui))

(defn run               
  []
  (rf/dispatch-sync [:initialize]) 
  (mount-ui))                      
