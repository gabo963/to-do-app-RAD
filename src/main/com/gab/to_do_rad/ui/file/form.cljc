(ns com.gab.to-do-rad.ui.file.form
  (:require
    [com.gab.to-do-rad.model.file.attributes :as r.file]
    [com.fulcrologic.rad.form-options :as fo]
    [com.fulcrologic.rad.form :as form]))

(form/defsc-form FileForm [this props]
  {fo/id            r.file/id
   fo/layout-styles {:form-container :file-as-icon}
   fo/attributes    [r.file/uploaded-on
                     r.file/sha
                     r.file/filename]})
