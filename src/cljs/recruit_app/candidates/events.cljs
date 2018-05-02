(ns recruit-app.candidates.events
  (:require [recruit-app.events :as events]
            [re-pressed.core :as rp]
            [recruit-app.util.events :as ev]
            [recruit-app.util.uri :as u]
            [recruit-app.util.response :as r]
            [recruit-app.util.candidate :as c]
            [ajax.core :as ajax]
            [clojure.tools.reader.edn :as edn]
            [recruit-app.util.search :as su]
            [cognitect.transit :as t]
            [clojure.walk :refer [keywordize-keys]]
            [recruit-app.util.recruiter :as rec]
            [recruit-app.components.modal :as modal]
            [recruit-app.components.table :as table]))

(ev/reg-events "candidates" ["active-index" "view-type" "active-jobseeker-id"])
(ev/reg-toggle-event "candidates" "is-exporting?")
(ev/reg-toggle-event "candidates" "show-export-success-modal?")
(ev/reg-toggle-event "candidates" "dismissing?")

(defn load-view
  "First sets query params then dispatches event to load candidate data"
  [{:keys [db]} _]
  {:dispatch     [:candidates/set-initial-candidate-data]
   :ga/page-view ["/candidate" {}]})

(defn set-initial-candidate-data
  "Fetches recruiter info and loads data based on view type"
  [{:keys [db]}]
  (let [view-type (-> db :candidates :view-type)
        guest? (-> db :recruiter :recruiter-id rec/valid-recruiter-id? not)
        {:keys [jobseekerId jobLocationId key]} (:query-params db)
        secure-id (or jobseekerId (-> db :candidates :active-jobseeker-id))]
    {:dispatch-n (cond-> [[:candidates/active-jobseeker-id-change secure-id]
                          [:confirm-email/key-change key]
                          [:scroll-top]]
                         jobLocationId (conj [:job/active-job-loc jobLocationId])
                         (or (= view-type :apps) (= view-type :views)) (conj [:job/load-view])
                         (= view-type :search) (conj [:candidates/load-saved-search])
                         (= view-type :project) (conj [:project-list/load-view])
                         secure-id (conj [:candidates/fetch-candidate secure-id])
                         guest? (conj [::modal/open-modal ::modal/confirm-email]))}))

(defn email-candidate
  "Dispatches events to open email modal"
  [{:keys [db]} [_ candidate]]
  {:dispatch-n [[:email/set-email-recipients [(c/email-recipient candidate)]]
                [:email/togg-email-modal]]})

(defn export-to-ats-success
  "Checks for errors/code and will dispatch either failure or show success modal"
  [_ [_ {:keys [errors code]}]]
  (if (or errors code)
    {:dispatch [:candidates/export-to-ats-failure]}
    {:dispatch-n [[::modal/close-modal ::modal/exporting-to-ats]
                  [::modal/open-modal ::modal/export-to-ats-success]]}))

(defn export-to-ats-failure
  "Closes exporting modal and adds error alert"
  [_ _]
  {:dispatch-n [[::modal/close-modal ::modal/exporting-to-ats]
                [:alerts/add-error "Failed to export user to ATS."]]})

(defn export-to-ats
  "Returns http-xhrio request to export candidate to ATS"
  [{:keys [db]} [_ secure-id job-id]]
  {:dispatch      [::modal/open-modal ::modal/exporting-to-ats]
   :ra-http-xhrio {:method          :post
                   :uri             (u/uri :export-to-ats)
                   :params          {:secure-id secure-id
                                     :job-id    job-id}
                   :format          (ajax/json-request-format)
                   :response-format (ajax/json-response-format {:keywords? true})
                   :on-success      [:candidates/export-to-ats-success]
                   :on-failure      [:candidates/export-to-ats-failure]}})

(defn download-resume
  "Routes to download resume URL"
  [{:keys [db]} [_ candidate job-location-id resume-version]]
  {:external-route (u/uri :download-resume (:secure-id candidate) (c/resume-filename candidate) job-location-id)
   :dispatch       [:resume/resume-downloaded (:secure-id candidate) job-location-id resume-version]})

(defn dismiss-candidate-success
  "Dispatches event to add dismiss entry for candidate"
  [_ [_ jobseeker-id job-id job-location-id]]
  {:dispatch-n [[:candidates/toggle-dismissing?]
                [:job/dismiss-candidate jobseeker-id job-id job-location-id]]})

(defn dismiss-candidate-failure
  "Dispatches event to add error alert"
  [_ _]
  {:dispatch-n [[:candidates/toggle-dismissing?]
                [:alerts/add-error "Failed To Dismiss Candidate."]]})

(defn dismiss-candidate
  "Returns http-xhrio request to dismiss candidate"
  [_ [_ secure-id jobseeker-id job-id job-location-id]]
  {:dispatch      [:candidates/toggle-dismissing?]
   :ra-http-xhrio {:method          :post
                   :uri             (u/uri :dismiss-candidate)
                   :params          {:secure-id       secure-id
                                     :job-location-id job-location-id
                                     :job-id          job-id}
                   :format          (ajax/json-request-format)
                   :response-format (ajax/json-response-format {:keywords? true})
                   :on-success      [:candidates/dismiss-candidate-success jobseeker-id job-id job-location-id]
                   :on-failure      [:candidates/dismiss-candidate-failure]}})

