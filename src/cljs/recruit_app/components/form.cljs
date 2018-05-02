(ns recruit-app.components.form
  (:require [re-com.core :as rc]
            [re-frame.core :as rf]
            [recruit-app.components.util :as util]
            [stylefy.core :refer [use-style]]
            [recruit-app.styles :as styles]
            [recruit-app.util.date :as d]
            [cljs.spec.alpha :as s]
            [recruit-app.components.layout :as layout]
            [recruit-app.components.icon :as icon]
            [recruit-app.util.typeahead :refer [typeahead]]))

(defn inline-display
  "Displays a label and uneditable data inline"
  [& {:keys [label value]}]
  [util/recom-component-with-styles
   rc/h-box
   [:children [[:div
                (use-style styles/inline-display-label)
                (str label ": ")]
               [:div
                (use-style styles/inline-display-value)
                value]]]
   styles/inline-display])

(defn form-label
  "Renders a form label"
  [& {:keys [label]}]
  [util/recom-component-with-styles
   rc/label
   [:label label]
   styles/form-label])

(defn input-error
  "Displays error message"
  [message]
  [:div
   (use-style styles/input-error)
   message])

(defn input-flag
  "Renders flag for input in small type"
  [text]
  [:div
   (use-style styles/input-flag)
   text])

(defn input-text
  "Renders a text input with input-text styling

  Params:
   :ns          Namespace of the model associated with input
   :type        Type to be used to subscribe to model of input (i.e. (rf/subscribe [:ns/type]))
   :label       Optional label to be displayed above input
   :class       Optional class added to holder
   :placeholder Optional placeholder text for input
   :attr        Optional map of attributes to be added to input
   :validation  Optional Validation regex to be run on input values before on-change
   :input-type  Optional type of input (default is :input)
   :spec        Optional validation spec (requires :error-msg and a sub of show-errors? within ns)
   :error-msg   Message to be displayed when input value does not pass spec and show-errors? is true
   :disabled?   Whether or not input is disabled? (default is false)
   :height      Keyword denoting height (options are :normal or :tall)
   :flag        Optional flag to be displayed at top right of input"
  [& {:keys [ns type label class placeholder attr validation input-type spec error-msg
             disabled? height flag]
      :or   {height :normal input-type :input disabled? false}}]
  (let [input-model (rf/subscribe [(keyword ns (str type))])
        show-errors? (when (and spec error-msg)
                       (rf/subscribe [(keyword ns "show-errors?")]))]
    (fn []
      [util/recom-component-with-styles
       rc/v-box
       [:class class
        :children [(when label
                     [layout/row-top
                      :padding-bottom 6
                      :justify :between
                      :children [[form-label
                                  :label label]
                                 (when flag
                                   [input-flag flag])]])
                   [util/recom-component-with-styles
                    rc/input-text
                    [:model input-model
                     :width "none"
                     :placeholder placeholder
                     :on-change #(rf/dispatch [(keyword ns (str type "-change")) %])
                     :change-on-blur? false
                     :validation-regex validation
                     :attr attr
                     :disabled? disabled?
                     :input-type input-type]
                    (if (= height :tall) styles/input-text-tall styles/input-text)]
                   (when (and spec error-msg @show-errors? (not (s/valid? spec @input-model)))
                     [input-error error-msg])]]
       styles/input-holder])))

(defn character-limit
  [char-count]
  [:span
   (use-style styles/character-limit)
   (str (- 5000 char-count) " characters left")])

(defn input-textarea [& {:keys [ns type label class placeholder attr validation
                                input-type spec error-msg disabled? rows
                                char-limit flag]}]
  (let [input-model (rf/subscribe [(keyword ns (str type))])
        show-errors? (rf/subscribe [(keyword ns "show-errors?")])]
    (fn []
      [util/recom-component-with-styles
       rc/v-box
       [:class class
        :children [(when label
                     [layout/row-top
                      :padding-bottom 6
                      :justify :between
                      :children [[form-label
                                  :label label]
                                 (when flag
                                   [input-flag flag])]])
                   [util/recom-component-with-styles
                    rc/input-textarea
                    [:model input-model
                     :width "none"
                     :placeholder placeholder
                     :on-change #(rf/dispatch [(keyword ns (str type "-change")) %])
                     :change-on-blur? false
                     :validation-regex validation
                     :attr attr
                     :disabled? (or disabled? false)
                     :input-type (or input-type :textarea)
                     :rows (or rows 3)]
                    styles/input-textarea]
                   (when char-limit
                     [layout/row-bottom
                      :padding 5
                      :justify :end
                      :children [[character-limit (count @input-model)]]])
                   (when (and spec error-msg @show-errors? (not (s/valid? spec @input-model)))
                     [input-error error-msg])]]
       styles/input-holder])))

