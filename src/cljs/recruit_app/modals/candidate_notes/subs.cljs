(ns recruit-app.modals.candidate-notes.subs
  (:require [re-frame.core :as rf]
            [recruit-app.util.subscription :as subs]))

(subs/reg-subs "candidate-notes" [["candidate-notes" {}] ["active-id" nil] ["notes" ""] ["is-saving?" false]])

(rf/reg-sub
  :candidate-notes/active-candidate-notes
  :<- [:candidates/active-id]
  :<- [:candidate-notes/candidate-notes]
  (fn [[secure-id candidate-notes] _]
    (get candidate-notes secure-id)))
