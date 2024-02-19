(ns com.gab.to-do-rad.todo-spec
  (:require
    [com.gab.to-do-rad.model.todo.attributes :refer [#?(:clj todo-completed-time)]]
    [fulcro-spec.core :refer [specification assertions component =>]]
    [com.fulcrologic.rad.ids :refer [new-uuid]]
    [com.gab.to-do-rad.model.todo.attributes :refer [statuses]]
    [malli.core :as m]
    [malli.generator :as mg]
    [malli.error :as me]))

(def enum-statuses
  (-> statuses
    (keys)
    (conj [:enum])
    (flatten)
    (vec)))

(defn todo-validation
  [{:todo/keys [done receipt? receipt doneDate]}]
  (if receipt?
    (if done
      (and (inst? doneDate) (not (nil? receipt)))
      (and (not (inst? doneDate)) (nil? receipt)))
    (if done
      (and (inst? doneDate) (nil? receipt))
      (nil? receipt))))

(def todo-schema
  (m/schema
    [:and
     [:map
      {:title "To-do"}
      [:todo/id uuid?]
      [:todo/text string?]
      [:todo/done boolean?]
      [:todo/due inst?]
      [:todo/doneDate {:optional true} inst?]
      [:todo/receipt? boolean?]
      [:todo/receipt
       [:maybe
        [:map
         [:receipt/id uuid?]]]]
      [:todo/status enum-statuses]
      [:todo/category
       [:map
        [:category/id uuid?]]]]
     [:fn todo-validation]]))

(mg/generate todo-schema)

(def todo-many {:todo/id       (new-uuid 1)
                :todo/text     "Buy some vallenato songs"
                :todo/done     true
                :todo/due      #inst"2024-02-25T21:27:00.034-00:00"
                :todo/doneDate #inst"2024-02-14T21:27:00.034-00:00"
                :todo/receipt? false
                :todo/receipt  nil
                :todo/status   :todo.status/WIP
                :todo/category {:category/id #uuid "ffffffff-ffff-ffff-ffff-000000000453"}
                })

(m/validate todo-schema todo-many)

(def todo-zero {:todo/id       (new-uuid 2)
                :todo/done     true
                :todo/due      #inst"2024-02-14T21:27:00.034-00:00"
                :todo/doneDate #inst"2024-02-14T21:27:00.034-00:00"})

(def todo-neg {:todo/id       (new-uuid 3)
               :todo/done     true
               :todo/due      #inst"2024-02-03T21:27:00.034-00:00"
               :todo/doneDate #inst"2024-02-14T21:27:00.034-00:00"})

(def todo-not-done {:todo/id       (new-uuid 4)
                    :todo/done     false
                    :todo/due      #inst"2024-02-03T21:27:00.034-00:00"
                    :todo/doneDate nil})

(def todo-crazy-due {:todo/id       (new-uuid 4)
                     :todo/done     false
                     :todo/due      inc
                     :todo/doneDate #inst"2024-02-12T21:27:00.034-00:00"})

(def todo-crazy-doneDate {:todo/id       (new-uuid 4)
                          :todo/done     false
                          :todo/due      #inst"2024-02-03T21:27:00.034-00:00"
                          :todo/doneDate "This is a date"})

(comment
  (me/humanize (m/explain todo-schema todo-many))

  )

(specification "To-do Schema"
  (assertions
    "To-do not done"
    (-> (m/explain todo-schema todo-many)
      (me/humanize)) => nil))

#?(:clj (specification "To-do Completed Time"
          (assertions
            "Todo Completed Time Many Days"
            (todo-completed-time todo-many) => {:todo/completed-time 11})
          (assertions
            "Todo Completed Time 0 days"
            (todo-completed-time todo-zero) => {:todo/completed-time 0})
          (assertions
            "Todo Completed Time Negative days"
            (todo-completed-time todo-neg) => {:todo/completed-time -11})
          (assertions
            "Not done todo"
            (todo-completed-time todo-not-done) => {:todo/completed-time 0})
          (assertions
            "Due date is not an instant"
            (todo-completed-time todo-crazy-due) => {:todo/completed-time 0})
          (assertions
            "Done Date is not an instat"
            (todo-completed-time todo-crazy-doneDate) => {:todo/completed-time 0})))

