(ns recruit-app.marketinghome.db)


(def generic-emails [
                      ;Default domains included */
                      "aol.com", "att.net", "comcast.net", "facebook.com", "gmail.com", "gmx.com", "googlemail.com",
                      "google.com", "hotmail.com", "hotmail.co.uk", "mac.com", "me.com", "mail.com", "msn.com",
                      "live.com", "sbcglobal.net", "verizon.net", "yahoo.com", "yahoo.co.uk",
                      ;/* Other global domains */
                      "email.com", "games.com", "gmx.net", "hush.com", "hushmail.com", "icloud.com", "inbox.com",
                      "lavabit.com", "love.com", "outlook.com", "pobox.com", "rocketmail.com",
                      "safe-mail.net", "wow.com", "ygm.com", "ymail.com" "zoho.com", "fastmail.fm",
                      "yandex.com",
                      ;/* United States ISP domains */
                      "bellsouth.net", "charter.net", "comcast.net", "cox.net", "earthlink.net", "juno.com",
                      ;/* British ISP domains */
                      "btinternet.com", "virginmedia.com", "blueyonder.co.uk", "freeserve.co.uk", "live.co.uk",
                      "ntlworld.com", "o2.co.uk", "orange.net", "sky.com", "talktalk.co.uk", "tiscali.co.uk",
                      "virgin.net", "wanadoo.co.uk", "bt.com",
                      ;/* Domains used in Asia */
                      "sina.com", "qq.com", "naver.com", "hanmail.net", "daum.net", "nate.com", "yahoo.co.jp", "yahoo.co.kr",
                      "yahoo.co.id", "yahoo.co.in", "yahoo.com.sg", "yahoo.com.ph",
                      ;/* French ISP domains */
                      "hotmail.fr", "live.fr", "laposte.net", "yahoo.fr", "wanadoo.fr", "orange.fr", "gmx.fr", "sfr.fr", "neuf.fr",
                      "free.fr",
                      ;/* German ISP domains */
                      "gmx.de", "hotmail.de", "live.de", "online.de", "t-online.de" "web.de", "yahoo.de",
                      ;/* Russian ISP domains */
                      "mail.ru", "rambler.ru", "yandex.ru", "ya.ru", "list.ru",
                      ;/* Belgian ISP domains */
                      "hotmail.be", "live.be", "skynet.be", "voo.be", "tvcablenet.be", "telenet.be",
                      ;/* Argentinian ISP domains */
                      "hotmail.com.ar", "live.com.ar", "yahoo.com.ar", "fibertel.com.ar", "speedy.com.ar", "arnet.com.ar",
                      ;/* Domains used in Mexico */
                      "hotmail.com" , "gmail.com", "yahoo.com.mx", "live.com.mx", "yahoo.com", "hotmail.es", "live.com", "hotmail.com.mx",
                      "prodigy.net.mx", "msn.com"])

(def email-regex #"(?:[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\.[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*|\"(?:[\x01-\x08\x0b\x0c\x0e-\x1f\x21\x23-\x5b\x5d-\x7f]|\\[\x01-\x09\x0b\x0c\x0e-\x7f])*\")@(?:(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?|\[(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?|[a-z0-9-]*[a-z0-9]:(?:[\x01-\x08\x0b\x0c\x0e-\x1f\x21-\x5a\x53-\x7f]|\\[\x01-\x09\x0b\x0c\x0e-\x7f])+)\])")
(defn email-domain [email] (subs email (+ 1 (clojure.string/index-of email "@"))))
(defn generic-email? [email] (boolean (some (partial = (email-domain email)) generic-emails)))
(defn valid-email-format? [email] (boolean (= email (re-matches email-regex email))))



