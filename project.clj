(defproject funcool/beicon "0.1.0-SNAPSHOT"
  :description "A rxjs idiomatic wrapper for ClojureScript"
  :url "https://github.com/funcool/beicon"
  :license {:name "Public Domain"
            :url "http://unlicense.org/"}
  :dependencies [[org.clojure/clojure "1.7.0" :scope "provided"]
                 [org.clojure/clojurescript "1.7.48" :scope "provided"]
                 [funcool/cats "1.0.0"]]
  :deploy-repositories {"releases" :clojars
                        "snapshots" :clojars}
  :source-paths ["src" "assets"]
  :test-paths ["test"]
  :jar-exclusions [#"\.swp|\.swo|user.clj"]
  :codeina {:sources ["src"]
            :reader :clojurescript
            :target "doc/dist/latest/api"}
  :plugins [[funcool/codeina "0.3.0"]
            [lein-externs "0.1.3"]])
