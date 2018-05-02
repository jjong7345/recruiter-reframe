(ns recruit-app.components.loading
  (:require [re-com.core :as rc]
            [recruit-app.util.img :as img]
            [recruit-app.styles :as styles]
            [recruit-app.components.util :as util]))

(defn loading-circle-image
  []
  [:img.loading {:src   (img/url :loading-circle-url)
                 :style {:width  "100%"
                         :height "auto"}}])

(defn- loading-circle
  [& {:keys [class styles]}]
  [util/recom-component-with-styles
   rc/h-box
   [:justify :center
    :class class
    :style {:align-items "center"}
    :align :center
    :children [[loading-circle-image]]]
   styles])

(defn loading-circle-tiny
  [& {:keys [class]}]
 [loading-circle
  :class class
  :styles styles/loading-circle-tiny])

(defn loading-circle-small
  [& {:keys [class]}]
  [loading-circle
   :class class
   :styles styles/loading-circle-small])

(defn loading-circle-large
  [& {:keys [class]}]
  [loading-circle
   :class class
   :styles styles/loading-circle-large])

(defn primary-button-loader
  []
  [loading-circle
   :styles styles/primary-button-loader])

(defn loading-cover
  []
  [util/recom-component-with-styles
   rc/box
   [:justify :center
    :align :center
    :child [loading-circle-large]]
   styles/loading-cover])

(defn loading-page
  []
  [util/recom-component-with-styles
   rc/h-box
   [:justify :center
    :children [[loading-circle-large]]]
   styles/loading-page])

(defn loading-overlay-wrapper
  [& {:keys [class child size on-click show-loading?]}]
  [util/recom-component-with-styles
   rc/v-box
   [:justify :center
    :align :center
    :class class
    :children (merge [child]
                     (when show-loading?
                       [rc/box
                        :style {:position "absolute"}
                        :child (case size
                                 :tiny [loading-circle-tiny]
                                 :small [loading-circle-small]
                                 :large [loading-circle-large]
                                 [loading-circle-large])]))
    :attr {:on-click on-click}]
   styles/loading-overlay-wrapper])