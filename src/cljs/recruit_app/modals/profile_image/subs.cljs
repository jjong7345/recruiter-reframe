(ns recruit-app.modals.profile-image.subs
  (:require [recruit-app.util.subscription :as s]))

(s/reg-subs "profile-image" [["image-url" ""] ["image-type" "image/jpeg"] ["crop-data" {}]])
