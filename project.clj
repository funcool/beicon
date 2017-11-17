(defproject funcool/beicon "4.0.0"
  :description "Reactive Streams for Clojure(Script)"
  :url "https://github.com/funcool/beicon"
  :license {:name "BSD (2-Clause)"
            :url "http://opensource.org/licenses/BSD-2-Clause"}

  :dependencies [[org.clojure/clojure "1.8.0" :scope "provided"]
                 [org.clojure/clojurescript "1.9.946" :scope "provided"]
                 [io.reactivex.rxjava2/rxjava "2.1.6"]]

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

  :profiles
  {:dev {:dependencies [[org.clojure/tools.namespace "0.2.11"]
                        [funcool/promesa "1.9.0"]]
         :aliases {"test-all" ["with-profile" "dev,1.9:dev,1.7:dev" "test"]}
         :global-vars {*warn-on-reflection* false}
         :plugins [[funcool/codeina "0.5.0"]
                   [lein-ancient "0.6.10"]]}
   :1.9 {:dependencies [[org.clojure/clojure "1.9.0-rc1"]]}
   :1.7 {:dependencies [[org.clojure/clojure "1.7.0"]]}})

