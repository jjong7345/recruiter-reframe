(ns recruit-app.util.dropdowns)

(defn function
  []
  [{:id 0 :label "Select"}
   {:id 9 :label "Corporate HR"}
   {:id 10 :label "Executive/Agency Recruitment"}
   {:id 11 :label "Recruitment Advertising Agency"}
   {:id 2 :label "Sales"}
   {:id 3 :label "Marketing"}
   {:id 4 :label "Finance"}
   {:id 5 :label "Technology"}
   {:id 6 :label "Operations/General Management"}
   {:id 7 :label "Law"}
   {:id 8 :label "Other"}])

(defn role
  [function]
  (cond (= function 0) [{:id 0 :label "Choose Function First"}]
        (= function 9) [{:id 0 :label "Select"}
                        {:id 14 :label "Corporate HR Hiring Manager"}
                        {:id 6 :label "Corporate HR Recruiter"}
                        {:id 5 :label "Corporate HR Director"}
                        {:id 4 :label "Corporate HR Executive"}]
        (= function 10) [{:id 0 :label "Select"}
                         {:id 1 :label "Contingency Search"}
                         {:id 15 :label "Staffing Agency"}
                         {:id 2 :label "Retained Search"}]
        (= function 11) [{:id 0 :label "Select"}
                         {:id 17 :label "Project Manager"}
                         {:id 16 :label "Account Director"}
                         {:id 18 :label "Interactive Strategy"}
                         {:id 19 :label "Business Development"}]
        :else [{:id 0 :label "Select"}
               {:id 13 :label "Other"}
               {:id 12 :label "Owner/Proprietor"}
               {:id 11 :label "Staff"}
               {:id 8 :label "VP"}
               {:id 10 :label "Manager"}
               {:id 7 :label "C-Level"}
               {:id 9 :label "Director"}]))

(defn search-role []
 [{:id [2101, 2102, 2103, 2104, 2105, 2201, 2202, 2203, 2204, 2205, 2206, 2207, 2901, 2902, 2903, 2904, 2905, 2906,
        2907, 2908, 2401, 2402, 2403, 2404, 2405, 2406, 2407, 2408, 2409, 2410, 2411, 2412, 2413, 2001, 2002, 2003,
        2004, 2005, 2501, 2502, 2503, 2504, 2505, 2506, 2507, 2801, 2802, 2803, 2804, 2805, 2806, 2807, 2809, 2601,
        2602, 2603, 2604, 2605, 2606, 2301, 2302, 2303, 2304, 2305, 2306, 2307, 2701, 2702, 2703] :label "All"}
  {:id [2301 2302 2303 2304 2305 2306 2307] :label "Accounting & Finance"}
  {:id [2801 2802 2803 2804 2805 2806 2807 2809] :label "Engineering & Construction"}
  {:id [2901 2902 2903 2904 2905 2906 2907 2908] :label "Healthcare"}
  {:id [2601 2602 2603 2604 2605 2606] :label "Human Resources & Legal"}
  {:id [2201 2202 2203 2204 2205 2206 2207] :label "Marketing, Media & Design"}
  {:id [2501 2502 2503 2504 2505 2506 2507] :label "Operations & General Management"}
  {:id [2701 2702 2703] :label "Project Management"}
  {:id [2101 2102 2103 2104 2105] :label "Sales & Business Development"}
  {:id [2001 2002 2003 2004 2005] :label "Science & Education"}
  {:id [2401 2402 2403 2404 2405 2406 2407 2408 2409 2410 2411 2412 2413] :label "Technology"}])

(defn search-education []
  [{:id 1 :label "All"}
   {:id 2 :label "HS Diploma"}
   {:id 3 :label "Associates"}
   {:id 4 :label "Bachelor"}
   {:id 5 :label "Master"}
   {:id 6 :label "Doctorate"}])

(defn search-distance []
  [{:id nil :label "No Limit"}
   {:id 5 :label "5 mi"}
   {:id 10 :label "10 mi"}
   {:id 15 :label "15 mi"}
   {:id 25 :label "25 mi"}
   {:id 50 :label "50 mi"}
   {:id 75 :label "75 mi"}
   {:id 100 :label "100 mi"}
   {:id 125 :label "125 mi"}])

