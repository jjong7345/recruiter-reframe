(ns recruit-app.routes
  (:require-macros [secretary.core :refer [defroute]])
  (:import goog.History)
  (:require [secretary.core :as secretary]
            [goog.events :as events]
            [goog.history.EventType :as EventType]
            [re-frame.core :as rf]))

(def post-job-form-routes #{"/post-job" "/post-job/preview"})

(defn- dispatch-route
  "Dispatches route via secretary"
  [event]
  (let [route (.-token event)]
    (when-not (contains? post-job-form-routes route)
      (rf/dispatch [:post-job/clear-form]))
    (rf/dispatch [:scroll-top])
    (secretary/dispatch! route)))

(defn hook-browser-navigation! []
  (doto (History.)
    (events/listen
      EventType/NAVIGATE
      dispatch-route)
    (.setEnabled true)))

(defn set-page-element! [el content]
  (set! (.-innerHTML el) content))

(defn set-page-title! [content]
  (let [element (aget (js/document.getElementsByTagName "title") 0)]
    (when element
      (set-page-element! element content))))

(defn app-routes []
  (secretary/set-config! :prefix "#")
  ;; --------------------
  ;; define routes here
  (defroute "/" []
            (rf/dispatch [:set-page-head-title "Home"])
            (rf/dispatch [:set-active-panel :home-panel]))

  (defroute "/onboarding" []
            (rf/dispatch [:set-page-head-title "onboarding-almost there"])
            (rf/dispatch [:set-active-panel :onboarding-panel]))

  (defroute "/login" []
            (rf/dispatch [:set-page-head-title "Please sign in"])
            (rf/dispatch [:set-active-panel :login-panel]))

  (defroute "/change-password/:token" [token]
            (rf/dispatch [:set-page-head-title "Create new password"])
            (rf/dispatch [:login/forgot-password-token-change token])
            (rf/dispatch [:login/toggle-show-change-password?])
            (rf/dispatch [:set-active-panel :login-panel]))

  (defroute "/search/" []
            (rf/dispatch [:set-page-head-title "Search"])
            (rf/dispatch [:go-to-route "/search"]))

  (defroute "/search" []
            (rf/dispatch [:set-page-head-title "Search"])
            (rf/dispatch [:set-active-panel :search-panel]))

  (defroute "/search/:search-id" [search-id]
            (rf/dispatch [:set-page-head-title "Search"])
            (rf/dispatch [:search/active-saved-search-id-change search-id])
            (rf/dispatch [:set-active-panel :search-panel]))

  (defroute "/search/:search-id/:page" [search-id page]
            (rf/dispatch [:set-page-head-title "Search"])
            (rf/dispatch [:search/active-saved-search-id-change search-id])
            (rf/dispatch [:search/current-page-change (js/parseInt page)])
            (rf/dispatch [:set-active-panel :search-panel]))

  (defroute "/searchtab" [query-params]
            (rf/dispatch [:set-page-head-title "Search"])
            (rf/dispatch [:go-to-route (str "/search/" (:id query-params))]))

  (defroute "/saved-searches" []
            (rf/dispatch [:set-page-head-title "Manage Saved Searches"])
            (rf/dispatch [:set-active-panel :saved-searches-panel]))

  (defroute "/post-job" []
            (rf/dispatch [:set-page-head-title "Create a Job Post"])
            (rf/dispatch [:set-active-panel :post-job-panel]))

  (defroute "/jobs" []
            (rf/dispatch [:set-page-head-title "Manage jobs"])
            (rf/dispatch [:job/clear-active-job])
            (rf/dispatch [:set-active-panel :jobs-panel]))

  (defroute "/jobs/:job-id/promote" [job-id]
            (rf/dispatch [:set-page-head-title "Manage jobs"])
            (rf/dispatch [:jobs/promote-job-modal (js/parseInt job-id)]))

  (defroute "/jobs/:job-id/purchase-promotions" [job-id]
            (rf/dispatch [:set-page-head-title "Manage jobs"])
            (rf/dispatch [:jobs/purchase-promotions (js/parseInt job-id)]))

  (defroute "/job/:job-id" [job-id]
            (rf/dispatch [:set-page-head-title "Manage jobs"])
            (rf/dispatch [:job/set-active-tab :tab0])
            (rf/dispatch [:set-active-panel :job-panel])
            (rf/dispatch [:job/set-active-job (js/parseInt job-id)]))

  (defroute "/job/:job-id/apps" [job-id]
            (rf/dispatch [:set-page-head-title "Manage jobs"])
            (rf/dispatch [:job/set-active-tab :tab1])
            (rf/dispatch [:set-active-panel :job-panel])
            (rf/dispatch [:job/set-active-job (js/parseInt job-id)]))

  (defroute "/job/:job-id/views" [job-id]
            (rf/dispatch [:set-page-head-title "Manage jobs"])
            (rf/dispatch [:job/set-active-tab :tab2])
            (rf/dispatch [:set-active-panel :job-panel])
            (rf/dispatch [:job/set-active-job (js/parseInt job-id)]))

  (defroute "/job/:job-id/apps/:job-location-id" [job-id job-location-id]
            (rf/dispatch [:set-page-head-title "Manage jobs"])
            (rf/dispatch [:job/set-active-tab :tab1])
            (rf/dispatch [:set-active-panel :job-panel])
            (rf/dispatch [:job/set-active-job (js/parseInt job-id)])
            (rf/dispatch [:job/active-job-loc (js/parseInt job-location-id)]))

  (defroute "/job/:job-id/views/:job-location-id" [job-id job-location-id]
            (rf/dispatch [:set-page-head-title "Manage jobs"])
            (rf/dispatch [:job/set-active-tab :tab2])
            (rf/dispatch [:set-active-panel :job-panel])
            (rf/dispatch [:job/set-active-job (js/parseInt job-id)])
            (rf/dispatch [:job/active-job-loc (js/parseInt job-location-id)]))

  (defroute "/post-job/edit/:job-id" [job-id]
            (rf/dispatch [:set-page-head-title "Create a Job Post"])
            (rf/dispatch [:set-active-panel :post-job-panel])
            (rf/dispatch [:post-job/editing?-change true])
            (rf/dispatch [:post-job/job-id-change (js/parseInt job-id)]))

  (defroute "/post-job/thank-you/:job-id" [job-id]
            (rf/dispatch [:set-page-head-title "Job Pending Approval"])
            (rf/dispatch [:set-active-panel :post-thank-panel])
            (rf/dispatch [:post-job.thank-you/job-id-change (js/parseInt job-id)]))

  (defroute "/post-job/preview" []
            (rf/dispatch [:set-page-head-title "Preview Job Post"])
            (rf/dispatch [:set-active-panel :post-preview-panel]))

  (defroute "/post-job/publish" []
            (rf/dispatch [:set-page-head-title "Publish Your Job"])
            (rf/dispatch [:set-active-panel :post-publish-panel]))

  (defroute "/account" []
            (rf/dispatch [:set-page-head-title "My Account"])
            (rf/dispatch [:set-active-panel :account-panel]))

  (defroute "/account/subscriptions" []
            (rf/dispatch [:set-active-panel :account-panel])
            (rf/dispatch [:account/set-active-tab :tab2]))

  (defroute "/superuser" []
            (rf/dispatch [:set-page-head-title "Super User"])
            (rf/dispatch [:set-active-panel :superuser-panel]))

  (defroute "/get-full-access" []
            (rf/dispatch [:set-page-title "Full Access Resume Database & Candidate Search | Ladders Recruiter"])
            (rf/dispatch [:set-active-panel :get-full-access-panel]))

  (defroute "/get-full-access/confirm" []
            (rf/dispatch [:set-page-title "Full Access Resume Database & Candidate Search | Ladders Recruiter"])
            (rf/dispatch [:get-full-access/confirmation-page?-change true])
            (rf/dispatch [:set-active-panel :get-full-access-panel]))

  (defroute "/projects" []
            (rf/dispatch [:set-page-head-title "Manage projects"])
            (rf/dispatch [:set-active-panel :projects-panel]))

  (defroute "/projects/:project-id" [project-id]
            (rf/dispatch [:projects/curr-project-id-change project-id])
            (rf/dispatch [:set-page-head-title "Manage projects"])
            (rf/dispatch [:set-active-panel :project-list-panel]))

  (defroute "/candidates" [query-params]
            (rf/dispatch [:set-page-head-title "Candidate"])
            (rf/dispatch [:candidates/active-jobseeker-id-change (:jobseekerId query-params)])
            (when-let [job-loc-id (:jobLocationId query-params)]
              (rf/dispatch [:job/active-job-loc (js/parseInt job-loc-id)]))
            (rf/dispatch [:set-active-panel :candidate-profile-panel]))

  (defroute "/candidate/:index/job/:job-id/:type/:job-loc-id" [index job-id type job-loc-id query-params]
            (rf/dispatch [:set-page-head-title "Candidate"])
            (rf/dispatch [:job/set-active-job (js/parseInt job-id)])
            (rf/dispatch [:job/active-job-loc (js/parseInt job-loc-id)])
            (rf/dispatch [:candidates/view-type-change (keyword type)])
            (rf/dispatch [:candidates/active-index-change (js/parseInt index)])
            (rf/dispatch [:set-active-panel :candidate-profile-panel])
            (when-let [secure-id (:jobseekerId query-params)]
              (rf/dispatch [:candidates/active-jobseeker-id-change secure-id])))

  (defroute "/candidate/search/:saved-search-id/:index/:sort-by" [saved-search-id index sort-by query-params]
            (rf/dispatch [:set-page-head-title "Candidate"])
            (rf/dispatch [:search-results/assoc-saved-search-id saved-search-id])
            (rf/dispatch [:search/current-page-change (quot (js/parseInt index) 10)])
            (rf/dispatch [:search/sort-by-change sort-by])
            (rf/dispatch [:candidates/view-type-change :search])
            (rf/dispatch [:candidates/active-index-change (js/parseInt index)])
            (rf/dispatch [:set-active-panel :candidate-profile-panel])
            (when-let [secure-id (:jobseekerId query-params)]
              (rf/dispatch [:candidates/active-jobseeker-id-change secure-id])))

  (defroute "/candidate/search/:index/:sort-by" [index sort-by query-params]
            (rf/dispatch [:set-page-head-title "Candidate"])
            (rf/dispatch [:search/current-page-change (quot (js/parseInt index) 10)])
            (rf/dispatch [:search/sort-by-change sort-by])
            (rf/dispatch [:candidates/view-type-change :search])
            (rf/dispatch [:candidates/active-index-change (js/parseInt index)])
            (rf/dispatch [:set-active-panel :candidate-profile-panel])
            (when-let [secure-id (:jobseekerId query-params)]
              (rf/dispatch [:candidates/active-jobseeker-id-change secure-id])))

  (defroute "/candidate/project/:project-id/:index" [project-id index query-params]
            (rf/dispatch [:set-page-head-title "Candidate"])
            (rf/dispatch [:projects/curr-project-id-change project-id])
            (rf/dispatch [:candidates/active-index-change (js/parseInt index)])
            (rf/dispatch [:candidates/view-type-change :project])
            (rf/dispatch [:set-active-panel :candidate-profile-panel])
            (when-let [secure-id (:jobseekerId query-params)]
              (rf/dispatch [:candidates/active-jobseeker-id-change secure-id])))

  (defroute "/faqs" []
            (rf/dispatch [:set-page-head-title "Online Recruitment Service for the Job Market | FAQs"])
            (rf/dispatch [:set-active-panel :faqs-panel]))

  (defroute "/terms-of-use" []
            (rf/dispatch [:set-page-head-title "Recruiter User Agreement"])
            (rf/dispatch [:set-active-panel :terms-of-use-panel]))

  (defroute "/referral" []
            (rf/dispatch [:set-page-head-title "Employee Referral | Ladders"])
            (rf/dispatch [:set-active-panel :referral-hiring-panel]))

  (defroute "/404" []
            (rf/dispatch [:set-active-panel :page-404-panel]))

  (defroute "/pricing" []
            (rf/dispatch [:set-page-head-title "Pricing"])
            (rf/dispatch [:set-active-panel :pricing-panel]))

  (defroute "/recruiter-admin" []
            (rf/dispatch [:recruiter-admin/clear-db])
            (rf/dispatch [:teams/clear-db])
            (rf/dispatch [:set-page-head-title "Recruiter Admin"])
            (rf/dispatch [:set-active-panel :recruiter-admin-search]))

  (defroute "/recruiter-admin/search-results" []
            (rf/dispatch [:recruiter-admin/clear-active-recruiter])
            (rf/dispatch [:set-page-head-title "Recruiter Admin"])
            (rf/dispatch [:set-active-panel :recruiter-admin-search-results]))

  (defroute "/recruiter-admin/pending" []
            (rf/dispatch [:recruiter-admin/clear-active-recruiter])
            (rf/dispatch [:set-page-head-title "Recruiter Admin - Pending Recruiters"])
            (rf/dispatch [:set-active-panel :recruiter-admin-pending-queue]))

  (defroute "/recruiter-admin/pending-escalated" []
            (rf/dispatch [:recruiter-admin/clear-active-recruiter])
            (rf/dispatch [:set-page-head-title "Recruiter Admin - Pending Escalated Recruiters"])
            (rf/dispatch [:set-active-panel :recruiter-admin-pending-escalated-queue]))

  (defroute "/recruiter-admin/approved-recently" []
            (rf/dispatch [:recruiter-admin/clear-active-recruiter])
            (rf/dispatch [:set-page-head-title "Recruiter Admin - Recently Approved Recruiters"])
            (rf/dispatch [:set-active-panel :recruiter-admin-approved-recently]))

  (defroute "/recruiter-admin/:recruiter-id" [recruiter-id]
            (rf/dispatch [:recruiter-admin/active-recruiter-id-change (js/parseInt recruiter-id)])
            (rf/dispatch [:recruiter-admin/editing-recruiter-change nil])
            (rf/dispatch [:set-page-head-title "Recruiter Admin"])
            (rf/dispatch [:set-active-panel :recruiter-admin-recruiter-profile]))

  (defroute "/teams" []
            (rf/dispatch [:set-page-head-title "Recruiter Teams"])
            (rf/dispatch [:set-active-panel :teams-panel]))

  (defroute "/teams/:team-id" [team-id]
            (rf/dispatch [:teams/active-team-id-change team-id])
            (rf/dispatch [:set-page-head-title "Recruiter Teams"])
            (rf/dispatch [:set-active-panel :team-edit-panel]))

  (defroute "/search-results" []
            (rf/dispatch [:set-page-head-title "Search Results"])
            (rf/dispatch [:search-results/page-change nil])
            (rf/dispatch [:set-active-panel :search-results]))

  (defroute "/search-results/p/:page" [page]
            (rf/dispatch [:set-page-head-title "Search Results"])
            (rf/dispatch [:search-results/page-change (js/parseInt page)])
            (rf/dispatch [:set-active-panel :search-results]))

  (defroute "/search-results/:saved-search-id" [saved-search-id]
            (rf/dispatch [:set-page-head-title "Search Results"])
            (rf/dispatch [:search-results/assoc-saved-search-id saved-search-id])
            (rf/dispatch [:search-results/page-change nil])
            (rf/dispatch [:set-active-panel :saved-search-results]))

  (defroute "/search-results/:saved-search-id/:page" [saved-search-id page]
            (rf/dispatch [:set-page-head-title "Search Results"])
            (rf/dispatch [:search-results/assoc-saved-search-id saved-search-id])
            (rf/dispatch [:search-results/page-change (js/parseInt page)])
            (rf/dispatch [:set-active-panel :saved-search-results]))

  ;; --------------------
  (hook-browser-navigation!))
