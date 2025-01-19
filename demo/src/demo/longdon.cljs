(ns demo.longdon
 (:require
   [missionary.core :as m]
   [flowy.client :refer [boot-with-retry connector]]
   [flowy.reflower :refer [task]]
  )
   (:import missionary.Cancelled))

(defn print-val [state msg]
  (println "received: " msg))

(defn longdon
   [write read]
   ; this task gets called whenever the connection is established.
   (println "longdon init1")
   ;(println "write: " write)
   ;(println "read: " read)
   ; write:  #object[clojure.core$comp$fn__5876 0x4b8cfe5f clojure.core$comp$fn__5876@4b8cfe5f]
   ;;read:  #object[flowy.ring_adapter$boot_BANG_$fn__8738 0x68e9d993 flowy.ring_adapter$boot_BANG_$fn__8738@68e9d993
   ;(m/seed ["a" "b" "c"]) 
   (let [msg-in (m/stream
                 (m/observe read))]
    (m/sp
        (try
          (println "longdong sp start")
                        ;(m/? (write "123"))
          (m/? (write {:op :exec 
                       :id 33 
                       :fun 'demo.fortune-cookie/get-cookie
                       }))
          (m/?
           (m/reduce print-val 0 msg-in))
          (println "longdon DONE! success!")
          (catch js/Exception ex
            (println "longdon crashed: " ex))
          (catch Cancelled _
            (println "longdon was cancelled.")
             ;(m/? shutdown!)
            true)))))


(defn run-task []
  (let [t1 (task 'demo.fortune-cookie/get-cookie)
        t2 (task 'demo.fortune-cookie/get-cookie)
        t3 (task 'demo.fortune-cookie/get-cookie)
        t4 (task 'demo.fortune-cookie/get-cookie 5)
        ]
    (t1 #(println "task1 finished success:" %)
       #(println "task1 finished error:" %))
    (t2 #(println "task2 finished success:" %)
        #(println "task2 finished error:" %))
    (t3 #(println "task3 finished success:" %)
        #(println "task3 finished error:" %))
    (t4 #(println "task4 (fixed) finished success:" %)
        #(println "task4 (fixed) error:" %))
    (t4 #(println "task4/2 (fixed) success:" %)
        #(println "task4/2 (fixed) error:" %))
    
    ))



(defn start []
  #_((boot-with-retry
    longdon
     connector)  
   #(println "longdon finished success:" %)
   #(println "longdon finished error:" %)
  )
  ; run clj task
  (run-task)
  )

