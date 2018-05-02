(ns recruit-app.modals.confirm-email.events
  (:require [recruit-app.util.events :as ev]
            [recruit-app.events :as events]
            [recruit-app.util.uri :as u]
            [ajax.core :as ajax]
            [cljs.reader :as edn]
            [recruit-app.components.modal :as modal]))

(ev/reg-events "confirm-email" ["email" "key" "show-errors?"])

(defn submit-failure
  "Dispatch event to show errors"
  [_ _]
  {:dispatch [:confirm-email/show-errors?-change true]})

(defn submit-success
  "Checks if company ID returned is valid and shows error if not"
  [{:keys [db]} [_ company-id]]
  (let [company-id (js/parseInt company-id)
        base-events [[:onboarding/email-change (-> db :confirm-email :email)]
                     [:confirm-email/email-exists? (-> db :confirm-email :email)]]
        confirmed-events (into base-events [[:recruiter/email-confirmed?-change true]
                                            [::modal/close-modal ::modal/confirm-email]])]
    (case company-id
      -1 {:dispatch-n (conj base-events [:confirm-email/show-errors?-change true])}
      0 {:dispatch-n confirmed-events}
      {:dispatch-n (conj
                     confirmed-events
                     [:onboarding/company-id-change company-id])})))

(defn submit
  "Submits key and email from form to API endpoint"
  [{:keys [db]} _]
  {:dispatch [:confirm-email/show-errors?-change false]
   :ra-http-xhrio {:method          :post
                   :uri             (u/uri :confirm-email)
                   :params          (select-keys (:confirm-email db) [:key :email])
                   :format          (ajax/json-request-format)
                   :response-format (ajax/json-response-format {:keywords? true})
                   :on-success      [:confirm-email/submit-success]
                   :on-failure      [:confirm-email/submit-failure]}})

(defn email-exists-success
  "Sets flag whether email exists"
  [db [_ response]]
  (assoc-in db [:confirm-email :email-exists?] (edn/read-string response)))

(defn email-exists-failure
  "Sets flag that email does not exist"
  [db _]
  (assoc-in db [:confirm-email :email-exists?] false))

(defn email-exists?
  "Calls API to determine if email exists"
  [_ [_ email]]
  {:ra-http-xhrio {:method          :post
                   :uri             (u/uri :email-exists?)
                   :params          {:email email}
                   :format          (ajax/json-request-format)
                   :response-format (ajax/raw-response-format)
                   :on-success      [:confirm-email/email-exists-success]
                   :on-failure      [:confirm-email/email-exists-failure]}})

(defn click-button-unauthenticated
  "Opens create-account modal if email does not already exists, otherwise routes
  to login"
  [{:keys [db]} _]
  (if (-> db :confirm-email :email-exists?)
    {:dispatch [:login/email-exists-reroute (-> db :confirm-email :email)]}
    {:dispatch [::modal/open-modal ::modal/create-account]}))

(events/reg-event-fx
  :confirm-email/submit-success
  submit-success)

(events/reg-event-fx
  :confirm-email/submit-failure
  submit-failure)

(events/reg-event-fx
  :confirm-email/submit
  submit)

(events/reg-event-fx
  :confirm-email/email-exists?
  email-exists?)

(events/reg-event-db
  :confirm-email/email-exists-success
  email-exists-success)

(events/reg-event-db
  :confirm-email/email-exists-failure
  email-exists-failure)

(events/reg-event-fx
  :confirm-email/click-button-unauthenticated
  click-button-unauthenticated)
