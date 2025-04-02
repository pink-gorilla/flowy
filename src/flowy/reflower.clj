(ns flowy.reflower
  (:require
   [missionary.core :as m]
   [tick.core :as t]
   [human-id.core :refer [human-id]]
   [flowy.executor :as exec]
   [flowy.log :as l])
  (:import
   [missionary Cancelled]
   [java.time.format DateTimeFormatter]
   [java.time ZoneId]))

(def rfc-1123-formatter
  (-> (DateTimeFormatter/ofPattern "EEE, dd MMM yyyy HH:mm:ss 'GMT'")
      (.withZone (ZoneId/of "GMT"))))

(defn cookie-string [name value]
  (let [expiry-date (-> (t/zoned-date-time)
                        (t/>> (t/new-duration (* 365 10) :days))
                        (.format rfc-1123-formatter))]
    (str name "=" value "; expires=" expiry-date "; path=/;")))

(defn start-sp [logger write service {:keys [id] :as clj-call}]
  (m/sp
   (try
     (let [v (exec/call-fn service clj-call)]
       ; success case
       (m/? (write {:op :exec :id id :val (m/? v)})))
     (catch Exception ex
       (m/? (write {:op :exec :id id :err (ex-message ex)}))))))

(defn start-clj [logger write service {:keys [id] :as clj-call}]
  (m/sp
   (try
     (let [v (m/via m/cpu (exec/call-fn service clj-call))]
      ; success case
       (m/? (write {:op :exec
                    :id id
                    :val (m/? v)})))
     (catch Exception ex
       (m/? (write {:op :exec :id id :err (ex-message ex)}))))))

(defn start-ap [logger write service {:keys [id] :as clj-call}]
  (let [f (exec/call-fn service clj-call)]
    (m/reduce (fn [_s v]
                (try
                  (m/? (write {:op :exec
                               :id id
                               :val v}))
                  (catch Cancelled _
                    (l/log logger "ap cancelled on ws close"))))
              nil f)))

(defn start-executing
  "returns a missionary task which can execute the clj-call"
  [logger write {:keys [mode] :as service} {:keys [id] :as clj-call}]
  (case mode
    :sp (start-sp logger write service clj-call)
    :ap (start-ap logger write service clj-call)
    :clj (start-clj logger write service clj-call)
    ; default :clj
    (start-clj logger write service clj-call)))

(defn start-reflower [exs]
  (fn [ring-req]
    ;(println "flomaysta: a")
    ;(println "a: " a)
    (println "reflower client wants to connect with ring-req: " (keys ring-req))
    (let [browser-id (or (:flowy-browser-id ring-req) (human-id))
          logger (l/create-logger browser-id)]
      (l/log logger "\n\nsession started: " browser-id)
      (fn [write read]
        (let [msg-in (m/stream
                      (m/observe read))
              running (atom {})
              add-task (fn [id t]
                         (swap! running assoc id t))
              remove-task (fn [id]
                            (swap! running dissoc id))
              cancel-task (fn [task-id]
                            (if-let [dispose! (get @running task-id)]
                              (do  (l/log logger "cancelling task: " task-id)
                                   (dispose!)
                                   (remove-task task-id))
                              (l/log logger "task to cancel not found: " task-id)))
              cancel-all (fn []
                           (let [task-ids (keys @running)]
                             (l/log logger "cancelling running tasks: " task-ids)
                             (doall (map cancel-task task-ids))
                             (l/log logger "all running tasks cancelled!")))
              process-msg (fn [_state {:keys [op id] :as msg}]
                            (case op
                              nil
                              (l/log logger "ignoring msg without op: " msg)

                              :exec
                              (if-let [s (exec/get-service exs msg)]
                                (let [t (start-executing logger write s msg)
                                      dispose! (t
                                                (fn [r]
                                                  (l/log logger "task completed: " r)
                                                  (remove-task id))
                                                (fn [e]
                                                  (l/log logger "task crashed: " e)
                                                  (remove-task id)))]
                                  (add-task id dispose!))
                                (l/log logger "not task found for msg: " msg))
                              :cancel
                              (cancel-task id)
                            ; else
                              (l/log logger "unknown op: " msg)))]
          (m/sp
           (try
             (l/log logger "reflower task starting for websocket session")
             (m/? (write {:op :message
                          :val browser-id
                          :cookie (cookie-string "flowy-browser-id" browser-id)}))
             (m/?
              (m/reduce process-msg 0 msg-in))
             (l/log logger "reflower finished successfully!")

             (catch Exception ex
               (l/log logger "reflower crashed: " ex)
               (cancel-all))
             (catch Cancelled _
               (l/log logger "reflower got cancelled.")
               (cancel-all)
             ;(m/? shutdown!)
               true))))))))

(comment
  (cookie-string "a" "1")

 ; 
  )

