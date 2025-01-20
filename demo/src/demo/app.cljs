(ns demo.app
  (:require
   [uix.core :refer [defui $]]
   [uix.dom]
   [flowy.reflower :refer [task flow]]
   [flowy.uix :refer [use-flow]]))

(defui button [{:keys [on-click children]}]
  ($ :button.btn {:on-click on-click}
     children))

(defui cookie []
  (let [[state set-state!] (uix.core/use-state "please press the button to get a fortune cookie (multiple presses are ok)")
        cookie-t (task 'demo.fortune-cookie/get-cookie)
        get-cookie (fn []  
                     (cookie-t (fn [c]
                                 (set-state! c))
                               (fn [err]
                                 (println "could not get cookie. error: " err))))]
    ($ :<>
       ($ button {:on-click #(get-cookie)} "get fortune cookie")
       ($ :p (str "cookie: " state)))))


(def f (flow 'demo.counter/counter-fn))

(defui counterflow []
  (let [flow-counter (use-flow f "waiting...")]
   ($ :<>
     ($ :p (str "counter: " flow-counter)))))

(defui uixdemo []
  (let [[state set-state!] (uix.core/use-state 0)]
     ($ :<>
       ($ button {:on-click #(set-state! dec)} "-")
       ($ :span state)
       ($ button {:on-click #(set-state! inc)} "+"))))

(defui app []
  (let [[menu set-menu!] (uix.core/use-state "select something")]
    ($ :<>
       ; selection menu
       ($ :select {:on-click #(set-menu! (-> % .-target .-value))}
          ($ :option {:value "uixstate"} "uix state demo")
          ($ :option {:value "flowcounter"} "flow counter")
          ($ :option {:value "cookie"} "fortune cookie"))
         ($ :p "selected: " menu)
       ($ :hr)
       (case menu
         "uixstate"
         ($ uixdemo)
         "flowcounter"
         ($ counterflow)
         "cookie"
         ($ cookie)
         ; default
         ($ :<>)
       ))))



(defn start-app []
  (let [root (uix.dom/create-root (js/document.getElementById "app"))]
    (uix.dom/render-root ($ app) root)))

