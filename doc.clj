(require '[codox.main :as codox])

(codox/generate-docs
 {:output-path "doc/dist/latest"
  :metadata {:doc/format :markdown}
  :language :clojurescript
  :name "funcool/beicon"
  :themes [:rdash]
  :source-paths ["src"]
  :namespaces [#"^beicon\."]
  :source-uri "https://github.com/funcool/beicon/blob/master/{filepath}#L{line}"})
