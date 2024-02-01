(ns com.gab.to-do-rad.ui.todo.done-report
  (:require
    [com.gab.to-do-rad.model.todo.attributes :as todo]
    [com.gab.to-do-rad.model.category.attributes :as category]
    [com.gab.to-do-rad.model.file.attributes :as file]
    [com.fulcrologic.rad.report :as report]
    [com.fulcrologic.rad.report-options :as ro]
    [com.gab.to-do-rad.ui.todo.form :refer [TodoForm]]))

(report/defsc-report TodoDoneReport [this props]
  {ro/title               "Done To-Do List"
   ro/source-attribute    :todo/all-todos
   ro/row-visible?        (fn [_ row]
                            (let [row-done (get row :todo/done)]
                              row-done))
   ro/row-pk              todo/id
   ro/columns             [todo/text category/label todo/due todo/doneDate todo/completed-time todo/status todo/done file/filename]
   ro/column-formatters   {:todo/done (fn [this v] (if v "Yes" "No"))}
   ro/run-on-mount?       true
   ro/form-links          {todo/text TodoForm}
   ro/initial-sort-params {:sort-by          :todo/due
                           :sortable-columns #{:todo/due :todo/doneDate :category/label :todo/status}
                           :ascending?       true}
   ro/route               "todo-done-report"})
