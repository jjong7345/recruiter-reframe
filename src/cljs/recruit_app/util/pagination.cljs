(ns recruit-app.util.pagination
  (:require [re-com.core :as rc]
            [re-frame.core :as rf]))

(defn page
  [num is-selected? on-change-page]
  (fn [num is-selected? on-change-page]
    [:div {:class (str "link" (when is-selected? " selected"))
           :on-click on-change-page} (str (+ 1 num))]))

(defn prev-btn
  [current-page on-change-page]
  (fn [current-page on-change-page]
    [:div.link.prev {:on-click (partial on-change-page (- current-page 1))} [:span.arrow "‹"] " PREV"]))

(defn next-btn
  [current-page on-change-page]
  (fn [current-page on-change-page]
    [:div.link.next {:on-click (partial on-change-page (+ current-page 1))} "NEXT " [:span.arrow "›"]]))

(defn ellipsis
  []
  [:div.ellipsis "..."])

(defn pagination
  [current-page pages on-change-page]
  (fn [current-page pages on-change-page]
    (let [last-page (last pages)
          make-page #(vector page % (= % current-page) (partial on-change-page %))
          middle-vec (when (< 1 current-page (- last-page 1)) (range (- current-page 1) (+ current-page 2)))]
      [rc/h-box
       :class "pagination"
       :justify :center
       :children (cond-> []
                         (< 0 current-page) (conj [prev-btn current-page on-change-page])
                         true (conj (make-page 0))
                         (< 2 current-page) (conj [ellipsis])
                         (and (<= current-page 1) (< 1 last-page)) (conj (make-page 1))
                         (and (= current-page 1) (< 2 last-page)) (conj (make-page 2))
                         (not-empty middle-vec) ((fn [page-coll] (reduce #(conj %1 (make-page %2)) page-coll middle-vec)))
                         (and (= (- last-page 1) current-page) (< 0 (- last-page 2))) (conj (make-page (- last-page 2)))
                         (and (<= (- last-page 1) current-page) (< 1 current-page) (< 0 (- last-page 1))) (conj (make-page (- last-page 1)))
                         (< current-page (- last-page 2)) (conj [ellipsis])
                         (< 0 last-page) (conj (make-page last-page))
                         (< current-page last-page) (conj [next-btn current-page on-change-page]))])))