(defn greetings [name]
  [{:id 0 :label (str "Dear " name ", ")}
   {:id 1 :label (str "Hello " name ", ")}
   {:id 2 :label (str "Hi " name ", ")}
   {:id 3 :label (str name ", ")}])

(defn industry []
  [{:id 0 :label "Select Industry"}
   {:id 1001 :label "Accounting, Finance & Insurance "}
   {:id 1002 :label "Aerospace & Defense"}
   {:id 1003 :label "Agriculture & Natural Resources"}
   {:id 1004 :label "Automotive"}
   {:id 1005 :label "Energy & Utilities"}
   {:id 1006 :label "Food & Beverages"}
   {:id 1007 :label "Education"}
   {:id 1008 :label "Healthcare"}
   {:id 1009 :label "Hospitality & Recreation"}
   {:id 1010 :label "Manufacturing"}
   {:id 1011 :label "Media"}
   {:id 1012 :label "Government & Non-Profit"}
   {:id 1013 :label "Pharmaceuticals & Biotech"}
   {:id 1014 :label "Professional, Scientific & Technical Services"}
   {:id 1015 :label "Real Estate & Construction"}
   {:id 1016 :label "Retail & Consumer Goods"}
   {:id 1017 :label "Technology"}
   {:id 1018 :label "Telecommunications"}
   {:id 1019 :label "Transportation"}
   {:id 1020 :label "Wholesale"}])

(defn employees []
  [{:id 0 :label "Select"}
   {:id 4 :label "1-100"}
   {:id 5 :label "101-1,000"}
   {:id 6 :label "1,001-10,000"}
   {:id 7 :label "10,000+"}])

(defn salaries [default]
  [{:id 0 :label default}
   {:id 40 :label "40K"}
   {:id 50 :label "50K"}
   {:id 60 :label "60K"}
   {:id 70 :label "70K"}
   {:id 80 :label "80K"}
   {:id 90 :label "90K"}
   {:id 100 :label "100K"}
   {:id 110 :label "110K"}
   {:id 120 :label "120K"}
   {:id 130 :label "130K"}
   {:id 140 :label "140K"}
   {:id 150 :label "150K"}
   {:id 160 :label "160K"}
   {:id 170 :label "170K"}
   {:id 180 :label "180K"}
   {:id 190 :label "190K"}
   {:id 200 :label "200K"}
   {:id 225 :label "225K"}
   {:id 250 :label "250K"}
   {:id 275 :label "275K"}
   {:id 300 :label "300K"}
   {:id 325 :label "325K"}
   {:id 350 :label "350K"}
   {:id 375 :label "375K"}
   {:id 400 :label "400K"}
   {:id 425 :label "425K"}
   {:id 450 :label "450K"}
   {:id 475 :label "475K"}
   {:id 500 :label "500K+"}])

