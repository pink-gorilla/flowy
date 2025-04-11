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
  

(defn counter-bad-fn []
  ; should return an ap, but to simulate something bad, we return nil
  nil)



(def counter-ex
  (m/stream
   (m/ap
    (println "creating counter")
    (loop [i 0]
      (m/amb
       (if (= i 3)
         (throw (ex-info "counter-ex" {:message "i is 3, so it throws an ex."}))
         (m/? (m/sleep 5000 i)))
       (recur (inc i)))))))


(defn counter-ex-fn []
  counter-ex)
  