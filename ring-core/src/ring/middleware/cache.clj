(ns ring.middleware.cache
  "Adds cache control headers to response"
  (:require [ring.middleware.headers :as headers]))

(defn wrap-cache-control
   "Middleware to set the Cache-Control http header. Map entries with boolean
   values either write their key if true, or nothing if false.
   Example:
   {:max-age 3600 :public false :must-revalidate true}
   => Cache-Control: max-age=3600, must-revalidate"
   [handler header-map]
   (headers/wrap-headers handler
     {"Cache-Control" (headers/header-options header-map ", ")}))