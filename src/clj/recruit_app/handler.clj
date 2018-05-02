(ns recruit-app.handler
  (:require [compojure.core :refer [GET PUT POST DELETE defroutes context wrap-routes]]
            [compojure.route :refer [resources]]
            [ring.util.response :refer [resource-response redirect]]
            [ring.middleware.reload :refer [wrap-reload]]
            [ring.middleware.json :refer [wrap-json-params wrap-json-response]]
            [ring.middleware.keyword-params :refer (wrap-keyword-params)]
            [ring.middleware.multipart-params :refer [wrap-multipart-params]]
            [ring.middleware.stacktrace :refer [wrap-stacktrace]]
            [ring.middleware.params :refer [wrap-params]]
            [recruit-app.search.api :as search]
            [recruit-app.email-templates.api :as email]
            [recruit-app.jobs.api :as jobs]
            [recruit-app.dashboard.api :as dashboard]
            [recruit-app.resume.api :as resume]
            [recruit-app.auth.handlers :as handlers]
            [recruit-app.auth.security :as sec]
            [recruit-app.location.api :as loc]
            [recruit-app.recruiter.api :as rec]
            [recruit-app.suggested-candidates.api :as sug]
            [recruit-app.candidate-notes.api :as cn]
            [recruit-app.recruiter-image.api :as img]
            [recruit-app.projects.api :as p]
            [recruit-app.ats.api :as ats]
            [recruit-app.candidates.api :as c]
            [recruit-app.tracking.api :as t]
            [recruit-app.util.request :as r]
            [recruit-app.util.route :as cr]
            [recruit-app.shopify.api :as s]
            [recruit-app.superuser.api :as su]
            [recruit-app.inventory.api :as i]
            [recruit-app.company.api :as co]
            [recruit-app.account.api :as a]
            [recruit-app.auth.forgot-password :as fp]
            [recruit-app.email.forgot-password :as fpe]
            [recruit-app.email.account-pending :as ape]
            [recruit-app.email.verify-email :as ve]
            [recruit-app.slack.api :as slack]
            [recruit-app.teams.api :as teams]
            [recruit-app.sales-leads.api :as sl]
            [recruit-app.saved-search.api :as saved-search]
            [ring.util.response :as rr]
            [cheshire.core :as json]
            [recruit-app.contact-candidate.api :as cc]
            [recruit-app.email.verify-email :as ve]
            [config.core :refer [env]]
            [cemerick.url :as u]
            [recruit-app.company.api :as company]
            [recruit-app.shopify.webhooks :as swh]
            [recruit-app.util.middleware :as mw]
            [recruit-app.util.encryption :as d]))

(def ^:private recruiter-profile-base "https://www.theladders.com/recruiter/")

(defroutes plain-routes
           ;Defines a list of routes that require preservation of their body data before it gets parsed or manipulated
           (POST "/webhooks/shopify-order-created" req (swh/order-created req)))

(defroutes public-routes
           (GET "/" [] (resource-response "index.html" {:root "public"}))
           (POST "/login" req (sec/login (:params req)))
           (GET "/candidates/:secure-id" [secure-id] (c/candidate secure-id))
           (POST "/confirm-email" req (resume/confirm-email (:params req)))
           (GET "/resume/:secure-id/:filename" req (resume/resume (:params req)))
           (GET "/resume-metadata/:secure-id" [secure-id] (resume/resume-metadata secure-id))
           (POST "/getmestarted" req (rr/response (json/generate-string "/")))
           (POST "/recruiters" req (sec/create-recruiter (:params req)))
           (POST "/recruiters/email-exists" req (rec/email-exists? (-> req :params :email)))
           (POST "/company/preapproved" req (co/preapproved-company-id (-> req :params :email)))
           (context "/verify" []
             (GET "/:code" [code] (sec/verify-code code))
             (GET "/:code/valid" [code] (sec/verification-code-valid? code)))
           (context "/password" []
             (POST "/send-email" req (fpe/send-email (-> req :params :email)))
             (POST "/token-valid/:token" [token] (fp/token-valid? token))
             (PUT "/" req (fp/change-password (:params req))))
           (POST "/landing/get-full-access" req (sl/request-info-unauthenticated :full-access (:params req)))
           (POST "/newly-approved-recruiter/:recruiter-id" [recruiter-id] (sl/submit-newly-approved-recruiter-info :approved-recruiters recruiter-id))
           (POST "/referral" req (slack/slack-referral-hiring (:params req)))
           (resources "/"))

