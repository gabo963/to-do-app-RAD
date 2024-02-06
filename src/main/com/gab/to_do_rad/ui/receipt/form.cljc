(ns com.gab.to-do-rad.ui.receipt.form
  (:require
    #?(:clj  [com.fulcrologic.fulcro.dom-server :as dom :refer [div label input]]
       :cljs [com.fulcrologic.fulcro.dom :as dom :refer [div label input]])
    [com.gab.to-do-rad.model.receipt.attributes :as receipt]
    [com.gab.to-do-rad.ui.file.form :refer [FileForm]]
    [com.gab.to-do-rad.model.attributes :as model]
    [com.fulcrologic.rad.form-options :as fo]
    [com.fulcrologic.fulcro.algorithms.form-state :as fs]
    [com.fulcrologic.rad.form :as form]
    [com.fulcrologic.fulcro.components :as comp :refer [defsc]]
    [com.fulcrologic.rad.picker-options :as picker-options]
    [com.fulcrologic.fulcro.ui-state-machines :as uism]))

(def receipt-validator (fs/make-validator (fn [form field]
                                            (let [value (get form field)]
                                              (case field
                                                (= :valid (model/all-attribute-validator form field)))))))


(form/defsc-form ReceiptForm [this props {:todo/keys [id]}]
  {fo/id           receipt/id
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
                                    ::form/added-via-upload? true}}})
