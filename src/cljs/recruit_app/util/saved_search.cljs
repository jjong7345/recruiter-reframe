(ns recruit-app.util.saved_search)

(def email-frequencies
  {0 "Do Not Email"
   1 "Daily"
   2 "Once A Week"})

(def email-intervals
  {1 " - Sundays"
   2 " - Mondays"
   3 " - Tuesdays"
   4 " - Wednesdays"
   5 " - Thursdays"
   6 " - Fridays"
   7 " - Saturdays"})

(defn email-frequency-display-name
  "Return a email frequency display name. (eg 'Do Not Email', 'Daily', 'Once A Week - Sundays)"
  [saved-search]
  (let [frequency-type (get-in saved-search [:reporting :email :frequency-type])
        interval (get-in saved-search [:reporting :email :interval])]
    (str (get email-frequencies frequency-type)
         (when (= frequency-type 2)
           (get email-intervals interval)))))