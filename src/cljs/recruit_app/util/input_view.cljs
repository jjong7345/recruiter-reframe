(ns recruit-app.util.input-view
  (:require [re-frame.core :as rf]
            [re-com.core :as rc]
            [cljs.spec.alpha :as s]
            [goog.events.KeyCodes]
            [recruit-app.components.loading :as lo]))

(defn error-message [ns model spec error-msg]
  (let [show-errors? (rf/subscribe [(keyword ns "show-errors?")])]
    (fn [ns model spec error-msg]
      (when (and @show-errors? (not (s/valid? spec model)))
        [:div.error-msg error-msg]))))

(defn character-limit
  [char-count]
  (fn [char-count]
    [:span.char-count (str (- 5000 char-count) " characters left")]))

(defn- character-validation-option
  [char-limit]
  (if char-limit
    [:validation-regex (re-pattern (str "^.{0," char-limit "}$"))]
    []))

(defn- options
  [input-options char-limit]
  (->> char-limit
       character-validation-option
       (into input-options)))

(defn inputs-view
  [& {:keys [ns type label info input-type input-options spec error-msg char-limit placeholder]}]
  (let [input-model (rf/subscribe [(keyword ns (str type))])
        input-options (options input-options char-limit)]
    (fn [& {:keys [ns type label info input-type input-options char-limit placeholder]}]
      [rc/v-box
       :width "none"
       :class (str type "-holder holder")
       :children [(when (or label info)
                    [rc/h-box
                     :class "labels"
                     :justify :between
                     :children (cond-> []
                                       label (conj [rc/label :class "input-label" :label label])
                                       info (conj [rc/label :class "info" :label info]))])
                  (into [input-type
                         :class (str type)
                         :model @input-model
                         :width "none"
                         :on-change #(rf/dispatch [(keyword ns (str type "-change")) %])
                         :change-on-blur? false
                         :placeholder placeholder] input-options)
                  (when char-limit
                    [character-limit (count @input-model)])
                  (when spec
                    [error-message ns @input-model spec error-msg])]])))


(defn input-view
  ([ns type label info]
   [inputs-view
    :ns ns
    :type type
    :label label
    :info info
    :input-type rc/input-text])
  ([ns type label]
   (input-view ns type label "")))

(defn input-area-view
  ([ns type label info]
   (inputs-view ns type label info rc/input-textarea))
  ([ns type label]
   (input-area-view ns type label "")))

(defn input [& {:keys [ns type label info placeholder attr validation input-type]}]
  (let [input-model (rf/subscribe [(keyword ns (str type))])]
    (fn []
      [rc/v-box
       :size "initial"
       :width "none"
       :class (str type "-holder holder")
       :children [(when (or label info)
                    [rc/h-box
                     :class "labels"
                     :justify :between
                     :children [[rc/label :class "input-label" :label label]
                                [rc/label :class "info" :label info]]])
                  [rc/input-text
                   :class (str type " ")
                   :model input-model
                   :width "none"
                   :placeholder placeholder
                   :on-change #(rf/dispatch [(keyword ns (str type "-change")) %])
                   :change-on-blur? false
                   :validation-regex validation
                   :attr attr
                   :input-type (or input-type :input)]]])))

(defn specd-input-view [& {:keys [ns type label model info spec error-msg placeholder input-type input-options max-length]}]
  (let [input-model (rf/subscribe [(keyword ns (str type))])
        show-errors? (rf/subscribe [(keyword ns "show-errors?")])]
    (fn [& {:keys [model]}]
      (let [input-model (or model @input-model)
            input-type (or input-type rc/input-text)]
        [rc/v-box
         :width "none"
         :class (str type "-holder holder")
         :children [(when (or label info)
                      [rc/h-box
                       :class "labels"
                       :justify :between
                       :children [[rc/label :class "input-label" :label label]
                                  [rc/label :class "info" :label info]]])
                    (into [] (concat [input-type
                                      :class (str type " " (when (and @show-errors? (not (s/valid? spec input-model))) " error"))
                                      :model input-model
                                      :width "none"
                                      :placeholder placeholder
                                      :on-change #(rf/dispatch [(keyword ns (str type "-change")) %])
                                      :change-on-blur? false]
                                     (or input-options [])
                                     (when max-length [:attr {:max-length max-length}])))
                    (when spec [error-message ns input-model spec error-msg])]]))))

