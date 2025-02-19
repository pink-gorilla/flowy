(ns demo.appreagent
  (:require 
   [reagent.dom.client :as rdom]
   [demo.reagent :refer [app]]))

;; Mount the app to the DOM
(defn start []
  (let [root (rdom/create-root (.getElementById js/document "app"))]
    (rdom/render root [app])))

