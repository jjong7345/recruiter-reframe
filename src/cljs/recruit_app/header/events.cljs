(ns recruit-app.header.events
  (:require [recruit-app.util.events :as ev]))

(ev/reg-toggle-event "header" "show-header-dropdown?")
