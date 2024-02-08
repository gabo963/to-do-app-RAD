(ns com.gab.to-do-rad.ui.receipt.state-machine
  (:require
    #?(:cljs [com.fulcrologic.fulcro.dom :as dom :refer [div label input button p i]])
    [com.gab.to-do-rad.model.receipt.resolvers :as r.receipt]
    [com.fulcrologic.fulcro.algorithms.form-state :as fs]
    [com.fulcrologic.rad.form :as form]
    [com.fulcrologic.fulcro.components :as comp]
    [com.fulcrologic.rad.attributes :as attr]
    [com.fulcrologic.fulcro.mutations :as m]
    [com.fulcrologic.fulcro.ui-state-machines :as uism :refer [defstatemachine]]))

(def state-saving-options
  {::uism/events
   (merge form/global-events
     {:event/save
      {::uism/handler (fn [{::uism/keys [state-map event-data] :as env}]
                        (let [form-class          (uism/actor-class env :actor/form)
                              form-ident          (uism/actor->ident env :actor/form)
                              {::form/keys [id save-mutation]} (comp/component-options form-class)
                              master-pk           (::attr/qualified-key id)
                              proposed-form-props (fs/completed-form-props state-map form-class form-ident)]
                          (if (form/valid? form-class proposed-form-props)
                            (let [data-to-save  (form/calc-diff env)
                                  params        (merge event-data data-to-save)
                                  save-mutation (or save-mutation `form/save-form)
                                  todo-mutation r.receipt/associate-receipt-todo
                                  {{:keys [confirmed]} ::form/triggers} (some-> form-class (comp/component-options))
                                  todo-id       (get-in (uism/retrieve env :options) [:todo/id])]
                              (if todo-id
                                (-> env
                                  (form/clear-server-errors)
                                  (uism/trigger-remote-mutation :actor/form save-mutation
                                    (merge params
                                      {::uism/error-event :event/save-failed
                                       ::master-pk        master-pk
                                       ::form/id          (second form-ident)
                                       ::m/returning      form-class
                                       ::uism/ok-event    :event/saved}))
                                  (uism/trigger-remote-mutation :actor/form todo-mutation
                                    {:receipt/id (second form-ident)
                                     :todo/id    todo-id})
                                  (cond->
                                    confirmed (confirmed form-ident))
                                  (uism/activate :state/saving))
                                (-> env
                                  (form/clear-server-errors)
                                  (uism/trigger-remote-mutation :actor/form save-mutation
                                    (merge params
                                      {::uism/error-event :event/save-failed
                                       ::master-pk        master-pk
                                       ::id               (second form-ident)
                                       ::m/returning      form-class
                                       ::uism/ok-event    :event/saved}))
                                  (cond->
                                    confirmed (confirmed form-ident))
                                  (uism/activate :state/saving))))
                            (-> env
                              (uism/apply-action fs/mark-complete* form-ident)
                              (uism/activate :state/editing)))))}
      :event/cancel
      {::uism/handler (fn [{::uism/keys [state-map event-data] :as env}]
                        (-> env
                          (uism/activate :state/editing)))}
      :event/revert-actions
      {::uism/handler (fn [env]
                        (let [form-ident (uism/actor->ident env :actor/form)]
                          (-> env
                            (form/clear-server-errors)
                            (uism/apply-action fs/pristine->entity* form-ident)
                            (uism/activate :state/editing))))}})})

(defn editing-save-event
  [{::uism/keys [state-map event-data] :as env}]
  (-> env
    (uism/activate :state/saving-options)))

(defstatemachine saving-options-machine
  (-> form/form-machine
    (assoc-in [::uism/states :state/saving-options] state-saving-options)
    (assoc-in [::uism/states :state/editing ::uism/events :event/save ::uism/handler] editing-save-event)))

