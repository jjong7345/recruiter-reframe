(ns recruit-app.footer.events
  (:require [recruit-app.util.events :as ev]))

(ev/reg-toggle-event "footer" "show-contact?")