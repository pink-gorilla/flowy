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

(defui app []
  (let [flow-counter (use-flow f "waiting...")
        ;[state set-state!] (uix.core/use-state 0)
        ]
    ($ :<>
       ;($ button {:on-click #(set-state! dec)} "-")
       ;($ :span state)
       ;($ button {:on-click #(set-state! inc)} "+")
       ($ :hr)
       ($ :p (str "counter: " flow-counter))
       
       )))



(defn start-app []
  (let [root (uix.dom/create-root (js/document.getElementById "app"))]
    (uix.dom/render-root ($ app) root)))

