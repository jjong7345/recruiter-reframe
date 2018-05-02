(ns recruit-app.util.chart
  (:require [re-frame.core :as rf]
            [re-com.core :as rc
             :refer-macros [handler-fn]]
            [reagent.core :as reagent]
            [rid3.core :as rid3]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Vars

(def transition-duration 800)
(def education-bar-max-width 260)

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Util Fns

(defn apply-gap
  [width gap]
  (if (> width 1)
    (- width gap)
    width))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; SVG

(defn education-svg-did-mount
  [node ratom]
  (-> node
      (.attr "class" "barchart-education-svg")
      (.attr "width" 320)
      (.attr "height" 100)))

(defn experience-svg-did-mount
  [node _]
  (-> node
      (.attr "class" "barchart-experience")
      (.attr "width" 39)
      (.attr "height" 100)))

(defn salary-svg-did-mount
  [node _]
  (-> node
      (.attr "class" "barchart-salary-svg")
      (.attr "width" 320)
      (.attr "height" 24)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Bar

(defn education-bar-did-mount
  [node ratom]
  (let [width education-bar-max-width
        height 104
        data-n (count (get @ratom :dataset))
        rect-height 24
        x-scale (-> js/d3
                    .scaleLinear
                    (.domain #js [0 100])
                    (.range #js [0 width]))]
    (-> node
        (.style "shape-rendering" "crispEdges")
        (.attr "x" 0)
        (.attr "y" (fn [_ i]
                     (* i rect-height)))
        (.attr "height" (- rect-height 6))
        (.attr "opacity" 0.2)
        .transition
        (.duration transition-duration)
        (.attr "width" (fn [d]
                         (x-scale (aget d "applicants"))))
        (.attr "opacity" 1))))

(defn experience-applicants-bar-did-mount
  [node ratom]
  (let [user-height (-> @ratom
                        (:dataset)
                        (get 0)
                        (:applicants))
        bar-height (if (= user-height 0) 1 user-height)]
    (-> node
        (.attr "x" 0)
        (.attr "width" 18)
        (.attr "y" 80)
        .transition
        (.duration transition-duration)
        (.attr "y" (- 80 bar-height))
        (.attr "height" bar-height))))

(defn experience-average-bar-did-mount
  [node ratom]
  (let [average-height (-> @ratom
                           (:dataset)
                           (get 0)
                           (:average))]
    (-> node
        (.attr "x" 21)
        (.attr "width" 18)
        (.attr "y" 80)
        .transition
        (.duration transition-duration)
        (.attr "y" (- 80 average-height))
        (.attr "height" average-height))))

(defn salary-bar-did-mount
  [node ratom]
  (let [x-scale (-> js/d3
                    .scaleLinear
                    (.domain #js [0 100])
                    (.range #js [0 320]))
        start-x (reagent/atom 0)
        gap 1]
    (-> node
        (.attr "class" (fn [_ i] (str "salary-range-" (inc i))))
        (.attr "y" 0)
        (.attr "height" 24)
        (.attr "x" (fn [_ i]
                     (let [prev-width (-> @ratom
                                          (:dataset)
                                          (get (- i 1))
                                          (:applicants))]
                       (swap! start-x #(+ prev-width @start-x))
                       (x-scale @start-x))))
        .transition
        (.duration transition-duration)
        (.attr "width" (fn [d]
                         (-> d
                             (aget "applicants")
                             (x-scale)
                             (apply-gap gap)))))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Bar Label

(defn education-bar-label-common
  [node ratom]
  (let [width education-bar-max-width
        height 104
        data-n (count (get @ratom :dataset))
        rect-height 24]
    (-> node
        (.attr "y" (fn [_ i]
                     (+ (* i rect-height)
                        (/ rect-height 2.5))))
        (.attr "alignment-baseline" "middle")
        (.text (fn [d] (aget d "label"))))))

(defn education-bar-label-did-mount
  [node ratom]
  (let [width education-bar-max-width
        x-scale (-> js/d3
                    .scaleLinear
                    (.domain #js [0 100])
                    (.range #js [0 width]))]
    (-> node
        (education-bar-label-common ratom)
        .transition
        (.duration transition-duration)
        (.attr "x" (fn [d] (+ (x-scale (aget d "applicants"))
                              8))))))

(defn experience-bar-label-did-mount
  [node ratom]
  (let [label (-> @ratom
                  (:dataset)
                  (get 0)
                  (:label))]
    (-> node
        (.attr "y" 90)
        (.attr "x" "50%")
        (.attr "text-anchor" "middle")
        (.attr "alignment-baseline" "middle")
        (.text label))))


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; education-chart

(defn education-chart
  "Render education chart using Reagent/D3. ratom can be a reagent/atom or re-frame subscription"
  [ratom]
  [rid3/viz
   {:id     "barchart-education"
    :ratom  ratom
    :svg    {:did-mount education-svg-did-mount}
    :pieces [{:kind      :elem-with-data
              :class     "bar"
              :tag       "rect"
              :did-mount education-bar-did-mount}

             {:kind      :elem-with-data
              :class     "bar-label"
              :tag       "text"
              :did-mount education-bar-label-did-mount}]}])


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; experience-chart

(defn experience-chart
  [ratom index]
  "Render experience chart using Reagent/D3. ratom can be a reagent/atom or re-frame subscription"
  [rid3/viz
   {:id     (str "year-range-" index)
    :ratom  ratom
    :svg    {:did-mount experience-svg-did-mount}
    :pieces [{:kind      :elem-with-data
              :class     "applicants"
              :tag       "rect"
              :did-mount experience-applicants-bar-did-mount}
             {:kind      :elem-with-data
              :class     "site-average"
              :tag       "rect"
              :did-mount experience-average-bar-did-mount}

             {:kind      :elem-with-data
              :class     "experience-range"
              :tag       "text"
              :did-mount experience-bar-label-did-mount}]}])

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; salary-chart

(defn salary-chart
  "Render salary chart using Reagent/D3. ratom can be a reagent/atom or re-frame subscription"
  [ratom]
  [rid3/viz
   {:id     "barchart-salary"
    :ratom  ratom
    :svg    {:did-mount salary-svg-did-mount}
    :pieces [{:kind      :elem-with-data
              :class     "applicants"
              :tag       "rect"
              :did-mount salary-bar-did-mount}]}])

