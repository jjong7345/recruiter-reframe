(ns recruit-app.util.date
  (:require [cljs-time.core :as t]
            [cljs-time.format :as f]
            [cljs-time.coerce :as c]
            [goog.date.UtcDateTime :as dt]))

(def formats
  "All date formats used on site"
  {:date           "M/d/yy"
   :date-and-time  "M/d/yy h:mmA"
   :month-and-date "MMM dd, yyyy"
   :month-and-year "MMM yyyy"
   :year           "yyyy"})

(defn year-of-ms-date [ms-date]
  (try (f/unparse (f/formatter "yyyy") (c/from-long ms-date))
       (catch js/Error e "")))

(defn timestamp
  "Parses string and returns timestamp in given format"
  ([string]
   (timestamp string (f/formatters :date-time-no-ms)))
  ([string date-format]
   (when (string? string)
     (f/parse date-format string))))

(defn unix-timestamp
  "Returns unix-timestamp from goog.date.UtcDateTime"
  [datetime]
  (/ (.getTime datetime) 1000))

(defn utc-date-time
  "Returns instance of goog.date.UtcDateTime for UNIX timestamp.
  UNIX timestamp must be multiplied by 1000 first before creating UtcDateTime"
  [unix-timestamp]
  (when unix-timestamp
    (dt/fromTimestamp (* 1000 unix-timestamp))))

(defn db-date-time
  "Creates date time object for db date string"
  [db-date]
  (when db-date
    (f/parse (f/formatters :date-time-no-ms) db-date)))

(defn db-date-string
  "Returns db date string given datetime object"
  [datetime]
  (when datetime
    (f/unparse (f/formatters :date-time-no-ms) datetime)))

(defn subscribe-date-time
  "Creates date time object for subscribe_date string"
  [subscribe-date]
  (db-date-time subscribe-date))

(defn subscribe-date-time-with-ms
  "Creates date time with ms object for subscribe_date string"
  [subscribe-date]
  (f/parse (f/formatters :date-time) subscribe-date))

(defn formatted-date
  "Formats datetime to given formatter"
  [formatter-key datetime]
  (when datetime
    (f/unparse (f/formatter (get formats formatter-key)) datetime)))

(defn most-recent-date
  "Return the most recent date string among input dates"
  [dates]
  (->> dates
       (keep db-date-time)
       (sort t/after?)
       first))

(defn days-passed-since
  "Return days interval between given datetime and now"
  [datetime]
  (when datetime
    (-> datetime
        (t/interval (t/now))
        (t/in-days))))