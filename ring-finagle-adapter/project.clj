(defproject ring/ring-finagle-adapter "0.3.11"
  :description "Ring Finagle adapter."
  :url "http://github.com/mmcgrana/ring"
  :dependencies [[ring/ring-core "0.3.11"]
                 [com.twitter/finagle-http "1.8.5"]]
  :repositories {"twitter" "http://maven.twttr.com/"}
  :dev-dependencies [[clj-http "0.1.3"]])
