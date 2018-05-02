(ns recruit-app.specs.recruiter
  (:require #?(:clj [clojure.spec.alpha :as s]
               :cljs [cljs.spec.alpha :as s])
                    [recruit-app.specs.common :as common]))

(s/def ::recruiter-id pos-int?)
(s/def ::firstname string?)
(s/def ::lastname string?)
(s/def ::email common/email?)
(s/def ::telephone string?)
(s/def ::street string?)
(s/def ::city string?)
(s/def ::state_province string?)
(s/def ::postal_code string?)
(s/def ::country string?)
(s/def ::title string?)
(s/def ::specialty string?)
(s/def ::recruiter_website_url string?)
(s/def ::recruiter_blog_url string?)
(s/def ::recruiter_facebook_url string?)
(s/def ::recruiter_linkedin_url string?)
(s/def ::recruiter_twitter_user_name string?)
(s/def ::recruiter_guest_job_function_id pos-int?)
(s/def ::name string?)
(s/def ::job_function (s/keys :req-un [::id ::name]))
(s/def ::company (s/keys :req-un [::id ::name]))
(s/def ::parameter_definition_id pos-int?)
(s/def ::parameter_name string?)
(s/def ::description string?)
(s/def ::value string?)
(s/def ::contact-preference (s/keys :req-un [::parameter_definition_id
                                             ::parameter_name
                                             ::description
                                             ::value]))
(s/def ::contact_preferences (s/coll-of ::contact-preference))
(s/def ::profile_status_id pos-int?)
(s/def ::profile_status string?)
(s/def ::role_id pos-int?)
(s/def ::role (s/keys :req-un [::name ::role_id]))
(s/def ::roles (s/coll-of ::role))
(s/def ::email_optin_accepted boolean?)
(s/def ::subscribe_date common/datetime-string?)
(s/def ::recruiter_guest_role_id pos-int?)
(s/def ::recruiter_guest_role (s/keys :req-un [::recruiter_guest_role_id ::name]))

(s/def ::pjl-count nat-int?)
(s/def ::full-access boolean?)
(s/def ::account_executive (s/or :empty common/empty-map?
                                 :not-empty (s/keys :req-un [::firstname
                                                             ::lastname
                                                             ::email
                                                             ::telephone])))

(s/def ::recruiter (s/keys :req-un [::recruiter-id
                                    ::firstname
                                    ::lastname
                                    ::email
                                    ::telephone
                                    ::street
                                    ::city
                                    ::state_province
                                    ::postal_code
                                    ::country
                                    ::title
                                    ::specialty
                                    ::recruiter_website_url
                                    ::recruiter_blog_url
                                    ::recruiter_facebook_url
                                    ::recruiter_linkedin_url
                                    ::recruiter_twitter_user_name
                                    ::job_function
                                    ::company
                                    ::contact_preferences
                                    ::profile_status
                                    ::profile_status_id
                                    ::roles
                                    ::email_optin_accepted
                                    ::subscribe_date
                                    ::recruiter_guest_role
                                    ::paid-membership
                                    ::pjl-count
                                    ::full-access
                                    ::account_executive
                                    ::ats-provider]))
