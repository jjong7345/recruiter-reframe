(ns recruit-app.sales-leads.api
  (:require [recruit-app.recruiter.api :as rec]
            [recruit-app.hubspot.api :as hubspot]
            [config.core :refer [env]]
            [clojure.set :refer [rename-keys]]
            [recruit-app.kafka.api :as k]
            [recruit-app.kafka.event :as event]))

(defn submit-recruiter-info
  "Generic handler for submission of recruiter info to hubspot.
  -params: optional map that would be merged and submitted with the recruiter info."
  ([form recruiter-id]
   (submit-recruiter-info form nil recruiter-id))
  ([form params recruiter-id]
   (-> recruiter-id
       rec/recruiter-profile
       (merge params)
       (->> (hubspot/submit-form form)))))

(defn submit-full-access-recruiter-info
  "Submits form to hubspot with recruiter info"
  [form plan recruiter-id]
  (let [response (submit-recruiter-info form {:plan plan} recruiter-id)]
    (k/emit-event (event/full-access-clicked recruiter-id))
    response))

(defn request-info-unauthenticated
  "Submits form to hubspot with given info"
  [form form-values]
  (hubspot/submit-form
    form
    (rename-keys
      form-values
      {:first-name   :firstname
       :last-name    :lastname
       :phone-number :telephone})))

(defn submit-newly-approved-recruiter-info
  "Submits form to hubspot for newly approved recruiter"
  [form recruiter-id]
  (submit-recruiter-info form recruiter-id))