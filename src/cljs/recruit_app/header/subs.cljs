(ns recruit-app.header.subs
  (:require [re-frame.core :as rf]
            [recruit-app.util.subscription :as sub]))

(sub/reg-subs "header" [["show-header-dropdown?" false]])

(defn show-sign-in?
  "Show sign in button when not on login page"
  [[panel verified? is-fetching-user?] _]
  (and (not= panel :login-panel) verified? (not is-fetching-user?)))

(defn show-logged-in-header?
  "Show logged in header if user is authenticated and email has been verified"
  [[authenticated? verified?] _]
  (and authenticated? verified?))

(rf/reg-sub
  :header/show-sign-in?
  :<- [:active-panel]
  :<- [:recruiter/email_optin_accepted]
  :<- [:is-fetching-user?]
  show-sign-in?)

(rf/reg-sub
  :header/show-logged-in-header?
  :<- [:recruiter/is-authenticated?]
  :<- [:recruiter/email_optin_accepted]
  show-logged-in-header?)


