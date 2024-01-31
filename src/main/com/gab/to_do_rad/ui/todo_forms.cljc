(ns com.gab.to-do-rad.ui.todo-forms
  (:require
    [com.gab.to-do-rad.model.todo :as todo]
    [com.fulcrologic.rad.form-options :as fo]
    [com.fulcrologic.rad.form :as form]
    [com.fulcrologic.rad.report :as report]
    [com.fulcrologic.rad.report-options :as ro]
    [com.fulcrologic.rad.picker-options :as picker-options]
    [com.gab.to-do-rad.model.category :as category]
    [com.fulcrologic.fulcro.components :as comp]
    [com.fulcrologic.rad.control :as control]
    [clojure.string :as str]))

(form/defsc-form TodoForm [this props]
  {fo/id            todo/id
   fo/attributes    [todo/text
                     todo/due
                     todo/status
                     todo/category]
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
                     [:todo/due :todo/status :todo/category]]})

(defn- stringSearchValidator [search string]
  (let [search (some-> search (str/trim) (str/lower-case))
        string (some-> string (str/lower-case))]
    (or
      (nil? search)
      (empty? search)
      (and string (str/includes? string search)))))

(report/defsc-report TodoReport [this props]
  {ro/title               "To-Do List"
   ro/source-attribute    :todo/all-todos
   ro/row-pk              todo/id
   ro/columns             [todo/text category/label todo/due todo/status todo/done]
   ro/column-formatters   {:todo/done (fn [this v] (if v "Yes" "No"))}

   ro/row-visible?        (fn [filter-parameters row]
                            (let [{::keys [category filter-text show-done?]} filter-parameters
                                  row-category (get row :category/label)
                                  row-text     (get row :todo/text)
                                  row-done     (get row :todo/done)]
                              (and
                                (if row-done (= row-done show-done?) true)
                                (or (= "" category) (= category row-category))
                                (or (stringSearchValidator filter-text row-text))
                                )))

   ro/controls            {::category    {:type                          :picker
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
                                                                             categories))}
                           ::new-todo    {:type   :button
                                          :local? true
                                          :label  "New Todo"
                                          :action (fn [this _] (form/create! this TodoForm))}
                           ::search!     {:type   :button
                                          :local? true
                                          :label  "Filter"
                                          :class  "ui basic compact mini red button"
                                          :action (fn [this _] (report/filter-rows! this))}
                           ::filter-text {:type        :string
                                          :local?      true
                                          :placeholder "Search for a todo"
                                          :onChange    (fn [this _] (report/filter-rows! this))}
                           ::show-done?  {:type          :boolean
                                          :local?        true
                                          :style         :toggle
                                          :default-value false
                                          :onChange      (fn [this _] (control/run! this))
                                          :label         "Show Completed To-dos"}}

   ro/control-layout      {:action-buttons [::new-todo]
                           :inputs         [[::category]
                                            [::filter-text ::search! :_]
                                            [::show-done?]]}

   ro/row-actions         [{:label     "Mark Done"
                            :action    (fn [report-instance {:todo/keys [id]}]
                                         #?(:cljs
                                            (comp/transact! report-instance [(todo/mark-todo-done {:todo/id   id
                                                                                                   :todo/done true})]))
                                         (control/run! report-instance))
                            :disabled? (fn [_ row-props] (:todo/done row-props))}
                           {:label     "Mark Undone"
                            :action    (fn [report-instance {:todo/keys [id]}]
                                         #?(:cljs
                                            (comp/transact! report-instance [(todo/mark-todo-done {:todo/id   id
                                                                                                   :todo/done false})]))
                                         (control/run! report-instance))
                            :disabled? (fn [_ row-props] (not (:todo/done row-props)))}]
   ro/initial-sort-params {:sort-by          :todo/due
                           :sortable-columns #{:todo/due :category/label}
                           :ascending?       true}
   ro/run-on-mount?       true
   ro/form-links          {todo/text TodoForm}
   ro/route               "todo-report"})
