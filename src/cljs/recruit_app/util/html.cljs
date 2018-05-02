(ns recruit-app.util.html
  (:require [hickory.core :as h]
            [goog.string :as gs]))

(defn- unescape-entities
  [part]
  (cond
    (string? part) (gs/unescapeEntities part)
    (vector? part) (mapv unescape-entities part)
    :else part))

(defn- sanitize-html
  [element]
  (->> element
       (h/as-hiccup)
       (mapv unescape-entities)))

(defn- parse-to-hiccup
  [html]
  (mapv sanitize-html html))

(defn html-to-hiccup [html]
  (let [remove-style-regex #"style=\"(.*?);\"|style[ ]*=[ ]*\"(.*?);[ ]*\""
        newline-regex #"\n"]
    (-> html
        (clojure.string/replace remove-style-regex "")
        (clojure.string/replace newline-regex "")
        (h/parse-fragment)
        (parse-to-hiccup))))
