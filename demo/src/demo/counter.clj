(ns demo.counter
  (:require
   [missionary.core :as m]))

(def counter
  (m/stream
   (m/ap
    (println "creating counter")
    (loop [i 0]
      (m/amb
       (m/? (m/sleep 5000 i))
       ;(println "i: " i)
       (recur (inc i)))))))

(defn counter-fn []
  counter)
  