(defroutes rl-redirect-routes
           (GET "/home" req (rr/redirect (str (:host env) "/")))
           (GET "/login" req (rr/redirect (str (:host env) "/#/login")))
           (GET "/hiring-candidates-FAQ" req (rr/redirect (str (:host env) "/#/faqs")))
           (GET "/guest/resumeviewer" [jobseekerId] (rr/redirect (str (:host env) "/#/candidates?jobseekerId=" jobseekerId))))

(defroutes email-redirect-routes
           (GET "/recruiterprofile/myprofile" req (rr/redirect (str (:host env) "/#/account")))
           (GET "/recruiterprofile/communicationpreferences" req (rr/redirect (str (:host env) "/#/account/subscriptions")))
           (GET "/managejob/new" req (rr/redirect (str (:host env) "/#/post-job")))
           (GET "/login/" req (rr/redirect (str (:host env) "/#/login")))
           (GET "/forgotpasswordlink" req (rr/redirect (str (:host env) "/#/change-password/" (u/url-encode (-> req :params :token)))))
           (GET "/landing/" req (rr/redirect (str (:host env) "/#/get-full-access")))
           (GET "/resumeviewer" [jobseekerId jobLocationId] (rr/redirect (str (:host env) "/#/candidates?jobseekerId=" jobseekerId (when jobLocationId (str "&jobLocationId=" jobLocationId)))))
           (GET "/search" req (rr/redirect (str (:host env) "/#/search")))
           (GET "/search/" req (rr/redirect (str (:host env) "/#/search")))
           (GET "/newjob/postjob" req (rr/redirect (str (:host env) "/#/post-job")))
           (GET "/passport/directverify" [optinKey] (rr/redirect (str (:host env) "/verify/" optinKey)))
           (GET "/search/managesavedsearches" req (rr/redirect (str (:host env) "/#/saved-searches")))
           (GET "/sharedresumeviewer" [key jobseekerId] (rr/redirect (str (:host env) "/?key=" key "#/candidates?jobseekerId=" jobseekerId)))
           (GET "/managecandidates" req (rr/redirect (str (:host env) "/#/projects")))
           (GET "/managejobs" req (rr/redirect (str (:host env) "/#/jobs")))
           (GET "/managejobs/:job-id" [job-id] (rr/redirect (str (:host env) "/#/job/" job-id)))
           (GET "/savedsearch/:search-id" [search-id] (rr/redirect (str (:host env) "/#/search-results/" search-id))))

