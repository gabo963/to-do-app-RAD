(ns com.gab.to-do-rad.ui.todo.form
  (:require
    [com.gab.to-do-rad.model.todo.attributes :as todo]
    [com.gab.to-do-rad.model.category.attributes :as category]
    [com.gab.to-do-rad.ui.file.form :refer [FileForm]]
    [com.gab.to-do-rad.model.attributes :as model]
    [com.fulcrologic.rad.form-options :as fo]
    [com.fulcrologic.fulcro.algorithms.form-state :as fs]
    [com.fulcrologic.rad.form :as form]
    [com.fulcrologic.rad.picker-options :as picker-options]))

(def todo-validator (fs/make-validator (fn [form field]
                                         (let [value (get form field)]
                                           (case field
                                             (= :valid (model/all-attribute-validator form field)))))))

(form/defsc-form TodoForm [this props]
  {fo/id            todo/id
   fo/attributes    [todo/text
                     todo/due
                     todo/status
                     todo/category
                     todo/done
                     todo/files]
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
   fo/validator     todo-validator
   fo/subforms      {:todo/files {fo/ui                    FileForm
                                  fo/title                 "Files"
                                  fo/can-delete?           (fn [_ _] true)
                                  fo/layout-styles         {:ref-container :file}
                                  ::form/added-via-upload? true}}
   fo/layout        [[:todo/text]
                     [:todo/due :todo/status :todo/category]
                     [:todo/files]]})
