(ns recruit_app.dashboard.events
  (:require [recruit-app.events :as events]
            [recruit-app.db :as db]
            [recruit-app.util.events :as ev]
            [ajax.core :as ajax]
            [cljs.reader :as edn]
            [recruit-app.util.uri :as u]))

(ev/reg-events "dashboard" ["education-dataset" "experience-dataset" "salary-dataset" "dashboard-data"
                            "is-fetching?" "team-summary-data" "is-admin?" "loading-team-summary?"])

(defn on-dashboard-data-success
  "Store fetched dashboard into db"
  [{:keys [db]} [_ response]]
  {:dispatch-n [[:dashboard/dashboard-data-change response]
                [:dashboard/is-fetching?-change false]]})

(defn get-dashboard-data
  "Fetch recruiter's dashboard data"
  [{:keys [db]} _]
  {:dispatch      [:dashboard/is-fetching?-change true]
   :ra-http-xhrio {:method          :get
                   :uri             (u/uri :recruiter-dashboard)
                   :format          (ajax/json-request-format)
                   :response-format (ajax/json-response-format {:keywords? true})
                   :on-success      [:dashboard/on-dashboard-data-success]
                   :on-failure      [:dashboard/on-dashboard-data-success]}})

(defn on-team-summary-data-success
  "Store fetched team summary into db"
  [{:keys [db]} [_ response]]
  {:dispatch-n [[:dashboard/team-summary-data-change response]
                [:dashboard/loading-team-summary?-change false]]})

(defn get-team-summary-data
  "Fetch recruiter's team summary data"
  [{:keys [db]} _]
  (let [recruiter-id (-> db :recruiter :recruiter-id)]
    {:dispatch      [:dashboard/loading-team-summary?-change true]
     :ra-http-xhrio {:method          :get
                     :uri             (u/uri :team-summary recruiter-id)
                     :format          (ajax/json-request-format)
                     :response-format (ajax/json-response-format {:keywords? true})
                     :on-success      [:dashboard/on-team-summary-data-success]
                     :on-failure      [:dashboard/loading-team-summary?-change false]}}))


(defn on-is-admin-success
  "Store fetched team id of admin into db"
  [{:keys [db]} [_ response]]
  {:dispatch [:dashboard/is-admin?-change response]})

(defn get-is-admin
  "Fetch recruiter's team id"
  [{:keys [db]} _]
  (let [recruiter-id (-> db :recruiter :recruiter-id)]
    {:ra-http-xhrio {:method          :get
                     :uri             (u/uri :is-admin)
                     :format          (ajax/json-request-format)
                     :response-format (ajax/json-response-format {:keywords? true})
                     :on-success      [:dashboard/on-is-admin-success]
                     :on-failure      [:http-no-on-failure]}}))

(defn go-to-applicants
  "Route to job applicants page"
  [_ [_ job-id]]
  {:route (str "/job/" job-id "/apps")})

(defn go-to-manage-jobs
  "route to manage jobs page"
  [_ [_ active-tab]]
  {:route    "/jobs"
   :dispatch [:jobs/active-tab-change active-tab]})

(defn post-job
  "Routes to post job page"
  [_ _]
  {:route "/post-job"})

(defn get-initial-data
  "Fetch initial data for the page"
  [_ _]
  {:dispatch-n [[:dashboard/get-is-admin]
                [:dashboard/get-dashboard-data]
                [:dashboard/get-team-summary-data]]})

(events/reg-event-fx
  :dashboard/get-initial-data
  get-initial-data)

(events/reg-event-fx
  :dashboard/get-dashboard-data
  get-dashboard-data)

(events/reg-event-fx
  :dashboard/get-team-summary-data
  get-team-summary-data)

(events/reg-event-fx
  :dashboard/on-dashboard-data-success
  on-dashboard-data-success)

(events/reg-event-fx
  :dashboard/on-team-summary-data-success
  on-team-summary-data-success)

(events/reg-event-fx
  :dashboard/get-is-admin
  get-is-admin)

(events/reg-event-fx
  :dashboard/on-is-admin-success
  on-is-admin-success)

(events/reg-event-fx
  :dashboard/go-to-applicants
  go-to-applicants)

(events/reg-event-fx
  :dashboard/go-to-manage-jobs
  go-to-manage-jobs)

(events/reg-event-fx
  :dashboard/post-job
  post-job)

