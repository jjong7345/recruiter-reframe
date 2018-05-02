(ns recruit-app.tracking.api
  (:require [recruit-app.util.encryption :as d]
            [clj-time.core :as t]
            [clj-time.coerce :as c]
            [config.core :refer [env]]
            [recruit-app.kafka.api :as k]
            [recruit-app.kafka.command :as command]
            [recruit-app.util.http :as http]
            [cheshire.core :as json]))

(defn candidate-view
  "Sends request to track candidate view"
  [params]
  (k/simple-command (command/candidate-viewed params)))