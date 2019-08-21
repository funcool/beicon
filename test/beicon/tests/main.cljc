(ns beicon.tests.main
  (:require [clojure.test :as t]
            [beicon.tests.test-core]))

#?(:cljs
   (do
     (enable-console-print!)
     (set! *main-cli-fn* #(t/run-tests 'promesa.tests.test-core)))
   :clj
   (defn -main
     [& args]
     (let [{:keys [fail]} (t/run-all-tests #"^beicon.tests.*")]
       (if (pos? fail)
         (System/exit fail)
         (System/exit 0)))))

#?(:cljs
   (defmethod t/report [:cljs.test/default :end-run-tests]
     [m]
     (if (t/successful? m)
       (set! (.-exitCode js/process) 0)
       (set! (.-exitCode js/process) 1))))
