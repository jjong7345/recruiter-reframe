(ns recruit-app.project-list.events
  (:require [recruit-app.events :as events]
            [recruit-app.db :as db]
            [recruit-app.util.events :as ev]
            [recruit-app.util.job :as ju]
            [clojure.set :refer [union difference]]
            [ajax.core :as ajax]
            [recruit-app.util.uri :as u]
            [recruit-app.util.projects :as p]
            [recruit-app.components.modal :as modal]
            [recruit-app.components.table :as table]))

(ev/reg-events "project-list" ["page-loaded?" "checked-candidates"])

(defn load-view
  "Loads initial data for view"
  [{:keys [db]} _]
  (let [active-project-id (-> db :projects :curr-project-id)]
    {:dispatch-n [[:projects/get-candidates-data active-project-id]
                  [:projects/get-projects-data]]}))

(defn remove-candidate-list
  "Iterates over ids and calls for deletion"
  [{:keys [db]} [_ checked-candidates]]
  (let [candidate-ids (map (comp :secureId :subscriber) checked-candidates)
        project-id (-> db :projects :curr-project-id)]
    {:dispatch-n (-> (mapv #(vector :projects/remove-candidate-from-project % project-id) candidate-ids)
                     (conj [:project-list/checked-candidates-change nil])
                     (conj [(table/clear-checked-event ::table/project-candidates)])
                     (conj [::modal/close-modal ::modal/delete-project-candidates]))}))

(defn back-to-all-projects
  "Routes back to Projects page"
  [_ _]
  {:route "/projects"})

(defn click-email-candidates
  "Dispatches events to populate email modal and open"
  [{:keys [db]} [_ checked-candidates]]
  {:dispatch-n [[:email/set-email-recipients (mapv p/email-candidate checked-candidates)]
                [:email/togg-email-modal]]})

(events/reg-event-fx
  :project-list/load-view
  load-view)

(events/reg-event-fx
  :project-list/back-to-all-projects
  back-to-all-projects)

(events/reg-event-fx
  :project-list/click-email-candidates
  click-email-candidates)

(events/reg-event-fx
  :project-list/remove-candidate-list
  remove-candidate-list)