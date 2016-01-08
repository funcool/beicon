(defproject funcool/beicon "0.5.0"
  :description "Reactive Streams for ClojureScript (built on top of RxJS)"
  :url "https://github.com/funcool/beicon"
  :license {:name "Public Domain" :url "http://unlicense.org/"}
  :dependencies [[org.clojure/clojure "1.7.0" :scope "provided"]
                 [org.clojure/clojurescript "1.7.189" :scope "provided"]
                 [funcool/promesa "0.7.0" :scope "provided"]
                 [funcool/cats "1.2.1"]]
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

  :plugins [[funcool/codeina "0.3.0"]])
