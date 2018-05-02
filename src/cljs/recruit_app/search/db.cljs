(ns recruit-app.search.db
  (:require [cljs.spec.alpha :as s]
            [recruit-app.specs.search :as search]))

(s/def ::search (s/keys :opt-un [::search/criteria]))
