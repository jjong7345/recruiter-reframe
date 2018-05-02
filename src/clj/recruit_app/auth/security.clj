(ns recruit-app.auth.security
  (:require [compojure.core :refer [defroutes GET POST]]
            [ring.util.response :as response]
            [clj-http.client :as http]
            [buddy.sign.jwt :as jwt]
            [buddy.core.keys :as ks]
            [ring.middleware.session :refer [wrap-session]]
            [ring.middleware.session.cookie :refer [cookie-store]]
            [recruit-app.auth.handlers :as auth]
            [clojure.java.io :as io]
            [recruit-app.recruiter.api :as r]
            [cheshire.core :as json]
            [ring.util.response :as rr]
            [clojure.set :refer [rename-keys]]
            [config.core :refer [env]]
            [recruit-app.email.verify-email :as ve]
            [recruit-app.email.api :as email]
            [recruit-app.account.api :as a]
            [taoensso.timbre :as log]))

(defn- ok?
  "Returns whether status code is 200"
  [{:keys [status]}]
  (= 200 status))

(def session-recruiter-info-keys
  "Keys in recruiter-profile to be stored in the session"
  [:recruiter-id :email :roles])

(defn- has-superuser-role-id?
  "Checks given set of ids for superuser role id"
  [role-ids]
  (contains? role-ids (:superuser-role-id env)))

(defn superuser?
  "Checks roles for user for superuser role"
  [{:keys [roles]}]
  (->> roles
       (map :role_id)
       set
       has-superuser-role-id?))

(def recruit-roles
  {:user 10 :admin 20 :superuser 30})

(defn any-granted? [req roles]
  (seq
    (clojure.set/intersection
      (set (map :role-id (-> req :user :roles)))
      (set (vals (select-keys recruit-roles roles))))))

(defn authenticate
  [credentials]
  (auth/authenticate credentials))

(defn refresh-auth-token
  [token]
  (let [resp (auth/refresh-auth-token token)]
    (if (= (:status resp) 200)
      [true (:body resp)]
      [false (:body resp)])))

(defn invalidate-refresh-token [token]
  (let [resp (auth/invalidate-refresh-token token)]
    (if (= (:status resp) 200)
      [true (:body resp)]
      [false (:body resp)])))

(defn unsign-token [token]
  (jwt/unsign token (-> env :auth :secret) {:alg :hs512}))

(defn unsigned-auth
  "Returns unsigned auth token if valid, otherwise nil"
  [auth-token]
  (when auth-token
    (try
      (unsign-token auth-token)
      (catch Exception e nil))))

(defn- with-superuser
  "Adds superuser? to recruiter profile.
  Doing this here so as not to repeat superuser logic in backend/frontend"
  [profile]
  (assoc profile :superuser? (superuser? profile)))

(defn- recruiter-profile
  "Unsigns auth token and fetches recruiter"
  [access-token]
  (when-let [user (unsigned-auth access-token)]
    (-> (:sub user)
        r/recruiter-profile
        (rename-keys {:recruiter_id :recruiter-id})
        with-superuser)))

(defn- recruiter-profile-response
  "Unsigns access token from response, fetches recruiter
  profile and adds to session"
  [access-token]
  (let [profile (recruiter-profile access-token)]
    (-> {:user profile}
        rr/response
        (assoc-in [:session :user] (select-keys
                                     profile
                                     session-recruiter-info-keys)))))

(defn- with-token-pair
  "Adds token pair from auth response to session"
  [response token-pair]
  (assoc-in response [:session :token-pair] token-pair))

(defn auth-success
  "Adds recruiter info and token pair to response"
  ([{:keys [access-token] :as token-pair}]
   (auth-success token-pair (recruiter-profile-response access-token)))
  ([token-pair response]
   (with-token-pair response token-pair)))

(defn auth-resp [resp]
  (condp = (:status resp)
    200 (auth-success (:body resp))
    401 (-> resp (assoc :body {:message "Authorization failed. Check credentials."
                               :resp    resp}))
    {:status 500 :body "Something went pearshape when trying to authenticate"}))

(defn login
  [req]
  (-> req
      (select-keys [:username :password])
      authenticate
      auth-resp))

(defn impersonate
  "Attempt to sign in as another user"
  [req]
  (-> {:token        (-> req :session :token-pair :access-token)
       :recruiter-id (-> req :params :recruiter-id)}
      (auth/impersonate)
      (auth-resp)))

