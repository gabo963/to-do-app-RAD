(ns com.gab.to-do-rad.ui.todo-forms
  (:require
    [com.gab.to-do-rad.model.todo :as todo]
    [com.fulcrologic.rad.form-options :as fo]
    [com.fulcrologic.rad.form :as form]
    [com.fulcrologic.rad.report :as report]
    [com.fulcrologic.rad.report-options :as ro]
    [com.fulcrologic.rad.picker-options :as picker-options]
    [com.gab.to-do-rad.model.category :as category]
    [com.fulcrologic.fulcro.components :as comp]))

(form/defsc-form TodoForm [this props]
  {fo/id            todo/id
   fo/attributes    [todo/text todo/done todo/due todo/status todo/category]
   fo/field-styles  {:todo/category :pick-one}
   fo/field-options {:todo/category {::picker-options/query-key       :category/all-categories
                                     ::picker-options/query-component category/Category
                                     ::picker-options/options-xform   (fn [_ options] (mapv
                                                                                        (fn [{:category/keys [id label]}]
                                                                                          {:text (str label) :value [:category/id id]})
                                                                                        (sort-by :category/label options)))
                                     ::picker-options/cache-time-ms   30000}}
   fo/cancel-route  ["todo-report"]
   fo/route-prefix  "todos"
   fo/title         "Edit To-do"
   fo/layout        [[:todo/text]
                     [:todo/due :todo/done]
                     [:todo/status :todo/category]]})

(report/defsc-report TodoReport [this props]
  {ro/title               "To-Do List"
   ro/source-attribute    :todo/all-todos
   ro/row-pk              todo/id
   ro/columns             [todo/text category/label todo/due todo/status todo/done]
   ro/column-formatters   {:todo/done (fn [this v] (if v "Yes" "No"))}
   ro/row-visible?        (fn [filter-parameters row] (let [{::keys [category]} filter-parameters
                                                            row-category (get row :category/label)]
                                                        (or (= "" category) (= category row-category))))
   ;; A sample server-query based picker that sets a local parameter that we use to filter rows.
   ro/controls            {::category {:type                          :picker
                                       :local?                        true
                                       :label                         "Category"
                                       :default-value                 ""
                                       :action                        (fn [this] (report/filter-rows! this))
                                       picker-options/cache-time-ms   30000
                                       picker-options/cache-key       :all-category-options
                                       picker-options/query-key       :category/all-categories
                                       picker-options/query-component category/Category
                                       picker-options/options-xform   (fn [_ categories]
                                                                        (into [{:text "All" :value ""}]
                                                                          (map
                                                                            (fn [{:category/keys [label]}]
                                                                              {:text label :value label}))
                                                                          categories))}}
   ro/row-actions         [{:label     "Mark Done"
                            :action    (fn [report-instance {:todo/keys [id]}]
                                         #?(:cljs
                                            (comp/transact! report-instance [(todo/mark-todo-done {:todo/id   id
                                                                                                   :todo/done true})])))
                            :disabled? (fn [_ row-props] (:todo/done row-props))}
                           {:label     "Mark Undone"
                            :action    (fn [report-instance {:todo/keys [id]}]
                                         #?(:cljs
                                            (comp/transact! report-instance [(todo/mark-todo-done {:todo/id   id
                                                                                                   :todo/done false})])))
                            :disabled? (fn [_ row-props] (not (:todo/done row-props)))}]
   ro/initial-sort-params {:sort-by          :todo/due
                           :sortable-columns #{:todo/due :category/label}
                           :ascending?       true}
   ro/run-on-mount?       true
   ro/form-links          {todo/id TodoForm}
   ro/route               "todo-report"})
