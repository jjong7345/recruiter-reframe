(ns recruit_app.account.events
  (:require [re-frame.core :as rf]
            [recruit-app.db :as db]
            [recruit-app.events :as events]
            [recruit-app.util.events :as ev]
            [ajax.core :as ajax]
            [cljs.reader :as edn]
            [recruit-app.util.uri :as u]
            [recruit-app.util.account :as au]
            [recruit-app.util.img :as img]))

(ev/reg-events "account" ["fname" "lname" "job-title" "function"
                          "company" "role" "company-site" "linkedin"
                          "facebook" "twitter" "blog" "bio"
                          "email" "phone" "ext" "street"
                          "city" "state" "zip" "country" "profile-img-url"
                          "ats" "job-board-token" "ats-name"
                          "secondary-api-key" "current-password"
                          "new-password" "confirm-password" "newsletter"
                          "special-offers" "connection-req" "feedback"
                          "suggested-cand" "search-based-cand" "show-errors?"
                          "lever-user-selected" "api-key"])

(ev/reg-toggle-event "account" "is-uploading?")
(ev/reg-toggle-event "account" "show-sample-bio?")
(ev/reg-toggle-event "account" "newsletter")
(ev/reg-toggle-event "account" "special-offers")
(ev/reg-toggle-event "account" "connection-req")
(ev/reg-toggle-event "account" "feedback")
(ev/reg-toggle-event "account" "suggested-cand")
(ev/reg-toggle-event "account" "search-based-cand")

(defn load-view
  "Dispatches events to load account data"
  [_ _]
  {:dispatch     [:account/init-account-data]
   :ga/page-view ["/account" {}]})

(defn set-active-tab
  "Sets active tab in db"
  [db [_ tab]]
  (assoc-in db [:account :active-tab] tab))

(defn set-default-image
  "Assocs default img url to profile img in db"
  [_ _]
  {:dispatch [:account/profile-img-url-change (img/url :default-profile-img)]})

(defn on-get-ats-settings
  [{:keys [db]} [_ response]]
  (let [resp (edn/read-string (str response))]
    {:db         (assoc-in db [:account :ats-settings] resp)
     :dispatch-n [[:account/api-key-change (:api-key resp)]
                  [:account/ats-dropdown-change {:id (:ats-provider resp)}]
                  [:account/lever-user-selected-change (:user-id resp)]
                  [:account/secondary-api-key-change (:secondary-api-key resp)]
                  [:account/job-board-token-change (:subdomain resp)]]}))

(defn init-account-data
  "Get profile data from :recruiter db and get ATS settings"
  [{:keys [db]} _]
  {:dispatch-n    [[:account/fname-change (get-in db [:recruiter :firstname])]
                   [:account/lname-change (get-in db [:recruiter :lastname])]
                   [:account/job-title-change (get-in db [:recruiter :title])]
                   [:account/function-change (get-in db [:recruiter :job_function :id])]
                   [:account/company-change (get-in db [:recruiter :company :name])]
                   [:account/role-change (get-in db [:recruiter :recruiter_guest_role :recruiter_guest_role_id])]
                   [:account/company-site-change (get-in db [:recruiter :recruiter_website_url])]
                   [:account/linkedin-change (get-in db [:recruiter :recruiter_linkedin_url])]
                   [:account/facebook-change (get-in db [:recruiter :recruiter_facebook_url])]
                   [:account/twitter-change (get-in db [:recruiter :recruiter_twitter_user_name])]
                   [:account/blog-change (get-in db [:recruiter :recruiter_blog_url])]
                   [:account/bio-change (get-in db [:recruiter :specialty])]
                   [:account/email-change (get-in db [:recruiter :email])]
                   [:account/phone-change (au/get-from-phone-number (get-in db [:recruiter :telephone]) "phone")]
                   [:account/ext-change (au/get-from-phone-number (get-in db [:recruiter :telephone]) "extension")]
                   [:account/street-change (get-in db [:recruiter :street])]
                   [:account/city-change (get-in db [:recruiter :city])]
                   [:account/state-change (get-in db [:recruiter :state_province])]
                   [:account/zip-change (get-in db [:recruiter :postal_code])]
                   [:account/country-change (get-in db [:recruiter :country])]
                   [:account/newsletter-change (au/get-contact-preference "newsletter" (get-in db [:recruiter :contact_preferences]))]
                   [:account/special-offers-change (au/get-contact-preference "special-offers" (get-in db [:recruiter :contact_preferences]))]
                   [:account/connection-req-change (au/get-contact-preference "connection-req" (get-in db [:recruiter :contact_preferences]))]
                   [:account/feedback-change (au/get-contact-preference "feedback" (get-in db [:recruiter :contact_preferences]))]
                   [:account/suggested-cand-change (au/get-contact-preference "suggested-cand" (get-in db [:recruiter :contact_preferences]))]
                   [:account/search-based-cand-change (au/get-contact-preference "search-based-cand" (get-in db [:recruiter :contact_preferences]))]]
   :ra-http-xhrio {:method          :get
                   :uri             (u/uri :ats-settings)
                   :timeout         5000
                   :format          (ajax/json-request-format)
                   :response-format (ajax/json-response-format {:keywords? true})
                   :on-success      [:account/on-get-ats-settings]
                   :on-failure      [:account/on-get-ats-settings]}})

