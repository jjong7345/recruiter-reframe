(ns recruit-app.components.header
  (:require [recruit-app.components.util :as util]
            [re-com.core :as rc]
            [recruit-app.styles :as styles]
            [recruit-app.components.typography :as typography]
            [recruit-app.components.layout :as layout]))

(defn- label
  "Creates rc/label component with given styles"
  [label styles]
  [util/recom-component-with-styles
   rc/label
   [:label label]
   styles])

(defn header-1
  "Applies header-1 styles to rc/label"
  [text]
  [label text styles/header-1])

(defn header-2
  "Applies header-2 styles to rc/label"
  [text]
  [label text styles/header-2])

(defn header-3
  "Applies header-3 styles to rc/label"
  [text]
  [label text styles/header-3])

(defn header-4
  "Applies header-4 styles to rc/label"
  [text]
  [label text styles/header-4])

(defn header-5
  "Applies header-5 styles to rc/label"
  [text]
  [label text styles/header-5])

(defn form-header
  "Applies form header styles to rc/label
  Form header differentiates a section of a form"
  [text]
  [label text styles/form-header])

(defn info-header
  "Header displayed above section of info (?)"
  [text]
  [label text styles/info-header])

(defn page-header
  "Renders title header for page"
  [& {:keys [header-text sub-header-text right-element]}]
  [util/recom-component-with-styles
   layout/column
   [:padding 0
    :children [[util/recom-component-with-styles
                rc/h-box
                [:justify :between
                 :children [[layout/column
                             :padding 0
                             :children [[layout/row-top
                                         :padding 0
                                         :children [[header-1 header-text]]]
                                        (when sub-header-text
                                          [layout/row-bottom
                                           :padding-top 6
                                           :children [[typography/body-copy-light sub-header-text]]])]]
                            (when right-element
                              [layout/column
                               :padding 0
                               :children [right-element]])]]
                styles/page-header]]]
   styles/page-header-container])

(defn sub-header
  "Returns header with styling for subheader (smaller text next to header)"
  [text]
  [label text styles/sub-header])

(defn section-header
  "Returns header with styling for section header"
  [text]
  [label text styles/section-header])
