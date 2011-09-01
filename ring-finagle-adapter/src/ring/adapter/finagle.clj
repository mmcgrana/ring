(ns ring.adapter.finagle
  "Adapter for the Finagle RPC system"
  (:import com.twitter.finagle.Service
           com.twitter.util.Future
           java.net.InetSocketAddress
           com.twitter.finagle.builder.ServerBuilder
           (com.twitter.finagle.http Request Http))
  (:require [ring.util.finagle :as finagle]))

(defn- create-service
  "Returns a Finagle Service for the given Ring handler"
  [handler]
  (proxy [com.twitter.finagle.Service] []
    (apply [request]
           (let [request-map (finagle/build-request-map (Request/apply request))
                 response-map (handler request-map)]
             (Future/value (finagle/build-response response-map))))))

(defn- create-builder
  "Construct a Finagle builder"
  [options]
  (-> (ServerBuilder/get)
    (.codec (Http/get))
    (.name (:name options "HttpServer"))
    (.bindTo (InetSocketAddress.
               (:host options "localhost")
               (:port options 80)))))

(defn run-finagle
  "Server the given handler according to the options.
  Options:
    :port
    :host
    :name"
  [handler options]
  (.unsafeBuild (create-builder options) (create-service handler)))
