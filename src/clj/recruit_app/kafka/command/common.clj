(ns recruit-app.kafka.command.common
  (:require [clj-time.core :as t]
            [clj-time.coerce :as c]))

(defn with-timestamp
  "Adds current timestamp to command"
  [key command]
  (assoc command key (c/to-long (t/now))))
