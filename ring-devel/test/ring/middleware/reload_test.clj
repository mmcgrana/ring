(ns ring.middleware.reload-test
  (:use clojure.test
        ring.middleware.reload))

(def app
  (wrap-reload (constantly :response) '(ring.middleware.reload)))

(deftest wrap-reload-smoke
  (is (= :response (app :request))))

(deftest wrap-reload-for-multiple-args
  (is (= :response (app :channel :request))))
