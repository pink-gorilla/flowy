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
   )
   (:import
    (org.eclipse.jetty.server.handler.gzip GzipHandler)
    (org.eclipse.jetty.websocket.server.config JettyWebSocketServletContainerInitializer JettyWebSocketServletContainerInitializer$Configurator)
    [missionary Cancelled]))


;; Static file handler for index.html
#_(def static-handler
  (-> (fn [_] (response/resource-response "index.html" {:root "public"}))
      wrap-resource
      wrap-content-type
      wrap-not-modified))

 ;((entrypoint ring-req) (comp write-msg io/encode) (fn [cb] (read-msg (comp cb io/decode)))))


(defn print-val [state msg]
  (println "flomaysta received: " msg)
  
  )

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
           true)
         
         ))  
        
        )
      
      )
    )
  ([write ?read] 
   ;(rec write ?read pst)
   (println "flomaysta 1")
   (m/seed ["a" "b" "c" "d"])
   )
  ([write ?read on-error]
   (println "flomaysta 2")
   (m/seed ["a" "b" "c"])))

(defn electric-websocket-middleware
  "Open a websocket and boot an Electric server program defined by `entrypoint`.
  Takes:
  - a ring handler `next-handler` to call if the request is not a websocket upgrade (e.g. the next middleware in the chain),
  - a `config` map eventually containing {:hyperfiddle.electric/user-version <version>} to ensure client and server share the same version,
    - see `hyperfiddle.electric-ring-adapter/wrap-reject-stale-client`
  - an Electric `entrypoint`: a function (fn [ring-request] (e/boot-server {} my-ns/My-e-defn ring-request))
  "
  [next-handler entrypoint]
  ;; Applied bottom-up
  (-> (wrap-electric-websocket next-handler entrypoint) ; 5. connect electric client
    ; 4. this is where you would add authentication middleware (after cookie parsing, before Electric starts)
      ;(cookies/wrap-cookies) ; 3. makes cookies available to Electric app
      ;(wrap-params)
      )) ; 1. parse query params

(defn not-found-handler [_ring-request]
  (-> (response/not-found "Not found")
      (response/content-type "text/plain")))

(def handler-ws 
  (-> not-found-handler
      (wrap-electric-websocket flowmaysta)))

 
 

(def handler 
  (ring/ring-handler
   (ring/router
    [;["/" {:handler static-handler}]
     ["/ws" {:handler handler-ws}]]
    {:data {;:db db
            :middleware [;my-middleware
                         ;parameters/parameters-middleware
                         ;wrap-keyword-params
                         ;middleware-db
                         ]}})
   (ring/routes
    ;(ring/create-resource-handler {:path "/"})
    (ring/create-default-handler
     {:not-found (constantly {:status 404 :body "Not found"})}))))


(defn- add-gzip-handler!
  "Makes Jetty server compress responses. Optional but recommended."
  [server]
  (.setHandler server
               (doto (GzipHandler.)
                 #_(.setIncludedMimeTypes (into-array ["text/css" "text/plain" "text/javascript" "application/javascript" "application/json" "image/svg+xml"])) ; only compress these
                 (.setMinGzipSize 1024)
                 (.setHandler (.getHandler server)))))

(defn- configure-websocket!
  "Tune Jetty Websocket config for Electric compat." [server]
  (JettyWebSocketServletContainerInitializer/configure
   (.getHandler server)
   (reify JettyWebSocketServletContainerInitializer$Configurator
     (accept [_this _servletContext wsContainer]
       (.setIdleTimeout wsContainer (java.time.Duration/ofSeconds 60))
       (.setMaxBinaryMessageSize wsContainer (* 100 1024 1024)) ; 100M - temporary
       (.setMaxTextMessageSize wsContainer (* 100 1024 1024))   ; 100M - temporary
       ))))

;; Run Jetty server
(defn -main [& args]
  (let [port 9000]
    (println (str "Starting server on http://localhost:" port))
    (run-jetty handler {:join? false
                        :port port
                        :ip "0.0.0.0"
                        :configurator (fn [server]
                                        (configure-websocket! server)
                                        (add-gzip-handler! server))
                        } )))