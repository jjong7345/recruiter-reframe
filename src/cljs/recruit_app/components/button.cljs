(ns recruit-app.components.button
  (:require [recruit-app.components.util :as util]
            [re-com.core :as rc]
            [recruit-app.styles :as styles]
            [recruit-app.components.loading :as loading]
            [recruit-app.components.hyperlink :as link]
            [stylefy.core :refer [use-style]]))

(defn primary-button
  "Applies styles for primary button to re-com component"
  [& {:keys [submitting?] :as params}]
  (if submitting?
    [:div
     (use-style styles/primary-button)
     [loading/primary-button-loader]]
    [util/recom-component-with-styles
     rc/button
     (mapcat identity (dissoc params :submitting?))
     styles/primary-button]))

(defn secondary-button
  "Applies secondary button styling to re-com button"
  [& params]
  [util/recom-component-with-styles
   rc/button
   params
   styles/secondary-button])

(defn primary-transaction-button
  "Applies styles for primary transaction button to re-com component"
  [& params]
  [util/recom-component-with-styles
   rc/button
   params
   styles/primary-transaction-button])

(defn primary-transaction-button-large
  "Applies styles for primary transaction button to re-com component"
  [& params]
  [util/recom-component-with-styles
   rc/button
   params
   styles/primary-transaction-button-large])

(defn primary-dashboard-button
  "Applies styles for primary dashboard button to re-com component"
  [& params]
  [util/recom-component-with-styles
   rc/button
   params
   styles/primary-dashboard-button])

(defn secondary-dashboard-button
  "Applies styles for secondary dashboard button to re-com component"
  [& params]
  [util/recom-component-with-styles
   rc/button
   params
   styles/secondary-dashboard-button])

(defn filter-button
  "Renders a tab-like filter button, either selected/unselected"
  [& {:keys [label on-click selected?]}]
  (if selected?
    [util/recom-component-with-styles
     rc/button
     [:label label
      :on-click on-click]
     styles/filter-button]
    [link/hyperlink
     :label label
     :on-click on-click]))

(defn job-promotion-btn
  "Renders an orange button related to job promotions"
  [& params]
  [util/recom-component-with-styles
   rc/button
   params
   styles/job-promotion-btn])

(defn primary-button-href
  "Renders a button link with href"
  [& params]
  [util/recom-component-with-styles
   rc/hyperlink-href
   params
   styles/primary-button])
