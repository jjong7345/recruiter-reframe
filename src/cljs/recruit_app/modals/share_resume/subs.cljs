(ns recruit-app.modals.share-resume.subs
  (:require [re-frame.core :as rf]
            [recruit-app.util.subscription :as subs]))

(subs/reg-subs "share-resume" [["emails" ""] ["message" ""] ["jobseeker-id" nil]
                               ["show-errors?" false]])

(rf/reg-sub
  :share-resume/form-values
  :<- [:share-resume/emails]
  :<- [:share-resume/message]
  :<- [:share-resume/jobseeker-id]
  (fn [[emails message jobseeker-id] _]
    {:emails       emails
     :message      message
     :jobseeker-id jobseeker-id}))
