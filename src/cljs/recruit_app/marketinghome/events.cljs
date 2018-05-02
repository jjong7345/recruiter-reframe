(ns recruit-app.marketinghome.events
  (:require [re-frame.core :as rf]
            [recruit-app.db :as db]
            [day8.re-frame.http-fx]
            [secretary.core :as sec]
            [ajax.core :as ajax]
            [cljs.reader :as edn]
            [clojure.walk :as w]
            [recruit-app.util.events :as ev]
            [recruit-app.util.uri :as u]))

(ev/reg-events "marketinghome" ["email"])
(ev/reg-toggle-event "marketinghome" "submitting?")

(defn email-exists-success
  "Parses response and routes to correct flow"
  [_ [_ email response]]
  (if (edn/read-string response)
    {:dispatch [:login/email-exists-reroute email]}
    {:dispatch [:marketinghome/submit-email email]}))

(defn email-exists-failure
  "Displays alert to user"
  []
  {:dispatch-n [[:marketinghome/toggle-submitting?]
                [:alerts/add-error "Unexpected Error Occurred. Please Try Again."]]})

(defn email-exists
  "Posts email address to endpoint to check if already exists, then continues"
  [_ [_ email]]
  {:dispatch   [:marketinghome/toggle-submitting?]
   :ra-http-xhrio {:method          :post
                   :uri             (u/uri :email-exists?)
                   :params          {:email email}
                   :format          (ajax/json-request-format)
                   :response-format (ajax/raw-response-format)
                   :on-success      [:marketinghome/email-exists-success email]
                   :on-failure      [:marketinghome/email-exists-failure]}})

(defn- company-id?
  "Checks if response has company-id"
  [response]
  (when (seq response)
    (let [id (js/parseInt response)]
      (and (not (js/isNaN id)) (< 0 id)))))

(defn submit-email-success
  "Routes to correct onboarding flow"
  [{:keys [db]} [_ email response]]
  {:dispatch-n (cond-> [[:marketinghome/clear-form]
                        [:login/clear-form]
                        [:onboarding/clear-form]
                        [:onboarding/email-change email]]
                       (company-id? response) (conj [:onboarding/company-id-change (js/parseInt response)]))
   :route      "/onboarding"})

(defn submit-email-failure
  "Displays alert to user"
  [{:keys [db]} _]
  (let [active-panel (:active-panel db)
        base-events [[:alerts/add-error "Unexpected Error Occurred. Please Try Again."]]]
    {:dispatch-n (if (= active-panel :login-panel)
                   (conj base-events [:login/creating-account?])
                   (conj base-events [:marketinghome/toggle-submitting?]))}))

(defn submit-email
  "Posts email address to endpoint to check for preapproved company"
  [_ [_ email]]
  {:ra-http-xhrio {:method          :post
                   :uri             (u/uri :onboarding)
                   :params          {:email email}
                   :format          (ajax/json-request-format)
                   :response-format (ajax/raw-response-format)
                   :on-success      [:marketinghome/submit-email-success email]
                   :on-failure      [:marketinghome/submit-email-failure]}})

(defn clear-form
  "Dissocs marketinghome from db"
  [db _]
  (dissoc db :marketinghome))

(rf/reg-event-fx
  :marketinghome/submit-email-success
  submit-email-success)

(rf/reg-event-fx
  :marketinghome/submit-email-failure
  submit-email-failure)

(rf/reg-event-fx
  :marketinghome/submit-email
  submit-email)

(rf/reg-event-fx
  :marketinghome/email-exists-success
  email-exists-success)

(rf/reg-event-fx
  :marketinghome/email-exists-failure
  email-exists-failure)

(rf/reg-event-fx
  :marketinghome/onboarding
  email-exists)

(rf/reg-event-db
  :marketinghome/clear-form
  clear-form)