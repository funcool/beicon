(require '[clojure.java.shell :as shell])
(require '[figwheel.main.api :as figwheel])
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
  {:main 'beicon.tests.test-core
   :output-to "out/tests.js"
   :output-dir "out/tests"
   :source-map "out/tests.js.map"
   :language-in  :ecmascript5
   :language-out :ecmascript5
   :target :nodejs
   :pretty-print true
   :pseudo-names true
   :verbose true})

(defmethod task "test"
  [[_ type]]
  (letfn [(build [optimizations]
            (api/build (api/inputs "src" "test")
                       (cond->  (assoc options :optimizations optimizations)
                         (= optimizations :none) (assoc :source-map true))))

          (run-tests []
            (let [{:keys [out err]} (shell/sh "node" "out/tests.js")]
              (println out err)
              ))

          (test-once []
            (build :none)
            (run-tests)
            (shutdown-agents))

          (test-watch []
            (println "Start watch loop...")
            (try
              (api/watch (api/inputs "src" "test")
                         (assoc options
                                :parallel-build false
                                :watch-fn run-tests
                                :cache-analysis false
                                :optimizations :none
                                :source-map true))
              (catch Exception e
                (println "ERROR:" e)
                (Thread/sleep 2000)
                (test-watch))))]

    (case type
      (nil "once") (test-once)
      "watch"      (test-watch)
      "build-none"     (build :none)
      "build-simple"   (build :simple)
      "build-advanced" (build :advanced)
      (do (println "Unknown argument to test task:" type)
          (System/exit 1)))))

(defmethod task "figwheel"
  [args]
  (figwheel/start
   {:id "dev"
    :options {:main 'beicon.user
              ;; :output-to "out/tests.js"
              ;; :output-dir "out/tests"
              ;; :source-map true
              :target :nodejs}
    :config {:open-url false
             :auto-testing false
             :watch-dirs ["src" "test"]}}))

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
