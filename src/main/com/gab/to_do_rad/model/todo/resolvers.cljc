(ns com.gab.to-do-rad.model.todo.resolvers
  (:require
    #?@(:clj
        [[com.wsscode.pathom.connect :as pc :refer [defmutation]]]
        :cljs
        [[com.fulcrologic.fulcro.mutations :as m :refer [defmutation]]])
    [com.wsscode.pathom.connect :as pc]
    [com.fulcrologic.rad.form :as form]
    [com.fulcrologic.rad.type-support.date-time :refer [now]]))

#?(:clj
   (pc/defresolver todo-category-resolver [{:keys [parser] :as env} {:todo/keys [id]}]
     {::pc/input  #{:todo/id}
      ::pc/output [:category/id :category/label]}
     (let [result (parser env [{[:todo/id id] [{:todo/category [:category/id :category/label]}]}])]
       (-> result
         (get-in [[:todo/id id] :todo/category])))))

#?(:clj
   (pc/defresolver todo-receipt-resolver [{:keys [parser] :as env} {:todo/keys [id]}]
     {::pc/input  #{:todo/id}
      ::pc/output [:receipt/id]}
     (let [result (parser env [{[:todo/id id] [{:todo/receipt [:receipt/id]}]}])]
       (-> result
         (get-in [[:todo/id id] :todo/receipt])))))

#?(:clj
   (defmutation mark-todo-done [env {:todo/keys [id done]}]
     {::pc/params #{:todo/id}
      ::pc/output [:todo/id]}
     (form/save-form* env {::form/id        id
                           ::form/master-pk :todo/id
                           ::form/delta     {[:todo/id id] {:todo/done     {:before (not done) :after done}
                                                            :todo/doneDate {:before nil :after (when done (now))}}}}))
   :cljs
   (defmutation mark-todo-done [{:todo/keys [id done]}]
     (action [{:keys [state]}]
       (swap! state assoc-in [:todo/id id :todo/done] done)
       (swap! state assoc-in [:todo/id id :todo/doneDate] (now)))
     (remote [_] true)))

(defn dissoc-in [m ks v]
  (update-in m ks dissoc v))

#?(:cljs
   (defmutation remove-okay-modal [{:todo/keys [id]}]
     (action [{:keys [state]}]
       (swap! state dissoc-in [:todo/id id] :ui/open-modal?))))

#?(:clj
   (def resolvers [todo-category-resolver mark-todo-done todo-receipt-resolver]))