(defn states
  []
  [{:id "" :label "Select"}
   {:id "AK" :label "Alaska"}
   {:id "AL" :label "Alabama"}
   {:id "AR" :label "Arkansas"}
   {:id "AZ" :label "Arizona"}
   {:id "CA" :label "California"}
   {:id "CO" :label "Colorado"}
   {:id "CT" :label "Connecticut"}
   {:id "DC" :label "Washington DC"}
   {:id "DE" :label "Delaware"}
   {:id "FL" :label "Florida"}
   {:id "GA" :label "Georgia"}
   {:id "GU" :label "Guam"}
   {:id "HI" :label "Hawaii"}
   {:id "IA" :label "Iowa"}
   {:id "ID" :label "Idaho"}
   {:id "IL" :label "Illinois"}
   {:id "IN" :label "Indiana"}
   {:id "KS" :label "Kansas"}
   {:id "KY" :label "Kentucky"}
   {:id "LA" :label "Louisiana"}
   {:id "MA" :label "Massachusetts"}
   {:id "MD" :label "Maryland"}
   {:id "ME" :label "Maine"}
   {:id "MI" :label "Michigan"}
   {:id "MN" :label "Minnesota"}
   {:id "MO" :label "Missouri"}
   {:id "MS" :label "Mississippi"}
   {:id "MT" :label "Montana"}
   {:id "NC" :label "North Carolina"}
   {:id "ND" :label "North Dakota"}
   {:id "NE" :label "Nebraska"}
   {:id "NH" :label "New Hampshire"}
   {:id "NJ" :label "New Jersey"}
   {:id "NM" :label "New Mexico"}
   {:id "NV" :label "Nevada"}
   {:id "NY" :label "New York"}
   {:id "OH" :label "Ohio"}
   {:id "OK" :label "Oklahoma"}
   {:id "OR" :label "Oregon"}
   {:id "PA" :label "Pennsylvania"}
   {:id "RI" :label "Rhode Island"}
   {:id "SC" :label "South Carolina"}
   {:id "SD" :label "South Dakota"}
   {:id "TN" :label "Tennessee"}
   {:id "TX" :label "Texas"}
   {:id "UT" :label "Utah"}
   {:id "VA" :label "Virginia"}
   {:id "VT" :label "Vermont"}
   {:id "WA" :label "Washington"}
   {:id "WI" :label "Wisconsin"}
   {:id "WV" :label "West Virginia"}
   {:id "WY" :label "Wyoming"}
   {:id "AB" :label "Alberta"}
   {:id "BC" :label "British Columbia"}
   {:id "MB" :label "Manitoba"}
   {:id "NB" :label "New Brunswick"}
   {:id "NL" :label "Newfoundland"}
   {:id "NS" :label "Nova Scotia"}
   {:id "NT" :label "NW Territories"}
   {:id "ON" :label "Ontario"}
   {:id "PE" :label "Prince Edward Is"}
   {:id "QC" :label "Quebec"}
   {:id "SK" :label "Saskatchewan"}
   {:id "YT" :label "Yukon"}])

(defn ats
  []
  [{:id ""      :label "Select"}
   {:id "lever" :label "Lever"}
   {:id "greenhouse" :label "Greenhouse"}
   {:id "icims" :label "iCIMS"}
   {:id "bigbiller" :label "Big Biller / Top Echelon"}
   {:id "taleo" :label "Taleo"}
   {:id "jobvite" :label "Jobvite"}
   {:id "silkroad" :label "Silkroad"}
   {:id "ultipro" :label "Ultipro"}
   {:id "kenexabrassring" :label "Kenexa Brassring"}
   {:id "jazz" :label "Jazz (Resumator)"}
   {:id "workable" :label "Workable"}
   {:id "smartrecruiters" :label "Smartrecruiters"}
   {:id "successfactors" :label "Successfactors (Sap)"}
   {:id "workday" :label "Workday"}
   {:id "ziprecruiter" :label "Ziprecruiter"}
   {:id "jobdiva" :label "Jobdiva"}
   {:id "bullhorn" :label "Bullhorn"}
   {:id "other" :label "Other"}])

