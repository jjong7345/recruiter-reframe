(ns recruit-app.login.events
  (:require [re-frame.core :as rf]
            [recruit-app.db :as db]
            [ajax.core :as ajax]
            [clojure.string :as cs]
            [recruit-app.util.uri :as u]
            [recruit-app.util.events :as ev]
            [recruit-app.components.modal :as modal]))

(ev/reg-events "login" ["username" "error-msg" "show-onboarding-message?"
                        "show-change-password-errors?" "forgot-password-token"
                        "show-create-account?" "show-forgot-password?"
                        "show-forgot-password-errors?"])
(ev/reg-toggle-event "login" "show-change-password?")
(ev/reg-toggle-event "login" "sending-forgot-password?")
(ev/reg-toggle-event "login" "logging-in?")
(ev/reg-toggle-event "login" "creating-account?")

(defn password-change
  "Dispatches event to hide errors and sets password in db"
  [{:keys [db]} [_ new-val]]
  {:db       (assoc-in db [:login :password] new-val)
   :dispatch [:login/show-change-password-errors?-change false]})

(defn confirm-password-change
  "Dispatches event to hide errors and sets password in db"
  [{:keys [db]} [_ new-val]]
  {:db       (assoc-in db [:login :confirm-password] new-val)
   :dispatch [:login/show-change-password-errors?-change false]})

(defn clear-form
  "Dissoc login from db"
  [db _]
  (dissoc db :login))

(defn login-failure
  "Adds login error message"
  [_ _]
  {:dispatch-n [[:login/show-onboarding-message?-change false]
                [:login/toggle-logging-in?]
                [:login/error-msg-change "This combination doesn’t match anything in our records. Select “Forgot password” to create a new password."]]})

(defn login-success
  "Adds authenticated user profile to db"
  [{:keys [db]} [_ response]]
  (cond-> {:db       (assoc db :recruiter (:user response))
           :dispatch [:login/clear-form]}
          (some #(= (:active-panel db) %) [:login-panel :onboarding-panel]) (assoc :route "/")))

(defn login
  "Attempts to authenticate user"
  [{:keys [db]} _]
  {:dispatch-n [[:login/toggle-logging-in?]
                [:login/error-msg-change nil]]
   :http-xhrio {:method          :post
                :uri             (u/uri :login)
                :params          (update (:login db) :username cs/trim)
                :format          (ajax/json-request-format)
                :response-format (ajax/json-response-format {:keywords? true})
                :on-success      [:login/login-success]
                :on-failure      [:login/login-failure]}})

(defn logout-failure
  "Routes to homepage"
  [_ _]
  {:route "/"})

(defn logout-success
  "Routes to homepage and reset db to default"
  [_ _]
  {:db    db/default-db
   :route "/"})

(defn logout
  "Returns http-xhrio request to logout"
  [_ _]
  {:ra-http-xhrio {:method          :get
                :uri             (u/uri :logout)
                :format          (ajax/json-request-format)
                :response-format (ajax/raw-response-format)
                :on-success      [:login/logout-success]
                :on-failure      [:login/logout-failure]}})

(defn show-login-form
  "Hides both forgot-password and create-account views"
  [_ _]
  {:dispatch-n [[:login/show-forgot-password?-change false]
                [:login/show-create-account?-change false]
                [:set-page-head-title "Please sign in"]]})

(defn forgot-password-success
  "Toggles modal and clears form"
  [_ _]
  {:dispatch-n [[:login/username-change ""]
                [::modal/open-modal ::modal/forgot-password-success]
                [:login/toggle-sending-forgot-password?]]})

(defn forgot-password-failure
  "Displays alert to user"
  [_ _]
  {:dispatch-n [[:login/toggle-sending-forgot-password?]
                [:login/show-forgot-password-errors?-change true]]})

(defn send-forgot-password-email
  "Sends forgot password email to email provided"
  [{:keys [db]} _]
  {:dispatch-n [[:login/toggle-sending-forgot-password?]
                [:login/show-forgot-password-errors?-change false]]
   :http-xhrio {:method          :post
                :uri             (u/uri :forgot-password)
                :params          {:email (-> db :login :username)}
                :format          (ajax/json-request-format)
                :response-format (ajax/raw-response-format)
                :on-success      [:login/forgot-password-success]
                :on-failure      [:login/forgot-password-failure]}})

(defn change-password-failure
  "Displays alerts to user"
  [_ _]
  {:dispatch [:alerts/add-error "Unexpected Error Occurred. Please Try Again."]})

(defn change-password-success
  "Displays alert and dispatches login success"
  [_ [_ response]]
  {:dispatch-n [[:login/login-success response]
                [:alerts/add-success "Password Successfully Changed!"]]})

(defn change-password
  "Sends request to change password and authenticate user"
  [{:keys [db]} _]
  {:http-xhrio {:method          :put
                :uri             (u/uri :change-password)
                :params          {:password (-> db :login :password)
                                  :token    (-> db :login :forgot-password-token)}
                :format          (ajax/json-request-format)
                :response-format (ajax/json-response-format {:keywords? true})
                :on-success      [:login/change-password-success]
                :on-failure      [:login/change-password-failure]}})

(defn create-account
  "Toggles submitting boolean and dispatches onboarding event"
  [_ [_ email]]
  {:dispatch-n [[:login/toggle-creating-account?]
                [:marketinghome/onboarding email]]})

(defn create-account-click
  "Dispatches events to show create account view and hide all other login views"
  [_ _]
  {:dispatch-n [[:login/show-create-account?-change true]
                [:login/show-forgot-password?-change false]
                [:set-page-head-title "Sign up"]]})

(defn forgot-password-click
  "Dispatches events to show forgot password view and hide all other login views"
  [_ _]
  {:dispatch-n [[:login/show-forgot-password?-change true]
                [:login/show-create-account?-change false]
                [:set-page-head-title "Forgot password"]]})

(defn email-exists-reroute
  "Routes to login page with message that user has an account"
  [_ [_ email]]
  {:dispatch-n [[:login/clear-form]
                [:login/username-change email]
                [:login/show-onboarding-message?-change true]
                [:go-to-login]]})

(rf/reg-event-fx
  :login/password-change
  password-change)

(rf/reg-event-fx
  :login/confirm-password-change
  confirm-password-change)

(rf/reg-event-db
  :login/clear-form
  clear-form)

(rf/reg-event-fx
  :login/login-failure
  login-failure)

(rf/reg-event-fx
  :login/login-success
  login-success)

(rf/reg-event-fx
  :login/logout-failure
  logout-failure)

(rf/reg-event-fx
  :login/logout-success
  logout-success)

(rf/reg-event-fx
  :login/do-login
  login)

(rf/reg-event-fx
  :login/logout
  logout)

(rf/reg-event-fx
  :login/show-login-form
  show-login-form)

(rf/reg-event-fx
  :login/send-forgot-password-email
  send-forgot-password-email)

(rf/reg-event-fx
  :login/forgot-password-success
  forgot-password-success)

(rf/reg-event-fx
  :login/forgot-password-failure
  forgot-password-failure)

(rf/reg-event-fx
  :login/change-password
  change-password)

(rf/reg-event-fx
  :login/change-password-failure
  change-password-failure)

(rf/reg-event-fx
  :login/change-password-success
  change-password-success)

(rf/reg-event-fx
  :login/create-account
  create-account)

(rf/reg-event-fx
  :login/create-account-click
  create-account-click)

(rf/reg-event-fx
  :login/forgot-password-click
  forgot-password-click)

(rf/reg-event-fx
  :login/email-exists-reroute
  email-exists-reroute)
