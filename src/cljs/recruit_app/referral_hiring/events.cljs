(ns recruit-app.referral-hiring.events
  (:require [recruit-app.events :as events]
            [recruit-app.db :as db]
            [recruit-app.util.events :as ev]
            [recruit-app.util.uri :as u]
            [ajax.core :as ajax]
            [recruit-app.components.modal :as modal]))

(ev/reg-toggle-event "referral-hiring" "read-more?")
(ev/reg-events "referral-hiring" ["show-errors?" "fullname" "email" "company" "referral"])

(defn new-referral
  [recruiter]
  {:fullname (str (:firstname recruiter) " " (:lastname recruiter))
   :email    (:email recruiter)
   :company  (:name (:company recruiter))
   :referral ""})

(defn set-referral-data
  [db _]
  (assoc-in db [:referral-hiring] (new-referral (:recruiter db))))

(defn submit-referral-success
  "Updates project in db and dispatches success alert"
  [{:keys [db]} [_]]
  {:dispatch [::modal/open-modal ::modal/referral-hiring-success]})

(defn submit-referral
  "Returns http-xhrio request to submit referral hiring bonus"
  [{:keys [db]} _]
  {:ra-http-xhrio {:method          :post
                   :uri             (u/uri :submit-referral)
                   :params          (:referral-hiring db)
                   :format          (ajax/json-request-format)
                   :response-format (ajax/raw-response-format)
                   :on-success      [:referral-hiring/submit-referral-success]
                   :on-failure      [:http-no-on-failure]}})

(events/reg-event-fx
  :referral-hiring/submit-referral
  submit-referral)

(events/reg-event-fx
  :referral-hiring/submit-referral-success
  submit-referral-success)

(events/reg-event-db
  :referral-hiring/set-referral-data
  set-referral-data)


