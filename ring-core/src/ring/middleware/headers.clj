(ns ring.middleware.headers
  "Common utilties for middleware"
  (:use [clojure.contrib.java-utils :only (as-str)])
  (:require [clojure.contrib.str-utils2 :as str] ))

(defn header-option
  "Converts a header option KeyValue into a string."
  [[key val]]
  (cond 
    (true? val)  (as-str key)
    (false? val) nil
    :otherwise   (as-str key "=" val)))

(defn header-options
  "Converts a map into an HTTP header options string."
  [m delimiter]
  (str/join delimiter
    (remove nil? (map header-option m))))

(defn wrap-headers
  "Merges a map of header name and values into the response.  Overwrites 
   existing headers."
  [handler headers]
  (fn [request]
    (if-let [response (handler request)]
      (assoc response :headers
             (merge (:headers response) headers)))))
