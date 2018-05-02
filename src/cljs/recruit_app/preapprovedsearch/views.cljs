(ns recruit-app.preapprovedsearch.views
  (:require [re-frame.core :as rf]
            [re-com.core :as rc
             :refer-macros [handler-fn]]
            [reagent.core :as reagent]
            [re-com.util :refer [enumerate]]
            [reagent.core :as r]
            [recruit-app.alerts.views :as alerts]
            [recruit-app.search.views :as sv]))

(defn alert-bar-first-time
  []
  (let [recruiter-name (rf/subscribe [:recruiter/firstname])]
    (fn []
      [rc/v-box
       :class "search pre-approved first-time"
       :children [[alerts/alert [:div {:class "alert-message"} "Welcome to Ladders, " [:span @recruiter-name]
                                 [:p [:span "Start searching our database now,"] " while your account is pending approval"]
                                 [:div.em "Sit tight! Profiles are usually approved within a few hours, Monday through Friday, 9AM to 6PM EST."]] "alert-bar"]]])))
