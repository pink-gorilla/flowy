(ns demo.app
  (:require [uix.core :refer [defui $]]
            [uix.dom]))

(defui button [{:keys [on-click children]}]
  ($ :button.btn {:on-click on-click}
     children))

(defui app []
  (let [[state set-state!] (uix.core/use-state 0)]
    ($ :<>
       ($ button {:on-click #(set-state! dec)} "-")
       ($ :span state)
       ($ button {:on-click #(set-state! inc)} "+"))))



(defn start-app []
  (let [root (uix.dom/create-root (js/document.getElementById "app"))]
   (uix.dom/render-root ($ app) root)))

