(ns com.gab.to-do-rad.model.todo
  (:require
    #?@(:clj
        [[com.wsscode.pathom.connect :as pc :refer [defmutation]]
         [com.gab.to-do-rad.components.database-queries :as queries]]
        :cljs
        [[com.fulcrologic.fulcro.mutations :as m :refer [defmutation]]])
    [com.fulcrologic.rad.attributes :as attr :refer [defattr]]
    [com.fulcrologic.rad.form :as form]
    [com.wsscode.pathom.connect :as pc]
    [com.fulcrologic.rad.attributes-options :as ao]))

(defattr id :todo/id :uuid
  {ao/identity? true
   ao/schema    :production})

(defattr text :todo/text :string
  {ao/schema     :production
   ao/identities #{:todo/id}})

(defattr done :todo/done :boolean
  {ao/schema     :production
   ao/identities #{:todo/id}})

(defattr due :todo/due :instant
  {ao/schema     :production
   ao/identities #{:todo/id}})

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
   ao/cardinality :many
   ao/schema      :production
   ao/identities  #{:todo/id}})

(defattr all-todos :todo/all-todos :ref
  {ao/target     :todo/id
   ao/pc-output  [{:todo/all-todos [:todo/id]}]
   ao/pc-resolve (fn [{:keys [query-params] :as env} _]
                   #?(:clj
                      {:todo/all-todos (queries/get-all-todos env query-params)}))})
#?(:clj
   (pc/defresolver todo-category-resolver [{:keys [parser] :as env} {:todo/keys [id]}]
     {::pc/input  #{:todo/id}
      ::pc/output [:category/id :category/label]}
     (let [result (parser env [{[:todo/id id] [{:todo/category [:category/id :category/label]}]}])]
       (-> result
         (get-in [[:todo/id id] :todo/category])
         (first)))))

#?(:clj
   (defmutation mark-todo-done [env {:todo/keys [id done]}]
     {::pc/params #{:todo/id}
      ::pc/output [:todo/id]}
     (form/save-form* env {::form/id        id
                           ::form/master-pk :todo/id
                           ::form/delta     {[:todo/id id] {:todo/done {:before (not done) :after done}}}}))
   :cljs
   (defmutation mark-todo-done [{:account/keys [id done]}]
     (action [{:keys [state]}]
       (swap! state assoc-in [:todo/id id :todo/done] done))
     (remote [_] true)))


(def attributes [id text done due status category all-todos])

#?(:clj
   (def resolvers [todo-category-resolver mark-todo-done]))
