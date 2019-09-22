(ns beicon.tests.helpers
  #?(:cljs (:require [beicon.core :as s])))

(def noop (constantly nil))

#?(:clj
   (defmacro with-timeout
     [ms & body]
     `(js/setTimeout
       (fn []
         (do
           ~@body))
       ~ms)))

#?(:cljs
   (defn drain!
     ([ob cb]
      (drain! ob cb #(println "Error: " %)))
     ([ob cb errb]
      (let [values (volatile! [])]
        (s/subscribe ob
                     #(vswap! values conj %)
                     #(errb %)
                     #(cb @values))))))
