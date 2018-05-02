(ns recruit-app.jobs.api
  (:require [recruit-app.util.http :as h]
            [cheshire.core :as json]
            [recruit-app.util.job :as ju]
            [taoensso.timbre :as log]
            [clj-http.client :as http]
            [config.core :refer [env]]
            [recruit-app.util.encryption :as d]
            [clojure.spec.alpha :as s]
            [recruit-app.specs.common :as common]
            [recruit-app.specs.job :as job-specs]))

(defn- jobs-url
  "Returns jobs url of given type from config"
  [type]
  (-> env :services :jobs (get type)))

(defn- update-job-url
  "Returns url from config with job-id attached"
  [job-id]
  (str (jobs-url :update) "/" job-id))

(defn read-jobs
  "Returns map of jobs for recruiter"
  [recruiter-id]
  (->> {:recruiter-id recruiter-id
        :size         1000}
       (json/generate-string)
       (h/post (jobs-url :read-multi))))

(defn jobs
  "Post to lambda endpoint to retrieve jobs from job-applications endpoint"
  [rec-id]
  (json/generate-string (read-jobs rec-id)))

(defn read-job
  "Calls API to fetch job by ID"
  [job-id]
  (->> {:job-id job-id}
       json/generate-string
       (h/post (jobs-url :read))))

(defn read-superuser-job
  "Calls the gardner endpoint to get a job from Job ES index by ID (w/o job apply info)"
  [job-id]
  (h/get (str (jobs-url :read-superuser) job-id)))

(defn job
  "Post to lambda endpoint to get job by ID"
  [{:keys [recruiter-id job-id]}]
  (->> (read-job job-id)
       (ju/recruiter-job recruiter-id)
       (json/generate-string)))

(defn index-job-by-id
  "Makes request to reindex job in `job-applications` ES"
  [job-id]
  (try
    (->> job-id
         (str (jobs-url :index-es))
         (http/post))
    (catch Exception e
      (log/error e :job-applications-indexing-error)
      (throw (Exception. (str " Failed to index job into `job-applications` index: "
                              job-id " --  Error " (.getMessage e)))))))

(defn index-harvested-job-by-id
  "Makes request to reindex harversted job into the `job` ES"
  [job-id]
  (try
    (->> {:config-name "job"
          :id          job-id}
         json/generate-string
         (h/post (jobs-url :harvest-index-es)))
    (catch Exception e
      (throw (Exception. (str " Failed to index harvested job into `job` index: "
                              job-id " -- Error " (.getMessage e)))))))

(defn index-posted-job
  "Checks for jobId in response and reindexes"
  [{:keys [body] :as response}]
  (when-let [job-id (-> body (json/parse-string true) :job :jobId)]
    (index-job-by-id job-id))
  response)

(defn post-job
  "Post new job to inside man"
  [req]
  (->> req
       :params
       ju/post-job-request
       json/generate-string
       h/post-request
       (http/post (jobs-url :create))
       index-posted-job
       h/as-json))

(defn update-job
  "Makes API call to update job. Returns updated job"
  [{:keys [jobId] :as params}]
  (let [response (->> params
                      ju/job-request
                      json/generate-string
                      h/post-request
                      (http/put (update-job-url jobId)))]
    (index-job-by-id jobId)
    (h/as-json response)))

(defn repost-job
  "Makes API call to repost job. Returns job"
  [job-id]
  (let [response (http/put (str (update-job-url job-id) "/repost"))]
    (index-job-by-id job-id)
    (h/as-json response)))

(defn delete-job
  "Makes API call to delete job. Returns job"
  [job-id]
  (let [response (http/delete (update-job-url job-id))]
    (index-job-by-id job-id)
    (h/as-json response)))

(defn job-promotion
  "fetches job-promotion status data for a particular job-id
    -req: a map with <job-id, job-id-value> pair"
  [req]
  (->> req
       json/generate-string
       (h/post (jobs-url :promotion))
       json/generate-string))

