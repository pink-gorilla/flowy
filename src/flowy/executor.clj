(ns flowy.executor
  ;(:require 
    ;[clojure.set :refer [select-keys]]
   ;)
  )


(defn- resolve-symbol [s]
  (try
    (requiring-resolve s)
    (catch Exception ex
      (println "Exception in exposing service " s " - symbol cannot be required.")
      (throw ex))))

(defn get-farg [{:keys [env]} fixed]
  (cond
    (nil? fixed) nil
    (keyword? fixed) (get env fixed)
    (set? fixed) (select-keys env fixed)
    :else fixed))

(defn expose
  "exposes one function 
   services args: this - created via clj-service.core
                  permission-service - created via modular.permission.core/start-permissions
   function args: service - fully qualified symbol
                  permission - a set following modular.permission role based access
                  fixed-args - fixed args to be passed to the function executor as the beginning arguments"
  [{:keys [services] :as this} {:keys [fun fixed] :as service-opts}]
  (println "exposing: " fun)
  (let [sfn (resolve-symbol fun)
        farg (get-farg this fixed)]
    (swap! services assoc fun (merge service-opts {:sfn sfn
                                                   :farg farg
                                                   })
           )
    (println "services: " (keys @services))
    ))


(defn start-executor
  "starts the clj-service service.
   exposes stateless services that are discovered via the extension system.
   non stateless services need to be exposed via expose-service"
  [{:keys [services env]}]
  (println "starting clj-services ..")
  (let [this {:env env
              :services (atom {})}]
     (doall 
       (for [service services]
         (expose this service)))
    ; return the service state
    this))



(defn call-fn [{:keys [sfn farg] :as service}  
               {:keys [args]
                :or {args []}
                :as clj-call}]
  (println "clj-call: " clj-call " service: " service)
  (let [args (if farg
               (concat [farg] args)
               args)]
    (apply sfn args)))



(defn exec-clj [{:keys [services] :as this} {:keys [fun] :as clj-call}]
  (if-let [s (get @services fun)]
    (call-fn s clj-call)
    (throw (ex-info "fun not found" {:fun fun}))))


