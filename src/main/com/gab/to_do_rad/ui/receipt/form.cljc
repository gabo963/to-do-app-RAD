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
    [com.fulcrologic.fulcro.ui-state-machines :as uism :refer [defstatemachine]]))

(def receipt-validator (fs/make-validator (fn [form field]
                                            (let [value (get form field)]
                                              (case field
                                                (= :valid (model/all-attribute-validator form field)))))))


(comment

  (defn after-registration
    [{::uism/keys [fulcro-app] :as uism-env}]
    (let [Form             (uism/actor-class uism-env :actor/form)
          form-ident       (uism/actor->ident uism-env :actor/form)
          state-map        (raw.app/current-state fulcro-app)
          props            (fns/ui->props state-map Form form-ident)
          registered-route (?! (some-> Form comp/component-options ::form/registered-route) fulcro-app props)
          routing-action   (fn [] (dr/change-route! fulcro-app registered-route props))]
      (sched/defer routing-action 100)
      uism-env))

  (defstatemachine receipt-form-machine
    (-> form/form-machine
      (assoc-in [::uism/states :state/editing ::uism/events :event/registered ::uism/handler] after-registration)))

  )


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
