(ns recruit-app.resume.api
  (:require [clj-http.client :as http]
            [cheshire.core :as json]
            [recruit-app.util.encryption :as d]
            [config.core :refer [env]]
            [ring.util.response :as rr]
            [recruit-app.kafka.api :as k]
            [clojure.string :as cs]
            [recruit-app.kafka.event :as event]
            [recruit-app.jobs.api :as job]
            [recruit-app.company.api :as company]
            [recruit-app.util.http :as h]
            [recruit-app.resume.share-key :as key])
  (:import [java.io InputStream PushbackInputStream]))

(def request-headers {:content-type :json
                      :as           :stream})

(defn content-disposition-header
  "Returns attachment header with filename provided"
  [filename]
  (str "attachment; filename=\"" filename "\""))

(defn- with-gzip
  "Add gzip encoding header for local development"
  [resp]
  (if (-> env :env (= :dev))
    (assoc-in resp [:headers :transfer-encoding] "gzip")
    resp))

(defn resume-response
  "Returns formatted resume response"
  [response filename]
  (-> response
      (assoc-in [:headers :content-type] "application/pdf")
      (assoc-in [:headers :connection] "keep-alive")
      with-gzip
      (assoc-in [:headers :content-disposition] (content-disposition-header filename))))

(defn- resume-url
  "Constructs URL to get resume for jobseeker"
  ([jobseeker-id recruiter-id]
   (resume-url
     jobseeker-id
     recruiter-id
     (when recruiter-id
       (job/has-applied? recruiter-id jobseeker-id))))
  ([jobseeker-id recruiter-id unredacted?]
   (str (if unredacted?
          (-> env :services :resumes :unredacted)
          (-> env :services :resumes :read))
        "?jobseeker-id="
        jobseeker-id
        (when (and recruiter-id (not unredacted?))
          (str "&recruiter-id="
               recruiter-id)))))

(defn- resume-metadata-url
  "Constructs URL to get resume metadata for jobseeker"
  [jobseeker-id]
  (str (-> env :services :resumes :read-metadata) jobseeker-id))

(defn- send-email [{:keys [recruiter-id jobseekerId] :as params} recipient]
  (->> (merge params {:recipient recipient :key (key/create recruiter-id jobseekerId recipient)})
       json/generate-string
       h/post-request
       (http/post (-> env :services :resumes :share-email))))

(defn resume-metadata
  [secure-id]
  (-> secure-id
      (d/decrypt-secureid)
      (resume-metadata-url)
      (http/get)
      :body))

(defn- get-resume
  "Returns resume for given jobseeker-id"
  [recruiter-id jobseeker-id filename]
  (-> jobseeker-id
      (resume-url recruiter-id)
      (http/get (merge request-headers))
      (resume-response filename)))

(defn convert-resume
  "Attempts to convert resume for jobseeker"
  [secure-id]
  (when-let [resume-id (-> (resume-metadata secure-id)
                           (json/parse-string true)
                           :resume-id)]
    (http/get
      (format
        (-> env :services :resumes :convert)
        (d/decrypt-secureid secure-id)
        resume-id))))

(defn resume
  "Returns resume for given secure-id"
  [{:keys [recruiter-id secure-id filename]}]
  (let [js-id (d/decrypt-secureid secure-id)
        {:keys [body] :as response} (get-resume recruiter-id js-id filename)
        pushback-stream (PushbackInputStream. body)
        bytes-read (.read pushback-stream)]
    (.unread pushback-stream bytes-read)
    (if (> bytes-read 0)
      (assoc response :body pushback-stream)
      (do (convert-resume secure-id)
          (get-resume recruiter-id js-id filename)))))

(defn resume-downloaded
  "Emit event that resume was downloaded"
  [params]
  (k/emit-event (event/resume-downloaded params)))

(defn company-id
  "Returns preapproved company id for email if not blank"
  [email]
  (let [id (-> email company/preapproved-company-id :body)]
    (if (seq id) id "0")))

(defn confirm-email
  "Confirms user email to view candidate resume"
  [{:keys [email key]}]
  (if (key/valid-key? key email)
    (company-id email)
    "-1"))

(defn share-resume
  "Shares resume to given recipients"
  [{:keys [recipients] :as params}]
  (doseq [recipient (set (cs/split recipients #","))]
    (send-email params recipient))
  (json/generate-string {:success true}))

(defn- create-key
  "Creates a key to retrieve resumes with"
  [recruiter-id jobseeker-id]
  (-> (format (-> env :services :resumes :create-key) recruiter-id jobseeker-id)
      http/get
      :body
      (json/parse-string true)
      :key))

(defn public-resume-url
  "Returns public url for candidate's resume"
  [secure-id recruiter-id]
  (let [jobseeker-id (d/decrypt-secureid secure-id)]
    (format
      (-> env :services :resumes :public-resume)
      jobseeker-id
      (create-key recruiter-id jobseeker-id))))
