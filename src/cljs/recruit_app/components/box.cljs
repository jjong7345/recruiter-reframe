(ns recruit-app.components.box
  (:require [re-com.core :as rc]
            [recruit-app.styles :as styles]
            [recruit-app.components.header :as header]
            [recruit-app.components.util :as util]
            [recruit-app.components.layout :as layout]))

(defn box-header-base
  [& [{:keys [label right justify]} styles]]
  [util/recom-component-with-styles
   layout/column
   [:padding 0
    :class "box-header"
    :children [[layout/wrapping-row
                :padding-top 0
                :padding-bottom 10
                :justify justify
                :align :end
                :children [[layout/col-left
                            :padding 0
                            :class (if right
                                     "col-xs-12 col-sm-12 col-md-6 col-lg-6"
                                     "col-xs-12")
                            :children [[header/header-4 label]]]
                           (when right
                             [layout/col-right
                              :padding 0
                              :class "col-xs-12 col-sm-12 col-md-6 col-lg-6"
                              :children [right]])]]]]
   styles/box-header])

(defn box-header
  [& params]
  [box-header-base params styles/box-header])

(defn box-header-no-underline
  [& params]
  [box-header-base params styles/box-header-no-underline])

(defn box-base
  "Renders a box with header"
  [& [{:keys [label body class top-right header-justify]} component]]
  [util/recom-component-with-styles
   rc/v-box
   [:class class
    :children [(when (or label top-right)
                 [component
                  :label label
                  :justify (or header-justify :between)
                  :right top-right])
               body]]
   styles/box])

(defn box
  "Renders a box with header"
  [& params]
  [box-base params box-header])

(defn box-2
  "Renders a box with header without underline"
  [& params]
  [box-base params box-header-no-underline])