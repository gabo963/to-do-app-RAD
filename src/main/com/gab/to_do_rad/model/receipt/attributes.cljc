(ns com.gab.to-do-rad.model.receipt.attributes
  (:require
    #?@(:clj
        [[com.gab.to-do-rad.components.database-queries :as queries]
         [java-time.api :as jt]]
        :cljs
        [[com.fulcrologic.fulcro.mutations :as m :refer [defmutation]]])
    [com.fulcrologic.rad.attributes :refer [defattr]]
    [com.fulcrologic.rad.report-options :as ro]
    [com.fulcrologic.rad.attributes-options :as ao]
    [com.fulcrologic.rad.form-options :as fo]
    [com.fulcrologic.rad.type-support.date-time :refer [now]]))

(defattr id :receipt/id :uuid
  {ao/identity? true
   ao/schema    :production})

(defattr text :receipt/text :string
  {ao/schema             :production
   ro/title              "Receipt Text"
   fo/validation-message "Require a text description"
   ao/required?          true
   ao/identities         #{:receipt/id}})

(defattr quantity :receipt/quantity :int
  {ao/schema             :production
   ro/title              "Received Quantity"
   ao/required?          true
   fo/validation-message "Quantity has to be greater than 0"
   ao/valid?             (fn [value _ _] (> value 0))
   ao/identities         #{:receipt/id}})

(defattr date :receipt/date :instant
  {ao/schema             :production
   ao/valid?             (fn [value _ _] (>= value (now)))
   fo/validation-message "Date has to be either now or later"
   ro/column-heading     "Date received"
   ao/identities         #{:receipt/id}})

(defattr files :receipt/files :ref
  {ao/target      :file/id
   ao/cardinality :many
   ao/schema      :production
   ao/identities  #{:receipt/id}})

(def attributes [id text quantity date files])