(defn fetch-candidate-success
  "Adds candidate to candidates map"
  [db [_ secure-id response]]
  (assoc-in db [:candidates :candidates secure-id] response))

(defn fetch-candidate-failure
  "No response"
  [_ _]
  {})

(defn fetch-candidate
  "Returns http-xhrio request to fetch candidate by secure-id"
  [{:keys [db]} [_ secure-id]]
  (when-not (-> db :candidates :candidates (get secure-id))
    {:http-xhrio {:method          :get
                  :uri             (u/uri :fetch-candidate secure-id)
                  :timeout         5000
                  :format          (ajax/json-request-format)
                  :response-format (ajax/json-response-format {:keywords? true})
                  :on-success      [:candidates/fetch-candidate-success secure-id]
                  :on-failure      [:candidates/fetch-candidate-failure]}}))

(defn- applied?
  "determines if the recruiter is viewing an application"
  [candidate-id job-location-id job-id jobs]
  (->> (get jobs job-id)
       :locations
       (filter #(= (:job_location_id %) job-location-id))
       first
       :applicants
       (filter #(= (:jobSeekerId %) candidate-id))
       first
       not-empty))

(defn track-candidate-view
  "Returns http-xhrio request to track candidate view"
  [{:keys [db]} [_ secure-id resume-version candidate-id job-location-id job-id]]
  (merge (when (applied? candidate-id job-location-id job-id (-> db :jobs :jobs))
           {:dispatch [:candidates/application-viewed candidate-id job-location-id]})
         {:http-xhrio {:method          :post
                       :uri             (u/uri :track-candidate-view secure-id)
                       :params          {:resume-version resume-version}
                       :timeout         5000
                       :format          (ajax/json-request-format)
                       :response-format (ajax/json-response-format {:keywords? true})
                       :on-success      [:http-no-on-success]
                       :on-failure      [:http-no-on-failure]}}))

(defn application-viewed
  "Returns http-xhrio request to track candidate view"
  [{:keys [db]} [_ candidate-id job-location-id]]
  {:http-xhrio {:method          :post
                :uri             (u/uri :application-viewed)
                :params          {:job-location-id job-location-id
                                  :member-id       candidate-id}
                :timeout         5000
                :format          (ajax/json-request-format)
                :response-format (ajax/json-response-format {:keywords? true})
                :on-success      [:http-no-on-success]
                :on-failure      [:http-no-on-failure]}})

(defn load-saved-search
  "Calls API to get saved search then runs current page"
  [{:keys [db]}]
  {:async-flow {:first-dispatch [:search-results/load-saved-search]
                :rules          [{:when     :seen?
                                  :events   :search-results/set-search-criteria
                                  :dispatch [:candidates/fetch-current-search-page]}]}})

(defn fetch-current-search-page
  "Calls pagination event with current page and search params"
  [{:keys [db]}]
  (let [page (Math/floor (/ (-> db :candidates :active-index) 10))]
    {:dispatch [:candidates/load-page page]}))

(defn load-page
  "Loads given page of search results"
  [{:keys [db]} [_ page]]
  (when (= (-> db :candidates :view-type) :search)
    {:dispatch [:search-results/load-page page]}))

(defn register-candidate-keypress
  "Registers event listener to go to next candidate route on left click"
  [_ [_ prev-route next-route]]
  {:dispatch [::rp/set-keydown-rules
              {:event-keys (cond-> []
                                   prev-route (conj [[:go-to-route prev-route]
                                                     [{:which 37}]])
                                   next-route (conj [[:go-to-route next-route]
                                                     [{:which 39}]]))}]})

(events/reg-event-fx
  :candidates/load-view
  load-view)

(events/reg-event-fx
  :candidates/email-candidate
  email-candidate)

(events/reg-event-fx
  :candidates/export-to-ats-success
  export-to-ats-success)

(events/reg-event-fx
  :candidates/export-to-ats-failure
  export-to-ats-failure)

(events/reg-event-fx
  :candidates/export-to-ats
  export-to-ats)

(events/reg-event-fx
  :candidates/download-resume
  download-resume)

(events/reg-event-fx
  :candidates/dismiss-candidate
  dismiss-candidate)

(events/reg-event-fx
  :candidates/dismiss-candidate-success
  dismiss-candidate-success)

(events/reg-event-fx
  :candidates/dismiss-candidate-failure
  dismiss-candidate-failure)

(events/reg-event-fx
  :candidates/fetch-candidate
  fetch-candidate)

(events/reg-event-db
  :candidates/fetch-candidate-success
  fetch-candidate-success)

(events/reg-event-fx
  :candidates/fetch-candidate-failure
  fetch-candidate-failure)

(events/reg-event-fx
  :candidates/track-candidate-view
  track-candidate-view)

(events/reg-event-fx
  :candidates/application-viewed
  application-viewed)

(events/reg-event-fx
  :candidates/set-initial-candidate-data
  set-initial-candidate-data)

(events/reg-event-fx
  :candidates/load-saved-search
  load-saved-search)

(events/reg-event-fx
  :candidates/fetch-current-search-page
  fetch-current-search-page)

(events/reg-event-fx
  :candidates/load-page
  load-page)

(events/reg-event-fx
  :candidates/register-candidate-keypress
  register-candidate-keypress)
