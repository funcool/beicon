(ns beicon.tests.core-tests
  (:require #?(:cljs [cljs.test :as t]
               :clj  [clojure.test :as t])
            [promesa.core :as p]
            [beicon.core :as s]
            #?(:clj  [beicon.tests.helpers :refer (drain! no-op)]
               :cljs [beicon.tests.helpers
                      :refer (no-op drain!)
                      :refer-macros (with-timeout)])))

#?(:clj (set! *warn-on-reflection* true))

#?(:clj (defn ex-message [^Exception e] (.getMessage e)))

;; event stream

(t/deftest observable-from-values
  #?(:cljs
     (t/async done
       (let [s (s/of 1 2 3 4 5 6 7 8 9)]
         (t/is (s/observable? s))
         (drain! s #(t/is (= % [1 2 3 4 5 6 7 8 9])))
         (s/on-end s done)))
     :clj
     (let [s (s/of 1 2 3 4 5 6 7 8 9)]
       (t/is (s/observable? s))
       (drain! s #(t/is (= % [1 2 3 4 5 6 7 8 9]))))))

(t/deftest observable-from-values-with-nil
  #?(:cljs
     (t/async done
       (let [s (s/of 1 nil 2)]
         (t/is (s/observable? s))
         (drain! s #(t/is (= % [1 nil 2])))
         (s/on-end s done)))

     :clj
     (let [s (s/of 1 nil 2)]
       (t/is (s/observable? s))
       (drain! s #(t/is (= % [1 nil 2]))))))

(t/deftest observable-from-vector
  #?(:cljs
     (t/async done
       (let [coll [1 2 3]
             s (s/from-coll coll)]
         (t/is (s/observable? s))
         (drain! s #(t/is (= % coll)))
         (s/on-end s done)))
     :clj
     (let [coll [1 2 3]
           s (s/from-coll coll)]
       (t/is (s/observable? s))
       (drain! s #(t/is (= % coll))))))

(t/deftest observable-from-vector-with-take
  #?(:cljs
     (t/async done
       (let [coll [1 2 3 4 5 6]
             s (->> (s/from-coll coll)
                    (s/take 2))]
         (t/is (s/observable? s))
         (drain! s #(t/is (= % [1 2])))
         (s/on-end s done)))
     :clj
     (let [coll [1 2 3 4 5 6]
           s (->> (s/from-coll coll)
                  (s/take 2))]
       (t/is (s/observable? s))
       (drain! s #(t/is (= % [1 2]))))))

(t/deftest observable-from-atom
  #?(:cljs
     (t/async done
       (let [a (atom 0)
             s (->> (s/from-atom a)
                    (s/take 4))]
         (t/is (s/observable? s))
         (drain! s #(do
                      (t/is (= % [1 2 3 4]))
                      (done)))
         (swap! a inc)
         (swap! a inc)
         (swap! a inc)
         (swap! a inc)))
     :clj
     (let [a (atom 0)
           s (->> (s/from-atom a)
                  (s/take 4))]
       (t/is (s/observable? s))
       (future
         (Thread/sleep 300)
         (swap! a inc)
         (swap! a inc)
         (swap! a inc)
         (swap! a inc))
       (drain! s #(t/is (= % [1 2 3 4]))))))

(t/deftest observable-from-set
  #?(:cljs
     (t/async done
       (let [coll #{1 2 3}
             s (s/from-coll coll)]
         (t/is (s/observable? s))
         (drain! s #(t/is (= (set %) coll)))
         (s/on-end s done)))
     :clj
     (let [coll #{1 2 3}
           s (s/from-coll coll)]
       (t/is (s/observable? s))
       (drain! s #(t/is (= (set %) coll))))))

(t/deftest observable-from-create
  #?(:cljs
     (t/async done
       (let [s (s/create (fn [sink]
                           (with-timeout 10
                             (sink 1)
                             (sink 2)
                             (sink (s/end 3)))))]
         (t/is (s/observable? s))
         (drain! s #(t/is (= % [1 2 3])))
         (s/on-end s done)))
     :clj
     (let [s (s/create (fn [sink]
                         (future
                           (sink 1)
                           (sink 2)
                           (sink (s/end 3)))))]
       (t/is (s/observable? s))
       (drain! s #(t/is (= % [1 2 3]))))))

(t/deftest observable-with-timeout
  #?(:cljs
     (t/async done
       (let [s (->> (s/timer 200)
                    (s/timeout 100 (s/just :timeout)))]

         (t/is (s/observable? s))
         (drain! s #(do
                      (t/is (= % [:timeout]))
                      (s/on-end s done)))))
     :clj
     (let [s (->> (s/timer 200)
                  (s/timeout 100 (s/just :timeout)))]

       (t/is (s/observable? s))
       (drain! s #(t/is (= % [:timeout]))))))

(t/deftest observable-errors-from-create
  #?(:cljs
     (t/async done
       (let [s (s/create (fn [sink]
                           (with-timeout 10
                             (sink 1)
                             (sink (ex-info "oh noes" {})))))]
         (t/is (s/observable? s))
         (drain! s
                 #(t/is (= % [1]))
                 #(t/is (= (ex-message %) "oh noes")))
         (s/on-error s done)))
     :clj
     (let [s (s/create (fn [sink]
                         (sink (ex-info "oh noes" {}))))]
       (t/is (s/observable? s))
       (drain! s
               no-op
               #(t/is (= (ex-message %) "oh noes"))))))

(t/deftest observable-from-promise
  #?(:cljs
     (t/async done
       (let [p (p/resolved 42)
             s (s/from-promise p)]
         (t/is (s/observable? s))
         (drain! s
                 #(t/is (= % [42])))
         (s/on-end s done)))
     :clj
     (let [p (p/resolved 42)
           s (s/from-promise p)]
       (t/is (s/observable? s))
       (drain! s #(t/is (= % [42]))))))

(t/deftest observable-from-rejected-promise
  #?(:cljs
     (t/async done
       (let [p (p/rejected (ex-info "oh noes" {}))
             s (s/from-promise p)]
         (t/is (s/observable? s))
         (drain! s
                 #(t/is (= % []))
                 #(t/is (= (ex-message %) "oh noes")))
         (s/on-error s done)))
     :clj
     (let [p (p/rejected (ex-info "oh noes" {}))
           s (s/from-promise p)]
       (t/is (s/observable? s))
       (drain! s
               #(t/is (= % []))
               #(t/is (instance? clojure.lang.ExceptionInfo
                                 (.getCause ^Exception %)))))))

(t/deftest observable-range
  #?(:cljs
     (t/async done
       (let [s (s/range 5)]
         (t/is (s/observable? s))
         (drain! s #(t/is (= % [0 1 2 3 4])))
         (s/on-end s done)))
     :clj
     (let [s (s/range 5)]
       (t/is (s/observable? s))
       (drain! s #(t/is (= % [0 1 2 3 4]))))))

(t/deftest observable-once
  #?(:cljs
     (t/async done
       (let [s (s/once 1)]
         (t/is (s/observable? s))
         (drain! s #(t/is (= % [1])))
         (s/on-end s done)))
     :clj
     (let [s (s/once 1)]
       (t/is (s/observable? s))
       (drain! s #(t/is (= % [1]))))))

(t/deftest observable-never
  #?(:cljs
     (t/async done
       (let [n (s/never)]
         (s/on-end n done)))))

(t/deftest observable-concat
  #?(:cljs
     (t/async done
       (let [s1 (s/from-coll [1 2 3])
             s2 (s/from-coll [4 5 6])
             cs (s/concat s2 s1)]
         (drain! cs #(t/is (= % [1 2 3 4 5 6])))
         (s/on-end cs done)))
     :clj
     (let [s1 (s/from-coll [1 2 3])
           s2 (s/from-coll [4 5 6])
           cs (s/concat s2 s1)]
       (drain! cs #(t/is (= % [1 2 3 4 5 6]))))))

(t/deftest observable-zip
  #?(:cljs
     (t/async done
       (let [s1 (s/from-coll [1 2])
             s2 (s/from-coll [4 5])
             s3 (s/from-coll [7 8])
             cs (s/zip s1 s2 s3)]
         (drain! cs #(t/is (= % [[1 4 7] [2 5 8]])))
         (s/on-end cs done)))
     :clj
     (let [s1 (s/from-coll [1 2])
           s2 (s/from-coll [4 5])
           s3 (s/from-coll [7 8])
           cs (s/zip s1 s2 s3)]
       (drain! cs #(t/is (= % [[1 4 7] [2 5 8]]))))))

(t/deftest observable-fjoin
  #?(:cljs
     (t/async done
       (let [s1 (s/from-coll [1 2])
             s2 (s/from-coll [4 5])
             s3 (s/from-coll [7 8])
             cs (s/fjoin vector
                         s1 s2 s3)]
         (drain! cs #(t/is (= % [[2 5 8]])))
         (s/on-end cs done)))))

(t/deftest observable-merge
  #?(:cljs
     (t/async done
       (let [s1 (s/from-coll [1 2 3])
             s2 (s/from-coll [:1 :2 :3])
             ms (s/merge s1 s2)]
         (drain! ms #(t/is (= (set %) #{:1 1 :2 2 :3 3})))
         (s/on-end ms done)))
     :clj
     (let [s1 (s/from-coll [1 2 3])
           s2 (s/from-coll [:1 :2 :3])
           ms (s/merge s1 s2)]
       (drain! ms #(t/is (= (set %) #{:1 1 :2 2 :3 3}))))))

(t/deftest observable-skip-while
  #?(:cljs
     (t/async done
       (let [nums (s/from-coll [1 1 1 2 3 4 5])
             sample (s/skip-while odd? nums)]
         (drain! sample #(t/is (= % [2 3 4 5])))
         (s/on-end sample done)))
     :clj
     (let [nums (s/from-coll [1 1 1 2 3 4 5])
           sample (s/skip-while odd? nums)]
       (drain! sample #(t/is (= % [2 3 4 5]))))))

(t/deftest bus-push
  #?(:cljs
     (t/async done
       (let [b (s/bus)]
         (t/is (s/bus? b))
         (drain! b #(t/is (= % [1 2 3])))
         (s/push! b 1)
         (s/push! b 2)
         (s/push! b 3)
         (s/end! b)
         (s/on-end b done)))
     :clj
     (let [b (s/bus)]
       (t/is (s/bus? b))
       (future
         (Thread/sleep 100)
         (s/push! b 1)
         (s/push! b 2)
         (s/push! b 3)
         (s/end! b))
       (drain! b #(t/is (= % [1 2 3]))))))

(t/deftest observable-filter-with-predicate
  #?(:cljs
     (t/async done
       (let [s (s/from-coll [1 2 3 4 5])
             fs (s/filter #{3 5} s)]
         (drain! fs #(t/is (= % [3 5])))
         (s/on-end fs done)))
     :clj
     (let [s (s/from-coll [1 2 3 4 5])
           fs (s/filter #{3 5} s)]
       (drain! fs #(t/is (= % [3 5]))))))

(t/deftest observable-map-with-ifn
  #?(:cljs
     (t/async done
       (let [s (s/from-coll [{:foo 1} {:foo 2}])
             fs (s/map :foo s)]
         (drain! fs #(t/is (= % [1 2])))
         (s/on-end fs done)))
     :clj
     (let [s (s/from-coll [{:foo 1} {:foo 2}])
           fs (s/map :foo s)]
       (drain! fs #(t/is (= % [1 2]))))))

(t/deftest observable-retry
  #?(:cljs
     (t/async done
       (let [errored? (volatile! false)
             s (s/create (fn [sink]
                           (if @errored?
                             (do
                               (sink 2)
                               (sink 3)
                               (sink s/end))
                             (do
                               (vreset! errored? true)
                               (sink (js/Error.))))))
             rs (s/retry 2 s)]
         (t/is (s/observable? rs))
         (drain! rs #(t/is (= % [2 3])))
         (s/on-end rs done)))
     :clj
     (let [errored? (volatile! false)
           s (s/create (fn [sink]
                         (if @errored?
                           (do
                             (sink 2)
                             (sink 3)
                             (sink s/end))
                           (do
                             (vreset! errored? true)
                             (sink (ex-info "" {}))))))
           rs (s/retry 2 s)]
       (t/is (s/observable? rs))
       (drain! rs #(t/is (= % [2 3]))))))

(t/deftest observable-with-latest-from
  #?(:cljs
     (t/async done
       (let [s1 (s/from-coll [0])
             s2 (s/from-coll [1 2 3])
             s3 (s/with-latest-from s1 s2)]
         (t/is (s/observable? s3))
         (drain! s3 #(t/is (= % [[1 0] [2 0] [3 0]])))
         (s/on-end s3 done)))
     :clj
     (let [s1 (s/from-coll [0])
           s2 (s/from-coll [1 2 3])
           s3 (s/with-latest-from s1 s2)]
       (t/is (s/observable? s3))
       (drain! s3 #(t/is (= % [[1 0] [2 0] [3 0]]))))))

(t/deftest observable-catch-1
  #?(:cljs
     (t/async done
       (let [s1 (s/throw (ex-info "error" {:foo :bar}))
             s2 (s/catch (fn [error]
                           (s/once (ex-data error)))
                    s1)]
         (t/is (s/observable? s2))
         (drain! s2 #(t/is (= % [{:foo :bar}])))
         (s/on-end s2 done)))
     :clj
     (let [s1 (s/throw (ex-info "error" {:foo :bar}))
           s2 (s/catch (fn [error]
                         (s/once (ex-data error)))
                  s1)]
       (t/is (s/observable? s2))
       (drain! s2 #(t/is (= % [{:foo :bar}]))))))

(t/deftest observable-catch-2
  #?(:cljs
     (t/async done
       (let [type1? #(= 1 (:type (ex-data %)))
             s1 (->> (s/throw (ex-info "error" {:type 1}))
                     (s/catch type1? #(s/once (ex-data %))))]
         (t/is (s/observable? s1))
         (drain! s1 #(t/is (= % [{:type 1}])))
         (s/on-end s1 done)))
     :clj
     (let [type1? #(= 1 (:type (ex-data %)))
           s1 (->> (s/throw (ex-info "error" {:type 1}))
                   (s/catch type1? #(s/once (ex-data %))))]
       (t/is (s/observable? s1))
       (drain! s1 #(t/is (= % [{:type 1}]))))))

(t/deftest observable-catch-3
  #?(:cljs
     (t/async done
       (let [type1? #(= 1 (:type (ex-data %)))
             type2? #(= 2 (:type (ex-data %)))
             s1 (->> (s/throw (ex-info "error" {:type 1}))
                     (s/catch type2? #(s/once (ex-data %)))
                     (s/catch type1? #(s/once (ex-data %))))]
         (t/is (s/observable? s1))
         (drain! s1 #(t/is (= % [{:type 1}])))
         (s/on-end s1 done)))
     :clj
     (let [type1? #(= 1 (:type (ex-data %)))
           type2? #(= 2 (:type (ex-data %)))
           s1 (->> (s/throw (ex-info "error" {:type 1}))
                   (s/catch type2? #(s/once (ex-data %)))
                   (s/catch type1? #(s/once (ex-data %))))]
       (t/is (s/observable? s1))
       (drain! s1 #(t/is (= % [{:type 1}]))))))

(t/deftest observable-to-atom
  #?(:cljs
     (t/async done
       (let [st (s/from-coll [1 2 3])
             a (s/to-atom st)]
         (s/on-end st #(do (t/is (= @a 3))
                           (done)))))
     :clj
     (let [st (s/from-coll [1 2 3])
           a (s/to-atom st)]
       (s/on-end st #(t/is (= @a 3))))))

(t/deftest observable-to-atom-with-atom
  #?(:cljs
     (t/async done
       (let [st (s/from-coll [1 2 3])
             vacc (volatile! [])
             a (atom 0)]
         (add-watch a
                    :acc
                    (fn [_ _ _ v]
                      (vswap! vacc conj v)))
         (s/to-atom st a)
         (s/on-end st #(do (t/is (= @a 3))
                           (t/is (= @vacc [1 2 3]))
                           (done)))))
     :clj
     (let [st (s/from-coll [1 2 3])
           vacc (volatile! [])
           a (atom 0)]
       (add-watch a
                  :acc
                  (fn [_ _ _ v]
                    (vswap! vacc conj v)))
       (s/to-atom st a)
       (s/on-end st #(do
                       (t/is (= @a 3))
                       (t/is (= @vacc [1 2 3])))))))

(t/deftest observable-to-atom-with-atom-and-function
  #?(:cljs
     (t/async done
       (let [st (s/from-coll [1 2 3])
             a (atom [])]
         (s/to-atom st a conj)
         (s/on-end st #(do (t/is (= @a [1 2 3]))
                           (done)))))
     :clj
     (let [st (s/from-coll [1 2 3])
           a (atom [])]
       (s/to-atom st a conj)
       (s/on-end st #(t/is (= @a [1 2 3]))))))

(t/deftest transform-with-stateless-transducers
  #?(:cljs
     (t/async done
       (let [s (s/from-coll [1 2 3 4 5 6])
             ts (s/transform (comp
                              (map inc)
                              (filter odd?))
                             s)]
         (drain! ts #(t/is (= % [3 5 7])))
         (s/on-end ts done)))
     :clj
     (let [s (s/from-coll [1 2 3 4 5 6])
           ts (s/transform (comp
                            (map inc)
                            (filter odd?))
                           s)]
       (drain! ts #(t/is (= % [3 5 7]))))))

(t/deftest transform-with-stateful-transducers
  #?(:cljs
     (t/async done
       (let [s (s/from-coll [1 2 3 4 5 6])
             ts (s/transform (comp
                              (partition-all 2)
                              (take 2))
                             s)]
         (drain! ts #(t/is (= % [[1 2] [3 4]])))
         (s/on-end ts done)))
     :clj
     (let [s (s/from-coll [1 2 3 4 5 6])
           ts (s/transform (comp
                            (partition-all 2)
                            (take 2))
                           s)]
       (drain! ts #(t/is (= % [[1 2] [3 4]]))))))

(t/deftest observe-on
  #?(:cljs
     (t/async done
       (let [coll [1 2 3]
             s (s/observe-on s/async (s/from-coll coll))]
         (t/is (s/observable? s))
         (drain! s #(t/is (= % coll)))
         (s/on-end s done)))
     :clj
     (let [coll [1 2 3]
           s (s/observe-on s/io (s/from-coll coll))]
       (t/is (s/observable? s))
       (drain! s #(t/is (= % coll))))))

(t/deftest subscribe-on
  #?(:cljs
     (t/async done
       (let [coll [1 2 3]
             s (s/subscribe-on s/queue (s/from-coll coll))]
         (t/is (s/observable? s))
         (drain! s #(t/is (= % coll)))
         (s/on-end s done)))
     :clj
     (let [coll [1 2 3]
           s (s/subscribe-on s/trampoline (s/from-coll coll))]
       (t/is (s/observable? s))
       (drain! s #(t/is (= % coll))))))

