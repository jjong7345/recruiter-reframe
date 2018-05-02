(ns recruit-app.search.events
  (:require [re-frame.core :as rf]
            [recruit-app.events :as events]
            [recruit-app.db :as db]
            [ajax.core :as ajax]
            [cljs.reader :as edn]
            [clojure.walk :as w]
            [clojure.string :as str]
            [recruit-app.util.uri :as u]
            [recruit-app.util.response :as r]
            [cemerick.url :as url]
            [day8.re-frame.async-flow-fx]
            [recruit-app.util.search :as su]
            [recruit-app.components.modal :as modal]
            [recruit-app.util.events :as ev]
            [recruit-app.components.table.registry]
            [recruit-app.components.table :as table]
            [recruit-app.search.criteria :as criteria]))

(table/register-backend-pagination ::table/search-results {:fetch-url (u/uri :search)})

(ev/reg-events "search" ["criteria" "show-no-result-error?"])
(ev/reg-events "search" "criteria" "search-criteria" ["radius" "company" "school" "candidate-name"
                                                      "title" "keyword" "min-degree-category-id" "work-experience-ids"
                                                      "work-experience-ids" "discipline-ids" "location"])

(ev/reg-events "search" "criteria" "search-parameters" ["include-desired-location?" "only-last-title?" "only-last-company?"])

(defn load-view
  [{:keys [db]} _]
  {:dispatch-n   [[:search/reset-all]
                  [:search/criteria-search-parameters-include-desired-location?-change true]]
   :ga/variation :external
   :ga/page-view ["/search" {}]})

(defn loc-auto-pr [db [_ callback response]]
  (let [resp (r/loc-auto-response response)]
    (callback resp)
    (assoc db :response resp)))

(defn add-loc [db [_ new-location]]
  (-> db (assoc-in [:search :criteria :search-criteria :location] (:name new-location))))

(defn loc-auto
  [{:keys [db]} [_ query callback]]
  (if (> (count query) 1)
    {:http-xhrio {:method          :get
                  :uri             (u/uri :location-autocomplete (url/url-encode query))
                  :format          (ajax/json-request-format)
                  :response-format (ajax/json-response-format {:keywords? true})
                  :on-success      [:search/loc-auto-pr callback]
                  :on-failure      [:search/loc-auto-pr]}
     :db         (assoc-in db [:search :criteria :search-criteria :location] query)}
    (do (callback []) {})))

(defn new-search [{:keys [db]} _]
  {:dispatch [:search/reset-all]
   :route    "/search"})

(defn click-candidate [{:keys [db]} [_ secure-id index]]
  (let [{:keys [saved-search-id sort-by]
         :or   {sort-by "recency"}} (:search-results db)]
    {:route (str
              "/candidate/search"
              (when saved-search-id (str "/" saved-search-id))
              "/" index
              "/" sort-by
              "?jobseekerId=" secure-id)}))

(defn click-fa [{:keys [db]} _]
  {:ga/event   ["full-access" "click" "curtain" {}]
   :dispatch-n [[:search/pay-curtain-click]
                [:go-to-pricing]]})

(defn pay-curtain-click
  "Sends request to API to track pay curtain click"
  [_ _]
  {:ra-http-xhrio {:method          :post
                   :uri             (u/uri :pay-curtain-click)
                   :format          (ajax/json-request-format)
                   :response-format (ajax/json-response-format {:keywords? true})
                   :on-success      [:http-no-on-success]
                   :on-failure      [:http-no-on-failure]}})

(defn set-saved-search
  "Copies search criteria from saved search db to search"
  [db [_ saved-search-id]]
  (assoc-in
    db
    [:search :criteria]
    (get-in db [:saved-searches :saved-searches saved-search-id])))

(defn copy-results-criteria
  "Copies criteria from search results to search criteria"
  [{:keys [search-results] :as db}]
  (assoc-in db [:search :criteria] (:criteria search-results)))

