(ns recruit-app.post-job.preview.events
  (:require [recruit-app.events :as events]
            [recruit-app.db :as db]
            [ajax.core :as ajax]
            [cljs.reader :as edn]
            [cljs.spec.alpha :as s]))

(defn load-view
  "Logs page view and redirects if necessary"
  [_ _]
  {:dispatch-n [[:post-job/redirect] [:scroll-top]]
   :ga/page-view ["/post-job/preview" {}]})

(events/reg-event-fx
  :post-job.preview/load-view
  load-view)