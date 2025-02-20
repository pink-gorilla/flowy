(ns flowy.config
  (:require 
   [flowy.client :refer [*ws-server-url*]]))

(defn change-port [{:keys [webly-http-port shadow-dev-http-port] :as ports} url]
  (let [detected-port (js/parseInt (:port url))]
    ; if the port matches the shadow-dev-http port, we are on shadow-dev, so we redirect
    (when  (= detected-port shadow-dev-http-port)
      (println "this is a shadow-cljs-dev session. connecting flowy to WEBLY SERVER on port: " shadow-dev-http-port)
      (set! (.-port url) (str webly-http-port)))))

(defn flowy-url [{:keys [webly-http-port shadow-dev-http-port] :as ports}]
  (let [url (new js/URL (.-location js/window))
        proto (.-protocol url)
        _ (set! (.-protocol url)
                (case proto
                  "http:" "ws:"
                  "https:" "wss:"
                  (throw (ex-info "Unexpected protocol" proto))))
          ;_ (.. url -searchParams (set "ELECTRIC_USER_VERSION" ELECTRIC_USER_VERSION))
        _ (set! (.-search url) "")
        _ (set! (.-hash url) "") ; fragment is forbidden in WS URL https://websockets.spec.whatwg.org/#ref-for-dom-websocket-websocket%E2%91%A0
        _ (set! (.-pathname url) "/flowy")
        _ (change-port ports url)
        url-s (.toString url)]
    (println "flowy-url: " url-s)
      ; ws://localhost:9000/?ELECTRIC_USER_VERSION=hyperfiddle_electric_client__dirty
    url-s))

(defn start-flowy-service [{:keys [mode ports]}]
  (when (= mode :dynamic)
    (println "flowy ports: " ports)
    (println "flowy url: " (flowy-url ports)))
  nil ; we dont wait on this.
  )