(ns recruit-app.kafka.event
  (:require [recruit-app.kafka.event.common :as common]
            [recruit-app.kafka.event.resume :as res]
            [recruit-app.kafka.event.search :as search]
            [ladders-domains.recruiter.full-access :as fa]
            [ladders-domains.recruiter.resume :as resume]
            [ladders-domains.recruiter.candidate-search :as search-event]
            [recruit-app.util.encryption :as e]
            [clojure.string :as cs]))

(defn full-access-clicked
  "Creates full access clicked kafka event"
  [recruiter-id]
  (->> {}
       (common/with-recruiter-id recruiter-id)
       (common/with-timestamp ::fa/timestamp)
       (fa/full-access-clicked 1)))

(defn resume-downloaded
  "Creates resume downloaded kafka event"
  [{:keys [recruiter-id secure-id job-location-id resume-version]}]
  (let [jobseeker-id (e/decrypt-secureid secure-id)]
    (->> {::resume/resume-version resume-version}
         (common/with-recruiter-id recruiter-id)
         (res/with-candidate-id jobseeker-id)
         (res/with-job-location-id job-location-id)
         (common/with-timestamp ::resume/timestamp)
         (resume/resume-downloaded 1))))

(defn search
  "Creates search kafka event"
  [{:keys [criteria offset limit sort-by recruiter-id]} saved-search-id count]
  (->> {::search-event/candidate-count count
        ::search-event/sort-by         sort-by
        ::search-event/pagination      {::search-event/start     offset
                                        ::search-event/page-size limit}}
       (common/with-recruiter-id recruiter-id)
       (search/with-saved-search-id saved-search-id)
       (search/with-criteria criteria)
       (common/with-timestamp ::search-event/timestamp)
       (search-event/search-run 1)))
