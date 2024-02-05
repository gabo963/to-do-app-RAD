(ns com.gab.to-do-rad.model.seed)

(defn new-category
  [id label & {:as extras}]
  (merge
    {:db/id          label
     :category/id    id
     :category/label label}
    extras))

(defn new-todo
  [id text due status done doneDate & {:as extras}]
  (merge
    {:todo/id     id
     :todo/text   text
     :todo/done   done
     :todo/status status
     :todo/due    due
     :todo/receipt? false}
    (if doneDate {:todo/doneDate doneDate} {})
    extras))
