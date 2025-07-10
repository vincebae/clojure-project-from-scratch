(ns my.backend
  (:require [charred.api :as charred]
            [org.httpkit.server :as http-kit]
            [reitit.ring :as reitit-ring]
            [ring.middleware.defaults :as defaults]
            ;; For development from ring-devel
            [ring.middleware.reload :as reload]
            [ring.util.response :as response])
  (:gen-class))

(defn index-html-handler [_request]
  (-> (response/resource-response "public/index.html")
      (response/content-type "text/html")))

;; Example API endpoint
(defn api-hello-handler [_request]
  {:status 200
   :headers {"Content-Type" "application/json"}
   :body (charred/write-json-str
          {:message "Hello from the Clojure JVM Backend!"})})

(def app-routes
  (reitit-ring/ring-handler
   (reitit-ring/router
    [["/" {:get index-html-handler}] ; Serve index.html at root
     ["/api/hello" {:get api-hello-handler}]])
   ;; Add more API routes here
   (reitit-ring/routes
    ;; Serve static files from resources/public
    (reitit-ring/create-resource-handler {:path "/"})
    (reitit-ring/create-default-handler))))

;; Apply middleware
;; For development, ring-defaults/wrap-defaults with site-defaults is good.
;; For production, you'd likely use api-defaults for API routes and secure site-defaults.
;; `wrap-reload` is great for development to auto-reload changed namespaces.
(defonce app (-> #'app-routes ; Use var-quote for reloadability
                 (defaults/wrap-defaults defaults/site-defaults)
                 ;; Only use wrap-reload in development
                 ;; Consider using a :dev profile in deps.edn to conditionally add this
                 (reload/wrap-reload)))

(defonce server (atom nil))

(defn start-server [& [port]]
  (let [port-str (or port (System/getenv "PORT") "3000")
        port-num (try (Integer/parseInt port-str) (catch NumberFormatException _ 3000))]
    (println (str "Starting server on port " port-num))
    (reset! server (http-kit/run-server #'app {:port port-num})) ; Use var-quote for app
    (println (str "Server started. Access at http://localhost:" port-num))))

(defn stop-server []
  (when @server
    (println "Stopping server...")
    (@server :timeout 100) ; Stop the server, timeout in ms
    (reset! server nil)
    (println "Server stopped.")))

