(ns recruit-app.shopify.api
  (:require [pandect.algo.sha256 :refer :all]
            [cheshire.core :as json]
            [clj-time.core :as time]
            [clj-time.format :as f]
            [buddy.core.bytes :as b]
            [clojure.data.codec.base64 :refer [encode]]
            [clojure.string :as cs]
            [config.core :refer [env]]
            [ring.util.response :as rr])
  (:import [java.util Arrays Base64]
           [java.security SecureRandom]
           [javax.crypto Cipher Mac]
           [javax.crypto.spec SecretKeySpec IvParameterSpec]
           [com.google.common.primitives Bytes]))

(defn- secret-keys
  "Hashes secret and derives encryption and signature keys using AES"
  []
  (let [hash (sha256-bytes (-> env :shopify :secret))]
    {:encryption-key (SecretKeySpec. (Arrays/copyOfRange hash 0 16) "AES")
     :signature-key  (SecretKeySpec. (Arrays/copyOfRange hash 16 32) "AES")}))

(defn- user-data
  "Returns json string of data to be encrypted"
  [redirect-url email]
  (json/generate-string
    {:email      email
     :created_at (f/unparse
                   (f/formatter "yyyy-MM-dd'T'HH:mmZ")
                   (time/now))
     :return_to  redirect-url}))

(defn- encryption-cipher
  "Returns cipher in ENCRYPT_MODE"
  [^SecretKeySpec key ^IvParameterSpec iv-spec]
  (let [cipher (Cipher/getInstance "AES/CBC/PKCS5Padding")]
    (.init cipher Cipher/ENCRYPT_MODE key iv-spec)
    cipher))

(defn- iv-spec
  "Return IvParameterSpec for random 16 byte"
  [iv]
  (let [random (SecureRandom.)]
    (.nextBytes random iv)
    (IvParameterSpec. iv)))

(defn- encrypt
  [key data]
  (let [iv (byte-array 16)
        iv-spec (iv-spec iv)
        cipher (encryption-cipher key iv-spec)
        encrypted (.update cipher (.getBytes data "utf-8"))
        tail (.doFinal cipher)]
    (b/concat iv encrypted tail)))

(defn- token-array
  "Returns signed byte array"
  [^SecretKeySpec signature-key cipher-text]
  (let [hmac-hasher (Mac/getInstance "HmacSHA256")]
    (.init hmac-hasher signature-key)
    (b/concat cipher-text (.doFinal hmac-hasher cipher-text))))

(defn- base64-encode
  "Returns base64-encoded string"
  [byte-arr]
  (.encodeToString (Base64/getEncoder) byte-arr))

(defn- cleaned-string
  "Returns cleaned base64 encoded string"
  [string]
  (-> string
      (cs/replace #"\\+" "-")
      (cs/replace #"/" "_")))

(defn multipass
  "Returns multipass URL for given email"
  ([email]
   (multipass email (-> env :shopify :base-url)))
  ([email redirect-url]
   (let [{:keys [encryption-key signature-key]} (secret-keys)]
     (->> email
          (user-data redirect-url)
          (encrypt encryption-key)
          (token-array signature-key)
          (base64-encode)
          (cleaned-string)
          (str (-> env :shopify :base-url) "/account/login/multipass/")))))
