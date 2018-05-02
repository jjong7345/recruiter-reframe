(ns recruit-app.get-full-access.specs
  (:require [cljs.spec.alpha :as s]))

(s/def ::first-name (and string? #(> (count %) 0)))
(s/def ::last-name (and string? #(> (count %) 0)))
(s/def ::email (and string? #(> (count %) 0)))
(s/def ::phone-number (and (or string? int?) #(> (count %) 0)))
(s/def ::company (and string? #(> (count %) 0)))
(s/def ::title (and string? #(> (count %) 0)))
(s/def ::comments string?)

(s/def ::contact (s/keys :req-un
                         [::first-name
                          ::last-name
                          ::email
                          ::phone-number
                          ::company
                          ::title]
                         :opt-un
                         [::comments]))
