(ns recruit-app.util.education)

(defn edu-name [id]
  (case id 3 "Associates"
          5 "Bachelors"
          7 "Masters"
          8 "Masters"
          10 "Other Masters"
          13 "EdD"
          14 "MD"
          1 "None"
          4 "Bachelors"
          6 "Other Bachelors"
          11 "PhD"
          12 "PsyD"
          15 "J.D."
          16 "Other Doctorate"
          2 "H.S. Diploma"
          9 "MBA"
           ""))