(defn on-profile-saved
  "Event on profile saved"
  [_ _]
  {:dispatch-n [[:account/show-errors?-change false] [:account/clear-passwords] [:alerts/add-success "Your profile was successfully updated. Thanks!"]]})

(defn on-password-change-fail
  "Event on password change fail"
  [_ _]
  {:dispatch-n [[:alerts/add-error "Could not change your password, make sure you are entering the correct password."]
                [:account/show-errors?-change true]]})

(defn profile-update
  "Update Recruiter's profile information"
  [{:keys [db]}]
  {:ra-http-xhrio {:method          :put
                   :uri             (u/uri :update-profile)
                   :timeout         5000
                   :params          (au/profile-request (:account db))
                   :format          (ajax/json-request-format)
                   :response-format (ajax/json-response-format {:keywords? true})
                   :on-success      [:account/on-profile-saved]
                   :on-failure      [:account/on-profile-saved]}})

(defn profile-update-fail
  "Dispatches events when submitting with errors"
  [_ _]
  {:dispatch-n [[:account/show-errors?-change true]
                [:alerts/add-error "Could not update your profile, make sure you are entering the correct information."]]})

(defn password-update
  "Update Recruiter's password"
  [{:keys [db]}]
  (let [old-password (get-in db [:account :current-password])
        new-password (get-in db [:account :new-password])]
    {:ra-http-xhrio {:method          :put
                     :uri             (u/uri :account-change-password)
                     :timeout         5000
                     :params          {:old-password old-password
                                       :new-password new-password}
                     :format          (ajax/json-request-format)
                     :response-format (ajax/json-response-format {:keywords? true})
                     :on-success      [:account/on-profile-saved]
                     :on-failure      [:account/on-password-change-fail]}}))

(defn communication-preference-update
  "Update Recruiter's Communication Preferences"
  [{:keys [db]} [_ key-name]]
  {:dispatch      [(keyword "account" (str "toggle-" key-name))]
   :ra-http-xhrio {:method          :put
                   :uri             (u/uri :update-profile)
                   :timeout         5000
                   :params          (au/contact_preferences-request
                                      key-name
                                      (-> db
                                          (get-in [:account (keyword key-name)])
                                          not))
                   :format          (ajax/json-request-format)
                   :response-format (ajax/json-response-format {:keywords? true})
                   :on-success      [:account/on-profile-saved]
                   :on-failure      [:account/on-profile-saved]}})

(defn goto-manage-saved-searches
  "Route to manage saved search page"
  [_ _]
  {:route "saved-searches"})

(defn on-lever-fetched-success
  "Store fetched Lever users data into db"
  [db [_ response]]
  (let [resp (edn/read-string (str response))]
    (assoc-in db [:account :lever-users] resp)))

(defn on-lever-fetched-fail
  "Alert on Lever data fetch fail"
  [_ _]
  {:dispatch [:alerts/add-error "We could not fetch your list of users, please make sure your API key is correct"]})

(defn fetch-lever-data
  "Fetch Lever data from Lever api"
  [{:keys [db]}]
  (let [api-key (get-in db [:account :api-key])]
    {:ra-http-xhrio {:method          :post
                     :uri             (u/uri :get-users-lever)
                     :timeout         5000
                     :params          {:api-key api-key}
                     :format          (ajax/json-request-format)
                     :response-format (ajax/json-response-format {:keywords? true})
                     :on-success      [:account/on-lever-fetched-success]
                     :on-failure      [:account/on-lever-fetched-fail]}}))

(defn fetch-ats-data
  "Fetch ats data on dropdown change"
  [{:keys [db]}]
  (let [ats (get-in db [:account :ats])]
    (cond
      (= "lever" (:id ats)) {:dispatch [:account/fetch-lever-data]}
      :else nil)))

