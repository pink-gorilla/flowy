(ns flowy.executor
  (:require
  ))


(defn- resolve-symbol [s]
  (try
    (requiring-resolve s)
    (catch Exception ex
      (println "Exception in exposing service " s " - symbol cannot be required.")
      (throw ex))))

(defn expose
  "exposes one function 
   services args: this - created via clj-service.core
                  permission-service - created via modular.permission.core/start-permissions
   function args: service - fully qualified symbol
                  permission - a set following modular.permission role based access
                  fixed-args - fixed args to be passed to the function executor as the beginning arguments"
  [{:keys [services] :as this} {:keys [fun-s] :as service-opts}]

  (let [fun-fn (resolve-symbol fun-s)]
    (swap! services assoc fun-s 
           service-opts {:fun-fn fun-fn})))


(defn start-executor
  "starts the clj-service service.
   exposes stateless services that are discovered via the extension system.
   non stateless services need to be exposed via expose-service"
  [services]
  (println "starting clj-services ..")
  (let [this {:services (atom {})}]
     (doall 
       (for [service services]
         (expose this service)))
    ; return the service state
    this))


(defn exec-clj [{:keys [fun args farg]}]
  (let [args (if farg
               (concat [farg] args)
               args)]
    (apply fun args)))

