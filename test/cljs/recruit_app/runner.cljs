(ns recruit-app.runner
    (:require [doo.runner :refer-macros [doo-tests]]
              [recruit-app.core-test]))

(doo-tests 'recruit-app.core-test)
