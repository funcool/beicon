(require
  '[cljs.repl :as repl]
  '[cljs.repl.nashorn :as nashorn]
  '[cljs.repl.node :as node])

(cljs.repl/repl
 (cljs.repl.node/repl-env)
 :output-dir "out"
 :cache-analysis true)
