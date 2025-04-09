(ns demo.wapp
  (:require
   [webly.spa.env :refer [get-resource-path]]))

(defn link [url text]
  [:a {:href url} [:span {:style {:padding "2px"}} text]])

(defn nav []
  [:div.bg-blue-300
   [link "/#" "goto options"]
   [link "/#/controls" "goto controls"]
   [link "/#/quanta" "goto quanta"]
   [link "/#/clj" "goto clj-options"]])

(defn wrap-app [page match]
  [:div
   ;[nav]
   [page match]])

(def routes
  [["/" {:name 'demo.reagent/app}]
   ])
