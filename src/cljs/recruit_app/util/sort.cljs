(ns recruit-app.util.sort
  (:require [cljs-time.core :as t]))

(defn after?
  "Returns true if first arg is not null and after second arg"
  [a b]
  (if a
    (if b (t/after? a b) true)
    false))

(defn before?
  "Returns true if first arg is not null and before second arg"
  [a b]
  (if a
    (if b (t/before? a b) true)
    false))

(defn time-comparator
  "Returns comparator to compare 2 time objects"
  [sort-order]
  (if (= sort-order :desc)
    after?
    (complement after?)))

(defn default-comparator
  "Default comparator uses compare function"
  [sort-order]
  (if (= sort-order :desc)
    (complement compare)
    compare))

(defn- flip
  "Returns comparator function with arguments flipped"
  [func]
  (fn [x y]
    (func y x)))

(defn sort-fn
  "Returns function that will create a sorting function based on sort-dir
  The comparator must sort ascendingly. If sort-dir is :desc, then it will
  use the complement of the comparator-fn"
  [key-fn asc-comparator]
  (fn [sort-dir]
    (if (= sort-dir :desc)
      (partial sort-by key-fn (flip asc-comparator))
      (partial sort-by key-fn asc-comparator))))
