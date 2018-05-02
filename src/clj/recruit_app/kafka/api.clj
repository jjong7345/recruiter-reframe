(ns recruit-app.kafka.api
  (:require [clj-http.client :as http]
            [config.core :refer [env]]
            [cheshire.core :as json]
            [org.purefn.irulan.command :as cmd]
            [taoensso.timbre :as log]
            [recruit-app.util.http :as h]))

(defn emit-event
  "Emits an event to kafka"
  [event]
  (future
    (try
      (http/post
        (-> env :services :kafka :event)
        (h/edn-request event))
      (catch Exception e
        (log/warn "Failed to emit event to kafka: " event " " e)))))

(defn simple-command
  "Sends simple command to kafka"
  [command]
  (future
    (try
      (http/post
        (-> env :services :kafka :simple-command)
        (h/edn-request command))
      (catch Exception e
        (log/warn "Failed to send simple command to kafka: " command " " e)))))

(defn query
  "Creates a query provided url"
  [url]
  (try
    (-> url
        (http/get {:socket-timeout 5000 :conn-timeout 5000})
        :body
        (json/parse-string true)
        :org.purefn.irulan.query/payload)
    (catch Exception e
      (log/warn "Failed to send query to kafka: " url " " e) [])))

(defn command
  "Sends command to kafka"
  [command]
  (try
    (http/post (-> env :services :kafka :command)
               (h/edn-request (::cmd/payload command)))
    (catch Exception e
      (log/warn "Failed to send command to kafka: " command " " e))))

(defn view
  "Calls rashomon and returns irulan payload for given view"
  [params]
  (try
    (->> (h/edn-request params)
         (http/post (-> env :services :kafka :view))
         h/body-map)
    (catch Exception e
      (log/warn "Failed to retrieve view from kafka" params))))

