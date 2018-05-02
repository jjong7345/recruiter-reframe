(ns recruit-app.util.account
  (:require [clojure.string :as cs]
            [cljs.spec.alpha :as s]
            [cljs.reader :refer [read-string]]
            [recruit-app.util.dropdowns :as dd]))

(defn get-from-phone-number
  "Extract phone number or extension from phone number formatted as like 123456789 x1234"
  [phone-number type]
  (-> phone-number
      (cs/split #"x")
      (nth (condp = type
             "phone" 0
             "extension" 1) nil)
      (str)
      (cs/trim)))

(defn profile-request
  "Create request body for update profile"
  [{:keys [job-title function role company-site linkedin facebook twitter blog bio phone ext street city state zip country]}]
  {:title job-title
   :recruiter_guest_job_function_id function
   :recruiter_guest_role_id role
   :recruiter_website_url company-site
   :recruiter_linkedin_url linkedin
   :recruiter_facebook_url facebook
   :recruiter_twitter_user_name twitter
   :recruiter_blog_url blog
   :specialty bio
   :telephone (str phone (when ext (str " x" ext)))
   :street street
   :city city
   :state_province state
   :postal_code zip
   :country country})

(def contact-preferences-map
  {:special-offers 142
   :connection-req 135
   :feedback 139
   :suggested-cand 151
   :search-based-cand 165
   :newsletter 18 })

(defn convert-to-boolean
  "Convert to Boolean if 1 or 0 is passed"
  [input]
  (condp = input
    1 true
    0 false
    input))

(defn convert-to-number
  "Convert to Number if boolean is passed"
  [input]
  (condp = input
    true 1
    false 0
    input))

(defn get-contact-preference
  "Get a contact-preference map from a vector of contact-preferences maps by key-name"
  [key-name contact-preferences]
  (->> contact-preferences
       (filter #(= (:parameter_definition_id %) ((keyword key-name) contact-preferences-map)))
       first
       :value
       read-string
       convert-to-boolean))


(defn contact_preferences-request
  "Create request body for communication preferences"
  [key-name val]
  {:communication-preference
   {:parameter-definition-id ((keyword key-name) contact-preferences-map)
    :value (cond
             (or (= key-name "connection-req") (= key-name "special-offers")) (str (convert-to-number val))
             :else (str val))}})

(defn ats-update-request
  "Create request body for ats update"
  [{:keys [api-key secondary-api-key lever-user-selected job-board-token ats-settings ats]
    :or {secondary-api-key "" job-board-token "" lever-user-selected "" api-key ""}}]
  {:recruiter-id (:recruiter-id ats-settings)
   :ats-provider (:id ats)
   :api-key api-key
   :secondary-api-key secondary-api-key
   :user-id  lever-user-selected
   :subdomain job-board-token})

(defn function-name
  [function]
  (->> (dd/function)
       (filter #(= (:id %) function))
       first
       :label))

(defn role-name
  [function role]
  (->> (dd/role function)
       (filter #(= (:id %) role))
       first
       :label))
