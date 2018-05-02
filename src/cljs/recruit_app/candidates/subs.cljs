(ns recruit-app.candidates.subs
  (:require [re-frame.core :as rf]
            [recruit-app.util.subscription :as subs]
            [recruit-app.util.candidate :as c]
            [recruit-app.util.uri :as u]
            [recruit-app.util.search :as su]
            [cemerick.url :as url]
            [recruit-app.util.date :as date]
            [recruit-app.util.candidate :as cu]
            [recruit-app.components.table :as table]
            [clojure.string :as str]))

(subs/reg-subs "candidates" [["is-exporting?" false] ["show-export-success-modal?" false]
                             ["source" ""] ["active-index" 0] ["view-type" nil]
                             ["candidates" {}] ["active-jobseeker-id" nil]
                             ["dismissing?" false]])

(defn candidate-route
  "Returns route to candidate at given index based on type"
  [[view-type index secure-id saved-search-id sort-by job-id job-loc-id project-id] _]
  (when index
    (str
      (case view-type
        :apps (str "/candidate/" index "/job/" job-id "/apps/" job-loc-id)
        :views (str "/candidate/" index "/job/" job-id "/views/" job-loc-id)
        :search (str "/candidate/search" (when saved-search-id (str "/" saved-search-id)) "/" index "/" sort-by)
        :project (str "/candidate/project/" project-id "/" index))
      "?jobseekerId=" secure-id)))

(defn metadata-fetched?
  "Returns whether metadata has been fetched for given secure-id"
  [[secure-id metadata-fetched] _]
  (contains? metadata-fetched secure-id))

(defn active-candidate-has-resume?
  "Checks if current candidate's resume metadata is fetched and not empty"
  [[metadata-fetched? resume-metadata] _]
  (or (not metadata-fetched?) (and metadata-fetched? resume-metadata)))

(defn active-candidate-application-history
  "Returns application-history for active candidate"
  [{:keys [application-history]} _]
  application-history)

