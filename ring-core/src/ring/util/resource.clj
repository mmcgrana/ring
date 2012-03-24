(ns ring.util.resource
  "utility function for reading in resources"
  (:use hiccup.core)
  (:require [clojure.java.io :as io]))

(defn style-resource [path]
  "Given a resource path, read the resource and return it as a html style element fragment"
  (html [:style {:type "text/css"} (slurp (io/resource path))]))
