(ns ring.handler.dump
  "Reflect Ring requests into responses for debugging."
  (:use hiccup.core
        hiccup.page
        hiccup.def
        ring.util.resource
        ring.util.response)
  (:require [clojure.set :as set]
            [clojure.pprint :as pprint]))


(def ring-keys
  '(:server-port :server-name :remote-addr :uri :query-string :scheme
    :request-method :content-type :content-length :character-encoding
    :ssl-client-cert :headers :body))

(defhtml req-pair
  [key req]
  [:tr [:td.key  (h (str key))]
       [:td.val  (h (pr-str (key req)))]])

(defhtml template
  [req]
  (html5
   [:head
    [:meta {:http-equiv "Content-Type" :content "text/html"}]
    [:title "Ring: Request Dump"]]
   (style-resource "css/dump.css")
   [:body
    [:div#content
     [:h3.info "Ring Request Values"]
     [:table.request
      [:tbody
       (for [key ring-keys]
         (req-pair key req))]]
     (if-let [user-keys (set/difference (set (keys req)) (set ring-keys))]
       (html
        [:br]
        [:table.request.user
         [:tbody [:tr
                  (for [key (sort user-keys)]
                    (req-pair key req))]]]))]]))

(defn handle-dump
  "Returns a response tuple corresponding to an HTML dump of the request
  req as it was recieved by this app."
  [req]
  (pprint/pprint req)
  (println)
  (-> (response (template req))
    (status 200)
    (content-type "text/html")))
