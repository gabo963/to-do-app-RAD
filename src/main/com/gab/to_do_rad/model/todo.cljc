(ns com.gab.to-do-rad.model.todo
  (:require
    #?@(:clj
        [[com.wsscode.pathom.connect :as pc :refer [defmutation]]
         [com.gab.to-do-rad.components.database-queries :as queries]
         [java-time.api :as jt]]
        :cljs
        [[com.fulcrologic.fulcro.mutations :as m :refer [defmutation]]])
    [com.fulcrologic.rad.attributes :as attr :refer [defattr]]
    [com.fulcrologic.rad.report-options :as ro]
    [com.fulcrologic.rad.form :as form]
    [com.wsscode.pathom.connect :as pc]
    [com.fulcrologic.rad.attributes-options :as ao]
    [com.fulcrologic.rad.type-support.date-time :refer [now]]
    [clojure.string :as str]
    [com.fulcrologic.rad.form-options :as fo]))

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
   ao/identities     #{:todo/id}
   ro/column-heading "Days Completed before or after due"
   ao/pc-input       #{:todo/id}
   ao/pc-output      [:todo/completed-time]
   ao/pc-resolve     (fn [{:keys [parser] :as env} {:todo/keys [id]}]
                       #?(:clj (let [result (get-in (parser env [{[:todo/id id] [:todo/done :todo/due :todo/doneDate]}]) [[:todo/id id]])
                                     {done     :todo/done
                                      due      :todo/due
                                      doneDate :todo/doneDate} result]
                                 (if done {:todo/completed-time (jt/as (jt/duration doneDate due) :days)} {:todo/completed-time 0}))))})

(defattr done-todos :todo/done-todos :ref
  {ao/target     :todo/id
   ao/pc-output  [{:todo/done-todos [:todo/id]}]
   ao/pc-resolve (fn [{:keys [query-params] :as env} _]
                   #?(:clj
                      {:todo/done-todos (queries/get-done-todos env query-params)}))})

#?(:clj
   (pc/defresolver todo-category-resolver [{:keys [parser] :as env} {:todo/keys [id]}]
     {::pc/input  #{:todo/id}
      ::pc/output [:category/id :category/label]}
     (let [result (parser env [{[:todo/id id] [{:todo/category [:category/id :category/label]}]}])]
       (-> result
         (get-in [[:todo/id id] :todo/category])))))

#?(:clj
   (defmutation mark-todo-done [env {:todo/keys [id done]}]
     {::pc/params #{:todo/id}
      ::pc/output [:todo/id]}
     (form/save-form* env {::form/id        id
                           ::form/master-pk :todo/id
                           ::form/delta     {[:todo/id id] {:todo/done     {:before (not done) :after done}
                                                            :todo/doneDate {:before nil :after (when done (now))}}}}))
   :cljs
   (defmutation mark-todo-done [{:account/keys [id done]}]
     (action [{:keys [state]}]
       (swap! state assoc-in [:todo/id id :todo/done] done))
     (remote [_] true)))

(def attributes [id text done due doneDate status category completed-time all-todos done-todos])

#?(:clj
   (def resolvers [todo-category-resolver mark-todo-done]))
