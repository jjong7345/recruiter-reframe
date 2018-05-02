(ns recruit-app.teams.api
  (:require [clj-http.client :as http]
            [cemerick.url :as url]
            [recruit-app.util.http :as h]
            [ladders-domains.recruiter.team :as team]
            [recruit-app.recruiter.api :as rec]
            [org.purefn.irulan.view :as view]
            [org.purefn.irulan.common :as icommon]
            [hal.uuid :as uuid]
            [clojure.spec.alpha :as s]
            [recruit-app.kafka.api :as kafka]
            [taoensso.timbre :as log]
            [cheshire.core :as json]
            [ring.util.response :as rr]
            [recruit-app.specs.common :as common]
            [recruit-app.teams.response :as team-resp]
            [recruit-app.teams.request :as team-req]
            [recruit-app.teams.http :as team-http]
            [recruit-app.dashboard.api :as dashboard])
  (:import [java.util UUID]))

(defn teams
  "Returns all recruiter teams. Optional limit/offset are permitted"
  [params]
  (http/get (str (team-http/url :read-all) "?" (url/map->query params))))

(defn team-members
  "Fetches all team members for given team"
  [team-id]
  (->> {::view/view-type ::team/team-recruiters-view
        ::view/key       (UUID/fromString team-id)}
       kafka/view))

(defn recruiter-teams
  "Fetches all admin teams for given admin"
  [recruiter-id]
  (->> {::view/view-type ::team/recruiter-teams-view
        ::view/key       (uuid/type-6 "subscriber" recruiter-id)}
       kafka/view))

(defn first-team-id-for-admin
  "Looks up the available teams for the given recruiter and returns the first one"
  [rec-id]
  (-> (recruiter-teams rec-id)
      :admin-teams
      keys
      first))

(defn is-admin
  [rec-id]
  (str (some? (first-team-id-for-admin rec-id))))

(defn team-dashboard-by-team-id
  "Returns team dashboard summary by team-id"
  [team-id]
  (if-let [team-members (->> team-id
                             team-members
                             :members
                             keys)]
    (->> team-members
         (map (comp uuid/int name))
         (map dashboard/recruiter-dashboard)
         json/generate-string
         rr/response)
    {:status 404}))

(defn team-dashboard-by-admin-id
  "Returns team dashboard summary by admin id, grabs 1st team only"
  [rec-id]
  (if-let [team-id (first-team-id-for-admin rec-id)]
    (-> (name team-id)
        team-dashboard-by-team-id)
    {:status 404}))

(defn team
  "Returns team by ID with all member information"
  [team-id]
  (if-let [team (-> (str (team-http/url :read-one) "/" team-id)
                    http/get
                    h/body-map)]
    (-> (merge team (->> (team-members team-id)
                         team-resp/members))
        json/generate-string
        rr/response)
    {:status 404}))

(defn read-by-name
  "Performs search by name"
  [name]
  (http/get (str (team-http/url :read-by-name) "/" (url/url-encode name))))

(defn add-member
  "Adds recruiter/admin by email

  The key parameter corresponds to which service endpoint to hit (admin or recruiter)
  This function just abstracts the logic to find a recruiter by email"
  [key {:keys [email] :as params}]
  (if-let [recruiter-id (rec/recruiter-id-by-email email)]
    (-> params
        (assoc :member-id recruiter-id)
        team-req/team-request
        (->> (team-http/submit-post-request key)))
    {:status 404}))

(defn create
  "Sends request to create new team and returns response"
  [{:keys [email] :as params}]
  (if-let [recruiter-id (rec/recruiter-id-by-email email)]
    (->> (assoc
           params
           :team-id (.toString (UUID/randomUUID))
           :member-id recruiter-id)
         team-req/team-request
         (team-http/submit-post-request :create)
         team-resp/create-team)
    {:status 404}))

(defn update-team
  "Sends request to update team"
  [params]
  (->> params
       team-req/team-request
       (team-http/submit-post-request :update)
       team-resp/update-team))

(defn delete
  "Sends request to delete team"
  [params]
  (->> params
       team-req/team-request
       (team-http/submit-post-request :delete)
       team-resp/delete-team))

(defn add-admin-by-email
  "Sends request to add admin to team"
  [params]
  (add-member :add-admin params))

(defn remove-admin
  "Sends request to remove admin from team"
  [params]
  (->> params
       team-req/team-request
       (team-http/submit-post-request :remove-admin)
       team-resp/remove-member))

(defn add-recruiter
  "Sends request to add recruiter to team"
  [params]
  (add-member :add-recruiter params))

(defn remove-recruiter
  "Sends request to remove recruiter from team"
  [params]
  (->> params
       team-req/team-request
       (team-http/submit-post-request :remove-recruiter)
       team-resp/remove-member))

(defn- try-add-admin
  "Tries to add admin and will add back as member if it fails"
  [params]
  (let [{:keys [status] :as response} (->> params
                                           team-req/team-request
                                           (team-http/submit-post-request :add-admin)
                                           team-resp/add-admin)]
    (if (= status 200)
      response
      (do (add-recruiter params)
          team-resp/make-admin-failure))))

(defn make-admin
  "Makes existing member an admin"
  [params]
  (let [{:keys [status]} (remove-recruiter params)]
    (if (= status 200)
      (try-add-admin params)
      team-resp/make-admin-failure)))

(defn search
  "Checks if query is a guid and either looks up by ID or searches by name"
  [query]
  (if (s/valid? icommon/valid-guid? query)
    (team query)
    (read-by-name query)))

(s/def ::limit pos-int?)
(s/def ::offset nat-int?)
(s/def ::team-id ::team/team-id)
(s/def ::team-name ::team/team-name)
(s/def ::recruiter-id pos-int?)
(s/def ::member-id pos-int?)
(s/def ::email common/email?)
(s/def ::query (s/or :id icommon/valid-guid?
                     :name ::team-name))

(s/fdef teams
        :args (s/cat :params (s/keys :opt-un [::limit ::offset])))

(s/fdef team
        :args (s/cat :team-id ::team-id))

(s/fdef create
        :args (s/cat :params (s/keys :req-un [::team-name ::recruiter-id
                                              ::email])))

(s/fdef update-team
        :args (s/cat :params (s/keys :req-un [::team-id ::team-name
                                              ::recruiter-id])))

(s/fdef delete
        :args (s/cat :params (s/keys :req-un [::team-id ::recruiter-id])))

(s/fdef make-admin
        :args (s/cat :params (s/keys :req-un [::team-id ::recruiter-id
                                              ::member-id])))

(s/fdef add-admin-by-email
        :args (s/cat :params (s/keys :req-un [::team-id ::recruiter-id
                                              ::email])))

(s/fdef remove-admin
        :args (s/cat :params (s/keys :req-un [::team-id ::recruiter-id
                                              ::member-id])))

(s/fdef add-recruiter
        :args (s/cat :params (s/keys :req-un [::team-id ::recruiter-id
                                              ::email])))

(s/fdef remove-recruiter
        :args (s/cat :params (s/keys :req-un [::team-id ::recruiter-id
                                              ::member-id])))

(s/fdef search
        :args (s/cat :query ::query))
