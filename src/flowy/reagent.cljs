(ns flowy.reagent
  (:require
   [reagent.core :as r]
   [missionary.core :as m]))

(defn flow->ratom [f initial-value]
  (println "FLOW INIT initial value: " initial-value "flow: " f)
  (let [curr-a (r/atom initial-value)
        dispose! (let [_ (println "FLOW SUBSCRIBE")
                       task (m/reduce (fn [_r v]
                                        (println "NEW FLOW VALUE: " v)
                                        (reset! curr-a v)
                                        v) initial-value f)]
                   (task
                    #(println "task completed. error: " %)
                    #(println "task crashed. error: " %)))]
    [curr-a dispose!]))

(defn task->ratom [t initial-value]
  (println "TASK INIT initial value: " initial-value)
  (let [curr-a (r/atom initial-value)
        dispose! (let [_ (println "GETTING TASK")
                       task (m/sp
                             (let [v (m/? t)]
                               (println "NEW TASK VALUE: " v)
                               (reset! curr-a v)
                               v))]
                   (task
                    #(println "task completed. value: " %)
                    #(println "task crashed. error: " %)))]
    [curr-a dispose!]))

