(ns my.app
  (:require [cljfx.api :as fx])
  (:gen-class))

(defonce state (atom nil))
(defonce renderer (atom nil))

(defn title-input [{:keys [title]}]
  {:fx/type :text-field
   :on-text-changed {:event/type ::set-title}
   :text title})

(defn todo-view [{:keys [text id done]}]
  {:fx/type :h-box
   :spacing 5
   :padding 5
   :children [{:fx/type :check-box
               :selected done
               :on-selected-changed {:event/type ::set-done :id id}}
              {:fx/type :label
               :style {:-fx-text-fill (if done :grey :black)}
               :text text}]})

(defn close-button [_]
  {:fx/type :button
   :text "close"
   :on-action {:event/type ::close-app}})

(defn root [{:keys [title showing]}]
  {:fx/type :stage
   :showing showing
   :title title
   :scene {:fx/type :scene
           :root {:fx/type :v-box
                  :children [{:fx/type :label
                              :text "Window title input"}
                             {:fx/type title-input
                              :title title}
                             {:fx/type todo-view
                              :text "Task 1"
                              :id 1
                              :done false}
                             {:fx/type todo-view
                              :text "Task 2"
                              :id 2
                              :done false}
                             {:fx/type close-button}]}}})

(declare stop)
(defn map-event-handler [event]
  (case (:event/type event)
    ::set-title (swap! state assoc :title (:fx/event event))
    ::set-done (swap! state assoc-in [:by-id (:id event) :done] (:fx/event event))
    ::close-app (stop @state)))

(defn create-renderer
  []
  (fx/create-renderer
   :middleware (fx/wrap-map-desc assoc :fx/type root)
   :opts {:fx.opt/map-event-handler map-event-handler}))

(defn init
  [_]
  {:title "My Cljfx App" :showing true})

(defn start
  [initial-state]
  (reset! state initial-state)
  (reset! renderer (create-renderer))
  (fx/mount-renderer state @renderer))

(defn stop
  [_]
  (fx/unmount-renderer state @renderer)
  (reset! state nil)
  (reset! renderer nil))

(defn -main
  [& args]
  (start (init {})))
