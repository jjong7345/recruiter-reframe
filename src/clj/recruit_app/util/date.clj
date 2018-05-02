(ns recruit-app.util.date
  (:require [clj-time.format :as tf]
            [clj-time.core :as time]
            [clj-time.coerce :as tc]
            [java-time :as jt]))

(def formats
  "All date formats used on site"
  {:date          "M/d/yy"
   :date-and-time "M/d/yy h:mma"})

(defn iso-8601-date-est
  "Returns ISO 8601 compliant date in EST."
  [gmt-time]
  (try
    (let [iso-time (time/to-time-zone (tc/from-string gmt-time) (time/time-zone-for-offset -5))]
      (tf/unparse
        (tf/with-zone (tf/formatters :date-time-no-ms) (.getZone iso-time)) iso-time))
    (catch Exception e nil)))

(defn formatted-datestring
  "Formats a string date to given formatter"
  [formatter-key date-string]
  (when date-string
    (-> date-string
        jt/zoned-date-time
        (jt/with-zone-same-instant "America/New_York")
        (->> (jt/format (get formats formatter-key))))))