(defn min-max-slider
  [ns {:keys [type label min max step label-fn]}]
  (let [min-val (rf/subscribe [(keyword ns (str "min-" type))])
        max-val (rf/subscribe [(keyword ns (str "max-" type))])
        label-fn (if label-fn label-fn str)]
    (fn []
      [:div.min-max-slider
       (when label [rc/label :class "slider-label" :label label])
       [rc/h-box
        :class "slider-labels"
        :justify :between
        :children [[rc/label :label (label-fn (str @min-val))]
                   [rc/label :label (label-fn (str @max-val))]]]
       [rc/slider
        :class "multirange original"
        :model min-val
        :min min
        :max max
        :step step
        :on-change #(rf/dispatch [(keyword ns (str "min-" type "-change")) %])]
       [rc/slider
        :class "multirange ghost"
        :model max-val
        :min min
        :max max
        :step step
        :on-change #(rf/dispatch [(keyword ns (str "max-" type "-change")) %])]])))

(defn drpdn-view [& {:keys [ns type label choices spec error-msg]}]
  (let [input-model (rf/subscribe [(keyword ns (str type))])
        show-errors? (rf/subscribe [(keyword ns "show-errors?")])]
    (fn []
      [rc/v-box
       :class (str type "-holder holder")
       :children [(when label [rc/label :class "name" :label label])
                  [rc/box :child [rc/single-dropdown
                                  :class (str type "-dropdown dropdown" (when (and @show-errors? (not (s/valid? spec @input-model))) " error"))
                                  :model input-model
                                  :max-height "270px"
                                  :on-change #(rf/dispatch [(keyword ns (str type "-change")) %])
                                  :choices choices]]
                  [error-message ns @input-model spec error-msg]]])))

(defn checkbox [& {:keys [model label name on-change]}]
  [rc/h-box
   :class "check-holder"
   :children (cond-> [[rc/checkbox
                       :model model
                       :attr {:id name}
                       :label ""
                       :on-change on-change]]
                     label (conj [:label.info {:for name} label]))])

(defn action-text-input
  [& {:keys [model on-change action-label on-action]}]
  (fn []
    [rc/h-box
     :class "action-text-input-holder"
     :children [[rc/input-text
                 :class "action-text-input"
                 :model model
                 :width "100%"
                 :on-change on-change
                 :change-on-blur? false]
                [rc/hyperlink
                 :class "action-input-link"
                 :label action-label
                 :on-click on-action]]]))

(defn search-on-key-enter
  [event callback]
  (condp = (aget event "which")
    goog.events.KeyCodes.ENTER (callback)
    true))

(defn actionable-on-enter-key
  [input-view callback]
  (fn []
    (conj input-view :attr {:on-key-down #(search-on-key-enter % callback)})))

(defn date-picker
  "Creates a datepicker component with optional label"
  [& {:keys [label model on-change]}]
  [rc/v-box
   :children [(when label
                [rc/label
                 :label label])
              [rc/datepicker
               :model model
               :on-change on-change]]])

(defn submit-btn
  "Renders a submit btn that will toggle to throbber when submitting"
  [& {:keys [submitting? class label on-click disabled?]
      :or   {disabled? false}}]
  (if submitting?
    [rc/box
     :class (str class " btn submit-loading-container")
     :justify :center
     :align :center
     :child [lo/loading-circle-tiny]]
    [rc/button
     :class (str class " btn")
     :label label
     :disabled? disabled?
     :on-click on-click]))

(defn wrap-prevent-default
  "Returns function that will call preventDefault when submitting form"
  [callback]
  (fn [event]
    (.preventDefault event)
    (callback)))
