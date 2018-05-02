(ns recruit-app.kafka.query
  (:require [recruit-app.kafka.query.interaction :as ri]))

(defn viewed
  [recruiter-id]
  (ri/ri recruiter-id :viewed))

(defn contacted
  [recruiter-id]
  (ri/ri recruiter-id :contacted))