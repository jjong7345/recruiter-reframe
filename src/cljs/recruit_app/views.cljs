(ns recruit-app.views
  (:require [re-frame.core :as rf]
            [re-com.core :as rc
             :refer-macros [handler-fn]]
            [reagent.core :as reagent]
            [re-com.util :refer [enumerate]]
            [cljs.reader :refer [read-string]]
            [reagent.core :as r]
            [recruit-app.post-job.views :as post-job]
            [recruit-app.jobs.views :as jobs]
            [recruit-app.project-list.views :as project-list]
            [recruit-app.job.views :as job]
            [recruit-app.post-job.preview.views :as preview]
            [recruit-app.search.views :as search]
            [recruit-app.preapprovedsearch.views :as preapprovedsearch]
            [recruit-app.marketinghome.views :as marketinghome]
            [recruit-app.projects.views :as projects]
            [recruit-app.account.views :as account]
            [recruit-app.header.views :as header]
            [recruit-app.footer.views :as footer]
            [recruit-app.login.views :as login]
            [recruit-app.post-job.publish.views :as publish]
            [recruit-app.post-job.thank-you.views :as post-thank-you]
            [recruit-app.get-full-access.views :as get-full-access]
            [recruit-app.candidates.views :as candidate-profile]
            [recruit-app.saved-searches.views :as saved-searches]
            [recruit-app.superuser.views :as superuser]
            [recruit-app.faqs.views :as faqs]
            [recruit-app.terms-of-use.views :as terms-of-use]
            [recruit-app.referral-hiring.views :as referral-hiring]
            [recruit-app.alerts.views :as a]
            [recruit-app.onboarding.views :as onboarding]
            [recruit-app.onboarding-email-verification.views :as email-verification]
            [recruit-app.page_404.views :as page-404]
            [recruit-app.pricing.views :as pricing]
            [recruit-app.recruiter-admin.views :as recruiter-admin]
            [recruit-app.teams.views :as teams]
            [recruit-app.dashboard.views :as dashboard]
            [recruit-app.search-results.views :as search-results]
            [recruit-app.declined-view.views :as dv]
            [secretary.core :as sec]))

;_+_+_+_+_+_+_+_+_+_+_+_+_+_+_+
;_+
;_+  Main App
;_+
;_+_+_+_+_+_+_+_+_+_+_+_+_+_+_+

(defn- panel
  [panel-name authenticated?]
  (case panel-name
    :home-panel (if authenticated? [dashboard/index] [marketinghome/index])
    :post-job-panel [post-job/index]
    :search-panel [search/index]
    :saved-searches-panel [saved-searches/index]
    :jobs-panel [jobs/index]
    :project-list-panel [project-list/index]
    :job-panel [job/index]
    :post-publish-panel [publish/index]
    :post-preview-panel [preview/index]
    :account-panel [account/index]
    :post-thank-panel [post-thank-you/index]
    :projects-panel [projects/index]
    :candidate-profile-panel [candidate-profile/index]
    :faqs-panel [faqs/index]
    :terms-of-use-panel [terms-of-use/index]
    :referral-hiring-panel [referral-hiring/index]
    :get-full-access-panel [get-full-access/index]
    :superuser-panel [superuser/index]
    :onboarding-panel [onboarding/index]
    :email-verification [email-verification/index]
    :pricing-panel [pricing/index]
    :page-404-panel [page-404/index]
    :login-panel [login/index]
    :recruiter-admin-search [recruiter-admin/search-panel]
    :recruiter-admin-search-results [recruiter-admin/search-results-panel]
    :recruiter-admin-pending-queue [recruiter-admin/pending-queue-panel]
    :recruiter-admin-pending-escalated-queue [recruiter-admin/pending-escalated-queue-panel]
    :recruiter-admin-approved-recently [recruiter-admin/recently-approved-recruiters-panel]
    :recruiter-admin-recruiter-profile [recruiter-admin/recruiter-profile-panel]
    :teams-panel [teams/teams-view]
    :team-edit-panel [teams/team-edit-view]
    :search-results [search-results/unsaved-view]
    :saved-search-results [search-results/saved-view]
    [page-404/index]))

(defn- panels
  "Check if user is authenticated and render correct panel"
  []
  (let [active-panel (rf/subscribe [:active-panel])
        show-email-verification? (rf/subscribe [:show-email-verification?])
        show-preapproved-search? (rf/subscribe [:show-preapproved-search?])
        show-panel? (rf/subscribe [:show-panel?])
        authenticated? (rf/subscribe [:recruiter/is-authenticated?])
        declined? (rf/subscribe [:recruiter/declined?])]
    (fn []
      (cond
        @show-email-verification? [panel :email-verification]
        @show-preapproved-search? [search/index]
        @declined? [dv/declined-view]
        @show-panel? [panel @active-panel @authenticated?]
        :else [login/index]))))

(defn- show-header?
  "Only show header if not in RL and not on home page"
  [active-panel]
  (not (= active-panel :get-full-access-panel)))

(defn- header
  "Check if should show logged out header, otherwise show normal header"
  []
  (let [show-logged-in-header? (rf/subscribe [:header/show-logged-in-header?])]
    (fn []
      (if @show-logged-in-header?
        [header/recruit-header-container]
        [header/lo-header]))))

(defn- show-footer?
  "Show footer when in RA (excluding full access page) or on marketing homepage"
  [active-panel fetching-user?]
  (and (not fetching-user?)
       (not (= active-panel :get-full-access-panel))))

(defn main-panel []
  (let [active-panel (rf/subscribe [:active-panel])
        is-fetching-user? (rf/subscribe [:is-fetching-user?])
        approved? (rf/subscribe [:recruiter/approved?])]
    (fn []
      [rc/v-box
       :height "100%"
       :children (cond-> []
                         (show-header? @active-panel) (conj [header])
                         (not @is-fetching-user?) (conj [a/alerts])
                         (not @approved?) (conj [preapprovedsearch/alert-bar-first-time])
                         (not @is-fetching-user?) (conj [panels])
                         (show-footer? @active-panel @is-fetching-user?) (conj [footer/recruit-footer]))])))
