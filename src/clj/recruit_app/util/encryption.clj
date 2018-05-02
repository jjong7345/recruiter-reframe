(ns recruit-app.util.encryption
  (:import  [java.nio ByteBuffer]
            [javax.crypto Cipher]
            [javax.crypto.spec SecretKeySpec]
            [org.apache.commons.codec.binary Base32]))

(defn ubyte [val]
  (if (>= val 128)
    (byte (- val 256))
    (byte val)))

(def aes-key
  (->> [0x43 0x6C 0xDE 0x8C 0x04 0x2E 0xC1 0xC7
        0xD1 0xB0 0xFF 0x09 0x6B 0xFA 0x48 0x7C]
       (map ubyte)
       (byte-array)))

(defn get-bytes
  "Convert String to byte array"
  [s]
  (.getBytes s "UTF-8"))

(defn base32
  "Takes a byte array and encodes it into a Base32 string"
  [b]
  (.encodeAsString (Base32.) b))

(defn debase32
  "Takes a Base32 string and decodes it to a byte array"
  [s]
  (.decode (Base32.) (get-bytes s)))

(defn get-cipher
  "Create a Cipher object with our key using the AES algorithm.
  It will be used to perform encryption or decryption"
  [mode secret-key]
  (let [key-spec (SecretKeySpec. secret-key "AES")]
    (doto (Cipher/getInstance "AES")
      (.init mode key-spec))))

(defn decrypt
  "Given a chunk of data and a cryptographical key,
    returns the encrypted Base32 representation
  Expects data and key to be of type byte[]
    key must be exactly 16 bytes (128 bits) in length
    since we are using the AES algorithm"
  [data key]
  (let [cipher (get-cipher Cipher/DECRYPT_MODE key)]
    (.doFinal cipher (debase32 data))))

(defn decrypt-secureid
  "Decrypt secure id into a raw subscriber id"
  [secure-id]
  (.getInt (ByteBuffer/wrap
             (decrypt (clojure.string/replace secure-id #"^01-sid-" "") aes-key))))

(defn encrypt
  "Given a chunk of data and a cryptographical key,
    returns the encrypted Base32 representation
  Expects data and key to be of type byte[]
    key must be exactly 16 bytes (128 bits) in length
    since we are using the AES algorithm"
  [data key]
  (let [cipher (get-cipher Cipher/ENCRYPT_MODE key)]
    (base32 (.doFinal cipher data))))

(defn encrypt-subscriberid
  "Encrypt a subscriber id into a secure id"
  [subscriber-id]
  ; id is a string or an integer, we must coerce it to an Int, then get the raw byte array representing that int
  (let [buffer (ByteBuffer/allocate 4)]
    (.putInt buffer (Integer. subscriber-id))
    (clojure.string/replace
      (str "01-sid-" (encrypt (.array buffer) aes-key))
      "=" "")))
