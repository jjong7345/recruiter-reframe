(ns recruit-app.events
  (:require [re-frame.core :as rf]
            [re-pressed.core :as rp]
            [recruit-app.db :as db]
            [secretary.core :as sec]
            [ajax.core :as ajax]
            [cljs.reader :as edn]
            [recruit-app.util.uri :as u]))

(defn- merged-with-default
  "Returns given db merged into default db"
  [db]
  (merge db/default-db db))

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

(rf/reg-event-db
  :set-default-db
  (fn [db _]
    (merged-with-default db)))

(rf/reg-event-db
  :merge-with-default-db
  (fn [db [_ resp]]
    (if-let [user (:user resp)]
      (-> db merged-with-default (assoc :recruiter user))
      (merged-with-default db))))

(rf/reg-event-fx
  :initialize-db-success
  (fn [{:keys [db]} [_ resp]]
    {:db       (assoc db :is-fetching-user? false)
     :dispatch [:merge-with-default-db resp]}))

(rf/reg-event-fx
  :initialize-db-failure
  (fn [{:keys [db]} [_ resp]]
    {:db       (assoc db :is-fetching-user? false)
     :dispatch [:set-default-db resp]}))

(rf/reg-event-fx
  :initialize-db
  (fn [{:keys [db]} _]
    {:db           (assoc db :is-fetching-user? true)
     :query-params true
     :http-xhrio   {:method          :get
                    :uri             (u/uri :user)
                    :format          (ajax/json-request-format)
                    :response-format (ajax/json-response-format {:keywords? true})
                    :on-success      [:initialize-db-success]
                    :on-failure      [:initialize-db-failure]}}))

(rf/reg-event-fx
  :set-active-panel
  (fn [{:keys [db]} [_ active-panel]]
    (cond-> {:db (assoc db :active-panel active-panel)}
            (not= active-panel :candidate-profile-panel)
            (assoc :dispatch [::rp/set-keydown-rules {:event-keys []}]))))

(rf/reg-event-fx
  :go-to-shopify
  (fn [_ _]
    {:external-route "/shopify"}))

(rf/reg-event-fx
  :go-to-route
  (fn [_ [_ route]]
    {:route route}))

(rf/reg-event-fx
  :scroll-top
  (fn [_ _]
    {:scroll {:x 0 :y 0}}))

(rf/reg-event-db
  :set-variation
  (fn [db [_ new-var]]
    (assoc db :variation new-var)))

(rf/reg-event-fx
  :http-no-on-success
  #())

(rf/reg-event-fx
  :http-no-on-failure
  #())

(rf/reg-event-fx
  :set-query-params
  (constantly {:query-params true}))

(rf/reg-event-fx
  :go-to-login
  go-to-login)

(rf/reg-event-fx
  :set-page-title
  set-page-title)

(rf/reg-event-fx
  :set-page-head-title
  set-page-head-title)

(rf/reg-event-fx
  :go-to-manage-jobs
  go-to-manage-jobs)

(rf/reg-event-fx
  :go-to-home
  go-to-home)

(rf/reg-event-fx
  :go-to-pricing
  go-to-pricing)

(rf/reg-event-fx
  :trigger-http-xhrio
  trigger-http-xhrio)

(rf/reg-event-fx
  :handle-on-failure
  handle-on-failure)

(rf/reg-event-fx
  :redirect-to-login
  redirect-to-login)

(rf/reg-event-fx
 :run-fx
 run-fx)
;_+_+_+_+_+_+_+_+_+_+_+_+_+_+_+
;_+
;_+  Custom effects
;_+
;_+_+_+_+_+_+_+_+_+_+_+_+_+_+_+

(defn new-window
  "Opens given route in new browser window"
  [route]
  ((.-open js/window) route))

(defn new-window-popup
  "Opens given route in new browser popup window"
  [[route target option]]
  ((.-open js/window) route target option))

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
  :scroll
  (fn [{:keys [x y]}]
    (.scrollTo js/window x y)))

(rf/reg-fx
  :ls
  (fn [[ls-key ls-value]]
    (.setItem js/localStorage ls-key (str ls-value))))

(rf/reg-fx
  :lls
  (fn [[ls-key save-loc]]
    (reset! re-frame.db/app-db (assoc-in @re-frame.db/app-db save-loc (edn/read-string (.getItem js/localStorage ls-key))))))

(rf/reg-fx
  :query-params
  query-params)

(rf/reg-fx
  :page-title
  page-title)

(rf/reg-fx
  :new-window-popup
  new-window-popup)

(rf/reg-fx
  :ra-http-xhrio
  ra-http-xhrio)
