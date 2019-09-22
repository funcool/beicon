(ns beicon.tests.main
  (:require [cljs.test :as t]
            [beicon.tests.test-core]))

(enable-console-print!)

(set! *main-cli-fn* #(t/run-tests 'beicon.tests.test-core))

(defmethod t/report [:cljs.test/default :end-run-tests]
  [m]
  (if (t/successful? m)
    (set! (.-exitCode js/process) 0)
    (set! (.-exitCode js/process) 1)))
