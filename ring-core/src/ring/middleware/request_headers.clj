(ns ring.middleware.request-headers
  "Request header parsing.")

(defn- read-header-value
  "Read header value string and parse it into a Clojure object using
  provided parsing function. If no function provided, return identity.

  parsing-rules is a map from header names to parsing functions."
  [parsing-rules header-name value]
  ((get parsing-rules
        header-name
        identity) value))

(defn- split-header-value
  "Split header value string containing multiple values into a
  sequence of values. If the string has only one value, return a
  singleton."
  [value]
  (map (fn [s] (.trim s))
       (.split value ",")))

(defn wrap-request-headers
  "Split all request header value strings into a sequence of
  strings. If parsing-rules are provided, parse every string according
  to the rules.

  parsing-rules is a map from header names to parsing functions. A
  parsing function takes one argument, the header value as a string,
  and returns an appropriate Clojure object."
  ([handler]
     (wrap-request-headers handler {}))
  ([handler parsing-rules]
     (fn [request]
       (handler
        (reduce (fn [req header-name]
                  (update-in req
                             [:headers header-name]
                             (fn [value]
                               (map (partial read-header-value
                                             parsing-rules
                                             header-name)
                                    (split-header-value value)))))
                request
                (keys (:headers request)))))))
