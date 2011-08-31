(ns ring.adapter.test.finagle
  (:use clojure.test
        ring.adapter.finagle
        ring.middleware.file)
  (:require [clj-http.client :as http])
  (:import java.io.File))

(defn- string-body [request]
  {:status  200
   :headers {"Content-Type" "text/plain"}
   :body    "Hello World"})

(deftest string-body-test
  (let [server (run-finagle string-body {:port 4347})]
    (try
      (Thread/sleep 2000)
      (let [response (http/get "http://localhost:4347")]
        (is (= (:status response) 200))
        (is (.startsWith (get-in response [:headers "content-type"])
                         "text/plain"))
        (is (= (:body response) "Hello World")))
      )))

(defn- seq-body [request]
  {:body (list "Hello" " " "World")})

(deftest seq-body-test
  (let [server (run-finagle seq-body {:port 4348})]
    (try
      (Thread/sleep 2000)
      (let [response (http/get "http://localhost:4348")]
        (is (= (:body response) "Hello World")))
      )))

(defn- lazy-seq-body [request]
  {:body (lazy-seq (list "Hello" " " "World" 1 2 3))})

(deftest lazy-seq-body-test
  (let [server (run-finagle lazy-seq-body {:port 4349})]
    (try
      (Thread/sleep 2000)
      (let [response (http/get "http://localhost:4349")]
        (is (= (:body response) "Hello World123")))
      )))

(defn- file-body-app [request]
  {:body (File. "test/public/clojure.png")})

(deftest file-body-test
  (let [server (run-finagle file-body-app {:port 4350})]
    (try
      (Thread/sleep 2000)
      (let [response (http/get "http://localhost:4350/clojure.png")]
        (is (= (:body response) (slurp "test/public/clojure.png"))))
      )))
