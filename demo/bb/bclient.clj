
(ns bclient
  (:require 
   [cognitect.transit :as transit]
   [clojure.java.io :as io]
   [babashka.http-client.websocket :as bws])
   (:import 
     (java.io ByteArrayInputStream ByteArrayOutputStream)))



  (defn decode [x]
    (let [ar (.array x)
          ;_ (println "ar:" ar)
          tjson (String. ar)
          ;_ (println "str: " tjson)
          in (io/input-stream (.getBytes tjson))
          reader (transit/reader in :json)
          v (transit/read reader)]
      ;(println "v: " v)
      v
      ))
  

(defn encode [v]
  (let [out (ByteArrayOutputStream.) ;; Use babashka.io/output-stream
        writer (transit/writer out :json)
        _ (transit/write writer v)
        s (.toString out) ; (.toByteArray out)
        ]
    ;(println "Transit-JSON encoded:" s)
    s
    ))


(defn on-message [message]
  ;(println "Received message:" message)
  (println "rcvd: " (decode message))
  )

(defn send! [ws v]
  (->> (encode v)
       (bws/send! ws))
  (println "sent: " v))

(defn on-open [ws]
  (println "WebSocket connection established.")
  ;; Send a message once connected
  ;(http/send ws "Hello, WebSocket!")
   (send! ws {:op :message 
              :val "Hello World!"})
  )


(defn start-client []
  (let [ws-url "ws://localhost:9000/ws"
        ws (bws/websocket 
                    {:uri ws-url
                     :on-open on-open
                     :on-close (fn [ws status reason]
                                 (println "WebSocket closed! status: " status "reason: " reason))
                     :on-message  (fn [ws msg last?]
                                    ;(println "Received message:" msg)
                                    (on-message msg)
                                    
                                    )})]
    ;; Keep the program running to keep the WebSocket connection open
    (send! ws {:op :exec
           :fun 'demo.fortune-cookie/get-cookie
           :id 99}) 
    (send! ws {:op :exec
               :fun 'demo.fortune-cookie/get-cookie
               :args [3]
               :id 123}) 
    (send! ws {:op :exec
               :fun 'demo.fortune-cookie/get-cookie
               :args [3]
               :id 456}) 
    (send! ws {:op :exec
               :fun 'demo.fortune-cookie/get-cookie
               :id 777}) 
    (send! ws {:op :exec
               :fun 'demo.counter/counter-fn
               :id "x347"}) 

    (Thread/sleep 5000)
    (send! ws {:a 1 :b "b" :y [1 2 3]}) 
    
    (Thread/sleep 5000)
    (bws/send! ws "HEARTBEAT")
    (Thread/sleep 5000)
    (bws/send! ws "HEARTBEAT")
    (Thread/sleep 5000)
    (bws/send! ws "HEARTBEAT")
    (Thread/sleep 5000)
    (send! ws "BONGO")
    (Thread/sleep 5000)
    (bws/send! ws "HEARTBEAT")
    (Thread/sleep 5000)
    (bws/send! ws "HEARTBEAT")
    (Thread/sleep 5000)
    (bws/send! ws "HEARTBEAT")
    (Thread/sleep 5000)
    (bws/send! ws "HEARTBEAT")
    (Thread/sleep 5000)
    (bws/send! ws "HEARTBEAT")
    (println "done here.")
    ;; Close the connection after 5 seconds
    (bws/close! ws)))

