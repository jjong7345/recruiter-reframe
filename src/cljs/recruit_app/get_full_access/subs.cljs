(ns recruit-app.get-full-access.subs
  (:require-macros [reagent.ratom :refer [reaction]])
  (:require [re-frame.core :as re-frame]
            [recruit-app.util.subscription :as sub-util]))

(sub-util/reg-subs "get-full-access" [["first-name" ""] ["last-name" ""]
                                      ["email" ""] ["phone-number" ""]
                                      ["company" ""] ["title" ""]
                                      ["comments" ""] ["show-errors?" false]
                                      ["confirmation-page?" false]
                                      ["show-confirmation-alert-bar?" true]])

(re-frame/reg-sub
  (keyword "get-full-access")
  (fn [db _]
    (let [contact (get-in db [(keyword "get-full-access")] {})]
      contact)))
