(ns recruit-app.util.middleware
  (:require [cheshire.core :as json]
            [recruit-app.util.http :as hutil]
            [recruit-app.util.request :as r]))

(defn wrap-recruiter-id
  "Adds recruiter-id to params of request"
  [handler]
  (fn [req]
    (handler
      (assoc-in req [:params :recruiter-id] (r/recruiter-id req)))))

(defn wrap-json-data
  "If request has a json body, appends original json string under :json-string key.
  Additionally, parses & puts parsed content under :json-params and :params."
  [handler]
  (fn [request]
    (handler
      (if (hutil/json-request? request)
        (let [string-body (-> request :body slurp)
              json-params (json/parse-string string-body true)]
          (-> request
              (assoc :json-string string-body
                     :json-params json-params)
              (update :params merge json-params)))
        request))))