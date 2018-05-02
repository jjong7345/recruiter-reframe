(ns recruit-app.modals.checkout.subs
  (:require-macros [reagent.ratom :refer [reaction]])
  (:require [re-frame.core :as rf]
            [recruit-app.util.subscription :as sub]))

(def reg-subs (partial sub/reg-subs "checkout"))

(reg-subs [["open-modal" nil] ["step" ""] ["modal-url" ""] ["promote-job-id" nil]])