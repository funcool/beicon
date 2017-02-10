(ns beicon.tests.helpers
  (:require [beicon.core :as s])
  #?(:clj (:import java.util.concurrent.CountDownLatch)))

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
                     #(cb @values)))))
   :clj
   (defn drain!
     ([ob cb]
      (drain! ob cb #(println "Error: " %)))
     ([ob cb errb]
      (let [values (volatile! [])
            latch (CountDownLatch. 1)]
        (s/subscribe ob
                     #(vswap! values conj %)
                     #(do
                        (errb %)
                        (.countDown latch))
                     #(do
                        (cb @values)
                        (.countDown latch)))
        (.await latch)))))


#?(:clj
   (defn flowable-drain!
     ([ob cb]
      (flowable-drain! ob cb #(println "Error: " %)))
     ([ob cb errb]
      (let [values (volatile! [])
            latch (CountDownLatch. 1)]
        (s/subscribe-with ob (reify s/ISubscriber
                               (-on-init [_ s]
                                 (s/request! s 1))

                               (-on-next [_ s v]
                                 (vswap! values conj v)
                                 (s/request! s 1))

                               (-on-error [_ s e]
                                 (errb e)
                                 (.countDown latch))

                               (-on-end [_ s]
                                 (cb @values)
                                 (.countDown latch))))
        (.await latch)))))
