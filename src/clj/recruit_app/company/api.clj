(ns recruit-app.company.api
  (:require [clojure.string :refer [split lower-case]]
            [config.core :refer [env]]
            [cheshire.core :as json]
            [recruit-app.util.http :as h]
            [taoensso.timbre :as log]
            [clj-http.client :as http]))

(defn- email-domain
  "Returns domain from email address"
  [email]
  (second (split email #"@")))

(defn- with-timeout
  "Adds 20 second timeout to request"
  [request]
  (assoc request :socket-timeout 20000 :conn-timeout 20000))

(defn preapproved-company-id
  "Returns preapproved company id for recruiter email"
  [email]
  (let [domain (email-domain email)]
    (str
      (if (= (lower-case domain) "theladders.com")
        (:ladders-company-id env)
        (try
          (->> {:domain domain}
               json/generate-string
               h/post-request
               with-timeout
               (http/post (-> env :services :recruiters :preapproved-company)))
          (catch Exception e
            (log/error "Failed to lookup preapproved company id for domain" domain)
            "0"))))))

(defn autocomplete
  "Returns autocomplete suggestions for given name"
  [{:keys [name]}]
  (->> {:name name}
       json/generate-string
       (h/post (-> env :services :company :autocomplete))))