(defn search
  "Calls pagination event with callback to either show error or route to search-results"
  [{:keys [db]}]
  {:dispatch [(table/pagination-event ::table/search-results)
              0
              (criteria/search-request-params (-> db :search :criteria))
              #(rf/dispatch [:search/handle-search-response %])]})

(defn handle-search-response
  "Routes to results if total is greater than 0, otherwise shows error"
  [_ [_ {:keys [total]}]]
  (if (= total 0)
    {:dispatch-n [[:search/show-no-result-error?-change true]
                  [(table/reset-event ::table/search-results)]]}
    {:dispatch-n [[:search-results/set-search-criteria]
                  [:search/show-no-result-error?-change false]]
     :route      "/search-results"}))

(defn limit-min
  [db min-db-path max-db-path min-value]
  (let [max-value (get-in db (vec (concat [:search] max-db-path)))]
    (if max-value
      (if (< min-value max-value)
        (assoc-in db (vec (concat [:search] min-db-path)) min-value)
        (assoc-in db (vec (concat [:search] min-db-path)) max-value))
      (assoc-in db (vec (concat [:search] min-db-path)) min-value))))

(defn limit-max
  [db min-db-path max-db-path max-value]
  (let [min-value (get-in db (vec (concat [:search] min-db-path)))]
    (if min-value
      (if (> max-value min-value)
        (assoc-in db (vec (concat [:search] max-db-path)) max-value)
        (assoc-in db (vec (concat [:search] max-db-path)) min-value))
      (assoc-in db (vec (concat [:search] max-db-path)) max-value))))

(defn min-experience-change
  [{:keys [db]} [_ min-experience]]
  {:db       (limit-min db [:min-experience] [:max-experience] min-experience)
   :dispatch [:search/get-work-experience-ids]})

(defn max-experience-change
  [{:keys [db]} [_ max-experience]]
  {:db       (limit-max db [:min-experience] [:max-experience] max-experience)
   :dispatch [:search/get-work-experience-ids]})

(defn criteria-search-criteria-salary-min-change
  [{:keys [db]} [_ min-salary]]
  {:db (limit-min db [:criteria :search-criteria :salary-min] [:criteria :search-criteria :salary-max] min-salary)})

(defn criteria-search-criteria-salary-max-change
  [{:keys [db]} [_ max-salary]]
  {:db (limit-max db [:criteria :search-criteria :salary-min] [:criteria :search-criteria :salary-max] max-salary)})

(defn criteria-search-criteria-location-change
  [{:keys [db]} [_ location]]
  (let [radius (get-in db [:search :criteria :search-criteria :radius])
        evt {:db (assoc-in db [:search :criteria :search-criteria :location] location)}]
    (if (not radius)
      (merge evt {:dispatch [:search/criteria-search-criteria-radius-change 25]})
      evt)))

(defn get-work-experience-ids
  "Get work-experience-ids from min and max experience values"
  [db _]
  (let [min (get-in db [:search :min-experience])
        max (get-in db [:search :max-experience])
        work-experience-ids (su/get-workexperience-ids min max)]
    (assoc-in db [:search :criteria :search-criteria :work-experience-ids] work-experience-ids)))

(defn reset-all
  "Dissociates search from db"
  [{:keys [db]}]
  {:db       (dissoc db :search :search-results)
   :dispatch [(table/reset-event ::table/search-results)]})

(events/reg-event-fx
  :search/load-view
  load-view)

(events/reg-event-db
  :search/loc-auto-pr
  loc-auto-pr)

(events/reg-event-db
  :search/add-loc
  add-loc)

(events/reg-event-fx
  :search/loc-auto
  loc-auto)

(events/reg-event-fx
  :search/new-search
  new-search)

(events/reg-event-fx
  :search/click-candidate
  click-candidate)

(events/reg-event-fx
  :search/click-fa
  click-fa)

(events/reg-event-fx
  :search/pay-curtain-click
  pay-curtain-click)

(events/reg-event-db
  :search/set-saved-search
  set-saved-search)

(events/reg-event-db
  :search/copy-results-criteria
  copy-results-criteria)

(events/reg-event-fx
  :search/search
  search)

(events/reg-event-fx
  :search/handle-search-response
  handle-search-response)

(events/reg-event-fx
  :search/min-experience-change
  min-experience-change)

(events/reg-event-fx
  :search/max-experience-change
  max-experience-change)

(events/reg-event-db
  :search/get-work-experience-ids
  get-work-experience-ids)

(events/reg-event-fx
  :search/criteria-search-criteria-salary-min-change
  criteria-search-criteria-salary-min-change)

(events/reg-event-fx
  :search/criteria-search-criteria-salary-max-change
  criteria-search-criteria-salary-max-change)

(events/reg-event-fx
  :search/reset-all
  reset-all)