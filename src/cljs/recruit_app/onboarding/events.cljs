(ns recruit-app.onboarding.events
  (:require [recruit-app.events :as events]
            [recruit-app.db :as db]
            [day8.re-frame.http-fx]
            [secretary.core :as sec]
            [ajax.core :as ajax]
            [cljs.reader :as edn]
            [clojure.walk :refer [keywordize-keys]]
            [cemerick.url :as url]
            [recruit-app.util.dropdowns :as dd]
            [cognitect.transit :as t]
            [recruit-app.util.events :as ev]
            [clojure.set :refer [rename-keys]]
            [recruit-app.util.uri :as u]))

(ev/reg-events "onboarding" ["firstname" "lastname" "phonenumber" "extension"
                             "companyname" "companytype" "zipcode" "error-msg"
                             "show-password-hint?" "company-id" "email"
                             "show-errors?"])
(ev/reg-toggle-event "onboarding" "show-throbber?")

(defn password-change
  "Toggle showing password hint and assocs to db"
  [{:keys [db]} [_ val]]
  {:db (assoc-in db [:onboarding :password] val)
   :dispatch [:onboarding/show-password-hint?-change true]})

(defn clear-form
  "Dissocs onboarding from db"
  [db _]
  (dissoc db :onboarding))

(defn process-response-success
  "Checks for error and redirects if none found"
  [{:keys [db]} [_ response]]
  {:dispatch-n [[:onboarding/clear-form]
                [:login/login-success response]]})

(defn process-preapproved-response-success
  "Submits pre-approved recruiter as a sales lead and redirects to generic handler"
  [_ [_ response]]
  (let [recruiter-id (-> response :user :recruiter-id)]
    {:dispatch-n [[:recruiter-admin/newly-approved-recruiter recruiter-id]
                  [:onboarding/process-response-success response]]}))

(defn process-response-fail
  "Parses error and dispatches error alert"
  [_ _]
  {:dispatch-n [[:onboarding/toggle-show-throbber?]
                [:alerts/add-error "Failed To Create New User. Please Try Again."]]})

(defn base-request
  "Returns base information used for both preapproved/non-preapproved"
  [{:keys [firstname lastname phonenumber extension password email]}]
  {:firstName   firstname
   :lastName    lastname
   :phone       (str phonenumber (when (seq extension) (str "x" extension)))
   :password    password
   :email       email})

(defn register-request
  "Returns formatted request when registering without preapproved company-id"
  [{:keys [zipcode companyname companytype] :as req}]
  (-> req
      base-request
      (assoc :zip zipcode)
      (assoc :companyName companyname)
      (assoc :companyType companytype)))

(defn preapproved-request
  "Returns formatted request for registering with preapproved company-id"
  [{:keys [company-id] :as req}]
  (-> req
      base-request
      (assoc :companyId company-id)))

(defn submit-register
  "Submits form to register endpoint"
  [{:keys [db]} _]
  {:dispatch   [:onboarding/toggle-show-throbber?]
   :ra-http-xhrio {:method          :post
                   :uri             (u/uri :register-nonpreapproved)
                   :params          (-> db :onboarding register-request)
                   :format          (ajax/json-request-format)
                   :response-format (ajax/json-response-format {:keywords? true})
                   :on-success      [:onboarding/process-response-success]
                   :on-failure      [:onboarding/process-response-fail]}})

(defn submit-preapproved
  "Submits form to preapproved endpoint"
  [{:keys [db]} _]
  {:dispatch   [:onboarding/toggle-show-throbber?]
   :ra-http-xhrio {:method          :post
                   :uri             (u/uri :register-preapproved)
                   :params          (-> db :onboarding preapproved-request)
                   :format          (ajax/json-request-format)
                   :response-format (ajax/json-response-format {:keywords? true})
                   :on-success      [:onboarding/process-preapproved-response-success]
                   :on-failure      [:onboarding/process-response-fail]}})

(defn go-to-passport-verify
  "Routes to passport verify page"
  [_ _]
  {:route "/passport/verify"})

(defn submit-failure
  "Dispatches events when submitting with errors"
  [_ _]
  {:dispatch-n [[:onboarding/show-errors?-change true]
                [:onboarding/show-password-hint?-change true]]})

(events/reg-event-fx
  :onboarding/password-change
  password-change)

(events/reg-event-db
  :onboarding/clear-form
  clear-form)

(events/reg-event-fx
  :onboarding/process-response-success
  process-response-success)

(events/reg-event-fx
  :onboarding/process-preapproved-response-success
  process-preapproved-response-success)

(events/reg-event-fx
  :onboarding/process-response-fail
  process-response-fail)

(events/reg-event-fx
  :onboarding/submit-register
  submit-register)

(events/reg-event-fx
  :onboarding/submit-preapproved
  submit-preapproved)

(events/reg-event-fx
  :onboarding/go-to-passport-verify
  go-to-passport-verify)

(events/reg-event-fx
  :onboarding/submit-failure
  submit-failure)
