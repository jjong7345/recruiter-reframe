(ns recruit-app.subs
    (:require-macros [reagent.ratom :refer [reaction]])
    (:require [re-frame.core :as rf]))

(def unauthenticated-panels #{:home-panel
                              :get-full-access-panel
                              :faqs-panel
                              :terms-of-use-panel
                              :referral-hiring-panel
                              :onboarding-panel
                              :page-404-panel
                              :pricing-panel})

(defn query-params
  "Returns query params from db"
  [{:keys [query-params]} _]
  query-params)

(defn show-email-verification?
  "Only show email verification view if user is authenticated and has not opted in"
  [[authenticated? opted-in?] _]
  (and authenticated? (not opted-in?)))

(defn show-preapproved-search?
  "Show preapproved search if user is logged in but not approved"
  [[authenticated? approved? active-panel] _]
  (and authenticated? (not approved?) (not= active-panel :search-results)))

(defn show-panel?
  "Show panel if user is authenticated or viewing panel that does not require
  authentication"
  [[authenticated? unauthenticated-panel?] _]
  (or authenticated? unauthenticated-panel?))

(defn unauthenticated-panel?
  "Returns whether current panel can be viewed unauthenticated"
  [[panel share-resume-view?] _]
  (or (contains? unauthenticated-panels panel) share-resume-view?))

(rf/reg-sub
 :name
 (fn [db]
   (:name db)))

(rf/reg-sub
 :active-panel
 (fn [db _]
   (:active-panel db :home-panel)))

(rf/reg-sub
  :variation
  (fn [db _]
    (:variation db 0)))

(rf/reg-sub
  :is-fetching-user?
  (fn [db _]
    (get db :is-fetching-user? true)))

(rf/reg-sub
  :query-params
  query-params)

(rf/reg-sub
  :show-email-verification?
  :<- [:recruiter/is-authenticated?]
  :<- [:recruiter/email_optin_accepted]
  show-email-verification?)

(rf/reg-sub
  :show-preapproved-search?
  :<- [:recruiter/is-authenticated?]
  :<- [:recruiter/approved?]
  :<- [:active-panel]
  show-preapproved-search?)

(rf/reg-sub
  :show-panel?
  :<- [:recruiter/is-authenticated?]
  :<- [:unauthenticated-panel?]
  show-panel?)

(rf/reg-sub
  :unauthenticated-panel?
  :<- [:active-panel]
  :<- [:candidates/share-resume-view?]
  unauthenticated-panel?)