(defroutes secured-routes
           (context "/jobs" []
             (GET "/" req (jobs/jobs (r/recruiter-id req)))
             (GET "/:job-id" req (jobs/job (:params req)))
             (POST "/" req jobs/post-job)
             (PUT "/:job-id" req (jobs/update-job (:params req)))
             (POST "/:job-id/repost" [job-id] (jobs/repost-job job-id))
             (POST "/application-viewed" req (jobs/application-viewed (:params req)))
             (POST "/:job-id/promote" [job-id] (jobs/promote-job job-id))
             (DELETE "/:job-id" [job-id] (jobs/delete-job job-id)))
           (GET "/user" [] sec/get-auth-user)
           (GET "/recruiter-dashboard" req (dashboard/recruiter-dashboard-request (-> req :params :recruiter-id)))
           (POST "/introductions/batch-contact" req (cc/contact-candidates (assoc (:params req) :recruiter-email (-> req :session :user :email))))
           (POST "/job-promotion" req (jobs/job-promotion (:params req)))
           (POST "/job-promotion-list" req (jobs/job-promotion-list (:params req)))
           (GET "/logout" [] sec/logout)
           (GET "/suggested/:job-id" [job-id] (sug/suggested job-id))
           (POST "/post-job" req (jobs/post-job req))
           (GET "/location/:q" [q] (loc/locations q))
           (GET "/templates" req (email/get-templates (r/recruiter-id req)))
           (PUT "/templates/:template-id" req (email/update-template (r/recruiter-id req) (:params req)))
           (POST "/templates" req (email/save-template (r/recruiter-id req) (:params req)))
           (DELETE "/templates/:template-id" req (email/delete-template (r/recruiter-id req) (:params req)))
           (POST "/search" req (search/search (:params req) (r/ladders-user? (r/email req))))
           (POST "/candidates" req (c/candidates (-> req :params :secure-ids)))
           (POST "/candidates/dismiss" req (jobs/dismiss-candidate (:params req)))
           (GET "/candidate-notes/:secure-id" req (cn/get-notes (r/recruiter-id req) (-> req :params :secure-id)))
           (PUT "/candidate-notes/:secure-id" req (cn/save-notes (r/recruiter-id req) (:params req)))
           (GET "/recruiter-images" req (img/get-image (r/recruiter-id req)))
           (context "/shopify" []
             (GET "/" req (rr/redirect (s/multipass (r/email req))))
             (GET "/promoted-job" req (s/multipass (r/email req) (-> env :shopify :redirects :promoted-job)))
             (GET "/full-access" req (do (sl/submit-full-access-recruiter-info :pay-curtain "Individual" (r/recruiter-id req))
                                         (rr/redirect (s/multipass (r/email req) (-> env :shopify :full-access))))))
           (GET "/recruiter-member-profile" req (rr/redirect (str recruiter-profile-base (r/recruiter-id req))))
           (wrap-multipart-params
             (POST "/recruiter-images" req (img/upload-image (r/recruiter-id req) (:params req))))
           (context "/projects" []
             (GET "/" req (p/projects (r/recruiter-id req)))
             (POST "/" req (p/add-project (r/recruiter-id req) (:params req)))
             (PUT "/:project-id" req (p/edit-project (r/recruiter-id req) (:params req)))
             (POST "/:project-id" req (p/delete-project (r/recruiter-id req) (-> req :params :project-id)))
             (POST "/:project-id/:secure-id" req (p/add-candidate-to-project (r/recruiter-id req) (-> req :params :project-id) (-> req :params :secure-id)))
             (DELETE "/:project-id/:secure-id" req (p/remove-candidate-from-project (r/recruiter-id req) (-> req :params :project-id) (-> req :params :secure-id)))
             (GET "/:secure-id" req (p/projects-for-candidate (r/recruiter-id req) (-> req :params :secure-id)))
             (GET "/:project-id/candidates" req (p/project-candidates (r/recruiter-id req) (-> req :params :project-id))))
           (context "/tracking" []
             (POST "/view/:secure-id" req (t/candidate-view (:params req))))
           (context "/account" []
             (PUT "/profile" req (a/update-recruiter-profile (r/recruiter-id req) (:params req)))
             (PUT "/change-password" req (a/change-password (r/recruiter-id req) (:params req))))
           (context "/ats" []
             (POST "/" req (ats/save-candidate (:params req)))
             (GET "/settings" req (ats/get-settings (r/recruiter-id req)))
             (POST "/settings" req (ats/save-settings (:params req)))
             (POST "/lever" req (ats/get-users-lever (:params req)))
             (GET "/greenhouse" [] (ats/get-greenhouse-url))
             (GET "/jobs" req (ats/jobs (-> req :params :recruiter-id))))
           (context "/teams" []
             (GET "/is-admin" req (teams/is-admin (-> req :params :recruiter-id)))
             (GET "/team-summary-by-admin/:rec-id" [rec-id] (teams/team-dashboard-by-admin-id (Integer. rec-id)))
             (GET "/team-summary-by-team/:team-id" [team-id] (teams/team-dashboard-by-team-id team-id)))
           (POST "/pay-curtain-click" req (sl/submit-full-access-recruiter-info :pay-curtain-click nil (r/recruiter-id req)))
           (POST "/enterprise-click" req (sl/submit-full-access-recruiter-info :pay-curtain "Enterprise" (r/recruiter-id req)))
           (POST "/resume/downloaded" req (resume/resume-downloaded (:params req)))
           (POST "/resume/share" req (resume/share-resume (:params req)))
           (GET "/company" req (company/autocomplete (:params req)))
           (POST "/verify/send" req (ve/send-email (r/recruiter-id req)))
           (context "/saved-searches" []
             (GET "/" req (saved-search/saved-searches (-> req :params :recruiter-id)))
             (GET "/:saved-search-id" [saved-search-id] (saved-search/saved-search saved-search-id))
             (POST "/" req (saved-search/create (:params req)))
             (PUT "/:search-id" req (saved-search/update-saved-search (:params req)))
             (DELETE "/:search-id" req (saved-search/delete (-> req :params :search-id)))
             (DELETE "/" req (saved-search/delete-multi (:params req)))))

