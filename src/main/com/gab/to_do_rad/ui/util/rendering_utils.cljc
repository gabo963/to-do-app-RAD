(ns com.gab.to-do-rad.ui.util.rendering-utils
  (:require
    #?(:clj  [com.fulcrologic.fulcro.dom-server :as dom :refer [div label input]]
       :cljs [com.fulcrologic.fulcro.dom :as dom :refer [div label input]])))

(defn toggle
  [disabled? instance done row-props key label action]
  (case disabled?
    false (dom/div :.ui.toggle.checkbox {:key key}
            (dom/input {:type     "checkbox" :name label :checked done
                        :onChange (fn [] (when action (action instance row-props)))})
            (dom/label {}))

    true (dom/div :.ui.toggle.checkbox {:key key}
           (dom/input {:type     "checkbox" :name label :checked done
                       :disabled "disabled"})
           (dom/label {}))))

(defn button
  [disabled? instance row-props key label action style]
  (case disabled?
    false (dom/button :.ui.button {:key     key
                                   :onClick (fn [] (when action (action instance row-props)))}
            label)
    true (dom/button :.ui.button.disabled {:key     key
                                           :onClick (fn [])}
           label)))

(defn buttonRowRenderer
  [instance
   {:todo/keys [done] :as row-props}
   {:keys [key disabled? visible? type label action style] :or {disabled? false visible? true}}]
  (when visible?
    (case type
      :boolean (toggle disabled? instance done row-props key label action)
      (button disabled? instance row-props key label action style))
    ))

