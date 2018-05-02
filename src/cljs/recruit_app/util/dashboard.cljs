(ns recruit-app.util.dashboard)

(def experience-site-average-map
  {:<5 2 :5-7 3 :8-10 7 :11-15 13 :>15 75})

(defn experience-site-average
  [key]
  (key experience-site-average-map))

(def education-site-average-map
  {:Phd 7 :Masters 45 :Bachelors 92 :Unknown 100})

(defn education-site-average
  [key]
  (key education-site-average-map))

(defn trim-long-saved-search-name
  [string]
  (let [total-string (count string)
        max 50
        trimmed-string (subs string 0 max)]
    (if (> total-string max)
      (str trimmed-string "...")
      trimmed-string)))