(defroutes superuser-routes
           (context "/redact-permissions" []
             (GET "/:recruiter-id" [recruiter-id] (su/redact-permissions recruiter-id))
             (PUT "/:recruiter-id" req (su/save-redact-permissions (:params req)))
             (DELETE "/:recruiter-id" [recruiter-id] (su/remove-redact-permissions recruiter-id)))
           (context "/inventory" []
             (GET "/:recruiter-id" [recruiter-id] (i/inventory recruiter-id))
             (POST "/:recruiter-id" req (i/save-inventory (:params req))))
           (POST "/impersonate" req sec/impersonate)
           (GET "/superuser/jobs/:job-id" [job-id] (json/generate-string (jobs/read-superuser-job job-id)))
           (POST "/jobs/:job-id/promotions" req (jobs/admin-promote (:params req) (r/recruiter-id req)))
           (POST "/jobs/:job-id/cancel-promotion" req (jobs/cancel-promotion (-> req :params :job-id) (r/recruiter-id req)))
           (POST "/verify/send/:recruiter-id" [recruiter-id] (ve/send-email recruiter-id))
           (context "/recruiters" []
             (GET "/pending" req (rec/pending-recruiters))
             (GET "/pending-escalated" req (rec/pending-escalated-recruiters))
             (GET "/approved-past-week" _ (rec/approved-past-week))
             (GET "/:id" [id] (json/generate-string (rec/recruiter-profile id)))
             (PUT "/:id" req (rec/update-recruiter (:params req) (r/recruiter-id req)))
             (PUT "/:id/approve" [id] (rec/approve id))
             (PUT "/:id/escalate" [id] (ape/escalate-and-email id))
             (PUT "/:id/decline" [id] (rec/decline id))
             (PUT "/:id/permanently-decline" [id] (rec/permanently-decline id))
             (GET "/fetch-admin-note/:id" [id] (json/generate-string (rec/get-admin-note id)))
             (POST "/create-admin-note" req (json/generate-string (rec/create-admin-note (:params req) (r/recruiter-id req))))
             (POST "/update-admin-note" req (json/generate-string (rec/update-admin-note (:params req) (r/recruiter-id req)))))
           (POST "/recruiter-search" req (rec/search (:params req)))
           (mw/wrap-recruiter-id
             (context "/teams" []
               (POST "/read-all" req (teams/teams (:params req)))
               (GET "/:team-id" [team-id] (teams/team team-id))
               (POST "/search" req (teams/search (-> req :params :query)))
               (POST "/" req (teams/create (:params req)))
               (PUT "/:team-id" req (teams/update-team (:params req)))
               (DELETE "/:team-id" req (teams/delete (:params req)))
               (POST "/:team-id/make-admin" req (teams/make-admin (:params req)))
               (POST "/:team-id/add-admin-by-email" req (teams/add-admin-by-email (:params req)))
               (POST "/:team-id/remove-admin" req (teams/remove-admin (:params req)))
               (POST "/:team-id/add-recruiter" req (teams/add-recruiter (:params req)))
               (POST "/:team-id/remove-recruiter" req (teams/remove-recruiter (:params req))))))

(defroutes app-routes
           (-> public-routes
               mw/wrap-recruiter-id
               wrap-json-response)
           rl-redirect-routes
           email-redirect-routes
           (-> secured-routes
               mw/wrap-recruiter-id
               (wrap-routes sec/wrap-authentication)
               wrap-json-response)
           (-> superuser-routes
               (wrap-routes sec/wrap-superuser-route)
               (wrap-routes sec/wrap-authentication)
               wrap-json-response)
           (cr/not-found-redirect (str (:host env) "/#/404")))

(defroutes dev-handler
           (-> plain-routes
               wrap-keyword-params
               wrap-reload
               (wrap-routes mw/wrap-json-data))
           (-> app-routes
               wrap-reload
               wrap-keyword-params
               wrap-json-params
               wrap-stacktrace
               wrap-params
               sec/wrap-authorized-redirects
               (sec/wrap-auth-cookie "LaddersCookie456")))

(defroutes handler
           (-> plain-routes
               wrap-keyword-params
               (wrap-routes mw/wrap-json-data))
           (-> app-routes
               wrap-keyword-params
               wrap-json-params
               wrap-stacktrace
               wrap-params
               sec/wrap-authorized-redirects
               (sec/wrap-auth-cookie "LaddersCookie456")))