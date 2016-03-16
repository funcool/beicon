(ns beicon.tests.runner
  (:require [clojure.string :as str]
            [cljs.test :as t]
            [beicon.tests.core-tests]))

(enable-console-print!)

(defn- main
  []
  (t/run-tests
   (t/empty-env)
   'beicon.tests.core-tests))

(set! *main-cli-fn* main)

(defmethod t/report [:cljs.test/default :end-run-tests]
  [m]
  (if (t/successful? m)
    (set! (.-exitCode js/process) 0)
    (set! (.-exitCode js/process) 1)))
