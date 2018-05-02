(ns recruit-app.post-job.preview.events
  (:require [re-frame.core :as rf]
            [recruit-app.db :as db]
            [ajax.core :as ajax]
            [cljs.reader :as edn]
            [cljs.spec.alpha :as s]))

(rf/reg-event-fx
  :post-job.preview/load-view
  (fn [_ _]
    {:dispatch-n [[:post-job/redirect] [:scroll-top]]
     :ga/page-view ["/post-job/preview" {}]}))