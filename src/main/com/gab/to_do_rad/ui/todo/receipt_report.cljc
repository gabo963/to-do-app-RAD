(ns com.gab.to-do-rad.ui.todo.receipt-report
  (:require
    [com.fulcrologic.fulcro.components :as comp]
    [com.gab.to-do-rad.model.receipt.attributes :as receipt]
    [com.gab.to-do-rad.model.todo.attributes :as todo]
    [com.fulcrologic.rad.report :as report]
    [com.fulcrologic.rad.report-options :as ro]
    [com.gab.to-do-rad.ui.todo.form :refer [TodoForm]]
    [com.fulcrologic.rad.attributes-options :as ao]
    [com.gab.to-do-rad.ui.receipt.form :refer [ReceiptForm]]
    [com.fulcrologic.rad.attributes :refer [new-attribute]]))

(def receipt-text
  (new-attribute :ui/receipt-text :string
    {ro/column-heading "Receipt Description"}))

(def receipt-quantity
  (new-attribute :ui/receipt-quantity :int
    {ro/column-heading "Receipt Quantity"}))

(report/defsc-report TodoReceiptReport [this props]
  {ro/title               "To-Do's With Receipts"
   ro/source-attribute    :todo/all-todos-receipts
   ro/row-visible?        (fn [_ row]
                            (let [row-receipt (get row :todo/receipt?)]
                              row-receipt))
   ro/row-pk              todo/id
   ro/columns             [todo/text todo/category todo/due todo/doneDate todo/completed-time todo/receipt? todo/done receipt-text receipt-quantity]
   ro/row-query-inclusion [{:todo/receipt (comp/get-query ReceiptForm)}]
   ro/column-formatters   {:todo/done           (fn [_ v] (if v "Yes" "No"))
                           :todo/receipt?       (fn [_ v] (if v "Yes" "No"))
                           :todo/category       (fn [_ v] (get v :category/label))
                           :ui/receipt-text     (fn [this v] (-> this
                                                               (comp/props)
                                                               (:ui/current-rows)
                                                               (first)
                                                               (:todo/receipt)
                                                               (:receipt/text)))
                           :ui/receipt-quantity (fn [this v] (-> this
                                                               (comp/props)
                                                               (:ui/current-rows)
                                                               (first)
                                                               (:todo/receipt)
                                                               (:receipt/quantity)))}
   ro/run-on-mount?       true
   ro/form-links          {todo/text    TodoForm
                           receipt/text ReceiptForm}
   ro/initial-sort-params {:sort-by          :todo/due
                           :sortable-columns #{:todo/due :todo/doneDate :category/label :todo/status}
                           :ascending?       true}
   ro/route               "todo-receipt-report"})
