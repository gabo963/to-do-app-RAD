(ns com.gab.to-do-rad.model-rad.attributes
  "Central place to gather all RAD attributes to ensure they get required and
   stay required.

   Also defines common helpful things related to the attributes of the model, such
   as a default form validator and attribute lookup."
  (:require
    [com.gab.to-do-rad.model-rad.file :as m.file]
    [com.gab.to-do-rad.model.todo.attributes :as m.todo]
    [com.gab.to-do-rad.model.category :as m.category]
    [com.fulcrologic.rad.attributes :as attr]))

(def all-attributes (into []
                      (concat
                        m.file/attributes
                        m.todo/attributes
                        m.category/attributes)))

(def key->attribute (attr/attribute-map all-attributes))

(def all-attribute-validator (attr/make-attribute-validator all-attributes))
