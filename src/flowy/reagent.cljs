(ns flowy.reagent
  (:require
   [reagent.core :as r]
   [missionary.core :as m]))

(defn flow->ratom [f initial-value]
  (println "FLOW INIT initial value: " initial-value "flow: " f)
  (let [curr-a (r/atom initial-value)
        dispose! (let [_ (println "flow->ratom SUBSCRIBE")
                       task (m/reduce (fn [_r v]
                                        (println "flow->ratom VALUE: " v)
                                        (reset! curr-a v)
                                        v) initial-value f)]
                   (task
                    #(println "flow->ratom completed:  " %)
                    #(println "flow->ratom error: " %)))]
    [curr-a dispose!]))

(defn task->ratom [t initial-value]
  (println "TASK INIT initial value: " initial-value)
  (let [curr-a (r/atom initial-value)
        dispose! (let [_ (println "task->ratom exec")
                       task (m/sp
                             (let [v (m/? t)]
                               (println "task->ratom VALUE: " v)
                               (reset! curr-a v)
                               v))]
                   (task
                    #(println "task->ratom completed. value: " %)
                    #(println "task->ratom crashed: " %)))]
    [curr-a dispose!]))

