(require '[cljs.build.api :as b])

(def options
  {:main 'beicon.tests.test_core
   :output-to "out/tests.js"
   :output-dir "out/tests"
   :target :nodejs
   :optimizations :advanced
   :language-in :es5
   :pretty-print true
   :verbose true})

(let [start (System/nanoTime)]
  (println "Building ...")
  (b/build (b/inputs "test" "src") options)
  (println "... done. Elapsed" (/ (- (System/nanoTime) start) 1e9) "seconds"))