(defn single-dropdown
  "Renders recom single-dropdown with styles"
  [& {:keys [ns type label class] :as args}]
  (let [model (rf/subscribe [(keyword ns (str type))])]
    (fn []
      [util/recom-component-with-styles
       rc/v-box
       [:class (str class " component-dropdown")
        :children [(when label
                     [layout/row-top
                      :padding-bottom 6
                      :children [[form-label
                                  :label label]]])
                   [util/recom-component-with-styles
                    rc/single-dropdown
                    (-> args
                        (assoc :model model
                               :on-change #(rf/dispatch [(keyword ns (str type "-change")) %]))
                        (dissoc :ns :type :label :class)
                        (->> (mapcat identity)
                             vec))
                    styles/single-dropdown]]]
       styles/input-holder])))

(defn dropdown
  "Renders recom single-dropdown with styles"
  [& {:keys [ns type label class choices dropdown-type dropdown-style input-style]}]
  (let [model (rf/subscribe [(keyword ns (str type))])]
    (fn []
      [util/recom-component-with-styles
       rc/v-box
       [:class (str class " " dropdown-type "-dropdown")
        :children [(when label
                     [layout/row-top
                      :padding-bottom 6
                      :children [[form-label
                                  :label label]]])
                   [util/recom-component-with-styles
                    rc/single-dropdown
                    [:model model
                     :choices choices
                     :on-change #(rf/dispatch [(keyword ns (str type "-change")) %])]
                     dropdown-style]]]
       input-style])))

(defn range-dropdown
  "Renders recom single-dropdown with styles"
  [& {:as params}]
  (-> params
      (assoc :dropdown-type "range")
      (assoc :dropdown-style styles/range-dropdown)
      (assoc :input-style styles/range-dropdown-input-holder)
      (->> (mapcat identity)
           (into [dropdown]))))

(defn standard-dropdown
  "Renders recom single-dropdown with styles"
  [& {:as params}]
  (-> params
      (assoc :dropdown-type "standard")
      (assoc :dropdown-style styles/standard-dropdown)
      (assoc :input-style styles/standard-dropdown-input-holder)
      (->> (mapcat identity)
           (into [dropdown]))))

(defn secondary-dropdown
  "Renders recom single-dropdown with styles"
  [& {:as params}]
  (-> params
      (assoc :dropdown-type "secondary")
      (assoc :dropdown-style styles/secondary-dropdown)
      (assoc :input-style styles/standard-dropdown-input-holder)
      (->> (mapcat identity)
           (into [dropdown]))))

(defn tertiary-dropdown
  "Renders recom single-dropdown with styles"
  [& {:as params}]
  (-> params
      (assoc :dropdown-type "tertiary")
      (assoc :dropdown-style styles/tertiary-dropdown)
      (assoc :input-style styles/standard-dropdown-input-holder)
      (->> (mapcat identity)
           (into [dropdown]))))

(defn datepicker-dropdown
  "Renders a datepicker component"
  [& {:keys [ns type label class selectable-fn]}]
  (let [model (rf/subscribe [(keyword ns (str type))])]
    [util/recom-component-with-styles
     rc/v-box
     [:class class
      :children [(when label
                   [layout/row-top
                    :padding-bottom 6
                    :children [[form-label
                                :label label]]])
                 [rc/datepicker-dropdown
                  :model model
                  :format (get d/formats :date)
                  :on-change #(rf/dispatch [(keyword ns (str type "-change")) %])
                  :selectable-fn (if selectable-fn selectable-fn (constantly true))]]]
     styles/input-holder]))

(defn checkbox
  "Renders a checkbox component"
  [& {:keys [ns type label disabled?]}]
  (let [model (rf/subscribe [(keyword ns (str type))])]
    (fn []
      [util/recom-component-with-styles
       rc/h-box
       [:class "checkbox-container"
        :children [[:label
                    (use-style styles/checkbox-label)
                    label
                    [:input
                     (merge
                       (use-style styles/checkbox)
                       {:type      "checkbox"
                        :checked   @model
                        :disabled  (boolean disabled?)
                        :on-change #(rf/dispatch [(keyword ns (str type "-change")) (aget % "target" "checked")])})]
                    [:span.checkmark
                     (use-style styles/checkmark)]]]]
       styles/checkbox-container])))

(defn toggle
  "Renders a toggle component"
  [& {:keys [ns type label]}]
  (let [active? (rf/subscribe [(keyword ns (str type))])]
    (fn []
      [util/recom-component-with-styles
       rc/h-box
       [:children [[:div
                    (merge
                      (use-style (if @active? styles/toggle-on styles/toggle-off))
                      {:on-click #(rf/dispatch [(keyword ns (str type "-change")) (not @active?)])})]
                   [:div
                    (use-style styles/checkbox-label)
                    label]]]
       styles/input-holder])))

(defn removable-option
  [& {:keys [label on-remove]}]
  [util/recom-component-with-styles
   rc/h-box
   [:justify :between
    :align :center
    :children [[layout/col-left
                :padding 25
                :children [[:div
                            (use-style styles/removable-option-text)
                            label]]]
               [layout/row-top
                :padding 2
                :children [[icon/x :on-click (or on-remove #())]]]]]
   styles/removable-option])

(defn slider
  "Render a slider component"
  [& {:keys [ns type class width min max step]}]
  (let [model (rf/subscribe [(keyword ns (str type))])]
    (fn []
      [util/recom-component-with-styles
       rc/slider
       [:class class
        :width width
        :model model
        :min min
        :max max
        :step (or step 1)
        :on-change #(rf/dispatch [(keyword ns (str type "-change")) %])]
       styles/slider])))

(defn multirange-slider
  "Render a multi-range slider"
  [& {:keys [ns min-type max-type class width min max min-format max-format step label]}]
  (let [min-model (rf/subscribe [(keyword ns (str min-type))])
        max-model (rf/subscribe [(keyword ns (str max-type))])]
    (fn []
      [util/recom-component-with-styles
       layout/column
       [:class class
        :padding 0
        :width "100%"
        :children [(when label
                     [layout/row-top
                      :padding-bottom 6
                      :children [[form-label
                                  :label label]]])
                   [util/recom-component-with-styles
                    rc/label
                    [:label (str (min-format @min-model) " - " (max-format @max-model))]
                    styles/slider-label]
                   [slider
                    :ns "search"
                    :type min-type
                    :class "multirange original"
                    :width width
                    :min min
                    :max max
                    :step step]
                   [slider
                    :ns "search"
                    :type max-type
                    :class "multirange ghost"
                    :width width
                    :min min
                    :max max
                    :step step]]]
       styles/multirange])))

(defn ra-typeahead
  [& {:keys [ns type class placeholder data-source on-change suggestion-to-string render-suggestion label]}]
  (let [model (rf/subscribe [(keyword ns (str type))])]
    (fn []
      [util/recom-component-with-styles
       layout/column
       [:padding 0
        :children [(when label
                     [layout/row-top
                      :padding-bottom 6
                      :children [[form-label
                                  :label label]]])
                   [typeahead
                    :input-change #(rf/dispatch [(keyword ns (str type "-change")) %])
                    :value @model
                    :class class
                    :placeholder placeholder
                    :data-source data-source
                    :on-change on-change
                    :width "none"
                    :suggestion-to-string suggestion-to-string
                    :render-suggestion render-suggestion
                    :change-on-blur? true]]]
       styles/ra-typeahead])))

(defn input-with-dropdown
  "Renders wrapper with input and dropdown"
  [& {:keys [input dropdown]}]
  [:div
   (use-style styles/input-with-dropdown-holder)
   input
   dropdown])
