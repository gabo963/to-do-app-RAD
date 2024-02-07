(ns com.gab.to-do-rad.model.receipt.resolvers
  (:require
    #?@(:clj
        [[com.wsscode.pathom.connect :as pc :refer [defmutation]]]
        :cljs
        [[com.fulcrologic.fulcro.mutations :as m :refer [defmutation]]])
    [com.fulcrologic.rad.form :as form]
    [com.fulcrologic.rad.type-support.date-time :refer [now]]))

#?(:clj
   (defmutation associate-receipt-todo [{:keys [pareser] :as env} {:receipt/keys [receipt-id]} {:todo/keys [todo-id]}]
     {::pc/input  #{:receipt/id :todo/id}
      ::pc/output []}
     (form/save-form* env {::form/id        todo-id
                           ::form/master-pk :todo/id
                           ::form/delta     {[:todo/id todo-id] {:todo/receipt  {:before nil :after {:receipt/id receipt-id}}
                                                                 :todo/done     {:before false :after true}
                                                                 :todo/doneDate {:before nil :after (now)}}}}))

   :cljs
   (defmutation associate-receipt-todo [{:receipt/keys [receipt-id]} {:todo/keys [todo-id]}]
     (action [{:keys [state]}]
       (swap! state assoc-in [:todo/id todo-id :todo/receipt] {:receipt/id receipt-id})
       (swap! state assoc-in [:todo/id todo-id :todo/done] true)
       (swap! state assoc-in [:todo/id todo-id :todo/doneDate] (now)))
     (remote [_] true)))

#?(:clj
   (def resolvers [associate-receipt-todo]))
