(ns recruit-app.components.layout
  (:require [re-com.core :as rc]
            [recruit-app.styles :as styles]
            [recruit-app.components.util :as util]
            [stylefy.core :refer [use-style]]))

(def padding-default 9)

(defn- padding-str
  "Returns padding str given 4 ints"
  [top right bottom left]
  (str top "px " right "px " bottom "px " left "px"))

(defn column
  "Renders rc/v-box with added :padding, :padding-left, :padding-right options.
  Accepts integers for each option"
  [& {:keys [padding padding-left padding-right class] :as params}]
  (let [padding-str (padding-str 0 (or padding-right padding padding-default)
                                 0 (or padding-left padding padding-default))]
    (-> (merge params (use-style styles/column))
        (update :class str " " class)
        (assoc-in [:style :padding] padding-str)
        (dissoc :padding :padding-left :padding-right)
        (->> (mapcat identity)
             (into [rc/v-box]))
        vec)))

(defn col-left
  "Renders column with no left padding"
  [& params]
  (apply column (conj (vec params) :padding-left 0)))

(defn col-right
  "Renders column with no right padding"
  [& params]
  (apply column (conj (vec params) :padding-right 0)))

(defn row
  "Renders rc/v-box with added :padding, :padding-top, :padding-bottom options
  Accepts integers for each option"
  [& {:keys [padding padding-top padding-bottom styles class]
      :or {styles styles/row}
      :as params}]
  (let [padding-str (padding-str (or padding-top padding padding-default) 0
                                 (or padding-bottom padding padding-default) 0)]
    (-> (merge params (use-style styles))
        (update :class str " " class)
        (assoc-in [:style :padding] padding-str)
        (dissoc :padding :padding-top :padding-bottom :styles)
        (->> (mapcat identity)
             (into [rc/h-box]))
        vec)))

(defn row-top
  "Renders row with no top padding"
  [& params]
  (apply row (conj (vec params) :padding-top 0)))

(defn row-bottom
  "Renders row with no bottom padding"
  [& params]
  (apply row (conj (vec params) :padding-bottom 0)))

(defn wrapping-row
  "Renders row component with flex-wrap = wrap"
  [& {:as params}]
  (-> params
      (assoc :styles styles/wrapping-row)
      (->> (mapcat identity)
           (into [row]))))

(defn wrapping-row-child
  "In order to create line spacing when wrapping, wrapping row children must
  have the same margin to counteract the margin used in wrapping-row"
  [component]
  [:div (use-style styles/wrapping-row-child) component])

(defn wrapping-row-with-children
  "Renders row component with flex-wrap = wrap
  It also wraps each child in 'wrapping-row-child'"
  [& {:keys [children] :as params}]
  (-> params
      (assoc :children (map (partial vector wrapping-row-child) children))
      (assoc :styles styles/wrapping-row-with-children)
      (->> (mapcat identity)
           (into [row]))))

(defn page-content
  "Wraps content in container with page content styling"
  [& components]
  [util/recom-component-with-styles
   rc/v-box
   [:class "content-holder"
    :children [[row-top
                :padding-bottom 40
                :children [[util/recom-component-with-styles
                            column
                            [:padding 0
                             :children components]
                            styles/page-content]]]]]
   styles/page-content-container])
