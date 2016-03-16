(ns beicon.tests.helpers
  #?(:cljs (:require [beicon.core :as s])))

(def no-op (fn [& args]))

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
     ([obs cb]
      (drain! obs cb #(println "Error: " %)))
     ([obs cb errb]
      (let [values (volatile! [])]
        (s/subscribe obs
                     #(vswap! values conj %)
                     #(errb %)
                     #(cb @values))))))
