(ns ring.middleware.nested-params)

(defn parse-nested-keys
  "Parse a parameter name into a list of keys using a 'C'-like index
  notation. e.g.
    \"foo[bar][][baz]\"
    => [\"foo\" \"bar\" \"\" \"baz\"]"
  [param-name]
  (let [[_ k ks] (re-matches #"(.*?)((?:\[.*?\])*)" param-name)
        keys     (if ks (map second (re-seq #"\[(.*?)\]" ks)))]
    (cons k keys)))

(defn- assoc-nested
  "Similar to assoc-in, but treats values of blank keys as elements in a
  list."
  [m [k & ks] v]
  (conj m
        (if k
          (if-let [[j & js] ks]
            (if (= j "")
              {k (assoc-nested (get m k []) js v)}
              {k (assoc-nested (get m k {}) ks v)})
            {k v})
          v)))

(defn- param-pairs
  "Return a list of name-value pairs for a parameter map."
  [params multi-value-suffix]
  (mapcat
    (fn [[name value]]
      (if (sequential? value)
        ;; If the parameter is multi-valued, but does not end with the appropriate suffix
        ;; ("[]" in the default 'parse-nested-keys' system), it will be appended here
        ;; to allow the rest of the middleware functions to operate on it appropriately.
        (let [name (if-not (.endsWith (str name) multi-value-suffix)
                     (str name multi-value-suffix)
                     name)]
          (for [v value]
            [name v]))
        [[name value]]))
    params))

(defn- nest-params
  "Takes a flat map of parameters and turns it into a nested map of
  parameters, using the function parse to split the parameter names
  into keys."
  [params parse multi-value-suffix]
  (reduce
    (fn [m [k v]]
      (assoc-nested m (parse k) v))
    {}
    (param-pairs params multi-value-suffix)))

(defn wrap-nested-params
  "Middleware to converts a flat map of parameters into a nested map.

  Uses the function in the :key-parser option to convert parameter names
  to a list of keys. Values in keys that are empty strings are treated
  as elements in a list. Defaults to using the parse-nested-keys function.

  e.g.
    {\"foo[bar]\" \"baz\"}
    => {\"foo\" {\"bar\" \"baz\"}}

    {\"foo[]\" \"bar\"}
    => {\"foo\" [\"bar\"]}

  The string in the :multi-value-suffix option determines what a parameter with
  multiple values should end in.  If a multi-valued parameter is present, but does
  not have this suffix, it is treated as though it does.  Defaults to \"[]\",
  corresponding to the parse-nested-keys function."
  [handler & [opts]]
  (fn [request]
    (let [parse   (:key-parser opts parse-nested-keys)
          multi-value-suffix (:multi-value-suffix opts "[]")
          request (update-in request [:params] nest-params parse multi-value-suffix)]
      (handler request))))
