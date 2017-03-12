(require '[cljs.build.api :as b])

(def options
  {:main 'beicon.tests.test_core
   :target :nodejs
   :output-to "out/tests.js"
   :output-dir "out/tests"
   :optimizations :none
   :pretty-print true
   :language-in  :ecmascript6
   :language-out :ecmascript5
   :verbose true})

(b/watch (b/inputs "test" "src") options)
