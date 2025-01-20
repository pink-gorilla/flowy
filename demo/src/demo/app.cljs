(ns demo.app
  (:require
   [uix.core :refer [defui $]]
   [uix.dom]
   [flowy.reflower :refer [task flow]]
   [flowy.uix :refer [use-flow]]))

(defui button [{:keys [on-click children]}]
  ($ :button.btn {:on-click on-click}
     children))


(def f (flow 'demo.counter/counter-fn))

(defui counterflow []
  (let [flow-counter (use-flow f "waiting...")]
   ($ :<>
     ($ :hr)
     ($ :p (str "counter: " flow-counter)))))

(defui uixdemo []
  (let [[state set-state!] (uix.core/use-state 0)]
     ($ :<>
       ($ button {:on-click #(set-state! dec)} "-")
       ($ :span state)
       ($ button {:on-click #(set-state! inc)} "+"))))

(defui app []
  (let [[menu set-menu!] (uix.core/use-state "select something")
        flow-counter (use-flow f "waiting...")
       ]
    ($ :<>
       
       ($ :select {:on-click #(set-menu! (-> % .-target .-value))}
          ($ :option {:value "flowcounter"} "flow counter")
          ($ :option {:value "uixstate"} "uix state demo")
          ($ :option {:value "c"} "c"))
         ($ :p "selected: " menu)

       (case menu
         "uixstate"
         ($ uixdemo)
         "flowcounter"
         ($ counterflow)
         ; default
         ($ :<>)
       ))))



(defn start-app []
  (let [root (uix.dom/create-root (js/document.getElementById "app"))]
    (uix.dom/render-root ($ app) root)))

