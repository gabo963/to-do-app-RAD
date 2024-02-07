(ns com.gab.to-do-rad.ui.todo.form
  (:require
    #?(:clj  [com.fulcrologic.fulcro.dom-server :as dom :refer [div label input button p i h3]]
       :cljs [com.fulcrologic.fulcro.dom :as dom :refer [div label input button p i h3]])
    [com.fulcrologic.semantic-ui.modules.modal.ui-modal :refer [ui-modal]]
    [com.fulcrologic.semantic-ui.modules.modal.ui-modal-header :refer [ui-modal-header]]
    [com.fulcrologic.semantic-ui.modules.modal.ui-modal-content :refer [ui-modal-content]]
    [com.fulcrologic.semantic-ui.modules.modal.ui-modal-actions :refer [ui-modal-actions]]
    [com.gab.to-do-rad.model.todo.attributes :as todo]
    [com.gab.to-do-rad.model.todo.resolvers :as r.todo]
    [com.gab.to-do-rad.model.category.attributes :as category]
    [com.gab.to-do-rad.model.attributes :as model]
    [com.fulcrologic.rad.form-options :as fo]
    [com.fulcrologic.fulcro.algorithms.form-state :as fs]
    [com.fulcrologic.rad.form :as form]
    [com.fulcrologic.fulcro.components :as comp]
    [com.fulcrologic.rad.picker-options :as picker-options]
    [com.fulcrologic.fulcro.ui-state-machines :as uism]
    [com.gab.to-do-rad.ui.change-monitor.monitor :refer [ui-change-table]]))

(def todo-validator (fs/make-validator (fn [form field]
                                         (let [value (get form field)]
                                           (case field
                                             (= :valid (model/all-attribute-validator form field)))))))

(form/defsc-form TodoForm [this props]
  {fo/id              todo/id
   fo/attributes      [todo/text
                       todo/due
                       todo/status
                       todo/category
                       todo/done
                       todo/receipt?]
   fo/field-styles    {:todo/category :pick-one}
   fo/field-options   {:todo/category {::picker-options/query-key       :category/all-categories
                                       ::picker-options/query-component category/Category
                                       ::picker-options/options-xform   (fn [_ options] (mapv
                                                                                          (fn [{:category/keys [id label]}]
                                                                                            {:text (str label) :value [:category/id id]})
                                                                                          (sort-by :category/label options)))
                                       ::picker-options/cache-time-ms   30000}}
   fo/cancel-route    ["todo-report"]
   fo/route-prefix    "todos"
   fo/title           "Edit To-do"
   fo/validator       todo-validator
   fo/layout          [[:todo/text]
                       [:todo/due :todo/status :todo/category]
                       [:todo/receipt?]]
   fo/query-inclusion [:ui/open-modal? :ui/lastChange]
   fo/triggers        {:saved     (fn [uism-env ident]
                                    (uism/apply-action
                                      uism-env (fn [state-map] (assoc-in state-map (conj ident :ui/open-modal?) true))))
                       :on-change (fn [uism-env form-ident qualified-key old-value new-value]
                                    (uism/apply-action uism-env (fn [state-map]
                                                                  (update-in state-map
                                                                    (conj form-ident :ui/lastChange)
                                                                    conj
                                                                    {:change/key qualified-key :change/old-value old-value :change/new-value new-value}))))}
   }
  (div
    (form/render-layout this props)

    (div :.ui.container
      (div :.ui.divider)
      (h3 "Change History")
      (div :.ui.segment
        (ui-change-table
          {:ui/lastChange (vec (:ui/lastChange props))})
        ))

    (ui-modal {:open (:ui/open-modal? props) :dimmer true}
      (ui-modal-header {} "To-do Saved Successfully")
      (ui-modal-content {}
        (div :.ui.segment
          (p "The to-do with text: " (i (:todo/text props)) " was saved successfully")
          )
        (ui-modal-actions {}
          (button :.positive.ui.button
            {:onClick (fn [] #?(:cljs (comp/transact! this [(r.todo/remove-okay-modal (comp/get-ident this))])))}
            "Close"))))
    )
  )
