(ns recruit-app.resume.subs
  (:require [re-frame.core :as rf]
            [recruit-app.util.subscription :as subs]))

(subs/reg-subs "resume" [["metadata" {}] ["metadata-fetched" #{}]])
