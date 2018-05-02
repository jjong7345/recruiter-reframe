(ns recruit-app.config)

(goog-define SHOW_ADMIN_REFRESH false)

(def debug? ^boolean js/goog.DEBUG)
(def show-admin-refresh?
  "This functionality currently doesn't work as expected so it's being hidden"
  js/recruit_app.config.SHOW_ADMIN_REFRESH)