(defn countries
  []
  [{:label "Select", :id ""}
   {:label "Afghanistan", :id "AF"}
   {:label "land Islands", :id "AX"}
   {:label "Albania", :id "AL"}
   {:label "Algeria", :id "DZ"}
   {:label "American Samoa", :id "AS"}
   {:label "AndorrA", :id "AD"}
   {:label "Angola", :id "AO"}
   {:label "Anguilla", :id "AI"}
   {:label "Antarctica", :id "AQ"}
   {:label "Antigua and Barbuda", :id "AG"}
   {:label "Argentina", :id "AR"}
   {:label "Armenia", :id "AM"}
   {:label "Aruba", :id "AW"}
   {:label "Australia", :id "AU"}
   {:label "Austria", :id "AT"}
   {:label "Azerbaijan", :id "AZ"}
   {:label "Bahamas", :id "BS"}
   {:label "Bahrain", :id "BH"}
   {:label "Bangladesh", :id "BD"}
   {:label "Barbados", :id "BB"}
   {:label "Belarus", :id "BY"}
   {:label "Belgium", :id "BE"}
   {:label "Belize", :id "BZ"}
   {:label "Benin", :id "BJ"}
   {:label "Bermuda", :id "BM"}
   {:label "Bhutan", :id "BT"}
   {:label "Bolivia", :id "BO"}
   {:label "Bosnia and Herzegovina", :id "BA"}
   {:label "Botswana", :id "BW"}
   {:label "Bouvet Island", :id "BV"}
   {:label "Brazil", :id "BR"}
   {:label "British Indian Ocean Territory", :id "IO"}
   {:label "Brunei Darussalam", :id "BN"}
   {:label "Bulgaria", :id "BG"}
   {:label "Burkina Faso", :id "BF"}
   {:label "Burundi", :id "BI"}
   {:label "Cambodia", :id "KH"}
   {:label "Cameroon", :id "CM"}
   {:label "Canada", :id "CA"}
   {:label "Cape Verde", :id "CV"}
   {:label "Cayman Islands", :id "KY"}
   {:label "Central African Republic", :id "CF"}
   {:label "Chad", :id "TD"}
   {:label "Chile", :id "CL"}
   {:label "China", :id "CN"}
   {:label "Christmas Island", :id "CX"}
   {:label "Cocos (Keeling) Islands", :id "CC"}
   {:label "Colombia", :id "CO"}
   {:label "Comoros", :id "KM"}
   {:label "Congo", :id "CG"}
   {:label "Congo, The Democratic Republic of the", :id "CD"}
   {:label "Cook Islands", :id "CK"}
   {:label "Costa Rica", :id "CR"}
   {:label "Cote D\"Ivoire", :id "CI"}
   {:label "Croatia", :id "HR"}
   {:label "Cuba", :id "CU"}
   {:label "Cyprus", :id "CY"}
   {:label "Czech Republic", :id "CZ"}
   {:label "Denmark", :id "DK"}
   {:label "Djibouti", :id "DJ"}
   {:label "Dominica", :id "DM"}
   {:label "Dominican Republic", :id "DO"}
   {:label "Ecuador", :id "EC"}
   {:label "Egypt", :id "EG"}
   {:label "El Salvador", :id "SV"}
   {:label "Equatorial Guinea", :id "GQ"}
   {:label "Eritrea", :id "ER"}
   {:label "Estonia", :id "EE"}
   {:label "Ethiopia", :id "ET"}
   {:label "Falkland Islands (Malvinas)", :id "FK"}
   {:label "Faroe Islands", :id "FO"}
   {:label "Fiji", :id "FJ"}
   {:label "Finland", :id "FI"}
   {:label "France", :id "FR"}
   {:label "French Guiana", :id "GF"}
   {:label "French Polynesia", :id "PF"}
   {:label "French Southern Territories", :id "TF"}
   {:label "Gabon", :id "GA"}
   {:label "Gambia", :id "GM"}
   {:label "Georgia", :id "GE"}
   {:label "Germany", :id "DE"}
   {:label "Ghana", :id "GH"}
   {:label "Gibraltar", :id "GI"}
   {:label "Greece", :id "GR"}
   {:label "Greenland", :id "GL"}
   {:label "Grenada", :id "GD"}
   {:label "Guadeloupe", :id "GP"}
   {:label "Guam", :id "GU"}
   {:label "Guatemala", :id "GT"}
   {:label "Guernsey", :id "GG"}
   {:label "Guinea", :id "GN"}
   {:label "Guinea-Bissau", :id "GW"}
   {:label "Guyana", :id "GY"}
   {:label "Haiti", :id "HT"}
   {:label "Heard Island and Mcdonald Islands", :id "HM"}
   {:label "Holy See (Vatican City State)", :id "VA"}
   {:label "Honduras", :id "HN"}
   {:label "Hong Kong", :id "HK"}
   {:label "Hungary", :id "HU"}
   {:label "Iceland", :id "IS"}
   {:label "India", :id "IN"}
   {:label "Indonesia", :id "ID"}
   {:label "Iran, Islamic Republic Of", :id "IR"}
   {:label "Iraq", :id "IQ"}
   {:label "Ireland", :id "IE"}
   {:label "Isle of Man", :id "IM"}
   {:label "Israel", :id "IL"}
   {:label "Italy", :id "IT"}
   {:label "Jamaica", :id "JM"}
   {:label "Japan", :id "JP"}
   {:label "Jersey", :id "JE"}
   {:label "Jordan", :id "JO"}
   {:label "Kazakhstan", :id "KZ"}
   {:label "Kenya", :id "KE"}
   {:label "Kiribati", :id "KI"}
   {:label "Korea, Democratic People\"S Republic of", :id "KP"}
   {:label "Korea, Republic of", :id "KR"}
   {:label "Kuwait", :id "KW"}
   {:label "Kyrgyzstan", :id "KG"}
   {:label "Lao People\"S Democratic Republic", :id "LA"}
   {:label "Latvia", :id "LV"}
   {:label "Lebanon", :id "LB"}
   {:label "Lesotho", :id "LS"}
   {:label "Liberia", :id "LR"}
   {:label "Libyan Arab Jamahiriya", :id "LY"}
   {:label "Liechtenstein", :id "LI"}
   {:label "Lithuania", :id "LT"}
   {:label "Luxembourg", :id "LU"}
   {:label "Macao", :id "MO"}
   {:label "Macedonia, The Former Yugoslav Republic of", :id "MK"}
   {:label "Madagascar", :id "MG"}
   {:label "Malawi", :id "MW"}
   {:label "Malaysia", :id "MY"}
   {:label "Maldives", :id "MV"}
   {:label "Mali", :id "ML"}
   {:label "Malta", :id "MT"}
   {:label "Marshall Islands", :id "MH"}
   {:label "Martinique", :id "MQ"}
   {:label "Mauritania", :id "MR"}
   {:label "Mauritius", :id "MU"}
   {:label "Mayotte", :id "YT"}
   {:label "Mexico", :id "MX"}
   {:label "Micronesia, Federated States of", :id "FM"}
   {:label "Moldova, Republic of", :id "MD"}
   {:label "Monaco", :id "MC"}
   {:label "Mongolia", :id "MN"}
   {:label "Montenegro", :id "ME"}
   {:label "Montserrat", :id "MS"}
   {:label "Morocco", :id "MA"}
   {:label "Mozambique", :id "MZ"}
   {:label "Myanmar", :id "MM"}
   {:label "Namibia", :id "NA"}
   {:label "Nauru", :id "NR"}
   {:label "Nepal", :id "NP"}
   {:label "Netherlands", :id "NL"}
   {:label "Netherlands Antilles", :id "AN"}
   {:label "New Caledonia", :id "NC"}
   {:label "New Zealand", :id "NZ"}
   {:label "Nicaragua", :id "NI"}
   {:label "Niger", :id "NE"}
   {:label "Nigeria", :id "NG"}
   {:label "Niue", :id "NU"}
   {:label "Norfolk Island", :id "NF"}
   {:label "Northern Mariana Islands", :id "MP"}
   {:label "Norway", :id "NO"}
   {:label "Oman", :id "OM"}
   {:label "Pakistan", :id "PK"}
   {:label "Palau", :id "PW"}
   {:label "Palestinian Territory, Occupied", :id "PS"}
   {:label "Panama", :id "PA"}
   {:label "Papua New Guinea", :id "PG"}
   {:label "Paraguay", :id "PY"}
   {:label "Peru", :id "PE"}
   {:label "Philippines", :id "PH"}
   {:label "Pitcairn", :id "PN"}
   {:label "Poland", :id "PL"}
   {:label "Portugal", :id "PT"}
   {:label "Puerto Rico", :id "PR"}
   {:label "Qatar", :id "QA"}
   {:label "Reunion", :id "RE"}
   {:label "Romania", :id "RO"}
   {:label "Russian Federation", :id "RU"}
   {:label "RWANDA", :id "RW"}
   {:label "Saint Helena", :id "SH"}
   {:label "Saint Kitts and Nevis", :id "KN"}
   {:label "Saint Lucia", :id "LC"}
   {:label "Saint Pierre and Miquelon", :id "PM"}
   {:label "Saint Vincent and the Grenadines", :id "VC"}
   {:label "Samoa", :id "WS"}
   {:label "San Marino", :id "SM"}
   {:label "Sao Tome and Principe", :id "ST"}
   {:label "Saudi Arabia", :id "SA"}
   {:label "Senegal", :id "SN"}
   {:label "Serbia", :id "RS"}
   {:label "Seychelles", :id "SC"}
   {:label "Sierra Leone", :id "SL"}
   {:label "Singapore", :id "SG"}
   {:label "Slovakia", :id "SK"}
   {:label "Slovenia", :id "SI"}
   {:label "Solomon Islands", :id "SB"}
   {:label "Somalia", :id "SO"}
   {:label "South Africa", :id "ZA"}
   {:label "South Georgia and the South Sandwich Islands", :id "GS"}
   {:label "Spain", :id "ES"}
   {:label "Sri Lanka", :id "LK"}
   {:label "Sudan", :id "SD"}
   {:label "Surilabel", :id "SR"}
   {:label "Svalbard and Jan Mayen", :id "SJ"}
   {:label "Swaziland", :id "SZ"}
   {:label "Sweden", :id "SE"}
   {:label "Switzerland", :id "CH"}
   {:label "Syrian Arab Republic", :id "SY"}
   {:label "Taiwan, Province of China", :id "TW"}
   {:label "Tajikistan", :id "TJ"}
   {:label "Tanzania, United Republic of", :id "TZ"}
   {:label "Thailand", :id "TH"}
   {:label "Timor-Leste", :id "TL"}
   {:label "Togo", :id "TG"}
   {:label "Tokelau", :id "TK"}
   {:label "Tonga", :id "TO"}
   {:label "Trinidad and Tobago", :id "TT"}
   {:label "Tunisia", :id "TN"}
   {:label "Turkey", :id "TR"}
   {:label "Turkmenistan", :id "TM"}
   {:label "Turks and Caicos Islands", :id "TC"}
   {:label "Tuvalu", :id "TV"}
   {:label "Uganda", :id "UG"}
   {:label "Ukraine", :id "UA"}
   {:label "United Arab Emirates", :id "AE"}
   {:label "United Kingdom", :id "GB"}
   {:label "United States", :id "US"}
   {:label "United States Minor Outlying Islands", :id "UM"}
   {:label "Uruguay", :id "UY"}
   {:label "Uzbekistan", :id "UZ"}
   {:label "Vanuatu", :id "VU"}
   {:label "Venezuela", :id "VE"}
   {:label "Viet Nam", :id "VN"}
   {:label "Virgin Islands, British", :id "VG"}
   {:label "Virgin Islands, U.S.", :id "VI"}
   {:label "Wallis and Futuna", :id "WF"}
   {:label "Western Sahara", :id "EH"}
   {:label "Yemen", :id "YE"}
   {:label "Zambia", :id "ZM"}
   {:label "Zimbabwe", :id "ZW"}])

(def inventory-actions
  "Options for superuser interacting with inventory"
  [{:id :purchase-inventory :label "Purchase Inventory"}
   {:id :use-inventory :label "Use Inventory"}])

(def inventory-types
  "Options for types of inventory to take action on in superuser menu"
  [{:id :promoted-job :label "Promoted Job"}])

(defn companytype
  []
  [{:id 1  :label "Corporate"}
   {:id 2 :label "Recruiting Firm"}])

(def email-frequency
  "Options for email frequency when edit saved search"
  [{:id 0 :label "Do Not Email"}
   {:id 1 :label "Daily"}
   {:id 2 :label "Once a Week"}])

(def email-interval
  "Options for email interval when edit saved search"
  [{:id 1 :label "Sunday"}
   {:id 2 :label "Monday"}
   {:id 3 :label "Tuesday"}
   {:id 4 :label "Wednesday"}
   {:id 5 :label "Thursday"}
   {:id 6 :label "Friday"}
   {:id 7 :label "Saturday"}])

(def team-roles
  "Roles within a recruiter team"
  [{:id "Staff" :label "Staff"}
   {:id "Administrator" :label "Administrator"}])
