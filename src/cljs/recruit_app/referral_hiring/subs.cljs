(ns recruit-app.referral-hiring.subs
  (:require [recruit-app.util.subscription :as rs]))

(rs/reg-subs "referral-hiring" [["referral" ""]
                                ["fullname" ""]\
                                ["company" ""]
                                ["email" ""]
                                ["recruiter" ""]
                                ["read-more?" false]
                                ["showing?" false]
                                ["show-errors?" false]])
