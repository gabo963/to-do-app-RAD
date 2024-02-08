(ns com.gab.to-do-rad.model.seed)

(defn new-category
  [id label & {:as extras}]
  (merge
    {:db/id          label
     :category/id    id
     :category/label label}
    extras))

(defn new-receipt
  [dbId id text quantity date & {:as extras}]
  (merge
    {:db/id            dbId
     :receipt/id       id
     :receipt/text     text
     :receipt/quantity quantity
     :receipt/date     date}
    extras))

(defn new-todo
  [id text due status done receipt? doneDate & {:as extras}]
  (merge
    {:todo/id       id
     :todo/text     text
     :todo/done     done
     :todo/status   status
     :todo/due      due
     :todo/receipt? receipt?}
    (if done {:todo/doneDate doneDate} {})
    extras))
