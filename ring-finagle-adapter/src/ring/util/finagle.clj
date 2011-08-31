(ns ring.util.finagle
  "Convert between a Ring handler and an HttpRquest/Response"
  (:require [clojure.java.io :as io]
            [clojure.string :as string])
  (:import (com.twitter.finagle.http Request Response)
           (java.io File InputStream FileInputStream)
           (org.jboss.netty.buffer ChannelBuffers ChannelBuffer)))

(defn- get-headers
  "Create a name/value mpa of all request headers"
  [request]
  (reduce
    (fn [headers, ^String name]
      (assoc headers
        (.toLowerCase name)
        (->> (.getHeaders request name)
             (.iterator)
             (iterator-seq)
             (string/join ","))))
    {}
    (iterator-seq (.iterator (.getHeaderNames request)))))

(defn set-status
  "Update a HttpServletResponse with a status code."
  [response, status]
  (.setStatus response status))

(defn set-content-type [response, content-type]
  (.setContentType
   response
   (-> content-type
       (string/split #";")
       first
       string/trim)))

(defn- get-charset [content-type]
  (->> content-type
       (re-find #"charset=(.+);?")
       (second)))

(defn- set-character-encoding [response, content-type]
  (when-let [charset (get-charset content-type)]
    (.setCharacterEncoding response charset)))

(defn- set-headers
  "Set the headers on the response object"
  [response, headers]
  (doseq [[key val-or-vals] headers]
    (if (string? val-or-vals)
      (.setHeader response key val-or-vals)
      (doseq [val val-or-vals]
        (.addHeader response key val)))))

(defn- set-body
  "Update a Response body with a String, ISeq, File or InputStream."
  [response, body]
  (cond
    (string? body)
      (.setContentString response body)
    (seq? body)
      (doseq [chunk body]
        (.write response (str chunk)))
    (instance? InputStream body)
      (let [buffer (make-array Byte/TYPE 1024)]
          (loop []
            (let [size (.read body buffer)]
              (when (pos? size)
                (do (.write response (ChannelBuffers/copiedBuffer buffer 0 size))
                    (recur))))))
    (instance? File body)
      (let [^File f body]
        (with-open [stream (FileInputStream. f)]
          (set-body response stream)))
    (nil? body)
      nil
    :else
      (throw (Exception. ^String (format "Unrecognized body: %s" body)))))

(defn build-request-map
  "Create the request map from the HttpRequest"
  [^Request request]
  {:server-port        (Integer/parseInt (->> (.getHeader request "host") (re-find #":(\d+)$") (second)) 10)
   :server-name        (->> (.getHeader request "host") (re-find #"([^:]+):") (second))
   :remote-addr        (apply str (drop 1 (.toString (.remoteAddress request))))
   :uri                (.path request)
   :query-string       (apply str (drop 1 (.toString (.params request))))
   :scheme             (keyword "http"); TODO(sprsquish): make this right
   :request-method     (keyword (-> (.method request) .toString .toLowerCase))
   :headers            (get-headers request)
   :content-type       (.getHeader request "Content-Type")
   :content-length     (.length request)
   :character-encoding (.getHeader request "Content-Encoding")
   :body               (.getInputStream request)})

(defn build-response
  "Createa an HttpResponse from the Ring response-map"
  [{:keys [status headers body]}]
  (let [response (Response/apply)]
    (when status
      (.setStatusCode response status))
    (doto response
      (set-headers headers)
      (set-body body))))
