(ns recruit-app.events
  (:require [re-frame.core :as rf]
            [re-pressed.core :as rp]
            [recruit-app.db :as db]
            [secretary.core :as sec]
            [ajax.core :as ajax]
            [cljs.reader :as edn]
            [recruit-app.util.uri :as u]
            [cljs.spec.alpha :as s]
            [recruit-app.config :as config]))

;_+_+_+_+_+_+_+_+_+_+_+_+_+_+_+
;_+
;_+  Interceptor Event Handlers
;_+
;_+_+_+_+_+_+_+_+_+_+_+_+_+_+_+

(defn validate-db
  "Validates db. Throws error if invalid"
  [db]
  (when-not (s/valid? ::db/db db)
    (throw (str "db spec validation failed: " (s/explain-str ::db/db db)))))

(def validate-db-interceptor (rf/after validate-db))
(def interceptors (when config/debug? [validate-db-interceptor]))

(defn reg-event-db
  "Registers an event-db in reframe with interceptors"
  [id db-handler]
  (rf/reg-event-db id interceptors db-handler))

(defn reg-event-fx
  "Registers an event-fx in reframe with interceptors"
  [id fx-handler]
  (rf/reg-event-fx id interceptors fx-handler))

;_+_+_+_+_+_+_+_+_+_+_+_+_+_+_+
;_+
;_+  Top Level Event Handlers
;_+
;_+_+_+_+_+_+_+_+_+_+_+_+_+_+_+

(defn- merged-with-default
  "Returns given db merged into default db"
  [db]
  (merge db/default-db db))

(defn set-default-db
  "Returns db merged into default db"
  [db _]
  (merged-with-default db))

(defn merge-with-default-db
  "Associates user into default db when set in response"
  [db [_ resp]]
  (if-let [user (:user resp)]
    (-> db merged-with-default (assoc :recruiter user))
    (merged-with-default db)))

(defn initialize-db-success
  "Toggles is-fetching-user? and associates user to db"
  [{:keys [db]} [_ resp]]
  {:db       (assoc db :is-fetching-user? false)
   :dispatch [:merge-with-default-db resp]})

(defn initialize-db-failure
  "Toggles is-fetching-user? and resets db to default"
  [{:keys [db]} [_ resp]]
  {:db       (assoc db :is-fetching-user? false)
   :dispatch [:set-default-db resp]})

(defn initialize-db
  "Calls API to current user info"
  [{:keys [db]} _]
  {:db           (assoc db :is-fetching-user? true)
   :query-params true
   :http-xhrio   {:method          :get
                  :uri             (u/uri :user)
                  :format          (ajax/json-request-format)
                  :response-format (ajax/json-response-format {:keywords? true})
                  :on-success      [:initialize-db-success]
                  :on-failure      [:initialize-db-failure]}})

(defn set-active-panel
  "Associates given panel to db"
  [{:keys [db]} [_ active-panel]]
  (cond-> {:db (assoc db :active-panel active-panel)}
          (not= active-panel :candidate-profile-panel)
          (assoc :dispatch [::rp/set-keydown-rules {:event-keys []}])))

(defn go-to-shopify
  "Routes to shopify page"
  [_ _]
  {:external-route "/shopify"})

(defn go-to-route
  "Returns fx to go to given route"
  [_ [_ route]]
  {:route route})

(defn scroll-top
  "Scrolls window to top of page"
  [_ _]
  {:scroll {:x 0 :y 0}})

(defn set-variation
  "Associates given variation to db"
  [db [_ new-var]]
  (assoc db :variation new-var))

(defn go-to-login
  "Routes to login page based on RL config"
  [_ _]
  {:route "/login"})

(defn set-page-title
  "Event that calls page-title fx with new title"
  [_ [_ new-title]]
  {:page-title new-title})

(defn set-page-head-title
  "Event that calls set-page-title fx with new head title concatenated with fixed tail title"
  [_ [_ new-head-title]]
  {:dispatch [:set-page-title (str new-head-title " | Job Posting and Candidate Search")]})

(defn go-to-manage-jobs
  "Routes to manage jobs page based on RL config"
  [_ _]
  {:route "/jobs"})

(defn go-to-pricing
  "Routes to pricing page based on RL config"
  [_ [_ new-window?]]
  (let [route "/pricing"]
    (if new-window?
      {:new-window (str "/#" route)}
      {:route route})))

(defn go-to-home
  "Routes to home page"
  [_ _]
  {:route "/"})

(defn redirect-to-login
  "Routes to login page and clear db"
  [_ _]
  {:db    db/default-db
   :route "/login"})

(defn run-fx [_ [_ fx]]
  fx)

(defn handle-on-failure
  "Handle ajax failure with global event"
  [_ [_ on-failure retry-fx response]]
  (let [status (:status response)
        unauthorized (= status 401)]
    (cond
      (and unauthorized retry-fx) {:dispatch-later [{:dispatch [:run-fx retry-fx] :ms 500}]}
      unauthorized {:dispatch [:redirect-to-login]}
      :else {:dispatch (conj on-failure response)})))

