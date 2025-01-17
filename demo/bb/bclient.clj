
(ns bclient
  (:require 
   [babashka.http-client.websocket :as bws]))

(defn on-message [message]
  (println "Received message:" message))

(defn on-open [ws]
  (println "WebSocket connection established.")
  ;; Send a message once connected
  ;(http/send ws "Hello, WebSocket!")
   (bws/send! ws "Hello World!")
  )


(defn start-client []
  (let [ws-url "ws://localhost:9000/ws"
        ws (bws/websocket 
                    {:uri ws-url
                     :on-open on-open
                     :on-close (fn [ws status reason]
                                 (println "WebSocket closed! status: " status "reason: " reason))
                     :on-message  (fn [ws msg last?]
                                   (println "Received message:" msg))})]
    ;; Keep the program running to keep the WebSocket connection open
    (Thread/sleep 5000)
    (bws/send! ws "HEARTBEAT")
    (Thread/sleep 5000)
    (bws/send! ws "HEARTBEAT")
    (Thread/sleep 5000)
    (bws/send! ws "HEARTBEAT")
    (Thread/sleep 5000)
    (bws/send! ws "HEARTBEAT")
    (Thread/sleep 5000)
    (bws/send! ws "BONGO")
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

