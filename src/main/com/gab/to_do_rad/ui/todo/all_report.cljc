(ns com.gab.to-do-rad.ui.todo.all-report
  (:require
    [com.gab.to-do-rad.model.todo.attributes :as todo]
    [com.gab.to-do-rad.model.todo.resolvers :as r.todo]
    [com.gab.to-do-rad.model.category.attributes :as category]
    [com.fulcrologic.rad.form :as form]
    [com.fulcrologic.rad.report :as report]
    [com.fulcrologic.rad.report-options :as ro]
    [com.fulcrologic.rad.picker-options :as picker-options]
    [com.fulcrologic.fulcro.components :as comp]
    [com.fulcrologic.rad.control :as control]
    [com.gab.to-do-rad.ui.todo.form :refer [TodoForm]]
    [com.gab.to-do-rad.ui.receipt.form :refer [ReceiptForm]]
    [clojure.string :as str]
    [com.fulcrologic.rad.semantic-ui-options :as suo]
    [com.gab.to-do-rad.ui.util.rendering-utils :refer [buttonRowRenderer]]))

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
   ro/columns             [todo/text todo/due todo/category todo/status todo/receipt? todo/done]
   suo/rendering-options  {suo/report-row-button-renderer buttonRowRenderer}
   ro/column-formatters   {:todo/done     (fn [this v] (if v "Yes" "No"))
                           :todo/receipt? (fn [this v] (if v "Yes" "No"))
                           :todo/category (fn [this v] (get v :category/label))}

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

   ro/row-visible?        (fn [filter-parameters row]
                            (let [{::keys [category filter-text show-done?]} filter-parameters
                                  row-category (get-in row [:todo/category :category/label])
                                  row-text     (get row :todo/text)
                                  row-done     (get row :todo/done)]
                              (and
                                (if row-done (= row-done show-done?) true)
                                (or (= "" category) (= category row-category))
                                (or (stringSearchValidator filter-text row-text))
                                )))

   ro/control-layout      {:action-buttons [::new-todo]
                           :inputs         [[::category]
                                            [::filter-text ::search! :_]
                                            [::show-done?]]}


   ro/row-actions         [{:label    "Toggle Done"
                            :type     :boolean
                            :visible? (fn [_ {:todo/keys [receipt?]}]
                                        (not receipt?))
                            :action   (fn [report-instance {:todo/keys [id done]}]
                                        #?(:cljs
                                           (comp/transact! report-instance [(r.todo/mark-todo-done {:todo/id   id
                                                                                                    :todo/done (not done)})]))
                                        (control/run! report-instance))}
                           {:label     "Receipt"
                            :visible?  (fn [_ {:todo/keys [receipt?]}]
                                         receipt?)
                            :disabled? (fn [_ {:todo/keys [done]}]
                                         done)
                            :action    (fn [report-instance {:todo/keys [id]}]
                                         (form/create! report-instance ReceiptForm {:todo/id id}))}
                           {:label  "Delete"
                            :action (fn [this {:todo/keys [id]}] (form/delete! this :todo/id id))}]
   ro/initial-sort-params {:sort-by          :todo/due
                           :sortable-columns #{:todo/due :category/label :todo/status}
                           :ascending?       true}
   ro/run-on-mount?       true
   ro/form-links          {todo/text TodoForm}
   ro/route               "todo-report"})
