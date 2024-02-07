(ns com.gab.to-do-rad.ui.todo.receipt-report
  (:require
    [com.gab.to-do-rad.model.receipt.attributes :as receipt]
    [com.gab.to-do-rad.model.todo.attributes :as todo]
    [com.gab.to-do-rad.model.category.attributes :as category]
    [com.gab.to-do-rad.model.file.attributes :as file]
    [com.fulcrologic.rad.report :as report]
    [com.fulcrologic.rad.report-options :as ro]
    [com.gab.to-do-rad.ui.todo.form :refer [TodoForm]]
    [com.gab.to-do-rad.ui.receipt.form :refer [ReceiptForm]]))

(report/defsc-report TodoReceiptReport [this props]
  {ro/title               "To-Do's With Receipts"
   ro/source-attribute    :todo/all-todos-receipts
   ro/row-visible?        (fn [_ row]
                            (let [row-receipt (get row :todo/receipt?)]
                              row-receipt))
   ro/row-pk              todo/id
   ro/columns             [todo/text category/label todo/due todo/completed-time todo/receipt? todo/done receipt/text receipt/quantity]
   ro/column-formatters   {:todo/done     (fn [_ v] (if v "Yes" "No"))
                           :todo/receipt? (fn [_ v] (if v "Yes" "No"))}
   ro/run-on-mount?       true
   ro/form-links          {todo/text    TodoForm
                           receipt/text ReceiptForm}
   ro/initial-sort-params {:sort-by          :todo/due
                           :sortable-columns #{:todo/due :todo/doneDate :category/label :todo/status}
                           :ascending?       true}
   ro/route               "todo-receipt-report"})
