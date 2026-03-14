(ns demo.wapp
  (:require
   [shadowx.core :refer [get-resource-path]]))

(defn link [url text]
  [:a {:href url} [:span {:style {:padding "2px"}} text]])


(defn wrap-app [page match]
  [:div
   [page match]])

(def routes
  [["/" {:name 'demo.reagent/app}]])
