(defproject ring/ring-core "1.1.0-beta1"
  :description "Ring core libraries."
  :url "http://github.com/mmcgrana/ring"
  :dependencies [[org.clojure/clojure "1.2.1"]
                 [commons-codec "1.6"]
                 [commons-io "2.1"]
                 [commons-fileupload "1.2.1"]
                 [javax.servlet/servlet-api "2.5"]
                 [clj-time "0.3.7"]]
  :profiles
  {:dev {:dependencies [[org.clojure/clojure-contrib "1.2.0"]]}})
