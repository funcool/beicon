(ns beicon.tests.v2-test
  (:require
   [cljs.test :as t]
   [beicon.v2.core :as rx]
   [beicon.v2.operators :as rxo]
   [beicon.tests.helpers
    :refer (noop drain!)
    :refer-macros (with-timeout)]))

;; event stream

(t/deftest observable-from-values
  (t/async done
    (let [s (rx/of 1 2 3 4 5 6 7 8 9)]
      (t/is (rx/observable? s))
      (drain! s #(do
                   (t/is (= % [1 2 3 4 5 6 7 8 9]))
                   (done))))))

(t/deftest observable-from-values-with-nil
  (t/async done
    (let [s (rx/of 1 nil 2)]
      (t/is (rx/observable? s))
      (drain! s #(do
                   (t/is (= % [1 nil 2]))
                   (done))))))

(t/deftest observable-from-vector
  (t/async done
    (let [coll [1 2 3]
          s (rx/from coll)]
      (t/is (rx/observable? s))
      (drain! s #(do
                   (t/is (= % coll))
                   (done))))))

(t/deftest observable-from-vector-with-take
  (t/async done
    (let [coll [1 2 3 4 5 6]
          s (->> (rx/from coll)
                 (rx/take 2))]
      (t/is (rx/observable? s))
      (drain! s #(t/is (= % [1 2])))
      (rx/on-end s done))))

(t/deftest observable-from-atom
  (t/async done
    (let [a (atom 0)
          s (->> (rx/from-atom a)
                 (rx/take 4))]
      (t/is (rx/observable? s))
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
          s (rx/from coll)]
      (t/is (rx/observable? s))
      (drain! s #(t/is (= (set %) coll)))
      (rx/on-end s done))))

(t/deftest observable-from-create
  (t/async done
    (let [s (rx/create (fn [sink]
                        (with-timeout 10
                          (rx/push! sink 1)
                          (rx/push! sink 2)
                          (rx/push! sink 3)
                          (rx/end! sink))))]
      (t/is (rx/observable? s))
      (drain! s #(t/is (= % [1 2 3])))
      (rx/on-end s done))))

(t/deftest observable-from-event
  (t/async done
    (let [target #js {:addEventListener #(do
                                           (t/is (= %1 "poked"))
                                           (%2 "once"))
                      :removeEventListener #()}
          s (rx/from-event target "poked")]
      (t/is (rx/observable? s))
      (rx/end! (drain! s #(do
                           (t/is (= % ["once"]))
                           (done)))))))

(t/deftest observable-with-timeout
  (t/async done
    (let [s (->> (rx/timer 200)
                 (rx/timeout 100 (rx/of :timeout)))]

      (t/is (rx/observable? s))
      (drain! s #(do
                   (t/is (= % [:timeout]))
                   (rx/on-end s done))))))

(t/deftest observable-pause-from-timer
  (t/async done
    (let [s (rx/timer 100)]
      (t/is (rx/observable? s))
      (drain! s #(do
                   (t/is (= % [0]))
                   (rx/on-end s done))))))

(t/deftest observable-interval-from-timer
  (t/async done
    (let [s (->> (rx/timer 100 100)
                 (rx/take 2))]
      (t/is (rx/observable? s))
      (drain! s #(do
                   (t/is (= % [0 1]))
                   (rx/on-end s done))))))

