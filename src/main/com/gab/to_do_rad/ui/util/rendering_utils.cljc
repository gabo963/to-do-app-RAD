(ns com.gab.to-do-rad.ui.util.rendering-utils
  (:require
    #?(:clj  [com.fulcrologic.fulcro.dom-server :as dom :refer [div label input]]
       :cljs [com.fulcrologic.fulcro.dom :as dom :refer [div label input]])))

(defn buttonRowRenderer
  [instance {:todo/keys [done] :as row-props} {:keys [key disabled? type label action]}]
  (when (not disabled?)
    (case type
      :boolean (dom/div :.ui.toggle.checkbox
                 (dom/input {:type     "checkbox" :name label :key key :checked done
                             :onChange (fn [] (when action (action instance row-props)))})
                 (dom/label {:key key}))
      (dom/button :.ui.button {:key     key
                               :onClick (fn [] (when action (action instance row-props)))}
        label))
    ))

