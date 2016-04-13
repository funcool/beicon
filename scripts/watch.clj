(require '[cljs.build.api :as b])

(b/watch (b/inputs "test" "src")
  {:main 'beicon.tests.runner
   :target :nodejs
   :output-to "out/tests.js"
   :output-dir "out"
   :optimizations :simple
   :pretty-print true
   :language-in  :ecmascript6
   :language-out :ecmascript5
   :verbose true})
