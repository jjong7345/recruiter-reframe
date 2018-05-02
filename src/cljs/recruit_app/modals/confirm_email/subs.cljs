(ns recruit-app.modals.confirm-email.subs
  (:require [recruit-app.util.subscription :as subs]))

(subs/reg-subs "confirm-email" [["email" ""] ["key" nil] ["show-errors?" false]
                                ["email-exists?" false]])
