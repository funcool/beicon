(ns beicon.tests.test-core
  (:require
   [cljs.test :as t]
   [promesa.core :as p]
   [beicon.core :as s]
   [beicon.tests.helpers
    :refer (noop drain!)
    :refer-macros (with-timeout)]))

;; event stream

(t/deftest observable-from-values
  (t/async done
    (let [s (s/of 1 2 3 4 5 6 7 8 9)]
      (t/is (s/observable? s))
      (drain! s #(do
                   (t/is (= % [1 2 3 4 5 6 7 8 9]))
                   (done))))))

(t/deftest observable-from-values-with-nil
  (t/async done
    (let [s (s/of 1 nil 2)]
      (t/is (s/observable? s))
      (drain! s #(do
                   (t/is (= % [1 nil 2]))
                   (done))))))

(t/deftest observable-from-vector
  (t/async done
    (let [coll [1 2 3]
          s (s/from coll)]
      (t/is (s/observable? s))
      (drain! s #(do
                   (t/is (= % coll))
                   (done))))))

(t/deftest observable-from-vector-with-take
  (t/async done
    (let [coll [1 2 3 4 5 6]
          s (->> (s/from coll)
                 (s/take 2))]
      (t/is (s/observable? s))
      (drain! s #(t/is (= % [1 2])))
      (s/on-end s done))))

(t/deftest observable-from-atom
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
      (swap! a inc))))

(t/deftest observable-from-set
  (t/async done
    (let [coll #{1 2 3}
          s (s/from coll)]
      (t/is (s/observable? s))
      (drain! s #(t/is (= (set %) coll)))
      (s/on-end s done))))

(t/deftest observable-from-create
  (t/async done
    (let [s (s/create (fn [sink]
                        (with-timeout 10
                          (rx/push! sink 1)
                          (rx/push! sink 2)
                          (rx/push! sink 3)
                          (rx/end! sink))))]
      (t/is (s/observable? s))
      (drain! s #(t/is (= % [1 2 3])))
      (s/on-end s done))))

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
                           (done)))))))

(t/deftest observable-with-timeout
  (t/async done
    (let [s (->> (s/timer 200)
                 (s/timeout 100 (s/of :timeout)))]

      (t/is (s/observable? s))
      (drain! s #(do
                   (t/is (= % [:timeout]))
                   (s/on-end s done))))))

(t/deftest observable-pause-from-timer
  (t/async done
    (let [s (s/timer 100)]
      (t/is (s/observable? s))
      (drain! s #(do
                   (t/is (= % [0]))
                   (s/on-end s done))))))

(t/deftest observable-interval-from-timer
  (t/async done
    (let [s (->> (s/timer 100 100)
                 (s/take 2))]
      (t/is (s/observable? s))
      (drain! s #(do
                   (t/is (= % [0 1]))
                   (s/on-end s done))))))

