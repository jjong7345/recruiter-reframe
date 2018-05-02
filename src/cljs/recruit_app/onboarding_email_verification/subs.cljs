(ns recruit-app.onboarding-email-verification.subs
  (:require [re-frame.core :as rf]
            [recruit-app.util.subscription :as sub]))

(def reg-projects-subs (partial sub/reg-subs "email-verification"))

(reg-projects-subs [["code" ""] ["show-error?" false]])