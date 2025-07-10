(ns my.frontend
  (:require
   [ajax.core :as ajax]
   ;; [day8.re-frame.tracing :refer-macros [fn-traced]]
   [re-frame.core :as rf]
   [reagent.dom :as rdom]))

;; Re-frame DB
(def default-db
  {:greeting "Hello from Re-frame!"
   :backend-message nil
   :loading? false})

;; Event Handlers

;; Initialize database
(rf/reg-event-db
 ::initialize-db
 (fn [_ _]
   default-db))

;; Update greeting
(rf/reg-event-db
 ::set-greeting
 (fn [db [_ new-greeting]]
   (assoc db :greeting new-greeting)))

;; Fetch message from backend
(rf/reg-event-fx
 ::fetch-backend-message
 (fn [{:keys [db]} _]
   ;; We return a map describing the effect we want to occur.
   ;; In this case, we want to set the :loading? flag in app-db
   ;; and then trigger an AJAX call as a separate side-effect.
   {:db (assoc db :loading? true)
    :dispatch-later [{:ms 10 ; give db a chance to update
                      :dispatch [::actually-do-the-fetch]}]}))

(rf/reg-event-fx
 ::actually-do-the-fetch
 (fn [_cofx _event-vec]
   ;; No changes to db in this handler, just the side-effecting AJAX call.
   ;; cljs-ajax.core/ajax-request returns a core.async channel.
   ;; We are not directly using the channel here but relying on callbacks.
   (ajax/ajax-request
     {:method          :get
      :uri             "/api/hello"
      :response-format (ajax/json-response-format {:keywords? true})
      :handler         #(rf/dispatch [::fetch-backend-message-success %]) ; Note: % is the full response
      :error-handler   #(rf/dispatch [::fetch-backend-message-failure %])}) ; Note: % is the error response
   {})) ; No further effects from this handler itself

;; Success handler remains largely the same, but ensure it handles the raw response correctly
(rf/reg-event-db
 ::fetch-backend-message-success
 (fn [db [_ response]] ; response is now the direct response from ajax-request
   (println "backend-message: " (get-in response [1 :message]))
   (-> db
       (assoc :backend-message (get-in response [1 :message]))
       (assoc :loading? false))))

(rf/reg-event-db
 ::fetch-backend-message-failure
 (fn [db [_ error-response]] ; error-response is now the direct error from ajax-request
  (js/console.error "Failed to fetch from backend (manual handler):" error-response)
  (-> db
      (assoc :backend-message "Error fetching message (manual).")
      (assoc :loading? false))))

;; Subscriptions

(rf/reg-sub
 ::greeting
 (fn [db _]
   (:greeting db)))

(rf/reg-sub
 ::backend-message
 (fn [db _]
   (:backend-message db)))

(rf/reg-sub
 ::loading?
 (fn [db _]
   (:loading? db)))

;; Views

(defn greeting-panel []
  (let [greeting @(rf/subscribe [::greeting])
        backend-message @(rf/subscribe [::backend-message])
        loading? @(rf/subscribe [::loading?])]
    [:div
     [:h1 greeting]
     [:p (if loading?
           "Loading message from backend..."
           (or backend-message "Click the button to fetch a message from the backend."))]
     [:button {:on-click #(rf/dispatch [::fetch-backend-message])}
      "Fetch from Backend"]
     [:br] [:br]
     [:input {:type "text"
              :value greeting
              :on-change #(rf/dispatch [::set-greeting (-> % .-target .-value)])}]
     (when @(rf/subscribe [::loading?]) ; Example of using 10x for debugging
       [:p.loading "Loading... (10x can show this too!)"])]))

(defn main-panel []
 [:div
  [greeting-panel]])

;; Add more component here

;; Entry point

(defn mount-root []
  ;; Good for hot-reloading
  (rf/clear-subscription-cache!)
  (let [target-el (.getElementById js/document "app")]
    (rdom/render [main-panel] target-el)))

(defn init! []
  (rf/dispatch-sync [::initialize-db]) ; Initialize app state
  (mount-root))

;; This is called by shadow-cljs on every hot-reload
;; :after-load in shadow-cljs.edn
(defn ^:dev/after-load re-render []
  (mount-root))