(defn ats-dropdown-change
  "Events on ats dropdown change"
  [_ [_ selected]]
  {:dispatch-n [[:account/ats-change selected]
                [:account/fetch-ats-data]]})

(defn ats-update
  "Update ats settings"
  [{:keys [db]}]
  (let [account (db :account)]
    {:ra-http-xhrio {:method          :post
                     :uri             (u/uri :ats-settings)
                     :timeout         5000
                     :params          (au/ats-update-request account)
                     :format          (ajax/json-request-format)
                     :response-format (ajax/json-response-format {:keywords? true})
                     :on-success      [:account/ats-update-complete]
                     :on-failure      [:account/ats-update-complete]}}))

(defn ats-update-complete
  "Event on ats setting update is completed"
  [{:keys [db]} [_ response]]
  (let [resp (edn/read-string (str response))
        ats-provider (get-in db [:account :ats :id])
        is-lever? (= ats-provider "lever")
        is-greenhouse? (= ats-provider "greenhouse")
        events [[:alerts/add-success "Your ATS settings were successfully updated. Thanks!"]]]
    (when (:recruiter-id resp)
      {:dispatch-n (cond-> events
                           is-greenhouse? (conj [:account/gh-redirect])
                           is-lever? (conj [:account/fetch-ats-data]))})))

(defn profile-preview
  "Pop up window profile preview window"
  [{:keys [db]}]
  (let [recruiter-id (get-in db [:recruiter :recruiter-id])
        preview-base-url (u/uri :recruiter-preview-base)]
    {:new-window-popup [(str preview-base-url recruiter-id) "_blank" "height=700,width=820,status=yes,toolbar=no,menubar=no,location=no,scrollbars=yes"]}))

(defn clear-passwords
  "Clear password fields"
  [db _]
  (update db :account dissoc :current-password :new-password :confirm-password))

(defn go-to-greenhouse
  "Go to Greenhouse auth page"
  [_ [_ response]]
  {:external-route response})

(defn get-greenhouse-url
  "Get Greenhouse auth page url"
  [_ _]
  {:ra-http-xhrio {:method          :get
                   :uri             (u/uri :greenhouse-auth-url)
                   :timeout         5000
                   :format          (ajax/json-request-format)
                   :response-format (ajax/raw-response-format)
                   :on-success      [:account/go-to-greenhouse]
                   :on-failure      [:account/go-to-greenhouse]}})

(defn gh-redirect
  "Events for greenhouse redirect"
  [_ [_ events]]
  (do
    (.setTimeout js/window #(rf/dispatch [:account/get-greenhouse-url]) 1500)
    {:dispatch-n events}))

(events/reg-event-db
  :account/set-active-tab
  set-active-tab)

(events/reg-event-fx
  :account/set-default-image
  set-default-image)

(events/reg-event-fx
  :account/init-account-data
  init-account-data)

(events/reg-event-fx
  :account/profile-update
  profile-update)

(events/reg-event-fx
  :account/profile-update-fail
  profile-update-fail)

(events/reg-event-fx
  :account/password-update
  password-update)

(events/reg-event-fx
  :account/communication-preference-update
  communication-preference-update)

(events/reg-event-fx
  :account/goto-manage-saved-searches
  goto-manage-saved-searches)

(events/reg-event-fx
  :account/on-get-ats-settings
  on-get-ats-settings)

(events/reg-event-fx
  :account/ats-dropdown-change
  ats-dropdown-change)

(events/reg-event-fx
  :account/fetch-ats-data
  fetch-ats-data)

(events/reg-event-fx
  :account/fetch-lever-data
  fetch-lever-data)

(events/reg-event-db
  :account/on-lever-fetched-success
  on-lever-fetched-success)

(events/reg-event-fx
  :account/on-lever-fetched-fail
  on-lever-fetched-fail)

(events/reg-event-fx
  :account/ats-update
  ats-update)

(events/reg-event-fx
  :account/ats-update-complete
  ats-update-complete)

(events/reg-event-fx
  :account/on-profile-saved
  on-profile-saved)

(events/reg-event-fx
  :account/profile-preview
  profile-preview)

(events/reg-event-fx
  :account/on-password-change-fail
  on-password-change-fail)

(events/reg-event-db
  :account/clear-passwords
  clear-passwords)

(events/reg-event-fx
  :account/go-to-greenhouse
  go-to-greenhouse)

(events/reg-event-fx
  :account/get-greenhouse-url
  get-greenhouse-url)

(events/reg-event-fx
  :account/gh-redirect
  gh-redirect)

(events/reg-event-fx
  :account/load-view
  load-view)
