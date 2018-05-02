(ns recruit-app.components.candidate
  (:require [recruit-app.util.candidate :as cu]
            [recruit-app.components.table :as table]
            [recruit-app.components.layout :as layout]))

(defn experience
  [{:keys [title company start-date end-date]}]
  (let [time-in-role (cu/date-range start-date end-date)]
    [table/multiline-content
     :top title
     :bottom (cu/company-and-time company time-in-role)]))

(defn experience-history
  "Renders last 3 entries in member's job history"
  [history-list]
  (let [[top-item bottom-item] (take 2 history-list)]
    [layout/column
     :padding 0
     :children [(when top-item
                  [layout/row
                   :padding 0
                   :children [[experience top-item]]])
                (when bottom-item
                  [layout/row-bottom
                   :padding 3
                   :children [[experience bottom-item]]])]]))