(defn job-promotion-list
  "fetches job-promotion status data for a given list of job ids
    -req: a map with <job-ids, list-of-job-id-values>"
  [req]
  (->> req
       json/generate-string
       (h/post (jobs-url :promotion-list))
       json/generate-string))

(defn promote-job
  "Promotes a pending or approved job"
  [job-id]
  (let [response (->> {:job-id job-id}
                      (json/generate-string)
                      (h/post (jobs-url :promote))
                      (json/generate-string))]
    (index-job-by-id job-id)
    response))

(defn applicant-ids
  "Returns a set of all jobseeker-ids that have applied to any of a recrutiers jobs"
  [rec-id]
  (->> (json/parse-string (jobs rec-id) true)
       (map :locations)
       flatten
       (map :applicants)
       flatten
       (map :jobSeekerId)
       set))

(defn has-applied?
  "Returns true if a jobseeker has applied to any of a recruiters jobs"
  [rec-id jobseeker-id]
  (contains? (applicant-ids rec-id) jobseeker-id))

(defn application-viewed
  "Sends request to track application view"
  [params]
  (try (->> params
            (json/generate-string)
            (h/post (-> env :services :jobs :application-view))
            (json/generate-string))
       (catch Exception e (log/error "Could not record application view for job location" (:job-location-id params)))))

(defn application-dismissed-email
  "sends the email to dismiss the application"
  [member-id job-location-id]
  (->> {:member-id       member-id
        :job-location-id job-location-id}
       (json/generate-string)
       (h/post (-> env :services :jobs :dismiss-email))))

(defn dismiss-candidate
  "Dismisses candidate for job (via job-location-id)"
  [{:keys [secure-id job-id job-location-id]}]
  (let [member-id (d/decrypt-secureid secure-id)
        response (->> {:jobseeker-id    member-id
                       :job-location-id job-location-id}
                      json/generate-string
                      h/post-request
                      (http/post (jobs-url :dismiss-candidate)))]
    (index-job-by-id job-id)
    (when (= 200 (:status response))
      (try (application-dismissed-email member-id job-location-id)
           (catch Exception e (log/error "Did not send dismiss candidate email for job location" job-location-id))))
    response))

(defn- index-promoted-job
  "Update the appropriate es index to reflect promoted status of job"
  [job-id]
  (try (index-job-by-id job-id)
       (catch Exception e
         (log/error e "Did not index the job:" job-id " into the `job-applications` index. It may be because it is a Harvested job.")))
  (try (index-harvested-job-by-id job-id)
       (catch Exception f
         (log/error f "Did not index the job: " job-id " into the `job` index. Something went haywire."))))

(defn admin-promote
  "Promotes job with given dates"
  [{:keys [job_id start_date end_date]} recruiter-id]
  (let [response (->> {:job-id     job_id
                       :start-date start_date
                       :end-date   end_date
                       :updater-id recruiter-id}
                      json/generate-string
                      h/post-request
                      (http/post (jobs-url :admin-promote)))]
    (index-promoted-job job_id)
    response))

(defn cancel-promotion
  "Cancels promotion for given job"
  [job-id recruiter-id]
  (let [response (->> {:job-id     job-id
                       :updater-id recruiter-id}
                      json/generate-string
                      h/post-request
                      (http/post (jobs-url :admin-promote-cancel)))]
    (index-promoted-job job-id)
    response))

(s/fdef cancel-promotion
        :args (s/cat :recruiter-id ::common/recruiter-id :job-id ::job-specs/job-id))

(s/def ::job_id ::job-specs/job-id)
(s/def ::start_date pos-int?)
(s/def ::end_date pos-int?)
(s/def ::promote-data (s/keys :req-un [::job_id ::start_date ::end_date]))

(s/fdef admin-promote
        :args (s/cat :promote-data ::promote-data :recruiter-id ::common/recruiter-id))
