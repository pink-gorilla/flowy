(ns flowy.jetty-config
  (:import
   (org.eclipse.jetty.server.handler.gzip GzipHandler)
   (org.eclipse.jetty.websocket.server.config JettyWebSocketServletContainerInitializer JettyWebSocketServletContainerInitializer$Configurator)))

(defn- add-gzip-handler!
  "Makes Jetty server compress responses. Optional but recommended."
  [server]
  (.setHandler server
               (doto (GzipHandler.)
                 #_(.setIncludedMimeTypes (into-array ["text/css" "text/plain" "text/javascript" "application/javascript" "application/json" "image/svg+xml"])) ; only compress these
                 (.setMinGzipSize 1024)
                 (.setHandler (.getHandler server)))))

(defn- configure-websocket!
  "Tune Jetty Websocket config for Electric compat." [server]
  (JettyWebSocketServletContainerInitializer/configure
   (.getHandler server)
   (reify JettyWebSocketServletContainerInitializer$Configurator
     (accept [_this _servletContext wsContainer]
       (.setIdleTimeout wsContainer (java.time.Duration/ofSeconds 60))
       (.setMaxBinaryMessageSize wsContainer (* 100 1024 1024)) ; 100M - temporary
       (.setMaxTextMessageSize wsContainer (* 100 1024 1024))   ; 100M - temporary
       ))))
(defn jetty-configurator [server]
  (configure-websocket! server)
  (add-gzip-handler! server))

