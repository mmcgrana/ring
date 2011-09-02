(ns ring.middleware.session.test.httpsession
  (:use clojure.test
        [ring.middleware.session store httpsession])
  (:import
    (javax.servlet.http HttpSession HttpServletRequest)))

(defn mock-session []
  (let [data (atom {})]
    (proxy [HttpSession] []
      (getAttribute [key] (@data key))
      (setAttribute [key val] (swap! data assoc key val))
      (removeAttribute [key] (swap! data dissoc key)))))

(defn mock-request []
  (proxy [HttpServletRequest] []
    (getSession [] (mock-session))))

(declare *store*)

(defn wrap-test
  [test-fn]
  (binding [*store* (http-session-store "test-app")]
    ((wrap-http-session-store test-fn) { :request (mock-request) })))

(deftest http-session-read-not-exist
  (wrap-test (fn [_]
    (is (read-session *store* "non-existent")))))

(deftest http-session-session-create
  (wrap-test (fn [_]
    (let [sess-key (write-session *store* nil {:foo "bar"})]
      (is (read-session *store* "non-existent"))))))

(deftest http-session-session-update
  (wrap-test (fn [_]
    (let [sess-key  (write-session *store* nil {:foo "bar"})]
      ;; this store ignores session key, as the container
      ;; is scoping the session automatically for us
      (is (= (read-session *store* sess-key) {:foo "bar"}))
      (is (= (read-session *store* "blah") {:foo "bar"}))))))

(deftest http-session-session-delete
  (wrap-test (fn [_]
    (let [sess-key  (write-session *store* nil {:foo "bar"})
          sess-key* (delete-session *store* sess-key)]
      (is (= (read-session *store* sess-key*) {}))))))
