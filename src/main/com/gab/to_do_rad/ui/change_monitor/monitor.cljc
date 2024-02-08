(ns com.gab.to-do-rad.ui.change-monitor.monitor
  (:require
    #?(:clj  [com.fulcrologic.fulcro.dom-server :as dom :refer [div label input button p i thead table tr td th tbody ul]]
       :cljs [com.fulcrologic.fulcro.dom :as dom :refer [div label input button p i thead table tr td th tbody ul]])
    [com.fulcrologic.fulcro.components :as comp :refer [defsc]]
    [com.gab.to-do-rad.ui.change-monitor.change-row :refer [ui-change-monitor ChangeRow]]))

(defsc ChangeTable [this {list :ui/lastChange :as props}]
  {:query [{:ui/lastChange (comp/get-query ChangeRow)}]
   :ident (fn [] [:component/id ::change-table])}
  (let [list (map (fn [dic num] (merge dic {:num num})) list (range (count list)))]
    (div
      (ul))
    (table :.ui.celled.table
      (thead
        (tr
          (th "#")
          (th "Key")
          (th "Old Value")
          (th "New Value")))
      (tbody
        (map ui-change-monitor list)))))

(def ui-change-table (comp/factory ChangeTable))