(defn with-retries
  "Adds given number of retries to fx"
  [fx retries]
  (let [no-retry (update-in fx [:http-xhrio :on-failure] conj nil)]
    (reduce
      (fn [retry-fx _]
        (update-in fx [:http-xhrio :on-failure] conj retry-fx))
      no-retry
      (take retries (range)))))

(defn trigger-http-xhrio
  "http-xhrio event with custom on-failure event
  Updating the fx to also have a request to run a retry
  The retry will not include another retry request"
  [_ [_ options]]
  (with-retries
    {:http-xhrio
     (update options :on-failure (partial vector :handle-on-failure))}
    4))

(reg-event-db
  :set-default-db
  set-default-db)

(reg-event-db
  :merge-with-default-db
  merge-with-default-db)

(reg-event-fx
  :initialize-db-success
  initialize-db-success)

(reg-event-fx
  :initialize-db-failure
  initialize-db-failure)

(reg-event-fx
  :initialize-db
  initialize-db)

(reg-event-fx
  :set-active-panel
  set-active-panel)

(reg-event-fx
  :go-to-shopify
  go-to-shopify)

(reg-event-fx
  :go-to-route
  go-to-route)

(reg-event-fx
  :scroll-top
  scroll-top)

(reg-event-db
  :set-variation
  set-variation)

(reg-event-fx
  :http-no-on-success
  #())

(reg-event-fx
  :http-no-on-failure
  #())

(reg-event-fx
  :set-query-params
  (constantly {:query-params true}))

(reg-event-fx
  :go-to-login
  go-to-login)

(reg-event-fx
  :set-page-title
  set-page-title)

(reg-event-fx
  :set-page-head-title
  set-page-head-title)

(reg-event-fx
  :go-to-manage-jobs
  go-to-manage-jobs)

(reg-event-fx
  :go-to-home
  go-to-home)

(reg-event-fx
  :go-to-pricing
  go-to-pricing)

(reg-event-fx
  :trigger-http-xhrio
  trigger-http-xhrio)

(reg-event-fx
  :handle-on-failure
  handle-on-failure)

(reg-event-fx
  :redirect-to-login
  redirect-to-login)

(reg-event-fx
  :run-fx
  run-fx)

;_+_+_+_+_+_+_+_+_+_+_+_+_+_+_+
;_+
;_+  Custom effects
;_+
;_+_+_+_+_+_+_+_+_+_+_+_+_+_+_+

(defn reload
  "Reload the current browser page"
  [_]
  (js/window.location.reload true))

(defn route
  "Change the current page to the route url"
  [route]
  (let [prefix (sec/get-config :prefix)
        query-string (aget js/window "location" "search")]
    (set! (.-location js/document) (str prefix route query-string))))

(defn external-route [route]
  (let [prefix (sec/get-config :prefix)]
    (set! (.-location js/document) (str route))))

(defn new-window
  "Opens given route in new browser window"
  [route]
  ((.-open js/window) route))

(defn scroll
  "Scrolls window to given coordinates"
  [{:keys [x y]}]
  (.scrollTo js/window x y))

(defn set-local-store-value
  "Sets given key in local store to given value"
  [[ls-key ls-value]]
  (.setItem js/localStorage ls-key (str ls-value)))

(defn copy-local-store
  "Associates local store values into db"
  [[ls-key save-loc]]
  (reset!
    re-frame.db/app-db
    (assoc-in
      @re-frame.db/app-db
      save-loc
      (edn/read-string (.getItem js/localStorage ls-key)))))

(defn new-window-popup
  "Opens given route in new browser popup window"
  [[route target option]]
  ((.-open js/window) route target option))

(defn- query-string
  "Returns query string from URL"
  []
  (-> js/window
      (aget "location" "search")
      (subs 1)))

(defn- add-keyed-param
  "Destructures regex match and sets value to given key in params map"
  [params [_ key val]]
  (assoc params (keyword key) val))

(defn query-params
  "Sets all query params to db"
  [_]
  (reset!
    re-frame.db/app-db
    (assoc
      @re-frame.db/app-db
      :query-params
      (->> (query-string)
           (re-seq #"([^&=]+)=?([^&]*)")
           (reduce add-keyed-param {})))))

(defn page-title
  "Sets page title to content" [content]
  (aset js/document "title" content))

(defn ra-http-xhrio
  "Trigger http-xhrio"
  [options]
  (rf/dispatch [:trigger-http-xhrio options]))

(rf/reg-fx
  :reload
  reload)

(rf/reg-fx
  :route
  route)

(rf/reg-fx
  :external-route
  external-route)

(rf/reg-fx
  :new-window
  new-window)

(rf/reg-fx
  :new-window-popup
  new-window-popup)

(rf/reg-fx
  :scroll
  scroll)

(rf/reg-fx
  :ls
  set-local-store-value)

(rf/reg-fx
  :lls
  copy-local-store)

(rf/reg-fx
  :query-params
  query-params)

(rf/reg-fx
  :page-title
  page-title)

(rf/reg-fx
  :ra-http-xhrio
  ra-http-xhrio)
