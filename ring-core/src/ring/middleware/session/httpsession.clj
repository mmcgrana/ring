(ns ring.middleware.session.httpsession
  "SessionStore that uses the servlet container's HttpSession implementation, which
  in some cases is configurable.  Very suitable for clustered environments like
  Google App Engine where you want to ensure that the session is replicated
  across servers automatically."
  (:use ring.middleware.session.store)
  (:import
    (java.io ByteArrayInputStream ByteArrayOutputStream)
    (java.io ObjectInputStream ObjectOutputStream)))

(declare *session*)

(defn serialize
  "Uses Java object serialization to convert obj into a byte array. obj, and all its contents
  must be primitives or implement java.io.Serializable."
  [obj]
  (let [bos (ByteArrayOutputStream.)]
    (with-open [oos (ObjectOutputStream. bos)]
      (.writeObject oos obj))
    (.toByteArray bos)))

(defn deserialize
  "Uses Java object serialization to convert bytes into a Java object"
  [bytes]
  (let [bis (ByteArrayInputStream. bytes)]
    (with-open [ois (ObjectInputStream. bis)]
      (.readObject ois))))

; SessionStore implementation backed by the HttpSession for the current user.
; session-key is used as the single HttpSession attribute for the entire ring
; session.
(deftype HttpSessionStore
  [session-key]
  SessionStore
  (read-session [_ _]
    (let [val (.getAttribute *session* session-key)]
      (if val (deserialize val) {})))
  (write-session [_ key data]
    (.setAttribute *session* session-key (serialize data))
    key)
  (delete-session [_ _]
    (.removeAttribute *session* session-key)
    nil))

(defn http-session-store
  "Creates a HttpSessionStore with the given key.  Each unique ring application
  should use a different session-key to avoid collisions."
  [session-key]
  (HttpSessionStore. session-key))

(defn wrap-http-session-store
  "Middleware hook for HttpSessionStore"
  [handler]
  (fn [request]
    (binding [*session* (.getSession (:request request))]
      (handler request))))