(defn active-candidate-is-dismissed?
  "Returns whether or not the candidate has been dismissed based on history"
  [application-history _]
  (->> application-history
       (filter #(= "Dismissed" (:apply_status %)))
       (not-empty)))

(defn show-application-bar?
  "Should show when current candidate has applied to active job"
  [apply-time _]
  (some? apply-time))

(defn active-candidate-apply-time
  "Return application info for current candidate and job"
  [candidate _]
  (:apply-time candidate))

(defn- try-subscribe
  "Attempts to subscribe and return data, return nil on error
  This try/catch is required for when the user refreshes the profile page
  and the table subscription is not yet registered"
  [sub]
  (try
    @(rf/subscribe [sub])
    (catch js/Error e [])))

(defn viewable-candidates
  "Returns map keyed by page of candidates viewable on profile page
  There are 4 ways to reach the candidate page:
   1. Applicants: Candidates who have applied to a job
   2. Viewers: Candidates who have viewed a job
   3. Search: Candidate results from performing a search
   4. Projects: Candidates who have been saved to given project

  Note: I didn't like having to subscribe directly here but these subs are
  dependent on table subscriptions that are dynamically created when the view
  is rendered"
  [[type search-candidates] _]
  (case type
    :apps {0 (try-subscribe :job/active-applicants)}
    :views {0 (try-subscribe :job/active-viewers)}
    :search search-candidates
    :project {0 (try-subscribe :project-list/active-project-candidates)}
    {}))

(defn applicants-back-route
  "Returns route from candidate profile page to applicants page"
  [[job-id job-location-id] _]
  (str "/#/job/" job-id "/apps" (when job-location-id (str "/" job-location-id))))

(defn views-back-route
  "Returns route from candidate profile page to views page"
  [[job-id job-location-id] _]
  (str "/#/job/" job-id "/views" (when job-location-id (str "/" job-location-id))))

(defn search-back-route
  "Returns route from candidate profile page to search page"
  [[saved-search-id search-page] _]
  (str "/#/search-results"
       (if saved-search-id
         (str "/" saved-search-id "/" search-page)
         (str "/p/" search-page))))

(defn projects-back-route
  "Returns route from candidate profile page to projects page"
  [project-id _]
  (str "/#/projects/" project-id))

(defn back-route
  "Returns route from candidate profile page to previous view depending on view type"
  [[type apps-route views-route search-route projects-route] _]
  (case type
    :apps apps-route
    :views views-route
    :search search-route
    :project projects-route
    "/"))

(defn active-candidate
  "Returns either candidate from view type or jobseeker"
  [[candidate jobseeker] _]
  (or candidate jobseeker))

(defn active-candidate-from-view
  "Returns candidate by index and page. Since the structure
  of candidates is a map keyed by page we need to first get
  the current page results and find the candidate within that page."
  [[candidates index page active-secure-id] _]
  (when index
    (let [{:keys [secure-id] :as candidate} (-> candidates
                                                (get page)
                                                (nth index nil))]
      (when (and secure-id active-secure-id (= secure-id active-secure-id))
        candidate))))

(defn candidate
  "Returns candidate by index and page. Since the structure
  of candidates is a map keyed by page we need to first get
  the current page results and find the candidate within that page."
  [[candidates index page] _]
  (when index
    (-> candidates
        (get page)
        (nth index nil))))

(defn current-page-index
  "Finds index of active candidate within current page
  by subtracting the product of current page and per-page
  from the overall index"
  [[page per-page index] _]
  (- index (* page per-page)))

(defn page
  "Returns page based on current index and results per page"
  [[per-page index] _]
  (quot index per-page))

(defn active-candidate-name
  "Returns formatted name from active candidate"
  [candidate _]
  (c/candidate-name candidate))

(defn active-candidate-location
  "Returns formatted string of active candidate location"
  [{:keys [location]} _]
  (c/candidate-location location))

(defn active-candidate-desired
  "Returns desired salary for active candidate"
  [candidate _]
  (:desired candidate))

(defn active-candidate-history
  "Returns formatted history for active candidate"
  [candidate _]
  (c/history candidate))

(defn active-candidate-id
  "Returns jobseeker-id for active candidate"
  [candidate _]
  (:id candidate))

(defn secure-id
  "Returns secure-id for candidate"
  [candidate _]
  (:secure-id candidate))

(defn current-candidate-number
  "Adds 1 to current index to give current candidate number"
  [index _]
  (+ index 1))

(defn viewable-candidate-count
  "Returns count of all viewable candidates in all pages"
  [candidates _]
  (reduce
    (fn [sum [_ candidates]] (+ sum (count candidates)))
    0
    candidates))

(defn total-candidates
  "When viewing candidates from search, use search
  total, otherwise use count of candidate collection"
  [[type search-total viewable-count] _]
  (case type
    :search search-total
    viewable-count))

(defn previous-candidate-index
  "Returns index of prev candidate, or nil if on first candidate"
  [current-index _]
  (let [new-index (- current-index 1)]
    (when (<= 0 new-index)
      new-index)))

(defn next-candidate-index
  "Returns index of next candidate, or nil if already on last candidate"
  [[total current-index] _]
  (let [new-index (+ current-index 1)]
    (when (< new-index total)
      new-index)))

(defn resume-url
  "Returns url for candidate resume"
  [[secure-id candidate search-term] _]
  (when secure-id
    (str
      (js/encodeURIComponent
        (u/uri :resume secure-id (c/resume-filename candidate)))
      (when search-term
        (str "#search=" (url/url-encode search-term) "&phrase=false")))))

(defn active-candidate-resume-metadata
  "Returns resume metadata for active candidate"
  [[secure-id metadata] _]
  (get metadata secure-id))

(defn per-page
  "Since search results are paginated, per page is according to
  search/per-page, other types, the per page will be the total
  result set"
  [[type total search-per-page] _]
  (case type
    :search search-per-page
    total))

(defn page-loaded?
  "Page is considered loaded when secure-id is not nil"
  [[secure-id is-fetching?] _]
  (and (not is-fetching?) (some? secure-id)))

(defn active-jobseeker
  "Returns jobseeker from candidates map"
  [[candidates secure-id] _]
  (-> candidates
      (get secure-id nil)
      (c/jobseeker->candidate secure-id)))

(defn show-candidate-scroller?
  "If candidate was retrieved from view (:apps, :views, :search) then show scroller"
  [candidate _]
  (some? candidate))

(defn is-fetching-candidate?
  "Returns whether or not candidate is still being fetched based on view type"
  [[view-type authenticated? is-job-fetched? is-searching? is-fetching-saved-searches?] _]
  (if authenticated?
    (case view-type
      :apps (not is-job-fetched?)
      :views (not is-job-fetched?)
      false)
    false))

(defn show-full-access-bar?
  "Only show if recruiter is not premium and not viewing applicants"
  [[is-premium? view-type blurred?] _]
  (not (or is-premium? (= view-type :apps) blurred?)))

(defn list-title
  "Returns title of list being viewed based on view type"
  [[view-type search-name] _]
  (case view-type
    :search search-name
    nil))

(defn resume-id
  "Returns resume-id for secure-id within metadata"
  [[secure-id metadata] _]
  (get-in metadata [secure-id :resume-id]))

(defn resume-version
  "Returns resume-id if not nil,
  otherwise if candidate has resume return digital-resume
  otherwise return empty string"
  [[has-resume? resume-id] _]
  (str (or resume-id (when has-resume? "digital-resume"))))

(defn blur-active-candidate?
  "Returns whether to blur candidate based on index, variation, full-access,
  and search filter"
  [[view-type index full-access view approved?] _]
  (if (= view-type :search)
    (su/blur-candidate? index full-access view approved?)
    false))

(defn show-resume?
  "When user is a guest, they must confirm their email before viewing resume"
  [[guest? email-confirmed?] _]
  (or (not guest?) email-confirmed?))

(defn show-export-to-ats-btn?
  "Export to ATS should be shown if user has ATS setup, candidate has resume
  and the user is not a gust"
  [[can-save-to-ats? has-resume? guest?] _]
  (and (not guest?) can-save-to-ats? has-resume?))

(defn show-setup-ats-btn?
  "Show setup ats btn if not showing export to ats btn"
  [show-export-to-ats-btn? _]
  (not show-export-to-ats-btn?))

(defn share-resume-view?
  "Considered viewing resume via share resume if on profile panel and key was sent with request"
  [[active-panel query-params] _]
  (and (= active-panel :candidate-profile-panel) (some? (:key query-params))))

(defn pdf-search-term
  "Returns skills from search if viewing candidate from search"
  [[skills view-type]]
  (let [skills (str/replace skills (js/RegExp. #"\b(and)|\b(or)" "gi") "")]
    (when (and (seq skills) (= view-type :search))
      skills)))

(defn last-active-date
  "Returns formatted last active date from active candidate"
  [{:keys [last-email-open last-login last-profile-update]} _]
  (when-let [most-recent-date (cu/last-active-date last-email-open last-login last-profile-update)]
    (let [num-days (date/days-passed-since most-recent-date)]
      (cond
        (= 0 num-days) (str "Last active: Today")
        (= 1 num-days) (str "Last active: 1 day ago")
        :else (str "Last active: " num-days " days ago")))))

(defn resume-upload-date
  "Returns formatted resume uploaded date from active candidate"
  [candidate _]
  (if (:last-resume-update candidate)
    (date/formatted-date :month-and-year (date/subscribe-date-time (:last-resume-update candidate)))
    nil))

(defn page-results-loaded?
  "Returns whether page is in map of pages of results"
  [[view-type page-map page]]
  (if (and page (= view-type :search))
    (contains? page-map page)
    true))

(defn next-page
  "Returns incremented page if not on last page"
  [[current-page total]]
  (let [next-page (inc current-page)
        last-page (Math/ceil (/ total 10))]
    (when-not (> next-page last-page)
      next-page)))

(defn previous-page
  "Returns decremented page if not less than zero"
  [current-page]
  (let [prev-page (dec current-page)]
    (when-not (< prev-page 0) prev-page)))

(defn years-experience
  "Returns years experience from given candidate"
  [{:keys [years-experience]}]
  years-experience)

(rf/reg-sub
  :candidates/viewable-candidates
  :<- [:candidates/view-type]
  :<- [:search/active-search-candidates]
  viewable-candidates)

(rf/reg-sub
  :candidates/applicants-back-route
  :<- [:job/active-job-id]
  :<- [:job/active-job-loc-id]
  applicants-back-route)

(rf/reg-sub
  :candidates/views-back-route
  :<- [:job/active-job-id]
  :<- [:job/active-job-loc-id]
  views-back-route)

(rf/reg-sub
  :candidates/search-back-route
  :<- [:search-results/saved-search-id]
  :<- [:candidates/current-page]
  search-back-route)

(rf/reg-sub
  :candidates/projects-back-route
  :<- [:projects/curr-project-id]
  projects-back-route)

(rf/reg-sub
  :candidates/back-route
  :<- [:candidates/view-type]
  :<- [:candidates/applicants-back-route]
  :<- [:candidates/views-back-route]
  :<- [:candidates/search-back-route]
  :<- [:candidates/projects-back-route]
  back-route)

(rf/reg-sub
  :candidates/active-candidate-from-view
  :<- [:candidates/viewable-candidates]
  :<- [:candidates/current-page-index]
  :<- [:candidates/current-page]
  :<- [:candidates/active-jobseeker-id]
  active-candidate-from-view)

(rf/reg-sub
  :candidates/active-candidate
  :<- [:candidates/active-candidate-from-view]
  :<- [:candidates/active-jobseeker]
  active-candidate)

(rf/reg-sub
  :candidates/current-page-index
  :<- [:candidates/current-page]
  :<- [:candidates/per-page]
  :<- [:candidates/active-index]
  current-page-index)

(rf/reg-sub
  :candidates/current-page
  :<- [:candidates/per-page]
  :<- [:candidates/active-index]
  page)

(rf/reg-sub
  :candidates/active-candidate-name
  :<- [:candidates/active-candidate]
  active-candidate-name)

(rf/reg-sub
  :candidates/active-candidate-location
  :<- [:candidates/active-candidate]
  active-candidate-location)

(rf/reg-sub
  :candidates/active-candidate-desired
  :<- [:candidates/active-candidate]
  active-candidate-desired)

(rf/reg-sub
  :candidates/active-candidate-history
  :<- [:candidates/active-candidate]
  active-candidate-history)

(rf/reg-sub
  :candidates/active-candidate-id
  :<- [:candidates/active-candidate]
  active-candidate-id)

(rf/reg-sub
  :candidates/active-id
  :<- [:candidates/active-candidate]
  secure-id)

(rf/reg-sub
  :candidates/current-candidate-number
  :<- [:candidates/active-index]
  current-candidate-number)

(rf/reg-sub
  :candidates/viewable-candidates-count
  :<- [:candidates/viewable-candidates]
  viewable-candidate-count)

(rf/reg-sub
  :candidates/total-candidates
  :<- [:candidates/view-type]
  :<- [(table/total-sub ::table/search-results)]
  :<- [:candidates/viewable-candidates-count]
  total-candidates)

(rf/reg-sub
  :candidates/previous-candidate-index
  :<- [:candidates/active-index]
  previous-candidate-index)

(rf/reg-sub
  :candidates/previous-candidate-current-page-index
  :<- [:candidates/previous-candidate-page]
  :<- [:candidates/per-page]
  :<- [:candidates/previous-candidate-index]
  current-page-index)

(rf/reg-sub
  :candidates/next-candidate-index
  :<- [:candidates/total-candidates]
  :<- [:candidates/active-index]
  next-candidate-index)

(rf/reg-sub
  :candidates/next-candidate-current-page-index
  :<- [:candidates/next-candidate-page]
  :<- [:candidates/per-page]
  :<- [:candidates/next-candidate-index]
  current-page-index)

(rf/reg-sub
  :candidates/resume-url
  :<- [:candidates/active-id]
  :<- [:candidates/active-candidate]
  :<- [:candidates/pdf-search-term]
  resume-url)

(rf/reg-sub
  :candidates/active-candidate-resume-metadata
  :<- [:candidates/active-id]
  :<- [:resume/metadata]
  active-candidate-resume-metadata)

(rf/reg-sub
  :candidates/per-page
  :<- [:candidates/view-type]
  :<- [:candidates/total-candidates]
  :<- [:search/per-page]
  per-page)

(rf/reg-sub
  :candidates/previous-candidate-route
  :<- [:candidates/view-type]
  :<- [:candidates/previous-candidate-index]
  :<- [:candidates/previous-candidate-secure-id]
  :<- [:search-results/saved-search-id]
  :<- [:search-results/sort-by]
  :<- [:job/active-job-id]
  :<- [:job/active-job-loc-id]
  :<- [:projects/curr-project-id]
  candidate-route)

(rf/reg-sub
  :candidates/next-candidate-route
  :<- [:candidates/view-type]
  :<- [:candidates/next-candidate-index]
  :<- [:candidates/next-candidate-secure-id]
  :<- [:search-results/saved-search-id]
  :<- [:search-results/sort-by]
  :<- [:job/active-job-id]
  :<- [:job/active-job-loc-id]
  :<- [:projects/curr-project-id]
  candidate-route)

(rf/reg-sub
  :candidates/page-loaded?
  :<- [:candidates/active-id]
  :<- [:candidates/is-fetching-candidate?]
  page-loaded?)

(rf/reg-sub
  :candidates/active-candidate-application-history
  :<- [:candidates/active-candidate]
  active-candidate-application-history)

(rf/reg-sub
  :candidates/active-candidate-is-dismissed?
  :<- [:candidates/active-candidate-application-history]
  active-candidate-is-dismissed?)

(rf/reg-sub
  :candidates/show-application-bar?
  :<- [:candidates/active-candidate-apply-time]
  show-application-bar?)

(rf/reg-sub
  :candidates/active-candidate-apply-time
  :<- [:candidates/active-candidate]
  active-candidate-apply-time)

(rf/reg-sub
  :candidates/active-candidate-metadata-fetched?
  :<- [:candidates/active-id]
  :<- [:resume/metadata-fetched]
  metadata-fetched?)

(rf/reg-sub
  :candidates/active-candidate-has-resume?
  :<- [:candidates/active-candidate-metadata-fetched?]
  :<- [:candidates/active-candidate-resume-metadata]
  active-candidate-has-resume?)

(rf/reg-sub
  :candidates/previous-candidate
  :<- [:candidates/viewable-candidates]
  :<- [:candidates/previous-candidate-current-page-index]
  :<- [:candidates/previous-candidate-page]
  candidate)

(rf/reg-sub
  :candidates/previous-candidate-page
  :<- [:candidates/per-page]
  :<- [:candidates/previous-candidate-index]
  page)

(rf/reg-sub
  :candidates/previous-candidate-secure-id
  :<- [:candidates/previous-candidate]
  secure-id)

(rf/reg-sub
  :candidates/next-candidate
  :<- [:candidates/viewable-candidates]
  :<- [:candidates/next-candidate-current-page-index]
  :<- [:candidates/next-candidate-page]
  candidate)

(rf/reg-sub
  :candidates/next-candidate-secure-id
  :<- [:candidates/next-candidate]
  secure-id)

(rf/reg-sub
  :candidates/next-candidate-page
  :<- [:candidates/per-page]
  :<- [:candidates/next-candidate-index]
  page)

(rf/reg-sub
  :candidates/active-jobseeker
  :<- [:candidates/candidates]
  :<- [:candidates/active-jobseeker-id]
  active-jobseeker)

(rf/reg-sub
  :candidates/show-candidate-scroller?
  :<- [:candidates/active-candidate-from-view]
  show-candidate-scroller?)

(rf/reg-sub
  :candidates/is-fetching-candidate?
  :<- [:candidates/view-type]
  :<- [:recruiter/is-authenticated?]
  :<- [:job/is-active-job-fetched?]
  :<- [(table/loading-sub ::table/search-results)]
  :<- [:saved-searches/fetching-all?]
  is-fetching-candidate?)

(rf/reg-sub
  :candidates/show-full-access-bar?
  :<- [:recruiter/full-access]
  :<- [:candidates/view-type]
  :<- [:candidates/blur-active-candidate?]
  show-full-access-bar?)

(rf/reg-sub
  :candidates/list-title
  :<- [:candidates/view-type]
  :<- [:search-results/criteria-search-name]
  list-title)

(rf/reg-sub
  :candidates/active-candidate-resume-id
  :<- [:candidates/active-id]
  :<- [:resume/metadata]
  resume-id)

(rf/reg-sub
  :candidates/active-candidate-resume-version
  :<- [:candidates/active-candidate-has-resume?]
  :<- [:candidates/active-candidate-resume-id]
  resume-version)

(rf/reg-sub
  :candidates/blur-active-candidate?
  :<- [:candidates/view-type]
  :<- [:candidates/active-index]
  :<- [:recruiter/full-access]
  :<- [:search-results/view]
  :<- [:recruiter/approved?]
  blur-active-candidate?)

(rf/reg-sub
  :candidates/show-resume?
  :<- [:recruiter/guest?]
  :<- [:recruiter/email-confirmed?]
  show-resume?)

(rf/reg-sub
  :candidates/show-setup-ats-btn?
  :<- [:candidates/show-export-to-ats-btn?]
  show-setup-ats-btn?)

(rf/reg-sub
  :candidates/show-export-to-ats-btn?
  :<- [:recruiter/can-save-to-ats?]
  :<- [:candidates/active-candidate-has-resume?]
  :<- [:recruiter/guest?]
  show-export-to-ats-btn?)

(rf/reg-sub
  :candidates/share-resume-view?
  :<- [:active-panel]
  :<- [:query-params]
  share-resume-view?)

(rf/reg-sub
  :candidates/pdf-search-term
  :<- [:search-results/criteria-search-criteria-keyword]
  :<- [:candidates/view-type]
  pdf-search-term)

(rf/reg-sub
  :candidates/last-active-date
  :<- [:candidates/active-candidate]
  last-active-date)

(rf/reg-sub
  :candidates/resume-upload-date
  :<- [:candidates/active-candidate]
  resume-upload-date)

(rf/reg-sub
  :candidates/next-page-loaded?
  :<- [:candidates/view-type]
  :<- [(table/page-map-sub ::table/search-results)]
  :<- [:candidates/next-page]
  page-results-loaded?)

(rf/reg-sub
  :candidates/previous-page-loaded?
  :<- [:candidates/view-type]
  :<- [(table/page-map-sub ::table/search-results)]
  :<- [:candidates/previous-page]
  page-results-loaded?)

(rf/reg-sub
  :candidates/next-page
  :<- [:candidates/current-page]
  :<- [:candidates/total-candidates]
  next-page)

(rf/reg-sub
  :candidates/previous-page
  :<- [:candidates/current-page]
  previous-page)

(rf/reg-sub
  :candidates/active-candidate-years-experience
  :<- [:candidates/active-candidate]
  years-experience)
