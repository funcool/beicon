(ns beicon.tests.test-core
  (:require #?(:cljs [cljs.test :as t]
               :clj  [clojure.test :as t])
            [promesa.core :as p]
            [beicon.core :as s]
            #?(:clj  [beicon.tests.helpers :refer (drain! noop flowable-drain!)]

               :cljs [beicon.tests.helpers
                      :refer (noop drain!)
                      :refer-macros (with-timeout)])))

;; #?(:clj (defn ex-message [^Exception e] (.getMessage e)))

;; event stream

(t/deftest observable-from-values
  #?(:cljs
     (t/async done
       (let [s (s/of 1 2 3 4 5 6 7 8 9)]
         (t/is (s/observable? s))
         (drain! s #(do
                      (t/is (= % [1 2 3 4 5 6 7 8 9]))
                      (done)))))
     :clj
     (let [s (s/of 1 2 3 4 5 6 7 8 9)]
       (t/is (s/observable? s))
       (drain! s #(t/is (= % [1 2 3 4 5 6 7 8 9]))))))

#?(:cljs
   (t/deftest observable-from-values-with-nil
     (t/async done
       (let [s (s/of 1 nil 2)]
         (t/is (s/observable? s))
         (drain! s #(do
                      (t/is (= % [1 nil 2]))
                      (done)))))))

(t/deftest observable-from-vector
  #?(:cljs
     (t/async done
       (let [coll [1 2 3]
             s (s/from-coll coll)]
         (t/is (s/observable? s))
         (drain! s #(do
                      (t/is (= % coll))
                      (done)))))
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

#?(:cljs
   (t/deftest observable-from-event
     (t/async done
       (let [target #js {:addEventListener #(do
                                             (t/is (= %1 "poked"))
                                             (%2 "once"))
                         :removeEventListener #()}
             s (s/from-event target "poked")]
         (t/is (s/observable? s))
         (s/end! (drain! s #(do
                              (t/is (= % ["once"]))
                              (done))))))))

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

(t/deftest observable-pause-from-timer
  #?(:cljs
     (t/async done
      (let [s (s/timer 100)]
        (t/is (s/observable? s))
        (drain! s #(do
                     (t/is (= % [0]))
                     (s/on-end s done)))))
     :clj
     (let [s (s/timer 100)]
       (t/is (s/observable? s))
       (drain! s #(t/is (= % [0]))))))

(t/deftest observable-interval-from-timer
  #?(:cljs
     (t/async done
      (let [s (->> (s/timer 100 100)
                   (s/take 2))]
        (t/is (s/observable? s))
        (drain! s #(do
                     (t/is (= % [0 1]))
                     (s/on-end s done)))))
     :clj
     (let [s (->> (s/timer 100 100)
                  (s/take 2))]
       (t/is (s/observable? s))
       (drain! s #(t/is (= % [0 1]))))))

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
                         (future (sink (ex-info "oh noes" {})))))]
       (t/is (s/observable? s))
       (drain! s
               (fn [x]
                 (println "onNext" x))
               #(do
                  (println %)
                  (t/is (= (ex-message %) "oh noes")))))))

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
           s (s/from-future p)]
       (t/is (s/observable? s))
       (drain! s #(t/is (= % [42]))))))

;; (t/deftest observable-from-rejected-promise
;;   #?(:cljs
;;      (t/async done
;;        (let [p (p/rejected (ex-info "oh noes" {}))
;;              s (s/from-promise p)]
;;          (t/is (s/observable? s))
;;          (drain! s
;;                  #(t/is (= % []))
;;                  #(t/is (= (ex-message %) "oh noes")))
;;          (s/on-error s done)))
;;      :clj
;;      (let [p (p/rejected (ex-info "oh noes" {}))
;;            s (s/from-future p)]
;;        (t/is (s/observable? s))
;;        (drain! s
;;                #(t/is (= % []))
;;                #(t/is (instance? clojure.lang.ExceptionInfo
;;                                  (.getCause ^Exception %)))))))

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

#?(:cljs
   (t/deftest observable-never
     (t/async done
       (let [n (s/never)]
         (s/on-end n done)))))

