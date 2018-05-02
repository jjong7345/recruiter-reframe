(ns recruit-app.util.email)

(defn with-to
  "Adds :to to email request for recruiter"
  [request {:keys [firstname lastname email]}]
  (assoc request :to {:address email
                      :name    (str firstname " " lastname)}))

(defn with-name
  "Adds :name of email to email request"
  [request name]
  (assoc request :name name))

(defn base-transactional-request
  "Returns request with :name and :to for given email"
  [recruiter name]
  (-> {}
      (with-to recruiter)
      (with-name name)))
