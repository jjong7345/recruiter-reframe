(ns recruit-app.core
  (:require [cljsjs.quill]
            [cljsjs.cropper]
            [reagent.core :as reagent]
            [re-frame.core :as rf]
            [re-frisk.core :refer [enable-re-frisk!]]
            [day8.re-frame.http-fx]
            [stylefy.core :as stylefy]
            [re-pressed.core :as rp]

            [recruit-app.components.table.registry]
            [recruit-app.components.modal.registry]

            [recruit-app.events]
            [recruit-app.login.events]
            [recruit-app.account.events]
            [recruit-app.recruiter.events]
            [recruit-app.post-job.events]
            [recruit-app.jobs.events]
            [recruit-app.job.events]
            [recruit-app.search.events]
            [recruit-app.saved-searches.events]
            [recruit-app.marketinghome.events]
            [recruit-app.projects.events]
            [recruit-app.get-full-access.events]
            [recruit-app.modals.email.events]
            [recruit-app.post-job.thank-you.events]
            [recruit-app.post-job.publish.events]
            [recruit-app.post-job.preview.events]
            [recruit-app.modals.checkout.events]
            [recruit-app.candidates.events]
            [recruit-app.resume.events]
            [recruit-app.modals.candidate-notes.events]
            [recruit-app.modals.share-resume.events]
            [recruit-app.modals.profile-image.events]
            [recruit-app.util.analytics]
            [recruit-app.project-list.events]
            [recruit-app.alerts.events]
            [recruit-app.superuser.events]
            [recruit-app.faqs.events]
            [recruit-app.referral-hiring.events]
            [recruit-app.onboarding-email-verification.events]
            [recruit-app.pricing.events]
            [recruit-app.modals.confirm-email.events]
            [recruit-app.onboarding.events]
            [recruit-app.modals.ats-select-job.events]
            [recruit-app.recruiter-admin.events]
            [recruit-app.header.events]
            [recruit-app.footer.events]
            [recruit-app.dashboard.events]
            [recruit-app.teams.events]
            [recruit-app.search-results.events]

            [recruit-app.subs]
            [recruit-app.recruiter.subs]
            [recruit-app.login.subs]
            [recruit-app.account.subs]
            [recruit-app.post-job.subs]
            [recruit-app.jobs.subs]
            [recruit-app.job.subs]
            [recruit-app.search.subs]
            [recruit-app.saved-searches.subs]
            [recruit-app.marketinghome.subs]
            [recruit-app.projects.subs]
            [recruit-app.get-full-access.subs]
            [recruit-app.modals.email.subs]
            [recruit-app.post-job.thank-you.subs]
            [recruit-app.modals.checkout.subs]
            [recruit-app.modals.candidate-notes.subs]
            [recruit-app.modals.share-resume.subs]
            [recruit-app.modals.profile-image.subs]
            [recruit-app.candidates.subs]
            [recruit-app.project-list.subs]
            [recruit-app.resume.subs]
            [recruit-app.alerts.subs]
            [recruit-app.superuser.subs]
            [recruit-app.referral-hiring.subs]
            [recruit-app.onboarding-email-verification.subs]
            [recruit-app.pricing.subs]
            [recruit-app.modals.confirm-email.subs]
            [recruit-app.onboarding.subs]
            [recruit-app.header.subs]
            [recruit-app.modals.ats-select-job.subs]
            [recruit-app.recruiter-admin.subs]
            [recruit-app.footer.subs]
            [recruit-app.dashboard.subs]
            [recruit-app.teams.subs]
            [recruit-app.search-results.subs]

            [recruit-app.routes :as routes]
            [recruit-app.views :as views]
            [recruit-app.config :as config]))


(defn dev-setup []
  (enable-console-print!)
  (when config/debug?
    (enable-re-frisk!)
    (println "dev mode")))

(defn mount-root []
  (rf/clear-subscription-cache!)
  (reagent/render [views/main-panel]
                  (.getElementById js/document "app")))

(defn ^:export init []
  (routes/app-routes)
  (rf/dispatch-sync [:initialize-db])
  (rf/dispatch-sync [::rp/add-keyboard-event-listener "keydown"])
  (stylefy/init)
  (dev-setup)
  (mount-root))
