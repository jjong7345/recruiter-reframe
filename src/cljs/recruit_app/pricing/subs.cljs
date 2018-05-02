(ns recruit-app.pricing.subs
  (:require [re-frame.core :as rf]
            [recruit-app.util.subscription :as sub]))

(def reg-projects-subs (partial sub/reg-subs "pricing"))

(reg-projects-subs [["plan" "basic"]])
