(ns com.gab.to-do-rad.components.save-middleware
  (:require
    [com.fulcrologic.rad.middleware.save-middleware :as r.s.middleware]
    [com.fulcrologic.rad.database-adapters.datomic-cloud :as datomic]
    [com.fulcrologic.rad.blob :as blob]
    [com.gab.to-do-rad.model.attributes :refer [all-attributes]]))

(def middleware
  (->
    (datomic/wrap-datomic-save)
    (blob/wrap-persist-images all-attributes)
    ;; This is where you would add things like form save security/schema validation/etc.

    ;; This middleware lets you morph values on form save
    (r.s.middleware/wrap-rewrite-values)))
