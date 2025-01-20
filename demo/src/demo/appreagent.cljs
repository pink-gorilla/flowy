(ns demo.appreagent
  (:require 
    [reagent.core :as r]
    [reagent.dom.client :as rdom]
    [flowy.reflower :refer [task flow]]
    [flowy.reagent :refer [flow->ratom]]))

(def f (flow 'demo.counter/counter-fn))

(defn counter-component []
  (r/with-let [[counter-a dispose!] (flow->ratom f "waiting ..")] 
    [:div
     [:p "counter: " (str @counter-a)]]
    (finally
      (println "Cleanup: stopping flow!")
      (dispose!)
      )))

(defn cookie-component []
  (r/with-let [state-a (r/atom "please press the button to get a fortune cookie (multiple presses are ok)")
               cookie-t (task 'demo.fortune-cookie/get-cookie)
               get-cookie (fn []
                            (cookie-t (fn [c]
                                        (reset! state-a c))
                                      (fn [err]
                                        (println "could not get cookie. error: " err))))]
    [:div
     [:button {:on-click #(get-cookie)} "get fortune cookie"]
     [:p (str "cookie: " @state-a)]]
    (finally
      (println "Cleanup: task does not need cleanup")
      )))


(defn app []
  (r/with-let [menu-a (r/atom nil)]
  [:div
   [:p "welcome to the reagent app..."]
    ; selection menu
       [:select {:on-click #(reset! menu-a (-> % .-target .-value))}
          [:option {:value "-"} "< Select an option >"]
          [:option {:value "flowcounter"} "flow counter"]
          [:option {:value "cookie"} "fortune cookie"]]
         [:p "selected: " @menu-a]
       [:hr]
       (case @menu-a
         "flowcounter"
         [counter-component]
         "cookie"
         [cookie-component]
         ; default
         [:<>]
         )]))

;; Mount the app to the DOM
(defn start []
  (let [root (rdom/create-root (.getElementById js/document "app"))]
    (rdom/render root [app])))