(defn logout [req]
  (when-let [refresh-token (-> req :session :token-pair :refresh-token)]
    (let [[ok? res] (invalidate-refresh-token refresh-token)]
      (when-not ok?
        (println "Warning : Failed to invalidate refresh token, action should be taken. Refresh Token: " refresh-token))))
  (assoc (response/response "Session deleted") :session nil))

(defn wrap-auth-cookie [handler cookie-secret]
  (-> handler
      (wrap-session
        {:store        (cookie-store {:key cookie-secret})
         :cookie-name  "ladders"
         :cookie-attrs {:max-age (* 60 60 24 30)}})))       ;; TODO: should probably add :secure true to enforce https

(defn- handle-token-refresh
  "Refreshes auth token and performs auth-success or returns 302 on error"
  [handler req refresh-token]
  (let [[ok? response] (refresh-auth-token refresh-token)]
    (if ok?
      (let [new-session (-> (auth-success response)
                            :session)]
        (-> (handler (assoc req :session new-session))
            (assoc :session new-session)))
      {:status 401 :body "You are not authorized for this feature"})))

(defn wrap-authentication [handler]
  (fn [req]
    (if (-> req :session :token-pair :access-token unsigned-auth)
      (handler req)
      (if-let [refresh-token (-> req :session :token-pair :refresh-token)]
        (handle-token-refresh handler req refresh-token)
        {:status 401 :body "You are not authorized for this feature"}))))

(defn wrap-restrict-by-roles [handler roles]
  (fn [req]
    (if (any-granted? req roles)
      (handler req)
      {:status 401 :body "You are not authorized for this feature"})))

(def redirect-whitelist
  [(re-pattern (str (:host env) "/.*"))
   (re-pattern (str (-> env :shopify :base-url) "/.*"))])

(defn wrap-authorized-redirects [handler]
  (fn [req]
    (let [resp (handler req)
          loc (get-in resp [:headers "Location"])]
      (if (and loc (not (some #(re-matches % loc) redirect-whitelist)))
        (do
          (log/warn "Possible redirect attack: " loc)
          (assoc-in resp [:headers "Location"] "/"))
        resp))))

(defn get-auth-user
  "Attempt to return auth-success from session token-pair"
  [req]
  (auth-success (-> req :session :token-pair)))

(defn wrap-superuser-route
  [handler]
  (fn [{:keys [session params] :as req}]
    (if (superuser? (:user session))
      (handler req)
      {:status 401 :body "You are not authorized for this feature"})))

(defn create-recruiter
  "Creates pending user and returns ID"
  [{:keys [email password] :as request}]
  (if-let [recruiter-id (r/create-recruiter request)]
    (do (ve/send-email recruiter-id)
        (login {:username email :password password}))
    (throw (Exception. "Failed to create recruiter"))))

(defn- get-recruiter-id
  [{:keys [access-token]}]
  (-> access-token
      unsigned-auth
      :sub))

(defn- update-email-optin
  "Updates user's email optin"
  [recruiter-id]
  (a/update-recruiter-profile recruiter-id {:email-optin true}))

(defn- approved?
  [recruiter-id]
  (= 5 (:profile_status_id (r/recruiter-profile recruiter-id))))

(defn- send-onboarding-emails
  [recruiter-id]
  (when (approved? recruiter-id) (email/send-email recruiter-id {} :onboarding-drip)))

(defn- code-verification-success
  "Updates user's email optin, sends welcome email and redirects to homepage"
  [{:keys [body]}]
  (let [recruiter-id (get-recruiter-id body)]
    (update-email-optin recruiter-id)
    (send-onboarding-emails recruiter-id))
  (auth-success body (rr/redirect "/")))

(defn- authenticate-via-code
  "Attempts to authenticate with code and redirects"
  [code]
  (try
    (when-not (auth/verified? code)
      (let [auth-response (auth/authenticate-via-code code)]
        (if (ok? auth-response)
          (code-verification-success auth-response)
          (auth-resp auth-response))))
    (catch Exception e nil)))

(defn verify-code
  "Verifies that code has not been used before and authenticates user"
  [code]
  (if-let [response (authenticate-via-code code)]
    response
    (rr/redirect "/#/login")))

(defn verification-code-valid?
  "Attempts to call auth/verified? with code, will get 500 from invalid code"
  [code]
  (try
    (auth/verified? code)
    "true"
    (catch Exception e "false")))
