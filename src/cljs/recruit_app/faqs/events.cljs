(ns recruit-app.faqs.events
  (:require [recruit-app.events :as events]))


(defn go-to-anchor
  [_ [_ anchor]]
  (.scrollIntoView (.getElementById js/document anchor)))

(events/reg-event-fx
  :faqs/go-to-anchor
  go-to-anchor)