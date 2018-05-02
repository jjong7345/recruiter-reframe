(ns recruit-app.declined-view.views
  (:require [recruit-app.components.layout :as layout]
            [recruit-app.components.header :as header]
            [recruit-app.components.typography :as type]
            [recruit-app.components.button :as btn]))

(defn declined-view
  "Renders message to user that their account has been declined"
  []
  [layout/column
   :children [[header/page-header
               :header-text "There's a problem with your profile"]
              [layout/page-content
               [layout/column
                :padding 0
                :class "col-xs-5"
                :children [[layout/row
                            :padding-top 30
                            :children [[type/body-copy-light "Thank you for your interest in Ladders. After careful review of the information you provided, we are unable to approve you for access to our site. This can happen for a number of reasons, but the most common reason is that it's come to our attention that you're using your profile for something other than its intended purpose."]]]
                           [layout/row
                            :children [[type/body-copy-light "At Ladders, we cater exclusively to the career-level job market. We check every recruiter and job post to ensure they are qualified for this market. Your information did not meet this criteria."]]]
                           [layout/row
                            :children [[type/body-copy-light "If you feel you have received this message in error, or have any questions about your account, please contact Recruiter Relations at 1-800-871-2574."]]]
                           [layout/row
                            :children [[type/body-copy-light "We apologize for any inconvenience, and appreciate your cooperation."]]]
                           [layout/row
                            :padding-bottom 0
                            :children [[type/body-copy-light "Regards,"]]]
                           [layout/row
                            :padding 0
                            :children [[type/body-copy-light "Recruiter Relations Team"]]]
                           [layout/row
                            :padding 0
                            :children [[type/body-copy-light "Ladders, Inc"]]]
                           [layout/row-bottom
                            :padding 30
                            :children [[btn/primary-button-href
                                        :label "Email recruiter support"
                                        :href "mailto:approvals@theladders.com"]]]]]]]])
