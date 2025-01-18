(ns app
  (:require
   [missionary.core :as m]
   [reitit.ring :as ring]
   [ring.adapter.jetty :refer [run-jetty]]
   [ring.middleware.resource :refer [wrap-resource]]
   [ring.middleware.content-type :refer [wrap-content-type]]
   [ring.middleware.not-modified :refer [wrap-not-modified]]
   [ring.util.response :as response]
   [ring.websocket :as ws]
   [flowy.ring-adapter :refer [ring-ws-handler wrap-electric-websocket]]
   [flowy.executor :as exec]
   [flowy.jetty-config :refer [jetty-configurator]]
   )
  (:import
  
   [missionary Cancelled]))


;; Static file handler for index.html
#_(def static-handler
    (-> (fn [_] (response/resource-response "index.html" {:root "public"}))
        wrap-resource
        wrap-content-type
        wrap-not-modified))

 ;((entrypoint ring-req) (comp write-msg io/encode) (fn [cb] (read-msg (comp cb io/decode)))))


(defn print-val [state msg]
  (println "flomaysta received: " msg))

(defn flowmaysta
  ([a]
    ;(println "flomaysta: a")
    ;(println "a: " a)
   (println "FLOMAYSTA INIT FROM A NEW RING REQ.")
    ;
   (fn [write read]
      ;(println "write: " write)
      ;(println "read: " read)
      ; write:  #object[clojure.core$comp$fn__5876 0x4b8cfe5f clojure.core$comp$fn__5876@4b8cfe5f]
      ;;read:  #object[flowy.ring_adapter$boot_BANG_$fn__8738 0x68e9d993 flowy.ring_adapter$boot_BANG_$fn__8738@68e9d993
      ;(m/seed ["a" "b" "c"]) 
     (let [msg-in (m/stream
                   (m/observe read))]
       (m/sp
        (try
          (println "I am the flomaysta app!")
                        ;(m/? (write "123"))
          (m/? (write "flomaysta-init"))
          (m/?
           (m/reduce print-val 0 msg-in))
          (println "FLOMAYSTA DONE! success!")
          (catch Exception ex
            (println "flo crashed: " ex))
          (catch Cancelled _
            (println "flomaysta shutting down..")
             ;(m/? shutdown!)
            true))))))
  ([write ?read]
   ;(rec write ?read pst)
   (println "flomaysta 1")
   (m/seed ["a" "b" "c" "d"]))
  ([write ?read on-error]
   (println "flomaysta 2")
   (m/seed ["a" "b" "c"])))


(defn not-found-handler [_ring-request]
  (-> (response/not-found "Not found")
      (response/content-type "text/plain")))

(def handler-ws
  (-> not-found-handler
      (wrap-electric-websocket flowmaysta)))




(def handler
  (ring/ring-handler
   (ring/router
    [["/" {:handler (fn [_]
                      (response/resource-response "public/index.html"))}]
     ["/ws" {:handler handler-ws}]
     ["/r/*" (ring/create-resource-handler)]
     ;["/r/*" (ring/create-resource-handler {:path "public" :root "/r/"})]
     ]
    {:data {;:db db
            :middleware [;my-middleware
                         ;parameters/parameters-middleware
                         ;wrap-keyword-params
                         ;middleware-db
                         ]}})
   (ring/routes
    (ring/create-default-handler
     {:not-found (constantly {:status 404 :body "Not found"})}))))


;; Run Jetty server
(defn -main [& args]
  (let [port 9000
        exs (exec/start-executor {:services [{:fun 'demo.fortune-cookie/get-cookie}
                                             {:fun 'demo.calculator/add}
                                             {:fun 'demo.calculator/subtract}]})]
    (println "demo cookie: " (exec/exec-clj exs {:fun 'demo.fortune-cookie/get-cookie}))
    (println (str "Starting server on http://localhost:" port))
    (run-jetty handler {:join? false
                        :port port
                        :ip "0.0.0.0"
                        :configurator jetty-configurator})))