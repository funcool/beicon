(ns beicon.tests-runner
  (:require [clojure.string :as str]
            [cljs.test :as test]
            [beicon.core-spec]))

(enable-console-print!)

(defn main
  []
  (test/run-tests
   (test/empty-env)
   'beicon.core-spec
   ))

(defmethod test/report [:cljs.test/default :end-run-tests]
  [m]
  (if (test/successful? m)
    (set! (.-exitCode js/process) 0)
    (set! (.-exitCode js/process) 1)))

(set! *main-cli-fn* main)
