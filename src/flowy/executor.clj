(ns flowy.executor)

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
   this - created via start-executor
   service-opts: 
      fun - fully qualified symbol
      fixed - a fixed args to be passed to a stateful function which is its first parameter"
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
  "starts the executor service 
   :services - a vec of service-definition maps
   :env - the environment that gets (entirely or modified passed to a service that is stateful)"
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

(defn get-service [{:keys [services] :as this} {:keys [fun] :as clj-call}]
  (get @services fun))

(defn exec-clj [{:keys [services] :as this} {:keys [fun] :as clj-call}]
  (if-let [s (get @services fun)]
    (call-fn s clj-call)
    (throw (ex-info "fun not found" {:fun fun}))))


