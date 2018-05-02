(ns recruit-app.saved-searches.events
  (:require [re-frame.core :as rf]
            [recruit-app.db :as db]
            [ajax.core :as ajax]
            [clojure.set :refer [union difference]]
            [recruit-app.util.ajax :as a]
            [recruit-app.util.events :as ev]
            [recruit-app.util.job :as ju]
            [recruit-app.util.uri :as u]
            [cljs.reader :as edn]
            [recruit-app.util.search :as su]
            [day8.re-frame.async-flow-fx]
            [recruit-app.components.table :as table]
            [recruit-app.components.modal :as modal]))

(ev/reg-events "saved-searches" ["editing-saved-search" "show-errors?" "fetching-all?"])
(ev/reg-events "saved-searches" "editing-saved-search" ["search-name" "search-id"])
(ev/reg-events "saved-searches" "editing-saved-search" "reporting" "email" ["frequency-type" "interval"])

(defn load-view
  "Dispatches events to load saved-searches data"
  [_ _]
  {:dispatch     [:saved-searches/get-initial-data]
   :ga/page-view ["/saved-searches" {}]})

(defn get-initial-data
  "Dispatches events to load saved-searches data"
  [_ _]
  {:dispatch [:saved-searches/fetch-saved-searches]})

(defn process-response-delete
  "Processes response from delete"
  [_ [_ response]]
  {:dispatch-n [[:saved-searches/fetch-saved-searches]
                [(table/clear-checked-event ::table/saved-searches)]
                [:saved-searches/toggle-is-fetching?]]})

(defn delete-saved-searches-failure
  "Renders alert to user"
  []
  {:dispatch [:alerts/add-error "Failed To Delete Saved Searches. Please Try Again."]})

(defn delete-saved-searches
  "Deletes selected saved searches"
  [{:keys [db]} [_ checked-saved-searches]]
  (let [data-to-send (map :search-id checked-saved-searches)]
    (when (> (count data-to-send) 0)
      {:ra-http-xhrio {:method          :delete
                       :uri             (u/uri :delete-saved-searches)
                       :params          {:saved-search-ids data-to-send}
                       :format          (ajax/json-request-format)
                       :response-format (ajax/raw-response-format)
                       :on-success      [:saved-searches/process-response-delete]
                       :on-failure      [:saved-searches/delete-saved-searches-failure]}
       :dispatch      [:saved-searches/toggle-is-fetching?]})))

(defn assoc-saved-search
  "Associates saved search to db"
  [db [_ {:keys [search-id] :as saved-search}]]
  (assoc-in db [:saved-searches :saved-searches search-id] saved-search))

(defn fetch-saved-search-success
  "Associates saved search to db and sets current search criteria"
  [_ [_ {:keys [saved-search]}]]
  {:dispatch [:saved-searches/assoc-saved-search saved-search]})

(defn fetch-saved-search
  "Fetches saved search from API for given id"
  [_ [_ saved-search-id]]
  {:ra-http-xhrio {:method          :get
                   :uri             (u/uri :saved-search saved-search-id)
                   :format          (ajax/json-request-format)
                   :response-format (ajax/json-response-format {:keywords? true})
                   :on-success      [:saved-searches/fetch-saved-search-success]
                   :on-failure      [:http-no-on-failure]}})

(defn fetch-saved-searches
  "Fetches all saved searches from API"
  []
  {:dispatch      [:saved-searches/fetching-all?-change true]
   :ra-http-xhrio {:method          :get
                   :uri             (u/uri :saved-searches)
                   :format          (ajax/json-request-format)
                   :response-format (ajax/json-response-format {:keywords? true})
                   :on-success      [:saved-searches/fetch-saved-searches-success]
                   :on-failure      [:saved-searches/fetching-all?-change false]}})

(defn fetch-saved-searches-success
  "Associates saved searches to db"
  [{:keys [db]} [_ saved-searches]]
  {:db       (assoc-in db [:saved-searches :saved-searches] (zipmap (map :search-id saved-searches) saved-searches))
   :dispatch [:saved-searches/fetching-all?-change false]})

(defn open-save-new-modal
  "Sets editing-saved-search with no name and opens modal"
  [{:keys [db]} [_ saved-search]]
  {:dispatch-n [[:saved-searches/editing-saved-search-change (dissoc saved-search :search-name :search-id :reporting)]
                [::modal/open-modal ::modal/edit-saved-search]]})

(defn open-edit-modal
  "Sets editing-saved-search and opens modal"
  [{:keys [db]} [_ saved-search]]
  {:dispatch-n [[:saved-searches/editing-saved-search-change saved-search]
                [::modal/open-modal ::modal/edit-saved-search]]})

(defn save-search-success
  "Routes to saved search view and fetches searches"
  [_ [_ update? {:keys [saved-search]}]]
  (if-let [search-id (:search-id saved-search)]
    (merge
      (when-not update?
        {:route (str "/search-results/" search-id)})
      {:dispatch-n [[:saved-searches/fetch-saved-searches]
                    [:alerts/add-success "Successfully saved search!"]]})
    {:dispatch [:saved-searches/save-search-failure]}))

(defn save-search-failure
  "Displays alert that saved search was not sent"
  []
  {:dispatch [:alerts/add-error "Failed To Save Search. Please Try Again."]})

(defn save-search
  [{:keys [db]} [_ {:keys [search-id] :as saved-search}]]
  {:ra-http-xhrio {:method          (if search-id :put :post)
                   :uri             (u/uri :save-search search-id)
                   :params          saved-search
                   :format          (ajax/json-request-format)
                   :response-format (ajax/json-response-format {:keywords? true})
                   :on-success      [:saved-searches/save-search-success (some? search-id)]
                   :on-failure      [:saved-searches/save-search-failure]}
   :dispatch      [::modal/close-modal ::modal/edit-saved-search]})

(rf/reg-event-fx
  :saved-searches/process-response-delete
  process-response-delete)

(rf/reg-event-fx
  :saved-searches/delete-saved-searches-failure
  delete-saved-searches-failure)

(rf/reg-event-fx
  :saved-searches/delete
  delete-saved-searches)

(rf/reg-event-fx
  :saved-searches/get-initial-data
  get-initial-data)

(rf/reg-event-fx
  :saved-searches/load-view
  load-view)

(rf/reg-event-db
  :saved-searches/assoc-saved-search
  assoc-saved-search)

(rf/reg-event-fx
  :saved-searches/fetch-saved-search
  fetch-saved-search)

(rf/reg-event-fx
  :saved-searches/fetch-saved-search-success
  fetch-saved-search-success)

(rf/reg-event-fx
  :saved-searches/fetch-saved-searches
  fetch-saved-searches)

(rf/reg-event-fx
  :saved-searches/fetch-saved-searches-success
  fetch-saved-searches-success)

(rf/reg-event-fx
  :saved-searches/open-save-new-modal
  open-save-new-modal)

(rf/reg-event-fx
  :saved-searches/open-edit-modal
  open-edit-modal)

(rf/reg-event-fx
  :saved-searches/save-search
  save-search)

(rf/reg-event-fx
  :saved-searches/save-search-success
  save-search-success)

(rf/reg-event-fx
  :saved-searches/save-search-failure
  save-search-failure)
