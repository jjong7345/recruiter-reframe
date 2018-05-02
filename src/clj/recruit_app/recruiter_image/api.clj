(ns recruit-app.recruiter-image.api
  (:require [clj-http.client :as http]
            [cheshire.core :as json]
            [clojure.java.io :as io]
            [config.core :refer [env]]
            [ring.util.response :as rr]))

(def image-request-headers {:content-type :json
                            :as           :stream})

(defn image-url
  "Returns image url with id attached"
  [recruiter-id]
  (str (-> env :services :recruiters :image) recruiter-id))

(defn get-image
  "Retrieves image for user."
  [recruiter-id]
  (try
    (-> recruiter-id
        (image-url)
        (http/get image-request-headers)
        (assoc-in [:headers :transfer-encoding] "gzip"))
    (catch Exception e (rr/response ""))))

(defn upload-image
  "Uploads image to S3. Takes a base64-encoded data url."
  [recruiter-id params]
  (-> recruiter-id
      (image-url)
      (http/put {:body (io/input-stream (get-in params ["imageProfile" :tempfile]))})
      (json/generate-string)))
