(ns ring.middleware.test.request-headers
  (:use clojure.test
        ring.middleware.request-headers))

(def some-parsing-rules
  {"accept" (fn [value]
              (let [[media-range & params] (.split value ";")]
                (if (empty? params)
                  media-range
                  (let [parameters (map (fn [s] (seq (.split (.trim s) "=")))
                                        params)]
                    [media-range
                     (apply hash-map (flatten parameters))]))))})

(def echo (wrap-request-headers identity))
(def echo-with-rules (wrap-request-headers identity some-parsing-rules))

(deftest wrap-request-headers-splits-multiple-values
  (let [req {:headers {"accept" "text/html, text/plain"}}]
    (is (= (list "text/html" "text/plain")
           (get-in (echo req) [:headers "accept"])))))

(deftest wrap-request-headers-trims-whitespaces-around-value
  (let [req {:headers {"accept" " text/html  ,text/plain;  q=0.2 "}}]
    (is (= (list "text/html" "text/plain;  q=0.2")
           (get-in (echo req) [:headers "accept"])))))

(deftest wrap-request-headers-makes-singletons-from-single-values
  (let [req {:headers {"accept" "text/html"}}]
    (is (= (list "text/html")
           (get-in (echo req) [:headers "accept"])))))

(deftest wrap-request-headers-works-for-empty-headers
  (let [req {:headers {}}]
    (is (= {} (get (echo req) :headers)))))

(deftest wrap-request-headers-handles-all-the-headers
  (let [req {:headers {"accept" "text/html, text/plain"
                       "accept-charset" "utf-8"
                       "accept-encoding" "*"}}]
    (is (= {"accept" (list "text/html" "text/plain")
            "accept-charset" (list "utf-8")
            "accept-encoding" (list "*")}
           (get (echo req) :headers)))))

(deftest wrap-request-headers-preserves-rest-of-request
  (let [req {:server-port 8080
             :server-name "127.0.0.1"
             :remote-addr "127.0.0.1"
             :uri "/just/for/test"
             :scheme :http
             :request-method :head
             :headers {}}]
    (is (= req (echo req)))))

(deftest wrap-request-headers-parses-header-values
  (let [req {:headers {"accept" "text/html; q=0.2;level=1, text/plain"}}]
    (is (= (list ["text/html" {"q" "0.2" "level" "1"}] "text/plain")
           (get-in (echo-with-rules req) [:headers "accept"])))))
