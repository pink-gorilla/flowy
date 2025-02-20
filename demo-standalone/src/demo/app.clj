(ns demo.app
  (:require
   [missionary.core :as m]
   [reitit.ring :as ring]
   [ring.adapter.jetty :refer [run-jetty]]
   [ring.middleware.resource :refer [wrap-resource]]
   [ring.middleware.content-type :refer [wrap-content-type]]
   [ring.middleware.not-modified :refer [wrap-not-modified]]
   [ring.util.response :as response]
   [ring.websocket :as ws]
   [flowy.ring-adapter :refer [ring-ws-handler wrap-electric-websocket handler-ws]]
   [flowy.executor :as exec]
   [flowy.jetty-config :refer [jetty-configurator]]
   [flowy.reflower :refer [start-reflower]]
   ;[demo.raw :refer [flowmaysta]]
   )
  (:import
   [missionary Cancelled]))


;; Static file handler for index.html
#_(def static-handler
    (-> (fn [_] (response/resource-response "index.html" {:root "public"}))
        wrap-resource
        wrap-content-type
        wrap-not-modified))


(defn make-handler [system]
  (ring/ring-handler
   ; router
   (ring/router
    [["/" {:handler (fn [_]
                      (response/resource-response "public/index.html"))}]
     ["/ping" {:get (fn [_] {:status 200 :body "pong"})}]
     ;"time"   {:get demo.handler/time-handler}
     ["/flowy" {:handler (handler-ws system)}]
     ["/r/*" (ring/create-resource-handler)]
     ;["/r/*" (ring/create-resource-handler {:path "public" :root "/r/"})]
     ]
    {:data {;:db db
            :middleware [;my-middleware
                         ;parameters/parameters-middleware
                         ;wrap-keyword-params
                         ;middleware-db
                         ]}})
   ; default handler
   (ring/routes
    (ring/create-default-handler
     {:not-found (constantly {:status 404 :body "Not found"})}))))


;; Run Jetty server
(defn -main [& args]
  (let [port 9000
        exs (exec/start-executor {} [; sp 
                                     {:fun 'demo.fortune-cookie/get-cookie}
                                     {:fun 'demo.calculator/add}
                                     {:fun 'demo.calculator/subtract}
                                     ; ap
                                     {:fun 'demo.counter/counter-fn :mode :ap}])
        rf (start-reflower exs)
        ;h (make-handler flowmaysta)
        h (make-handler rf)]
    ; test of executor service
    (println "demo cookie: " (exec/exec-clj exs {:fun 'demo.fortune-cookie/get-cookie}))
    ; start webserver
    (println (str "Starting server on http://localhost:" port))
    (run-jetty h {:join? false
                  :port port
                  :ip "0.0.0.0"
                  :configurator jetty-configurator})))