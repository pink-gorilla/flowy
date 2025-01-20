(ns demo.standalone
 (:require
   [missionary.core :as m]
   [flowy.reflower :refer [task flow]]))

;; runs tasks and flows and prints it to the browser console.

(defn run-task []
  (let [t1 (task 'demo.fortune-cookie/get-cookie)
        t2 (task 'demo.fortune-cookie/get-cookie)
        t3 (task 'demo.fortune-cookie/get-cookie)
        t4 (task 'demo.fortune-cookie/get-cookie 5)
        f1 (flow 'demo.counter/counter-fn)
        fprinter (m/reduce (fn [_ v]
                             (println "demo flow value: " v))
                           nil
                           f1)
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
    (fprinter #(println "f1 success:" %) #(println "f1 error:" %))))

(defn start []
  ; run clj task
  (run-task)
  )

