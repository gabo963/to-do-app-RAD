(ns com.gab.to-do-rad.model.todo.attributes
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
    [com.fulcrologic.rad.type-support.date-time :refer [now]]
    [clojure.string :as str]))

(defattr id :todo/id :uuid
  {ao/identity? true
   ao/schema    :production})

(defattr text :todo/text :string
  {ao/schema             :production
   ao/required?          true
   ao/valid?             (fn [value _] (-> value (str/trim) (count) (>= 3)))
   fo/validation-message "Text should be longer than 3 characters"
   ao/identities         #{:todo/id}})

(defattr done :todo/done :boolean
  {ao/schema     :production
   ao/identities #{:todo/id}})

(defattr due :todo/due :instant
  {ao/schema             :production
   ao/valid?             (fn [value props qualified-key] (if (get props :todo/done) true (> value (now))))
   fo/validation-message "Due date should be after today"
   ao/identities         #{:todo/id}})

(defattr doneDate :todo/doneDate :instant
  {ao/schema         :production
   ro/column-heading "Date Marked Done"
   ao/identities     #{:todo/id}})

(def statuses #:todo.status {:DONE   "Done"
                             :WIP    "In progress"
                             :LATE   "Late"
                             :CLOSED "Closed"
                             :PLAN   "Planned"})

(defattr status :todo/status :enum
  {ao/enumerated-values (set (keys statuses))
   ao/identities        #{:todo/id}
   ao/schema            :production
   ao/enumerated-labels statuses})

(defattr files :todo/files :ref
  {ao/target      :file/id
   ao/cardinality :many
   ao/schema      :production
   ao/identities  #{:todo/id}})

(defattr category :todo/category :ref
  {ao/target      :category/id
   ao/cardinality :one
   ao/schema      :production
   ao/required?   true
   ao/identities  #{:todo/id}})

(defattr all-todos :todo/all-todos :ref
  {ao/target     :todo/id
   ao/pc-output  [{:todo/all-todos [:todo/id]}]
   ao/pc-resolve (fn [{:keys [query-params] :as env} _]
                   #?(:clj
                      {:todo/all-todos (queries/get-all-todos env query-params)}))})

(defattr completed-time :todo/completed-time :int
  {ao/target         :todo/id
   ro/column-heading "Days Completed before or after due"
   ao/pc-input       #{:todo/id}
   ao/pc-output      [:todo/completed-time]
   ao/pc-resolve     (fn [{:keys [parser] :as env} {:todo/keys [id]}]
                       #?(:clj (let [result (get-in (parser env [{[:todo/id id] [:todo/done :todo/due :todo/doneDate]}]) [[:todo/id id]])
                                     {done     :todo/done
                                      due      :todo/due
                                      doneDate :todo/doneDate} result]
                                 (if done {:todo/completed-time (jt/as (jt/duration doneDate due) :days)} {:todo/completed-time 0}))))})


;;TODO: REVIEW
(defattr done-todos :todo/done-todos :ref
  {ao/target     :todo/id
   ao/pc-input   #{:todo/done}
   ao/pc-output  [{:todo/done-todos [:todo/id]}]
   ao/pc-resolve (fn [{:keys [query-params] :as env} {:todo/keys [done]}]
                   #?(:clj
                      {:todo/done-todos (queries/get-all-todos env {:todo/done done})}))})

(def attributes [id text done due doneDate status files category completed-time all-todos done-todos])
