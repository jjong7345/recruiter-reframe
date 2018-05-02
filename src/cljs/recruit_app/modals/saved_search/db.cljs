(ns recruit-app.modals.saved-search.db
  (:require [cljs.spec.alpha :as s]
            [clojure.string :as cs]))

(s/def ::name (s/and string? (complement cs/blank?)))