(t/deftest observable-concat
  #?(:cljs
     (t/async done
       (let [s1 (s/from-coll [1 2 3])
             s2 (s/from-coll [4 5 6])
             cs (s/concat s1 s2)]
         (drain! cs #(t/is (= % [1 2 3 4 5 6])))
         (s/on-end cs done)))
     :clj
     (let [s1 (s/from-coll [1 2 3])
           s2 (s/from-coll [4 5 6])
           cs (s/concat s1 s2)]
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

#?(:cljs
   (t/deftest observable-fjoin
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

(t/deftest subject-push
  #?(:cljs
     (t/async done
       (let [b (s/subject)]
         (t/is (s/subject? b))
         (drain! b #(t/is (= % [1 2 3])))
         (s/push! b 1)
         (s/push! b 2)
         (s/push! b 3)
         (s/end! b)
         (s/on-end b done)))
     :clj
     (let [b (s/subject)]
       (t/is (s/subject? b))
       (future
         (Thread/sleep 100)
         (s/push! b 1)
         (s/push! b 2)
         (s/push! b 3)
         (s/end! b))
       (drain! b #(t/is (= % [1 2 3]))))))

(t/deftest behavior-subject
  #?(:cljs
     (t/async done
       (let [b (s/behavior-subject -1)]
         (t/is (s/subject? b))
         (drain! b #(do
                      (t/is (= % [-1 1 2 3]))
                      (done)))
         (s/push! b 1)
         (s/push! b 2)
         (s/push! b 3)
         (s/end! b)))
     :clj
     (let [b (s/behavior-subject -1)]
       (t/is (s/subject? b))
       (future
         (Thread/sleep 100)
         (s/push! b 1)
         (s/push! b 2)
         (s/push! b 3)
         (s/end! b))
       (drain! b #(t/is (= % [-1 1 2 3]))))))

#?(:cljs
   (t/deftest observable-reduce
     (t/async done
       (let [s (->> (s/from-coll [4 5 6])
                    (s/reduce conj [1 2])
                    #_(s/reduce (fn [acc item]
                                (println  acc "|" item)
                                (conj acc item))
                              [1 2]))]
         (drain! s #(do (t/is (= % [[1 2 4 5 6]]))
                        (done)))))))


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


(t/deftest observable-map-indexed
  #?(:cljs
     (t/async done
       (let [s (s/from-coll [:a :b :c])
             fs (s/map-indexed vector s)]
         (drain! fs #(t/is (= % [[0 :a] [1 :b] [2 :c]])))
         (s/on-end fs done)))
     :clj
     (let [s (s/from-coll [:a :b :c])
           fs (s/map-indexed vector s)]
       (drain! fs #(t/is (= [[0 :a] [1 :b] [2 :c]] %))))))

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
             s3 (s/with-latest vector s1 s2)]
         (t/is (s/observable? s3))
         (drain! s3 #(t/is (= % [[1 0] [2 0] [3 0]])))
         (s/on-end s3 done)))
     :clj
     (let [s1 (s/from-coll [0])
           s2 (s/from-coll [1 2 3])
           s3 (s/with-latest vector s1 s2)]
       (t/is (s/observable? s3))
       (drain! s3 #(t/is (= % [[1 0] [2 0] [3 0]]))))))

(t/deftest observable-combine-latest
  #?(:cljs
     (t/async done
       (let [s1 (s/delay 10 (s/from-coll [9]))
             s2 (s/delay 10 (s/from-coll [2]))
             s3 (s/combine-latest s2 s1)]
         (t/is (s/observable? s3))
         (drain! s3 #(t/is (= % [[9 2]])))
         (s/on-end s3 done)))
     :clj
     (let [s1 (s/delay 10 (s/from-coll [9]))
           s2 (s/delay 10 (s/from-coll [2]))
           s3 (s/combine-latest s2 s1)]
       (t/is (s/observable? s3))
       (drain! s3 #(t/is (= % [[9 2]]))))))

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
           type3? #(= 3 (:type (ex-data %)))
           s1 (->> (s/throw (ex-info "error" {:type 1}))
                   (s/catch type2? #(s/just (ex-data %)))
                   (s/catch type3? #(s/just (ex-data %)))
                   (s/catch type1? #(s/just (ex-data %))))]
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
             s (s/observe-on :async (s/from-coll coll))]
         (t/is (s/observable? s))
         (drain! s #(t/is (= % coll)))
         (s/on-end s done)))
     :clj
     (let [coll [1 2 3]
           s (s/observe-on :io (s/from-coll coll))]
       (t/is (s/observable? s))
       (drain! s #(t/is (= % coll))))))

(t/deftest subscribe-on
  #?(:cljs
     (t/async done
       (let [coll [1 2 3]
             s (s/subscribe-on :queue (s/from-coll coll))]
         (t/is (s/observable? s))
         (drain! s #(t/is (= % coll)))
         (s/on-end s done)))
     :clj
     (let [coll [1 2 3]
           s (s/subscribe-on :trampoline (s/from-coll coll))]
       (t/is (s/observable? s))
       (drain! s #(t/is (= % coll))))))

(t/deftest scheduler-predicate-and-resolver
  #?(:cljs (t/is (s/scheduler? (s/scheduler :asap))))
  #?(:cljs (t/is (s/scheduler? (s/scheduler :queue))))
  #?(:cljs (t/is (s/scheduler? (s/scheduler :async))))
  #?(:cljs (t/is (s/scheduler? (s/scheduler :af))))
  #?(:cljs (t/is (s/scheduler? (s/scheduler :animation-frame))))
  #?(:clj (t/is (s/scheduler? (s/scheduler :computation))))
  #?(:clj (t/is (s/scheduler? (s/scheduler :io))))
  #?(:clj (t/is (s/scheduler? (s/scheduler :single))))
  #?(:clj (t/is (s/scheduler? (s/scheduler :thread))))
  #?(:clj (t/is (s/scheduler? (s/scheduler :trampoline)))))

#?(:clj
   (defn- flowable-from-coll
     [coll]
     (s/generate (fn [state sink]
                   (sink (first state))
                   (rest state))
                 coll)))

#?(:clj
   (t/deftest flowable-filter-with-predicate
     (let [s (flowable-from-coll [1 2 3 4 5])
           fs (s/filter #{3 5} s)]
       (flowable-drain! fs #(t/is (= % [3 5])))
       (drain! fs #(t/is (= % [3 5]))))))

#?(:clj
   (t/deftest flowable-concat
     (let [s1 (flowable-from-coll [1 2 3])
           s2 (flowable-from-coll [4 5 6])
           cs (s/concat s1 s2)]
       (drain! cs #(t/is (= % [1 2 3 4 5 6]))))))

;; --- CLJS Tests Entry-Point

#?(:cljs
   (do
     (enable-console-print!)
     (set! *main-cli-fn* #(t/run-tests))))

#?(:cljs
   (defmethod t/report [:cljs.test/default :end-run-tests]
     [m]
     (if (t/successful? m)
       (set! (.-exitCode js/process) 0)
       (set! (.-exitCode js/process) 1))))