(t/deftest observable-errors-from-create
  (t/async done
    (let [s (s/create (fn [sink]
                        (with-timeout 10
                          (s/push! sink 1)
                          (s/error! sink (ex-info "oh noes" {})))))]
      (t/is (s/observable? s))
      (drain! s
              #(t/is (= % [1]))
              #(t/is (= (ex-message %) "oh noes")))
      (s/on-error s done))))

(t/deftest observable-from-promise
  (t/async done
    (let [p (p/resolved 42)
          s (s/from p)]
      (t/is (s/observable? s))
      (drain! s
              #(t/is (= % [42])))
      (s/on-end s done))))

(t/deftest observable-range
  (t/async done
    (let [s (s/range 5)]
      (t/is (s/observable? s))
      (drain! s #(t/is (= % [0 1 2 3 4])))
      (s/on-end s done))))

(t/deftest observable-of
  (t/async done
    (let [s (s/of 1)]
      (t/is (s/observable? s))
      (drain! s #(t/is (= % [1])))
      (s/on-end s done))))

(t/deftest observable-empty
  (t/async done
    (let [n (s/empty)]
      (s/on-end n done))))

(t/deftest observable-concat
  (t/async done
    (let [s1 (s/from [1 2 3])
          s2 (s/from [4 5 6])
          cs (s/concat s1 s2)]
      (drain! cs #(t/is (= % [1 2 3 4 5 6])))
      (s/on-end cs done))))

(t/deftest observable-zip
  (t/async done
    (let [s1 (s/from [1 2])
          s2 (s/from [4 5])
          s3 (s/from [7 8])
          cs (s/zip s1 s2 s3)]
      (drain! cs #(t/is (= % [[1 4 7] [2 5 8]])))
      (s/on-end cs done))))

(t/deftest observable-fjoin
  (t/async done
    (let [s1 (s/from [1 2])
          s2 (s/from [4 5])
          s3 (s/from [7 8])
          cs (s/fjoin vector
                      s1 s2 s3)]
      (drain! cs #(t/is (= % [[2 5 8]])))
      (s/on-end cs done))))

(t/deftest observable-merge
  (t/async done
    (let [s1 (s/from [1 2 3])
          s2 (s/from [:1 :2 :3])
          ms (s/merge s1 s2)]
      (drain! ms #(t/is (= (set %) #{:1 1 :2 2 :3 3})))
      (s/on-end ms done))))

(t/deftest observable-skip-while
  (t/async done
    (let [nums (s/from [1 1 1 2 3 4 5])
          sample (s/skip-while odd? nums)]
      (drain! sample #(t/is (= % [2 3 4 5])))
      (s/on-end sample done))))

(t/deftest subject-as-ideref
  (t/async done
    (let [nums (s/from [1 1 1 2 3 4 5])
          sub (s/behavior-subject nil)]
      (s/on-end sub #(t/is (= @sub 5)
                           (done)))
      (s/subscribe-with nums sub))))

(t/deftest subject-push
  (t/async done
    (let [b (s/subject)]
      (t/is (s/subject? b))
      (drain! b #(t/is (= % [1 2 3])))
      (s/push! b 1)
      (s/push! b 2)
      (s/push! b 3)
      (s/end! b)
      (s/on-end b done))))

(t/deftest behavior-subject
  (t/async done
    (let [b (s/behavior-subject -1)]
      (t/is (s/subject? b))
      (drain! b #(do
                   (t/is (= % [-1 1 2 3]))
                   (done)))
      (s/push! b 1)
      (s/push! b 2)
      (s/push! b 3)
      (s/end! b))))

(t/deftest observable-reduce
  (t/async done
    (let [s (->> (s/from [4 5 6])
                 (s/reduce conj [1 2]))]
      (drain! s #(do (t/is (= % [[1 2 4 5 6]]))
                     (done))))))


(t/deftest observable-filter-with-ifn
  (t/async done
    (let [s (s/from [1 2 3 4 5])
          fs (s/filter #{3 5} s)]
      (drain! fs #(t/is (= % [3 5])))
      (s/on-end fs done))))

(t/deftest observable-map-with-ifn
  (t/async done
    (let [s (s/from [{:foo 1} {:foo 2}])
          fs (s/map :foo s)]
      (drain! fs #(t/is (= % [1 2])))
      (s/on-end fs done))))

(t/deftest observable-map-indexed
  (t/async done
    (let [s (s/from [:a :b :c])
          fs (s/map-indexed vector s)]
      (drain! fs #(t/is (= % [[0 :a] [1 :b] [2 :c]])))
      (s/on-end fs done))))

(t/deftest observable-retry
  (t/async done
    (let [errored? (volatile! false)
          s        (s/create (fn [sink]
                               (if @errored?
                                 (do
                                   (s/push! sink 2)
                                   (s/push! sink 3)
                                   (s/end! sink))
                                 (do
                                   (vreset! errored? true)
                                   (s/error! sink (js/Error.))))))
             rs (s/retry 2 s)]
      (t/is (s/observable? rs))
      (drain! rs #(t/is (= % [2 3])))
      (s/on-end rs done))))

(t/deftest observable-with-latest-from
  (t/async done
    (let [s1 (s/from [0])
          s2 (s/from [1 2 3])
          s3 (s/with-latest vector s1 s2)]
      (t/is (s/observable? s3))
      (drain! s3 #(t/is (= % [[1 0] [2 0] [3 0]])))
      (s/on-end s3 done))))

(t/deftest observable-combine-latest
  (t/async done
    (let [s1 (s/delay 10 (s/from [9]))
          s2 (s/delay 10 (s/from [2]))
          s3 (s/combine-latest s2 s1)
          s3 (s/map vec s3)]
      (t/is (s/observable? s3))
      (drain! s3 #(t/is (= % [[9 2]])))
      (s/on-end s3 done))))

(t/deftest observable-combine-latest-2
  (t/async done
    (let [s1 (s/delay 10 (s/from [9]))
          s2 (s/delay 10 (s/from [2]))
          s3 (->> (s/combine-latest [s1 s2])
                  (s/map vec)
                  (s/delay-at-least 100))]
      (t/is (s/observable? s3))
      (drain! s3 #(t/is (= % [[9 2]])))
      (s/on-end s3 done))))

(t/deftest observable-catch-1
  (t/async done
    (let [s1 (s/throw (ex-info "error" {:foo :bar}))
          s2 (s/catch (fn [error]
                        (s/of (ex-data error)))
                 s1)]
      (t/is (s/observable? s2))
      (drain! s2 #(t/is (= % [{:foo :bar}])))
      (s/on-end s2 done))))

(t/deftest observable-catch-2
  (t/async done
    (let [type1? #(= 1 (:type (ex-data %)))
          s1 (->> (s/throw (ex-info "error" {:type 1}))
                  (s/catch type1? #(s/of (ex-data %))))]
      (t/is (s/observable? s1))
      (drain! s1 #(t/is (= % [{:type 1}])))
      (s/on-end s1 done))))

(t/deftest observable-catch-3
  (t/async done
    (let [type1? #(= 1 (:type (ex-data %)))
          type2? #(= 2 (:type (ex-data %)))
          s1 (->> (s/throw (ex-info "error" {:type 1}))
                  (s/catch type2? #(s/of (ex-data %)))
                  (s/catch type1? #(s/of (ex-data %))))]
      (t/is (s/observable? s1))
      (drain! s1 #(t/is (= % [{:type 1}])))
      (s/on-end s1 done))))

(t/deftest observable-to-atom
  (t/async done
    (let [st (s/from [1 2 3])
          a (s/to-atom st)]
      (s/on-end st #(do (t/is (= @a 3))
                        (done))))))

(t/deftest observable-to-atom-with-atom
  (t/async done
    (let [st (s/from [1 2 3])
          vacc (volatile! [])
          a (atom 0)]
      (add-watch a :acc
                 (fn [_ _ _ v]
                   (vswap! vacc conj v)))
      (s/to-atom st a)
      (s/on-end st #(do (t/is (= @a 3))
                        (t/is (= @vacc [1 2 3]))
                        (done))))))

(t/deftest observable-to-atom-with-atom-and-function
  (t/async done
    (let [st (s/from [1 2 3])
          a (atom [])]
      (s/to-atom st a conj)
      (s/on-end st #(do (t/is (= @a [1 2 3]))
                        (done))))))

(t/deftest transform-with-stateless-transducers
  (t/async done
    (let [s (s/from [1 2 3 4 5 6])
          ts (s/transform (comp
                           (map inc)
                           (filter odd?))
                          s)]
      (drain! ts #(t/is (= % [3 5 7])))
      (s/on-end ts done))))

(t/deftest transform-with-stateful-transducers
  (t/async done
    (let [s (s/from [1 2 3 4 5 6])
          ts (s/transform (comp
                           (partition-all 2)
                           (take 2))
                          s)]
      (drain! ts #(t/is (= % [[1 2] [3 4]])))
      (s/on-end ts done))))

(t/deftest observe-on
  (t/async done
    (let [coll [1 2 3]
          s (s/observe-on :asap (s/from coll))]
      (t/is (s/observable? s))
      (drain! s #(t/is (= % coll)))
      (s/on-end s done))))

(t/deftest subscribe-on
  (t/async done
    (let [coll [1 2 3]
          s (s/subscribe-on :queue (s/from coll))]
      (t/is (s/observable? s))
      (drain! s #(t/is (= % coll)))
      (s/on-end s done))))

(t/deftest scheduler-predicate-and-resolver
  (t/is (s/scheduler? (s/scheduler :asap)))
  (t/is (s/scheduler? (s/scheduler :queue)))
  (t/is (s/scheduler? (s/scheduler :async)))
  (t/is (s/scheduler? (s/scheduler :af)))
  (t/is (s/scheduler? (s/scheduler :animation-frame))))
