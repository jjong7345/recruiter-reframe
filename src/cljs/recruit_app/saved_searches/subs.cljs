(ns recruit-app.saved-searches.subs
  (:require-macros [reagent.ratom :refer [reaction]])
  (:require [re-frame.core :as rf]
            [cljs.spec.alpha :as s]
            [recruit-app.util.subscription :as subs]
            [recruit-app.saved-searches.views :as view]
            [recruit-app.modals.saved-search.db :as specs]
            [recruit-app.util.search :as su]
            [recruit-app.search.criteria :as criteria]))

(subs/reg-subs "saved-searches" [["editing-saved-search" nil]
                                 ["show-errors?" false]
                                 ["fetching-all?" false]
                                 ["saved-searches" {}]])
(subs/reg-subs "saved-searches" "stats" [["emailed-count" 0]])
(subs/reg-subs "saved-searches" "editing-saved-search" [["search-name" ""] ["search-id" nil]])
(subs/reg-subs "saved-searches" "editing-saved-search" "reporting" "email" [["frequency-type" 0] ["interval" 1]])

(defn show-email-frequency?
  "Show email frequency dropdown when edit saved search modal is opened from saved searches page"
  [active-panel _]
  (not= active-panel :search-panel))

(defn show-email-interval?
  "Show email interval dropdown when edit saved search modal is opened from saved searches
  page and email frequency is 2(once a week)"
  [email-frequency _]
  (= email-frequency 2))

(defn updated-saved-search-to-edit
  "Update saved search request to send when editing from new title, email frequency or email interval
  If email interval is nil, make it default to 1. If email interval is more than 7, make it default to 1"
  [saved-search _]
  (let [{:keys [frequency-type interval last-send-time]
         :or   {frequency-type 0}} (-> saved-search :reporting :email)
        email-interval (cond
                         (and (= frequency-type 2) (nil? interval)) 1
                         (and (= frequency-type 2) (> interval 7)) 1
                         (= interval 0) nil
                         :else interval)]
    (-> saved-search
        (assoc-in [:reporting :email] {:frequency-type frequency-type
                                       :interval       email-interval
                                       :last-send-time last-send-time})
        (update :search-parameters (partial merge criteria/parameter-defaults)))))

(defn can-show-frequency-dropdown?
  "Check whether to allow the user to change email-frequency of a given search.
  If existing search is already sending emails, then frequency dropdown should be enabled.
  If existing search doesn't have emails turned on, check if there are < 10 emailed searches, and then show the dropdown
  (which would allow for the 10th to be added)
  Else, if (< emailed-count 10) is false, then don't show the frequency dropdown for this saved search."
  [[emailed-count saved-search-id saved-searches] _]
  (let [existing-email-frequency (get-in saved-searches [saved-search-id :reporting :email :frequency-type])]
    (or (> existing-email-frequency 0) (< emailed-count 10))))

(defn valid-name?
  "Check if saved search name is not empty when editing"
  [saved-search-name _]
  (s/valid? ::specs/name saved-search-name))

(defn display-no-saved-searches?
  "Return true if saved searches data is nil or empty and saved search data is not in the middle of fetching process"
  [saved-searches-data _]
  (empty? saved-searches-data))

(defn saved-searches-vector
  "Returns vals from saved searches data"
  [saved-searches]
  (if-not (empty? saved-searches)
    (vals saved-searches)
    []))

(defn stats
  "Returns stats map given vector of all saved searches"
  [saved-searches]
  {:total-count   (count saved-searches)
   :emailed-count (->> (map (comp :frequency-type :email :reporting) saved-searches)
                       (filter (partial < 0))
                       count)})

(rf/reg-sub
  :saved-searches/show-email-frequency?
  :<- [:active-panel]
  show-email-frequency?)

(rf/reg-sub
  :saved-searches/show-email-interval?
  :<- [:saved-searches/editing-saved-search-reporting-email-frequency-type]
  show-email-interval?)

(rf/reg-sub
  :saved-searches/can-show-frequency-dropdown?
  :<- [:saved-searches/stats-emailed-count]
  :<- [:saved-searches/editing-saved-search-search-id]
  :<- [:saved-searches/saved-searches]
  can-show-frequency-dropdown?)

(rf/reg-sub
  :saved-searches/valid-name?
  :<- [:saved-searches/editing-saved-search-search-name]
  valid-name?)

(rf/reg-sub
  :saved-searches/display-no-saved-searches?
  :<- [:saved-searches/saved-searches]
  display-no-saved-searches?)

(rf/reg-sub
  :saved-searches/saved-searches-vector
  :<- [:saved-searches/saved-searches]
  saved-searches-vector)

(rf/reg-sub
  :saved-searches/updated-saved-search-to-edit
  :<- [:saved-searches/editing-saved-search]
  updated-saved-search-to-edit)

(rf/reg-sub
  :saved-searches/stats
  :<- [:saved-searches/saved-searches-vector]
  stats)
