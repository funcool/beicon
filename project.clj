(defproject funcool/beicon "1.4.0"
  :description "Reactive Streams for ClojureScript (built on top of RxJS 5.x)"
  :url "https://github.com/funcool/beicon"
  :license {:name "Public Domain" :url "http://unlicense.org/"}
  :dependencies [[org.clojure/clojure "1.8.0" :scope "provided"]
                 [org.clojure/clojurescript "1.8.51" :scope "provided"]
                 [funcool/promesa "1.1.1" :scope "provided"]]
  :deploy-repositories {"releases" :clojars
                        "snapshots" :clojars}
  :source-paths ["src" "assets"]
  :test-paths ["test"]
  :jar-exclusions [#"\.swp|\.swo|user.clj"]

  :codeina {:sources ["src"]
            :reader :clojurescript
            :target "doc/dist/latest/api"
            :src-uri "http://github.com/funcool/beicon/blob/master/"
            :src-uri-prefix "#L"}

  :plugins [[funcool/codeina "0.4.0"]])
