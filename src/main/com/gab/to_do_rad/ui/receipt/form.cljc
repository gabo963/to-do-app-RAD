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
    [com.fulcrologic.fulcro.components :as comp]
    [com.fulcrologic.fulcro.ui-state-machines :as uism]
    [com.gab.to-do-rad.ui.receipt.state-machine :refer [saving-options-machine]]))

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
                    receipt/files]
   fo/cancel-route ["todo-receipt-report"]
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
                                    ::form/added-via-upload? true}}}
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
