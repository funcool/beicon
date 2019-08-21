(require '[clojure.java.shell :as shell])
(require '[cljs.build.api :as api])
(require '[badigeon.jar])
(require '[badigeon.deploy])

(defmulti task first)

(defmethod task :default
  [args]
  (let [all-tasks  (-> task methods (dissoc :default) keys sort)
        interposed (->> all-tasks (interpose ", ") (apply str))]
    (println "Unknown or missing task. Choose one of:" interposed)
    (System/exit 1)))

(def options
  {:main 'beicon.tests.main
   :output-to "out/tests.js"
   :output-dir "out/tests"
   :source-map "out/tests.js.map"
   :language-in  :ecmascript5
   :language-out :ecmascript5
   :target :nodejs
   :optimizations :advanced
   :pretty-print true
   :pseudo-names true
   :verbose true})

(defmethod task "build:tests"
  [args]
  (api/build (api/inputs "src" "test") options))

(defmethod task "jar"
  [args]
  (badigeon.jar/jar 'funcool/beicon
                    {:mvn/version "5.1.0"}
                    {:out-path "target/beicon.jar"
                     :mvn/repos '{"clojars" {:url "https://repo.clojars.org/"}}
                     :allow-all-dependencies? false}))

(defmethod task "deploy"
  [args]
  (let [artifacts [{:file-path "target/beicon.jar" :extension "jar"}
                   {:file-path "pom.xml" :extension "pom"}]]
    (badigeon.deploy/deploy
     'funcool/beicon "5.1.0"
     artifacts
     {:id "clojars" :url "https://repo.clojars.org/"}
     {:allow-unsigned? true})))

(defmethod task "build-and-deploy"
  [args]
  (task ["jar"])
  (task ["deploy"]))



;;; Build script entrypoint. This should be the last expression.

(task *command-line-args*)