(t/deftest observable-errors-from-create
  (t/async done
    (let [s (rx/create (fn [sink]
                        (with-timeout 10
                          (rx/push! sink 1)
                          (rx/error! sink (ex-info "oh noes" {})))))]
      (t/is (rx/observable? s))
      (drain! s
              #(t/is (= % [1]))
              #(t/is (= (ex-message %) "oh noes")))
      (rx/on-error s done))))

(t/deftest observable-from-promise
  (t/async done
    (let [p (js/Promise.resolve 42)
          s (rx/from p)]
      (t/is (rx/observable? s))
      (drain! s
              #(t/is (= % [42])))
      (rx/on-end s done))))

(t/deftest observable-range
  (t/async done
    (let [s (rx/range 5)]
      (t/is (rx/observable? s))
      (drain! s #(t/is (= % [0 1 2 3 4])))
      (rx/on-end s done))))

(t/deftest observable-of
  (t/async done
    (let [s (rx/of 1)]
      (t/is (rx/observable? s))
      (drain! s #(t/is (= % [1])))
      (rx/on-end s done))))

(t/deftest observable-empty
  (t/async done
    (let [n (rx/empty)]
      (rx/on-end n done))))

(t/deftest observable-concat-1
  (t/async done
    (let [s1 (rx/from [1 2 3])
          s2 (rx/from [4 5 6])
          cs (rx/concat s1 s2)]
      (drain! cs #(t/is (= % [1 2 3 4 5 6])))
      (rx/on-end cs done))))

(t/deftest observable-concat-2
  (t/async done
    (let [s1 (rx/from [1 2 3])
          s2 (rx/from [4 5 6])
          cs (rx/concat s1 s2 nil nil)]
      (drain! cs #(t/is (= % [1 2 3 4 5 6])))
      (rx/on-end cs done))))

(t/deftest observable-zip-1
  (t/async done
    (let [s1 (rx/from [1 2])
          s2 (rx/from [4 5])
          s3 (rx/from [7 8])
          cs (->> (rx/zip s1 s2 s3)
                  (rx/map vec))]
      (drain! cs #(t/is (= % [[1 4 7] [2 5 8]])))
      (rx/on-end cs done))))

(t/deftest observable-zip-2
  (t/async done
    (let [s1 (rx/from [1 2])
          s2 (rx/from [4 5])
          s3 (rx/from [7 8])
          cs (rx/zip vector s1 s2 s3)]
      (drain! cs #(t/is (= % [[1 4 7] [2 5 8]])))
      (rx/on-end cs done))))

(t/deftest observable-fjoin
  (t/async done
    (let [s1 (rx/from [1 2])
          s2 (rx/from [4 5])
          s3 (rx/from [7 8])
          cs (rx/fjoin vector
                      s1 s2 s3)]
      (drain! cs #(t/is (= % [[2 5 8]])))
      (rx/on-end cs done))))

(t/deftest observable-merge
  (t/async done
    (let [s1 (rx/from [1 2 3])
          s2 (rx/from [:1 :2 :3])
          ms (rx/merge s1 s2)]
      (drain! ms #(t/is (= (set %) #{:1 1 :2 2 :3 3})))
      (rx/on-end ms done))))

(t/deftest observable-skip-while
  (t/async done
    (let [nums (rx/from [1 1 1 2 3 4 5])
          sample (rx/skip-while odd? nums)]
      (drain! sample #(t/is (= % [2 3 4 5])))
      (rx/on-end sample done))))

(t/deftest subject-as-ideref
  (t/async done
    (let [nums (rx/from [1 1 1 2 3 4 5])
          sub (rx/behavior-subject nil)]
      (rx/on-end sub #(t/is (= @sub 5)
                           (done)))
      (rx/subscribe nums sub))))

(t/deftest subject-push
  (t/async done
    (let [b (rx/subject)]
      (t/is (rx/subject? b))
      (drain! b #(t/is (= % [1 2 3])))
      (rx/push! b 1)
      (rx/push! b 2)
      (rx/push! b 3)
      (rx/end! b)
      (rx/on-end b done))))

(t/deftest behavior-subject
  (t/async done
    (let [b (rx/behavior-subject -1)]
      (t/is (rx/subject? b))
      (drain! b #(do
                   (t/is (= % [-1 1 2 3]))
                   (done)))
      (rx/push! b 1)
      (rx/push! b 2)
      (rx/push! b 3)
      (rx/end! b))))

(t/deftest observable-reduce
  (t/async done
    (let [s (->> (rx/from [4 5 6])
                 (rx/reduce conj [1 2]))]
      (drain! s #(do (t/is (= % [[1 2 4 5 6]]))
                     (done))))))


(t/deftest observable-scan
  (t/async done
    (let [s (->> (rx/from [4 5 6])
                 (rx/scan conj [1]))]
      (drain! s #(do (t/is (= % [[1 4] [1 4 5] [1 4 5 6]]))
                     (done))))))

(t/deftest observable-merge-scan
  (t/async done
    (let [s (->> (rx/from [4 5 6])
                 (rx/merge-scan (fn [acc i] (rx/of (conj acc i))) [1]))]
      (drain! s #(do (t/is (= % [[1 4] [1 4 5] [1 4 5 6]]))
                     (done))))))

(t/deftest observable-filter-with-ifn
  (t/async done
    (let [s (rx/from [1 2 3 4 5])
          fs (rx/filter #{3 5} s)]
      (drain! fs #(t/is (= % [3 5])))
      (rx/on-end fs done))))

(t/deftest observable-map-with-ifn
  (t/async done
    (let [s (rx/from [{:foo 1} {:foo 2}])
          fs (rx/map :foo s)]
      (drain! fs #(t/is (= % [1 2])))
      (rx/on-end fs done))))

(t/deftest observable-map-filter-comp-kk
  (t/async done
    (let [s  (rx/of 1 2 3 4 5 6 7)
          x  (rx/comp (rxo/map inc)
                      (rxo/filter odd?))
          s  (rx/pipe x s)]
      (drain! s #(t/is (= % [3 5 7])))
      (rx/on-end s done))))

(t/deftest observable-map-indexed
  (t/async done
    (let [s (rx/from [:a :b :c])
          fs (rx/map-indexed vector s)]
      (drain! fs #(t/is (= % [[0 :a] [1 :b] [2 :c]])))
      (rx/on-end fs done))))

(t/deftest observable-retry
  (t/async done
    (let [errored? (volatile! false)
          s        (rx/create (fn [sink]
                               (if @errored?
                                 (do
                                   (rx/push! sink 2)
                                   (rx/push! sink 3)
                                   (rx/end! sink))
                                 (do
                                   (vreset! errored? true)
                                   (rx/error! sink (js/Error.))))))
             rs (rx/retry 2 s)]
      (t/is (rx/observable? rs))
      (drain! rs #(t/is (= % [2 3])))
      (rx/on-end rs done))))

(t/deftest observable-with-latest-from-1
  (t/async done
    (let [s1 (rx/from [1 2 3])
          s2 (rx/from [0])
          s3 (rx/from [4 5 6])
          s4 (->> s1
                  (rx/pipe (rxo/with-latest s2 s3))
                  (rx/map vec))]
      (t/is (rx/observable? s3))
      (drain! s4 #(t/is (= % [[1 0 6] [2 0 6] [3 0 6]])))
      (rx/on-end s3 done))))

(t/deftest observable-with-latest-from-2
  (t/async done
    (let [s1 (rx/from [1 2 3])
          s2 (rx/from [0])
          s3 (rx/from [4 5 6])
          s4 (->> s1
                  (rx/pipe (rxo/with-latest vector s2 s3)))]
      (t/is (rx/observable? s3))
      (drain! s4 #(t/is (= % [[1 0 6] [2 0 6] [3 0 6]])))
      (rx/on-end s3 done))))

(t/deftest observable-combine-latest-2
  (t/async done
    (let [s1 (rx/delay 10 (rx/from [9]))
          s2 (rx/delay 10 (rx/from [2]))
          s3 (->> (rx/combine-latest s1 s2)
                  (rx/map vec)
                  (rx/delay-at-least 100))]
      (t/is (rx/observable? s3))
      (drain! s3 #(t/is (= % [[9 2]])))
      (rx/on-end s3 done))))

(t/deftest observable-combine-latest-3
  (t/async done
    (let [s1 (rx/delay 10 (rx/from [9]))
          s2 (rx/delay 10 (rx/from [2]))
          s3 (->> (rx/combine-latest-all [s1 s2])
                  (rx/map vec)
                  (rx/delay-at-least 100))]
      (t/is (rx/observable? s3))
      (drain! s3 #(t/is (= % [[9 2]])))
      (rx/on-end s3 done))))

(t/deftest observable-combine-latest-4
  (t/async done
    (let [s1 (rx/delay 10 (rx/from [9]))
          s2 (rx/delay 10 (rx/from [2]))
          s3 (rx/delay 10 (rx/from [1]))
          s4 (rx/delay 10 (rx/from [3]))
          s5 (rx/delay 10 (rx/from [4]))
          s6 (rx/delay 10 (rx/from [5]))
          s3 (->> (rx/combine-latest s1 s2 s3 s4 s5 s6)
                  (rx/map vec)
                  (rx/delay-at-least 100))]
      (t/is (rx/observable? s3))
      (drain! s3 #(t/is (= % [[9 2 1 3 4 5]])))
      (rx/on-end s3 done))))

(t/deftest observable-combine-latest-5
  (t/async done
    (let [s1 (rx/delay 10 (rx/from [9]))
          s2 (rx/delay 10 (rx/from [2]))
          s3 (rx/delay 10 (rx/from [1]))
          s4 (rx/delay 10 (rx/from [3]))
          s5 (rx/delay 10 (rx/from [4]))
          s6 (rx/delay 10 (rx/from [5]))
          s3 (->> (rx/combine-latest vector s1 s2 s3 s4 s5 s6)
                  (rx/delay-at-least 100))]
      (t/is (rx/observable? s3))
      (drain! s3 #(t/is (= % [[9 2 1 3 4 5]])))
      (rx/on-end s3 done))))

(t/deftest observable-catch-0
  (t/async done
    (let [s1 (rx/throw (fn [] (ex-info "error" {:foo :bar})))
          s2 (rx/catch (fn [error]
                         (rx/of (ex-data error)))
                 s1)]
      (t/is (rx/observable? s2))
      (drain! s2 #(t/is (= % [{:foo :bar}])))
      (rx/on-end s2 done))))

(t/deftest observable-catch-1
  (t/async done
    (let [s1 (rx/throw (ex-info "error" {:foo :bar}))
          s2 (rx/catch (fn [error]
                         (rx/of (ex-data error)))
                 s1)]
      (t/is (rx/observable? s2))
      (drain! s2 #(t/is (= % [{:foo :bar}])))
      (rx/on-end s2 done))))

(t/deftest observable-catch-2
  (t/async done
    (let [type1? #(= 1 (:type (ex-data %)))
          s1 (->> (rx/throw (ex-info "error" {:type 1}))
                  (rx/catch type1? #(rx/of (ex-data %))))]
      (t/is (rx/observable? s1))
      (drain! s1 #(t/is (= % [{:type 1}])))
      (rx/on-end s1 done))))

(t/deftest observable-catch-3
  (t/async done
    (let [type1? #(= 1 (:type (ex-data %)))
          type2? #(= 2 (:type (ex-data %)))
          s1 (->> (rx/throw (ex-info "error" {:type 1}))
                  (rx/catch type2? #(rx/of (ex-data %)))
                  (rx/catch type1? #(rx/of (ex-data %))))]
      (t/is (rx/observable? s1))
      (drain! s1 #(t/is (= % [{:type 1}])))
      (rx/on-end s1 done))))

(t/deftest observable-to-atom
  (t/async done
    (let [st (rx/from [1 2 3])
          a (rx/to-atom st)]
      (rx/on-end st #(do (t/is (= @a 3))
                        (done))))))

(t/deftest observable-to-atom-with-atom
  (t/async done
    (let [st (rx/from [1 2 3])
          vacc (volatile! [])
          a (atom 0)]
      (add-watch a :acc
                 (fn [_ _ _ v]
                   (vswap! vacc conj v)))
      (rx/to-atom st a)
      (rx/on-end st #(do (t/is (= @a 3))
                        (t/is (= @vacc [1 2 3]))
                        (done))))))

(t/deftest observable-to-atom-with-atom-and-function
  (t/async done
    (let [st (rx/from [1 2 3])
          a (atom [])]
      (rx/to-atom st a conj)
      (rx/on-end st #(do (t/is (= @a [1 2 3]))
                        (done))))))

(t/deftest transform-with-stateless-transducers
  (t/async done
    (let [s  (rx/from [1 2 3 4 5 6])
          xf (comp
              (map inc)
              (filter odd?))
          ts (rx/transform xf s)]

      (drain! ts #(t/is (= % [3 5 7])))
      (rx/on-end ts done))))

(t/deftest transform-with-stateful-transducers
  (t/async done
    (let [s (rx/from [1 2 3 4 5 6])
          ts (rx/transform (comp
                           (partition-all 2)
                           (take 2))
                          s)]
      (drain! ts #(t/is (= % [[1 2] [3 4]])))
      (rx/on-end ts done))))

(t/deftest observe-on
  (t/async done
    (let [coll [1 2 3]
          s (rx/observe-on :asap (rx/from coll))]
      (t/is (rx/observable? s))
      (drain! s #(t/is (= % coll)))
      (rx/on-end s done))))

(t/deftest subscribe-on
  (t/async done
    (let [coll [1 2 3]
          s (rx/subscribe-on :queue (rx/from coll))]
      (t/is (rx/observable? s))
      (drain! s #(t/is (= % coll)))
      (rx/on-end s done))))

(t/deftest scheduler-predicate-and-resolver
  (t/is (rx/scheduler? (rx/scheduler :asap)))
  (t/is (rx/scheduler? (rx/scheduler :queue)))
  (t/is (rx/scheduler? (rx/scheduler :async)))
  (t/is (rx/scheduler? (rx/scheduler :af)))
  (t/is (rx/scheduler? (rx/scheduler :animation-frame))))
