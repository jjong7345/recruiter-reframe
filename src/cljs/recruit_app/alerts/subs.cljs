(ns recruit-app.alerts.subs
  (:require [re-frame.core :as rf]
            [recruit-app.util.subscription :as subs]))

(subs/reg-subs "alerts" [["error" []] ["success" []]])
