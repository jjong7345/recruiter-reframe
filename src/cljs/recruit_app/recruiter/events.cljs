(ns recruit-app.recruiter.events
  (:require [re-frame.core :as rf]
            [recruit-app.db :as db]
            [recruit-app.post-job.db :as pj-db]
            [ajax.core :as ajax]
            [cljs.reader :as edn]
            [cljs.spec.alpha :as s]
            [recruit-app.util.events :as re]
            [clojure.walk :as w]
            [recruit-app.util.uri :as u]))

(re/reg-events "recruiter" ["profile-img-last-update" "has-photo?"
                            "profile-status" "email-confirmed?" "ats-provider"])

(defn go-to-account-settings
  "Routes to account settings"
  [_ _]
  {:route "/account"})

(defn go-to-ats-settings
  "Routes to ats settings"
  [_ _]
  {:route "/account"})

(defn go-to-profile
  "Routes to recruiter profile"
  [{:keys [db]} _]
  {:external-route (str "https://www.theladders.com/recruiter/" (-> db :recruiter :recruiter-id))})

(defn decrement-promoted-job-count
  "Decrements pjl-count for recruiter"
  [db _]
  (update-in db [:recruiter :pjl-count] dec))

(rf/reg-event-fx
  :recruiter/go-to-account-settings
  go-to-account-settings)

(rf/reg-event-fx
  :recruiter/go-to-ats-settings
  go-to-ats-settings)

(rf/reg-event-fx
  :recruiter/go-to-profile
  go-to-profile)

(rf/reg-event-db
  :recruiter/decrement-promoted-job-count
  decrement-promoted-job-count)
