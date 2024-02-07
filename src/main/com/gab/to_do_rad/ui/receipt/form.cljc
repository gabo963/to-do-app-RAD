(ns com.gab.to-do-rad.ui.receipt.form
  (:require
    #?(:clj  [com.fulcrologic.fulcro.dom-server :as dom :refer [div label input button p i]]
       :cljs [com.fulcrologic.fulcro.dom :as dom :refer [div label input button p i]])
    [com.fulcrologic.semantic-ui.modules.modal.ui-modal :refer [ui-modal]]
    [com.fulcrologic.semantic-ui.modules.modal.ui-modal-header :refer [ui-modal-header]]
    [com.fulcrologic.semantic-ui.modules.modal.ui-modal-content :refer [ui-modal-content]]
    [com.fulcrologic.semantic-ui.modules.modal.ui-modal-actions :refer [ui-modal-actions]]
    [com.gab.to-do-rad.model.receipt.attributes :as receipt]
    [com.gab.to-do-rad.ui.file.form :refer [FileForm]]
    [com.gab.to-do-rad.model.attributes :as model]
    [com.fulcrologic.rad.form-options :as fo]
    [com.fulcrologic.fulcro.algorithms.form-state :as fs]
    [com.fulcrologic.rad.form :as form]
    [com.fulcrologic.fulcro.components :as comp :refer [defsc]]
    [com.fulcrologic.rad.picker-options :as picker-options]
    [com.fulcrologic.rad.attributes :as attr]
    [com.fulcrologic.fulcro.mutations :as m :refer [defmutation]]
    [com.fulcrologic.fulcro.algorithms.normalized-state :as fns]
    [com.fulcrologic.fulcro.routing.dynamic-routing :as dr]
    [com.fulcrologic.fulcro.ui-state-machines :as uism :refer [defstatemachine]]))


(comment form/form-machine)

(def state-saving-options
  {::uism/events
   (merge form/global-events
     {:event/save
      {::uism/handler (fn [{::uism/keys [state-map event-data] :as env}]
                        (let [form-class          (uism/actor-class env :actor/form)
                              form-ident          (uism/actor->ident env :actor/form)
                              {::keys [id save-mutation]} (comp/component-options form-class)
                              master-pk           (::attr/qualified-key id)
                              proposed-form-props (fs/completed-form-props state-map form-class form-ident)]
                          (if (form/valid? form-class proposed-form-props)
                            (let [data-to-save  (form/calc-diff env)
                                  params        (merge event-data data-to-save)
                                  save-mutation (or save-mutation `form/save-form)]
                              (-> env
                                (form/clear-server-errors)
                                (uism/trigger-remote-mutation :actor/form save-mutation
                                  (merge params
                                    {::uism/error-event :event/save-failed
                                     ::master-pk        master-pk
                                     ::id               (second form-ident)
                                     ::m/returning      form-class
                                     ::uism/ok-event    :event/saved}))
                                ;; TODO: ADD Receipt to the Todo/receipt saved in options
                                (uism/activate :state/saving)))
                            (-> env
                              (uism/apply-action fs/mark-complete* form-ident)
                              (uism/activate :state/editing)))))}
      :event/cancel
      {::uism/handler (fn [{::uism/keys [state-map event-data] :as env}]
                        (-> env
                          (uism/activate :state/editing )))}
      :event/revert-actions
      {::uism/handler (fn [env]
                          (let [form-ident (uism/actor->ident env :actor/form)]
                            (-> env
                              (form/clear-server-errors)
                              (uism/apply-action fs/pristine->entity* form-ident)
                              (uism/activate :state/editing ))))}})})

(defn editing-save-event
  [{::uism/keys [state-map event-data] :as env}]
  (-> env
    (uism/activate :state/saving-options)))

;;TASK: The old save of editing will be a state skip to the new :state/saving-options

(defstatemachine saving-options-machine
  (-> form/form-machine
    (assoc-in [::uism/states :state/saving-options] state-saving-options)
    (assoc-in [::uism/states :state/editing ::uism/events :event/save ::uism/handler] editing-save-event)))


(comment

  (get-in saving-options-machine [::uism/states :state/editing ::uism/events :event/saved ::uism/handler])

  )

(def receipt-validator (fs/make-validator (fn [form field]
                                            (let [value (get form field)]
                                              (case field
                                                (= :valid (model/all-attribute-validator form field)))))))

(form/defsc-form ReceiptForm [this props]
  {fo/id           receipt/id
   fo/machine      saving-options-machine
   fo/attributes   [receipt/text
                    receipt/quantity
                    receipt/date
                    receipt/valid
                    receipt/files]
   fo/cancel-route ["todo-report"]
   fo/route-prefix "receipts"
   fo/title        "Update Receipt"
   fo/validator    receipt-validator

   fo/layout       [[:receipt/text]
                    [:receipt/quantity :receipt/date]
                    [:receipt/files]]
   fo/subforms     {:receipt/files {fo/ui                    FileForm
                                    fo/title                 "Files"
                                    fo/can-delete?           (fn [_ _] true)
                                    fo/layout-styles         {:ref-container :file}
                                    ::form/added-via-upload? true}}
   }
  (div
    (form/render-layout this props)

    (ui-modal {:open (= (uism/get-active-state this (comp/get-ident this)) :state/saving-options) :dimmer true}
      (ui-modal-header {} "Saving...")
      (ui-modal-content {}
        (div :.ui.segment
          (p "Are you sure you want to save this?"))
        (ui-modal-actions {}
          (button :.positive.ui.button
            {:onClick (fn [] (uism/trigger! this (comp/get-ident this) :event/save))}
            "Yes")
          (button :.negative.ui.button
            {:onClick (fn [] (uism/trigger! this (comp/get-ident this) :event/revert-actions))}
            "Revert Changes")
          (button :.ui.button
            {:onClick (fn [] (uism/trigger! this (comp/get-ident this) :event/cancel))}
            "Cancel"))))))
