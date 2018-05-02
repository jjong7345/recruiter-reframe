(ns recruit-app.search-results.events
  (:require [ajax.core :as ajax]
            [recruit-app.util.uri :as u]
            [re-frame.core :as rf]
            [recruit-app.util.events :as ev]
            [recruit-app.search.criteria :as criteria]
            [recruit-app.components.table :as table]))

(ev/reg-events "search-results" ["saved-search-loaded?"
                                 "selected-filter"
                                 "criteria"
                                 "page"
                                 "show-minimum-criteria-error?"])
(ev/reg-events
  "search-results"
  "criteria"
  "search-parameters"
  ["include-candidates-viewed?"
   "include-candidates-contacted?"
   "include-candidates-never-viewed?"
   "include-candidates-never-contacted?"])

(defn load-saved-search
  "Fetches saved search and loads into search criteria"
  [{:keys [db]}]
  (when-let [saved-search-id (-> db :search-results :saved-search-id)]
    {:async-flow {:first-dispatch [:saved-searches/fetch-saved-search saved-search-id]
                  :rules          [{:when     :seen?
                                    :events   :saved-searches/assoc-saved-search
                                    :dispatch [:search/set-saved-search saved-search-id]}
                                   {:when     :seen?
                                    :events   :search/set-saved-search
                                    :dispatch [:search-results/set-search-criteria]}
                                   {:when     :seen?
                                    :events   :search/set-saved-search
                                    :dispatch [:search-results/saved-search-loaded?-change true]}]}}))

(defn set-search-criteria
  "Copies criteria from search db to search-results db"
  [{:keys [search] :as db}]
  (assoc-in db [:search-results :criteria] (:criteria search)))

(defn cancel-filter-click
  "Dispatches events to copy search criteria back and clear selected filter"
  []
  {:dispatch-n [[:search-results/selected-filter-change nil]
                [:search/copy-results-criteria]]})

(defn apply-filter-click
  "Dispatches events to copy search results criteria to search db and clear filter"
  [{:keys [db]}]
  (let [{:keys [search search-results]} db]
    {:dispatch-n (remove
                   nil?
                   [[:search-results/selected-filter-change nil]
                    (when-not (= (:criteria search) (:criteria search-results))
                      [:search-results/reset-table])
                    [:search-results/set-search-criteria]])}))

(defn assoc-saved-search-id
  "Associates given id to saved-search-id in db"
  [db [_ saved-search-id]]
  (assoc-in db [:search-results :saved-search-id] saved-search-id))

(defn saved-search-id-change
  "Assocs id to db and copies from saved-search"
  [{:keys [db]} [_ saved-search-id]]
  (let [saved-search (-> db :saved-searches :saved-searches (get saved-search-id))]
    {:route      (str "/search-results/" saved-search-id)
     :dispatch-n [[:search-results/assoc-saved-search-id saved-search-id]
                  [:search-results/reset-table]
                  [:search-results/criteria-change saved-search]
                  [:search/criteria-change saved-search]]}))

(def filter-search-criteria-keys
  "Keys within :search-criteria map related to given filter key.
  Used for removing a filter"
  {:keywords         [:keyword]
   :location         [:location :radius]
   :title            [:title]
   :salary           [:salary-min :salary-max]
   :candidate-name   [:candidate-name]
   :education        [:min-degree-category-id]
   :company          [:company]
   :school           [:school]
   :years-experience [:work-experience-ids]
   :role             [:discipline-ids]})

(def filter-search-parameter-keys
  "Keys within :search-parameters map related to given filter key.
  Used for removing a filter"
  {:title   [:only-last-title?]
   :company [:only-last-company?]})

(defn- dissoc-criteria
  "Dissocs keys from search-criteria for given key"
  [criteria criteria-key dissoc-keys]
  (if-not (empty? dissoc-keys)
    (apply
      (partial update criteria criteria-key dissoc)
      dissoc-keys)
    criteria))

