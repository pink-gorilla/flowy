(ns flowy.reflower
  (:require
   [missionary.core :as m]
   [flowy.executor :as exec])
  (:import
   [missionary Cancelled]))

(defn start-sp [write service {:keys [id] :as clj-call}]
  (let [v (exec/call-fn service clj-call)]
    (m/sp
     (m/? (write {:op :exec
                  :id id
                  :val (m/? v)})))))

(defn start-ap [write service {:keys [id] :as clj-call}]
  (let [f (exec/call-fn service clj-call)]
    (m/reduce (fn [_s v]
                (try 
                  (m/? (write {:op :exec
                    :id id
                    :val v}))
                  (catch Cancelled _
                     (println "ap cancelled on ws close."))))
              nil f)))

(defn start-clj [write service {:keys [id] :as clj-call}]
  (let [v (m/via m/cpu
                 (exec/call-fn service clj-call))]
    (m/sp
     (m/? (write {:op :exec
                  :id id
                  :val (m/? v)})))))

(defn start-executing
  "returns a missionary task which can execute the clj-call"
  [write {:keys [mode] :as service} {:keys [id] :as clj-call}]
  (case mode
    :sp (start-sp write service clj-call)
    :ap (start-ap write service clj-call)
    :clj (start-clj write service clj-call)
    ; default :clj
    (start-clj write service clj-call)))

(defn start-reflower [exs]
  (fn [ring-req]
    ;(println "flomaysta: a")
    ;(println "a: " a)
    (println "reflower client wants to connect with ring-req: " (keys ring-req))
    (fn [write read]
      (let [msg-in (m/stream
                    (m/observe read))
            running (atom {})
            add-task (fn [id t]
                       (swap! running assoc id t))
            remove-task (fn [id]
                          (swap! running dissoc id))
            process-msg (fn [_state {:keys [op id] :as msg}]
                          (case op
                            nil
                            (println "ignoring msg without op: " msg)

                            :exec
                            (if-let [s (exec/get-service exs msg)]
                              (let [t (start-executing write s msg)
                                    dispose! (t
                                              (fn [r]
                                                (println "task completed: " r)
                                                (remove-task id))
                                              (fn [e]
                                                (println "task crashed: " e)
                                                (remove-task id)))]
                                (add-task id dispose!))
                              (println "no task foudn for:" msg))
                            :cancel
                            (when-let [dispose! (get @running id)]
                              (dispose!)
                              (remove-task id))
                                ; else
                            (println "unknown op: " msg)))]
        (m/sp
         (try
           (println "reflower task starting for websocket sessing!")
                        ;(m/? (write "123"))
           (m/? (write {:op :message
                        :val "reflower started"}))
           (m/?
            (m/reduce process-msg 0 msg-in))
           (println "reflower DONE! success!")
           (catch Exception ex
             (println "reflower crashed: " ex))
           (catch Cancelled _
             (println "reflower shutting down..")
             ;(m/? shutdown!)
             true)))))))

