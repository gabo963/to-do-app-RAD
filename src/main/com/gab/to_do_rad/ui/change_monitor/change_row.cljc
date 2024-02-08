(ns com.gab.to-do-rad.ui.change-monitor.change-row
  (:require
    #?(:clj  [com.fulcrologic.fulcro.dom-server :as dom :refer [div label input button p i thead table tr td th tbody ul]]
       :cljs [com.fulcrologic.fulcro.dom :as dom :refer [div label input button p i thead table tr td th tbody ul]])
    [com.fulcrologic.fulcro.components :as comp :refer [defsc]]))

(defsc ChangeRow [this {:change/keys [key old-value new-value] num :num :as props}]
  {:query [:change/key :change/old-value :change/new-value :num]
   :ident (fn [] [:change/id (:change/id num)])}
  (tr
    (td {:data-label "#"} (str num))
    (td {:data-label "Key"} (str key))
    (td {:data-label "Old Value"} (str old-value))
    (td {:data-label "New Value"} (str new-value)))
  )

(def ui-change-monitor (comp/factory ChangeRow {:keyfn :num}))


