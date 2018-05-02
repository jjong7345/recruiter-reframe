(ns recruit-app.components.hyperlink
  (:require [re-com.core :as rc]
            [recruit-app.styles :as styles]
            [stylefy.core :refer [use-style]]
            [recruit-app.components.util :as util]))

(defn hyperlink
  "Renders re-com hyperlink with proper styles"
  [& params]
  [util/recom-component-with-styles rc/hyperlink params styles/hyperlink])

(defn hyperlink-small
  "Renders re-com hyperlink with proper styles"
  [& params]
  [util/recom-component-with-styles rc/hyperlink params styles/hyperlink-12])

(defn hyperlink-medium
  "Renders re-com hyperlink with proper styles"
  [& params]
  [util/recom-component-with-styles rc/hyperlink params styles/hyperlink-14])

(defn hyperlink-href
  "Renders re-com hyperlink-href with proper styles"
  [& params]
  [util/recom-component-with-styles rc/hyperlink-href params styles/hyperlink])

(defn hyperlink-href-small
  "Renders re-com hyperlink-href with proper styles"
  [& params]
  [util/recom-component-with-styles rc/hyperlink-href params styles/hyperlink-12])

(defn hyperlink-href-medium
  "Renders re-com hyperlink-href with proper styles"
  [& {:keys [label href]}]
  [:a
   (merge
     {:href href}
     (use-style styles/hyperlink-14))
   label])

(defn table-cell-hyperlink
  "Creates a component with table-cell-link styles"
  [& {:keys [label on-click]}]
  [:a
   (merge
     {:on-click on-click}
     (use-style styles/table-cell-link))
   label])

(defn table-cell-hyperlink-href
  "Creates a component with table-cell-link styles"
  [& params]
  [util/recom-component-with-styles rc/hyperlink-href params styles/table-cell-link])

(defn breadcrumb-hyperlink
  "Renders breadcrumb element to go back to previous page"
  [& params]
  [util/recom-component-with-styles
   rc/hyperlink
   params
   styles/breadcrumb-link])
