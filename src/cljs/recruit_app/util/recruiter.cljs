(ns recruit-app.util.recruiter)

(defn valid-recruiter-id?
  "Returns true if value is not nil and greater than zero"
  [recruiter-id]
  (and (some? recruiter-id) (< 0 (js/parseInt recruiter-id))))

(def editable-keys
  "Keys from the frontend map that should be sent to the backend upon edit"
  [:firstname
   :lastname
   :email
   :telephone
   :recruiter-website-url
   :job-function
   :recruiter-guest-role
   :title
   :street
   :city
   :state-province
   :postal-code
   :country
   :paid-membership
   :roles
   :superuser?])
