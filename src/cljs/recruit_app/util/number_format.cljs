(ns recruit-app.util.number_format
  (:require [clojure.string :as s]))

(defn currency-format
  "formats currency using the current locale
   to change locale set goog.i18n.NumberFormatSymbols eg:
   (set! goog.i18n.NumberFormatSymbols goog.i18n.NumberFormatSymbols_it_IT)
   see here for supported locales
   https://github.com/google/closure-library/blob/master/closure/goog/i18n/numberformatsymbols.js
  "
  [n]
  (.format (goog.i18n.NumberFormat. (.-CURRENCY goog.i18n.NumberFormat.Format)) n))

(defn convert-currency?
  [c]
  (if-not (s/index-of c "$")
    (str "$" (subs c 2))
    c))

(defn remove-cents
  [n]
  (s/replace n ".00" ""))

(defn number-conversion
  "Return number converted to thousands"
  [n]
  (convert-currency? (currency-format n)))
