(ns recruit-app.footer.subs
  (:require [re-frame.core :as rf]
            [recruit-app.util.subscription :as sub]))

(sub/reg-subs "footer" [["show-contact?" false]])
