(ns recruit-app.member)

(def experience-bands
  {1 [nil 5]
   2 [5 7]
   3 [8 10]
   4 [11 15]
   5 [15 nil]})

(def experience-map
  {1 "Less than 5"
   2 "5 - 7"
   3 "8 - 10"
   4 "11 - 15"
   5 "15+"})

(def degree-map
  {1 "All"
   2 "HS Diploma"
   3 "Associates"
   4 "Bachelor"
   5 "Master"
   6 "Doctorate"})
