(ns recruit-app.util.breadcrumb
  (:require [re-frame.core :as rf]))

(def breadcrumb
  "Returns proper breadcrumb for given active-panel"
  {:recruiter-admin-search                  {:label    "< Back to Main"
                                             :on-click #(rf/dispatch [:go-to-route "/recruiter-admin"])}
   :recruiter-admin-search-results          {:label    "< Back to Search Results"
                                             :on-click #(rf/dispatch [:go-to-route "/recruiter-admin/search-results"])}
   :recruiter-admin-pending-queue           {:label    "< Back to Pending Profiles"
                                             :on-click #(rf/dispatch [:recruiter-admin/back-to-pending-click])}
   :recruiter-admin-approved-recently       {:label    "< Back to Recently Approved Recruiters"
                                             :on-click #(rf/dispatch [:recruiter-admin/back-to-approved-recently-click])}
   :recruiter-admin-pending-escalated-queue {:label    "< Back to Pending Escalated Profiles"
                                             :on-click #(rf/dispatch [:recruiter-admin/back-to-pending-escalated-click])}})
