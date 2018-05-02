(ns recruit-app.util.email
  (:require [recruit-app.util.dropdowns :as dd]
            [cljs.spec.alpha :as s]))

(defn recipient-name
  "If one recipient, returns first name, else returns js-first-name tag"
  [recipients]
  (if (< 1 (count recipients)) "{js-first-name}" (-> recipients first :jobSeekerFirstName)))

(defn greeting-string
  "Returns greeting that matches selected id"
  [greetings selected]
  (->> greetings
       (filter #(= (:id %) selected))
       first
       :label))

(defn formatted-body
  "Returns concatenation of greeting and message body"
  [greeting msg-body]
  (str greeting "\n\n" msg-body))

(defn email-send-data
  [{:keys [recipients greetings subject msg-body]}]
  {:jobseekerIds (map :secure-id recipients)
   :note         (-> recipients
                     (recipient-name)
                     (dd/greetings)
                     (greeting-string (or greetings 0))
                     (formatted-body msg-body))
   :subject      subject})

(s/def ::subject (s/and string? seq))
(s/def ::msg-body (s/and string? seq))
(s/def ::email (s/keys :req-un [::msg-body ::subject]))
