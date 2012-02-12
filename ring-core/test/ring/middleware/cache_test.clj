(ns ring.middleware.cache-test
  (:use clojure.test
        ring.middleware.cache))

(deftest wrap-cache-control-add-cache-control-header
  (let [app (wrap-cache-control (constantly {:headers {} :body "body"}) {})]
    (is (contains? (-> (app {})
                       :headers) "Cache-Control"))))

(deftest wrap-cache-control-adds-truthy-keys
  (let [app (wrap-cache-control (constantly {:headers {} :body "body"}) {:foo false :bar true :baz false})]
    (is (= (-> (app {})
                       :headers
                       (get "Cache-Control"))
           "bar"))))

(deftest wrap-cache-control-numbers-equals
  (let [app (wrap-cache-control (constantly {:headers {} :body "body"}) {:max-age 3600})]
    (is (= (-> (app {})
               :headers
               (get "Cache-Control"))
           "max-age=3600"))))

(deftest wrap-cache-control-comma-delimited
  (let [app (wrap-cache-control (constantly {:headers {} :body "body"}) {:foo true :max-age 300})]
    (is (= (-> (app {})
               :headers
               (get "Cache-Control"))
           "foo, max-age=300"))))