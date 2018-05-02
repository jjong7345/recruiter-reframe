(ns recruit-app.kafka.command
  (:require [recruit-app.util.encryption :as d]
            [recruit-app.kafka.command.candidate :as candidate]
            [recruit-app.kafka.command.common :as common]
            [ladders-domains.tracking.candidate :as command]))

(defn candidate-viewed
  "Command to be sent when candidate is viewed"
  [{:keys [recruiter-id secure-id resume-version]}]
  (->> {:org.purefn.irulan.command/command-type ::command/view
        ::command/resume-version resume-version}
       (candidate/with-recruiter-id recruiter-id)
       (candidate/with-candidate-id (d/decrypt-secureid secure-id))
       (common/with-timestamp ::command/timestamp)))
