(ns development
  (:require
    [clojure.tools.namespace.repl :as tools-ns :refer [disable-reload! refresh clear set-refresh-dirs]]
    [com.gab.to-do-rad.components.database :refer [datomic-connections]]
    [com.gab.to-do-rad.components.ring-middleware]
    [com.gab.to-do-rad.components.server]
    [com.gab.to-do-rad.model.seed :as seed]
    [com.fulcrologic.rad.ids :refer [new-uuid]]
    [com.fulcrologic.rad.type-support.date-time :as dt]
    [datomic.client.api :as d]
    [mount.core :as mount]
    [taoensso.timbre :as log]))

;; Prevent tools-ns from finding source in other places, such as resources
(set-refresh-dirs "src/main" "src/dev")

(defn seed! []
  (dt/set-timezone! "America/Bogota")
  (let [connection (:main datomic-connections)]
    (when connection
      (log/info "SEEDING data.")
      (d/transact connection {:tx-data
                              [(seed/new-category (new-uuid 451) "Grocery")
                               (seed/new-category (new-uuid 452) "Shopping")
                               (seed/new-category (new-uuid 453) "Family")
                               (seed/new-todo (new-uuid 565) "Buy Eggs" #inst"2024-02-10T00:00:00.001000000-00:00" :todo.status/PLAN false false nil
                                 :todo/category "Grocery")
                               (seed/new-todo (new-uuid 566) "Buy Onions" #inst"2024-02-04T00:00:00.001000000-00:00" :todo.status/WIP false false nil
                                 :todo/category "Grocery")
                               (seed/new-todo (new-uuid 567) "Buy Picanha" #inst"2024-02-03T00:00:00.001000000-00:00" :todo.status/DONE true false #inst"2024-02-05T00:00:00.001000000-00:00"
                                 :todo/category "Grocery")
                               (seed/new-todo (new-uuid 568) "Buy Xbox" #inst"2024-03-10T00:00:00.001000000-00:00" :todo.status/CLOSED false false nil
                                 :todo/category "Shopping")
                               (seed/new-todo (new-uuid 569) "Book Dentist Appointment" #inst"2024-03-15T00:00:00.001000000-00:00" :todo.status/LATE false false nil
                                 :todo/category "Family")
                               (seed/new-todo (new-uuid 570) "Get a new Job" #inst"2023-12-27T00:00:00.001000000-00:00" :todo.status/CLOSED true false #inst"2023-12-26T00:00:00.001000000-00:00"
                                 :todo/category "Family")
                                 (seed/new-todo (new-uuid 571) "Buy a Nintendo Switch" #inst"2024-03-01T00:00:00.001000000-00:00" :todo.status/PLAN false true nil
                                 :todo/category "Shopping")
                               (seed/new-receipt "Receipt1" (new-uuid 891) "Received a single macbook pro" 1 #inst"2024-01-12T00:00:00.001000000-00:00")
                               (seed/new-todo (new-uuid 572) "Receive new job computer" #inst"2024-01-15T00:00:00.001000000-00:00" :todo.status/CLOSED true true #inst"2024-01-12T00:00:00.001000000-00:00"
                                 :todo/category "Family" :todo/receipt "Receipt1" )]}))))

(defn start []
  (mount/start-with-args {:config "config/dev.edn"})
  (seed!)
  :ok)

(defn stop
  "Stop the server."
  []
  (mount/stop))

(defn fast-restart
  "Stop, refresh, and restart the server."
  []
  (stop)
  (start))

(defn restart
  "Stop, refresh, and restart the server."
  []
  (stop)
  (tools-ns/refresh :after 'development/start))
