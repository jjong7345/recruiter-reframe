(ns recruit-app.onboarding-email-verification.events
  (:require [re-frame.core :as rf]
            [recruit-app.db :as db]
            [recruit-app.util.events :as ev]
            [ajax.core :as ajax]
            [cljs.reader :as edn]
            [recruit-app.util.projects :as p]
            [recruit-app.util.uri :as u]
            [recruit-app.util.response :as r]
            [cemerick.url :as url]
            [recruit-app.components.modal :as modal]))

(ev/reg-events "email-verification" ["code" "show-error?"])

(defn re-send-email-success
  "Handle re-send email success response"
  [_ _]
  {:dispatch [::modal/open-modal ::modal/resend-verification-success]})

(defn re-send-email-failure
  "Handle re-send email failure response"
  [_ _]
  {:dispatch [:alerts/add-error "Failed To Send Email. Please Try Again."]})

(defn re-send-email
  [{:keys [db]} _]
  {:ra-http-xhrio {:method          :post
                   :uri             (u/uri :verify-resend)
                   :params          {}
                   :format          (ajax/json-request-format)
                   :response-format (ajax/json-response-format {:keywords? true})
                   :on-success      [:email-verification/re-send-email-success]
                   :on-failure      [:email-verification/re-send-email-failure]}})

(defn- verification-code
  "Returns url-encoded verification code from db"
  [{:keys [email-verification]}]
  (-> email-verification
      :code
      url/url-encode))

(defn code-valid-success
  "Routes to verify URL"
  [{:keys [db]} [_ response]]
  (if (edn/read-string response)
    {:external-route (->> db
                          verification-code
                          (str "/verify/"))}
    {:dispatch [:email-verification/code-valid-failure]}))

(defn code-valid-failure
  "Displays alert to user that code was invalid"
  [_ _]
  {:dispatch [:alerts/add-error "Invalid Code. Please Enter A Valid Verification Code."]})

(defn verification-code-valid?
  [{:keys [db]} _]
  {:ra-http-xhrio {:method          :get
                   :uri             (u/uri :verification-code-valid? (verification-code db))
                   :response-format (ajax/raw-response-format)
                   :on-success      [:email-verification/code-valid-success]
                   :on-failure      [:email-verification/code-valid-failure]}})

(rf/reg-event-fx
  :email-verification/re-send-email
  re-send-email)

(rf/reg-event-fx
  :email-verification/re-send-email-success
  re-send-email-success)

(rf/reg-event-fx
  :email-verification/re-send-email-failure
  re-send-email-failure)

(rf/reg-event-fx
  :email-verification/code-valid-success
  code-valid-success)

(rf/reg-event-fx
  :email-verification/code-valid-failure
  code-valid-failure)

(rf/reg-event-fx
  :email-verification/verify
  verification-code-valid?)
