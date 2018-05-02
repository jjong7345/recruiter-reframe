(ns recruit-app.login.db
  (:require [cljs.spec.alpha :as s]
            [recruit-app.marketinghome.db :as m]))

(def email? (s/and string? (partial re-matches m/email-regex)))

(s/def ::email email?)
