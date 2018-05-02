(ns recruit-app.project-list.subs
  (:require-macros [reagent.ratom :refer [reaction]])
  (:require [re-frame.core :as rf]
            [clojure.set :refer [union]]
            [recruit-app.util.subscription :as subs]
            [recruit-app.util.candidate :as c]
            [recruit-app.util.job :as ju]
            [recruit-app.components.table :as table]))

(subs/reg-subs "project-list" [["page-loaded?" false] ["checked-candidates" #{}]])

(defn active-list-candidates
  "Returns active list candidates from projects data"
  [candidates _]
  (vals candidates))

(defn active-project-candidates
  "Returns candidates formatted for viewing on candidate profile page"
  [candidates _]
  (map c/list-candidate candidates))

(rf/reg-sub
  :project-list/active-list-candidates
  :<- [:projects/candidates-data]
  active-list-candidates)

(rf/reg-sub
  :project-list/active-project-candidates
  :<- [(table/sorted-data-sub ::table/project-candidates)]
  active-project-candidates)
