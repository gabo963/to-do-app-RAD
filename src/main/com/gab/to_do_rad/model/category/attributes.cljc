(ns com.gab.to-do-rad.model.category.attributes
  (:require
    [com.fulcrologic.rad.attributes :refer [defattr]]
    [com.fulcrologic.rad.attributes-options :as ao]
    [com.fulcrologic.fulcro.components :refer [defsc]]
    #?(:clj [com.gab.to-do-rad.components.database-queries :as queries])
    [com.fulcrologic.rad.report-options :as ro]))

(defsc Category [_ _]
  {:query [:category/id :category/label]
   :ident :category/id})

(defattr id :category/id :uuid
  {ao/identity? true
   ao/schema    :production})

(defattr label :category/label :string
  {ao/required?      true
   ao/identities     #{:category/id}
   ro/column-heading "Category"
   ao/schema         :production})

(defattr all-categories :category/all-categories :ref
  {ao/target     :category/id
   ao/pc-output  [{:category/all-categories [:category/id]}]
   ao/pc-resolve (fn [{:keys [query-params] :as env} _]
                   #?(:clj
                      {:category/all-categories (queries/get-all-categories env query-params)}))})

(def attributes [id label all-categories])