(defn remove-filter
  "Dissocs values from db for given filter key"
  [{:keys [db]} [_ filter-key]]
  (let [criteria (-> (get-in db [:search-results :criteria])
                     (dissoc-criteria :search-criteria (get filter-search-criteria-keys filter-key))
                     (dissoc-criteria :search-parameters (get filter-search-parameter-keys filter-key)))]
    {:dispatch-n [[:search-results/criteria-change criteria]
                  [:search/criteria-change criteria]
                  [:search-results/reset-table]]}))

(defn sort-by-change
  "Assocs sort-by to db and resets table"
  [{:keys [db]} [_ val]]
  {:db       (assoc-in db [:search-results :sort-by] val)
   :dispatch [:search-results/reset-table]})

(defn load-page
  "Loads given page of search results"
  [{:keys [db]} [_ page]]
  (when-let [criteria (-> db :search-results :criteria)]
    {:dispatch [(table/pagination-event ::table/search-results) page (criteria/search-request-params criteria)]}))

(defn reset-table
  "Removes page from route"
  [{:keys [db]}]
  (let [saved-search-id (-> db :search-results :saved-search-id)]
    {:route    (str "/search-results" (when saved-search-id (str "/" saved-search-id)))
     :dispatch [(table/reset-event ::table/search-results)]}))

(defn go-to-page
  "Goes to correct route given new page"
  [{:keys [db]} [_ page]]
  {:route (if-let [saved-search-id (-> db :search-results :saved-search-id)]
            (str "/search-results/" saved-search-id "/" page)
            (str "/search-results/p/" page))})

(defn update-search-filters
  "Sets search parameters when value for view is changed"
  [_ [_ view]]
  {:dispatch-n [[:search-results/criteria-search-parameters-include-candidates-viewed?-change (if (= view :unviewed) false true)]
                [:search-results/criteria-search-parameters-include-candidates-contacted?-change (if (= view :uncontacted) false true)]
                [:search-results/criteria-search-parameters-include-candidates-never-viewed?-change (if (= view :viewed) false true)]
                [:search-results/criteria-search-parameters-include-candidates-never-contacted?-change (if (= view :contacted) false true)]]})

(defn view-change
  "Dispatches event to record new search parameters then copies to search criteria"
  [_ [_ view]]
  {:async-flow {:first-dispatch [:search-results/update-search-filters view]
                :rules          [{:when     :seen?
                                  :events   :search-results/update-search-filters
                                  :dispatch [:search/copy-results-criteria]}
                                 {:when     :seen?
                                  :events   :search/copy-results-criteria
                                  :dispatch [:search-results/reset-table]}]}})

(defn flash-minimum-criteria-error
  "Dispatches event to show minimum criteria error and hide it after 3 seconds"
  []
  {:dispatch       [:search-results/show-minimum-criteria-error?-change true]
   :dispatch-later [{:ms 3000 :dispatch [:search-results/show-minimum-criteria-error?-change false]}]})

(rf/reg-event-fx
  :search-results/load-saved-search
  load-saved-search)

(rf/reg-event-db
  :search-results/set-search-criteria
  set-search-criteria)

(rf/reg-event-fx
  :search-results/cancel-filter-click
  cancel-filter-click)

(rf/reg-event-fx
  :search-results/apply-filter-click
  apply-filter-click)

(rf/reg-event-db
  :search-results/assoc-saved-search-id
  assoc-saved-search-id)

(rf/reg-event-fx
  :search-results/saved-search-id-change
  saved-search-id-change)

(rf/reg-event-fx
  :search-results/remove-filter
  remove-filter)

(rf/reg-event-fx
  :search-results/sort-by-change
  sort-by-change)

(rf/reg-event-fx
  :search-results/load-page
  load-page)

(rf/reg-event-fx
  :search-results/reset-table
  reset-table)

(rf/reg-event-fx
  :search-results/go-to-page
  go-to-page)

(rf/reg-event-fx
  :search-results/view-change
  view-change)

(rf/reg-event-fx
  :search-results/update-search-filters
  update-search-filters)

(rf/reg-event-fx
  :search-results/flash-minimum-criteria-error
  flash-minimum-criteria-error)
