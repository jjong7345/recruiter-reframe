(ns recruit-app.util.http
  (:require [cheshire.core :as json]
            [clj-http.client :as http]
            [ring.util.response :as rr]))

(defn body-map
  "Returns parsed body from response"
  [{:keys [body]}]
  (json/parse-string body true))

(defn get
  ([url & [req]]
   (-> url
       (http/get req)
       body-map)))

(defn put [url body]
  (-> url
      (http/put {:body         body
                 :content-type :json})
      body-map))

(defn post-request
  "Returns map for json post request with given body"
  [body]
  {:body         body
   :content-type :json})

(defn edn-request
  "Creates request in edn format"
  [body]
  {:body         (pr-str body)
   :content-type :edn})

(defn post
  ([url body]
   (post url body {}))
  ([url body headers]
   (-> url
       (http/post (merge (post-request body) headers))
       body-map)))

(defn as-json
  "Sets response header to be application/json"
  [response]
  (rr/content-type response "application/json"))

(defn delete
  ([url & [req]]
   (-> url
       (http/delete req)
       body-map)))

(defn json-request? [request]
  "Checks if the request has a json content type."
  (if-let [type (get-in request [:headers "content-type"])]
    (not (empty? (re-find #"^application/(.+\+)?json" type)))))