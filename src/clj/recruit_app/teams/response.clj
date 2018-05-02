(ns recruit-app.teams.response
  (:require [cheshire.core :as json]
            [ring.util.response :as rr]
            [ladders-domains.recruiter.team :as team]
            [recruit-app.recruiter.api :as rec]
            [hal.uuid :as uuid]))

(defn- error
  "Returns message with 500 status"
  [message]
  {:status 500
   :body   {:error message}})

(def team-already-exists
  (error "Team with given ID already exists."))
(def not-superuser
  (error "You are not authorized to perform this action."))
(def team-does-not-exist
  (error "Team with given ID does not exist."))
(def team-must-have-one-admin
  (error "Could not remove member. Team must have at least one admin."))
(def member-does-not-exist
  (error "Member does not exist on given team."))
(def admin-already-exists
  (error "Given member is already an admin on this team."))
(def make-admin-failure
  (error "Failed to make member an admin."))

(defn- unkeyed-map
  "Removes fully qualified keys from response"
  [response]
  (zipmap (->> response keys (map (comp keyword name))) (vals response)))

(defn- unkeyed-response
  "Removes fully qualified keys from response"
  [response]
  (rr/response (json/generate-string (unkeyed-map response))))

(defn create-team
  "Checks outcome of response and returns proper message"
  [{:keys [::team/outcome ::team/body]}]
  (case (keyword outcome)
    ::team/already-exists team-already-exists
    ::team/not-super-user not-superuser
    ::team/added (unkeyed-response body)
    :else {:status 500}))

(defn remove-member
  "Checks outcome of response and returns proper message"
  [{:keys [::team/outcome ::team/body]}]
  (case (keyword outcome)
    ::team/team-does-not-exist team-does-not-exist
    ::team/not-admin-or-super-user not-superuser
    ::team/team-must-have-one-admin team-must-have-one-admin
    ::team/does-not-exist member-does-not-exist
    ::team/removed (unkeyed-response body)
    :else {:status 500}))

(defn add-admin
  "Checks outcome of response and returns proper message"
  [{:keys [::team/outcome ::team/body]}]
  (case (keyword outcome)
    ::team/team-does-not-exist team-does-not-exist
    ::team/not-admin-or-super-user not-superuser
    ::team/already-exists admin-already-exists
    ::team/added (unkeyed-response body)
    :else {:status 500}))

(defn update-team
  "Checks outcome of response and returns proper message"
  [{:keys [::team/outcome ::team/body]}]
  (case (keyword outcome)
    ::team/does-not-exist team-does-not-exist
    ::team/not-admin-or-super-user not-superuser
    ::team/renamed (unkeyed-response body)
    :else {:status 500}))

(defn delete-team
  "Checks outcome of response and returns proper message"
  [{:keys [::team/outcome ::team/body]}]
  (case (keyword outcome)
    ::team/does-not-exist team-does-not-exist
    ::team/not-admin-or-super-user not-superuser
    ::team/removed (unkeyed-response body)
    :else {:status 500}))

(defn- recruiter-profile
  "Fetches recruiter profile from guid"
  [recruiter-guid]
  (assoc
    (rec/recruiter-profile (uuid/int recruiter-guid))
    :recruiter-guid
    recruiter-guid))

(defn members
  "Extracts member and admin ids and assocs profile data

  Admins and members data comes as maps of guid keys to date-added
  {:admins {:00000002-009b-cdc8-c000-000000000000 1521659897234}
   :members {:00000002-009b-cdc8-c000-000000000000 1521659897234}}"
  [{:keys [admins members] :as resp}]
  {:admins  (map (comp recruiter-profile name) (keys admins))
   :members (map (comp recruiter-profile name) (keys members))})
