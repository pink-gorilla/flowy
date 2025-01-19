(ns flowmaysta
  (:require 
    [missionary.core :as m]
   )
   (:import
   [missionary Cancelled])
  )


 ;((entrypoint ring-req) (comp write-msg io/encode) (fn [cb] (read-msg (comp cb io/decode)))))


(defn print-val [state msg]
  (println "flomaysta received: " msg))

(defn flowmaysta
  ([ring-req]
    ;(println "flomaysta: a")
    ;(println "a: " a)
   (println "FLOMAYSTA INIT FROM A NEW RING REQ: " ring-req)
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
  #_([write ?read]
   ;(rec write ?read pst)
   (println "flomaysta 1")
   (m/seed ["a" "b" "c" "d"]))
  #_([write ?read on-error]
   (println "flomaysta 2")
   (m/seed ["a" "b" "c"])))