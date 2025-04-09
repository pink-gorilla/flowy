(ns demo.reagent
  (:require
   [reagent.core :as r]
   [flowy.reflower :refer [task flow]]
   [flowy.reagent :refer [flow->ratom]]))

(defn counter-component [fn-symbol]
  (r/with-let [f (flow fn-symbol)
               [counter-a dispose!] (flow->ratom f "waiting ..")]
    [:div
     [:p "counter: " (str @counter-a)]]
    (finally
      (println "Cleanup: stopping flow!")
      (dispose!))))

(defn cookie-component [fn-symbol]
  (r/with-let [state-a (r/atom "please press the button to get a fortune cookie (multiple presses are ok)")]
    (let [cookie-t (task fn-symbol)
          get-cookie (fn []
                       (cookie-t (fn [c]
                                   (reset! state-a c))
                                 (fn [err]
                                   (println "could not get cookie. error: " err)
                                   (reset! state-a (str "error: " (ex-message err))))))]
      [:div
       [:p "endpoint: " (pr-str fn-symbol)]
       [:button {:on-click #(get-cookie)} "get fortune cookie"]
       [:p (str "cookie: " @state-a)]])))




(defn app []
  (r/with-let [menu-a (r/atom nil)]
    [:div
     [:p "welcome to the reagent app..."]
    ; selection menu
     [:select {:on-click #(reset! menu-a (-> % .-target .-value))}
      [:option {:value "-"} "< Select an option >"]
      [:option {:value "cookie"} "fortune cookie"]
      [:option {:value "bad-cookie"} "fortune cookie (exception)"]
      [:option {:value "flowcounter"} "flow counter"]
      [:option {:value "flowcounter-bad"} "flow counter (bad: no ap)"]]
     [:p "selected: " @menu-a]
     [:hr]
     (case @menu-a
       "cookie"
       [cookie-component 'demo.fortune-cookie/get-cookie]
       "bad-cookie"
       [cookie-component 'demo.fortune-cookie/get-cookie-bad]
       "flowcounter"
       [counter-component 'demo.counter/counter-fn]
       "flowcounter-bad"
       [counter-component 'demo.counter/counter-bad-fn]
         ; default
       [:<>])]))

