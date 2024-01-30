(ns com.gab.to-do-rad.components.database
  (:require
    [com.fulcrologic.rad.database-adapters.datomic-cloud :as datomic]
    [mount.core :refer [defstate]]
    [com.gab.to-do-rad.model-rad.attributes :refer [all-attributes]]
    [com.gab.to-do-rad.components.config :refer [config]]))

(defstate ^{:on-reload :noop} datomic-connections
  :start
  (datomic/start-databases all-attributes config))
