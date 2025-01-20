(ns flowy.uix
   (:require
    ; ["use-sync-external-store/with-selector" :refer [useSyncExternalStoreWithSelector]]
    [uix.core :as uix]
    [react :as r]
    [missionary.core :as m]))


 #_(defn- use-sync-external-store [subscribe get-snapshot]
     (useSyncExternalStoreWithSelector
      subscribe
      get-snapshot
      nil ;; getServerSnapshot, only needed for SSR
      identity ;; selector, not using, just returning the value itself
      =)) ;; value equality check

 (defn create-flow-subscriber [f initial-value]
   (println "FLOW INIT initial value: " initial-value)
   (let [curr-v (uix.core/use-ref initial-value)
         get-snapshot (fn []
                        (println "GET-SNAPSHOT: " @curr-v)
                        @curr-v)
         subscribe (fn [notify-fn]
                     (println "FLOW SUBSCRIBE")
                     (let [task (m/reduce (fn [_r v]
                                            (println "NEW FLOW VALUE: " v)
                                            (reset! curr-v v)
                                            (println "NOTIFYING..")
                                            (notify-fn)
                                            v) initial-value f)
                           dispose! (task
                                      #(println "task completed. error: " %)
                                       #(println "task crashed. error: " %))
                           ]
                      (fn []
                        (println "FLOW DISPOSE")
                        (dispose!)) 
                       ))]
     {:get-snapshot get-snapshot
      :subscribe subscribe}))

(defn create-flow-store [f initial-value]
   (println "FLOW INIT initial value: " initial-value "flow: " f)
   (let [curr-v (atom initial-value)
         get-snapshot (fn []
                        (println "GET-SNAPSHOT: " @curr-v)
                        @curr-v)
         subscribe (fn [notify-fn]
                     (println "FLOW SUBSCRIBE")
                     (let [task (m/reduce (fn [_r v]
                                            (println "NEW FLOW VALUE: " v)
                                            (reset! curr-v v)
                                            (println "NOTIFYING..")
                                            (notify-fn)
                                            v) initial-value f)
                           dispose! (task
                                     #(println "task completed. error: " %)
                                     #(println "task crashed. error: " %))]
                       (fn []
                         (println "FLOW DISPOSE")
                         (dispose!))))]
     {:get-snapshot get-snapshot
      :subscribe subscribe}))
  

(defn ->js-deps [coll]
  `(cljs.core/array (uix.hooks.alpha/use-clj-deps ~coll)))

(defn use-memo [fun deps]
  (let [jsdeps (->js-deps deps)]
    (r/useMemo (fn []
                 (apply fun deps)
                 ) (clj->js deps))))

(defn use-flow [f initial-value]
    ;;  manage subscription via hooks
  (let [x (use-memo create-flow-store [f initial-value])
        {:keys [subscribe get-snapshot]} x]
    (uix/use-sync-external-store subscribe get-snapshot)))


