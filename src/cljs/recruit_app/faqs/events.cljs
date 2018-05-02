(ns recruit-app.faqs.events
  (:require [re-frame.core :as rf]
            [recruit-app.db :as db]))


(defn go-to-anchor
  [_ [_ anchor]]
  (.scrollIntoView (.getElementById js/document anchor)))

(rf/reg-event-fx
  :faqs/go-to-anchor
  go-to-anchor)