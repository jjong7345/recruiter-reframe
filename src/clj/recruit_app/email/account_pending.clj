(ns recruit-app.email.account-pending
  (:require [recruit-app.email.api :as email]
            [recruit-app.recruiter.api :as r]))

(defn escalate-and-email
  "Escalates the recruiters profile then emails recruiter requesting
  more information"
  [recruiter-id]
  (let [response (r/escalate recruiter-id)]
    (email/send-email recruiter-id nil :pending-account)
    response))
