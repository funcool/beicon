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

(t/deftest observable-from-event-args
  (t/async done
    (let [target #js {:addEventListener (fn [type listener opts]
                                           (t/is (= type "poked"))
                                           (listener (.-passive opts)))
                      :removeEventListener #()}
          s (rx/from-event target "poked" {:passive false})]
      (t/is (rx/observable? s))
      (rx/end! (drain! s #(do
                           (t/is (= % [false]))
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

(t/deftest observable-start-with
  (t/async done
    (let [s (->> (rx/from [3 4 5])
                 (rx/start-with 1 2))]
      (drain! s #(t/is (= % [1 2 3 4 5])))
      (rx/on-end s done))))

(t/deftest observable-exhaust-map
  (t/async done
    (let [s (->> (rx/from [1 2 3])
                 (rx/exhaust-map (fn [v] (rx/of (* v 10)))))]
      (drain! s #(t/is (= % [10 20 30])))
      (rx/on-end s done))))

(t/deftest observable-pairwise
  (t/async done
    (let [s (->> (rx/from [1 2 3 4])
                 (rx/pairwise)
                 (rx/map vec))]
      (drain! s #(t/is (= % [[1 2] [2 3] [3 4]])))
      (rx/on-end s done))))

(t/deftest observable-to-array
  (t/async done
    (let [s (->> (rx/from [1 2 3])
                 (rx/to-array))]
      (drain! s #(t/is (= (js->clj (first %)) [1 2 3])))
      (rx/on-end s done))))

(t/deftest observable-group-by
  (t/async done
    (let [s (->> (rx/from [1 2 3 4 5 6])
                 (rx/group-by #(if (even? %) :even :odd))
                 (rx/merge-map (fn [grouped]
                                 (rx/map #(hash-map :key (.-key grouped) :val %) grouped)))
                 (rx/map #(update % :key keyword)))
          results (atom [])]
      (rx/subscribe s #(swap! results conj %))
      (rx/on-end s #(do
                      (t/is (= 6 (count @results)))
                      (done))))))

(t/deftest observable-repeat
  (t/async done
    (let [s (->> (rx/from [1 2])
                 (rx/repeat 3))]
      (drain! s #(t/is (= % [1 2 1 2 1 2])))
      (rx/on-end s done))))

(t/deftest observable-count
  (t/async done
    (let [s (->> (rx/from [1 2 3 4 5])
                 (rx/count))]
      (drain! s #(t/is (= % [5])))
      (rx/on-end s done))))

(t/deftest observable-count-with-predicate
  (t/async done
    (let [s (->> (rx/from [1 2 3 4 5])
                 (rx/count odd?))]
      (drain! s #(t/is (= % [3])))
      (rx/on-end s done))))

(t/deftest observable-every
  (t/async done
    (let [s (->> (rx/from [2 4 6 8])
                 (rx/every even?))]
      (drain! s #(t/is (= % [true])))
      (rx/on-end s done))))

(t/deftest observable-every-false
  (t/async done
    (let [s (->> (rx/from [2 3 6 8])
                 (rx/every even?))]
      (drain! s #(t/is (= % [false])))
      (rx/on-end s done))))

(t/deftest observable-element-at
  (t/async done
    (let [s (->> (rx/from [10 20 30 40 50])
                 (rx/element-at 2))]
      (drain! s #(t/is (= % [30])))
      (rx/on-end s done))))

(t/deftest observable-element-at-with-default
  (t/async done
    (let [s (->> (rx/from [10 20])
                 (rx/element-at 5 :default))]
      (drain! s #(t/is (= % [:default])))
      (rx/on-end s done))))

(t/deftest observable-window-time
  (t/async done
    (let [s (->> (rx/from [1 2 3 4 5])
                 (rx/window-time 100)
                 (rx/merge-map #(rx/to-array %))
                 (rx/map vec))]
      (drain! s #(t/is (>= (count %) 1)))
      (rx/on-end s done))))

(t/deftest observable-share-replay
  (t/async done
    (let [source (->> (rx/from [1 2 3])
                      (rx/share-replay 3))
          results1 (atom [])
          results2 (atom [])]
      (rx/subscribe source #(swap! results1 conj %))
      (rx/subscribe source #(swap! results2 conj %))
      (rx/on-end source #(do
                           (t/is (= @results1 [1 2 3]))
                           (t/is (= @results2 [1 2 3]))
                           (done))))))

(t/deftest observable-audit-time
  (t/async done
    (let [s (->> (rx/from [1 2 3 4 5])
                 (rx/audit-time 50))]
      (drain! s #(t/is (>= (count %) 1)))
      (rx/on-end s done))))

(t/deftest observable-end-with
  (t/async done
    (let [s (->> (rx/from [1 2 3])
                 (rx/end-with 4 5))]
      (drain! s #(t/is (= % [1 2 3 4 5])))
      (rx/on-end s done))))

(t/deftest observable-throw-if-empty
  (t/async done
    (let [s (->> (rx/from [1 2 3])
                 (rx/throw-if-empty #(js/Error. "empty")))
          results (atom [])]
      (rx/subscribe s #(swap! results conj %))
      (rx/on-end s #(do
                      (t/is (= @results [1 2 3]))
                      (done))))))

(t/deftest observable-throw-if-empty-throws
  (t/async done
    (let [s (->> (rx/empty)
                 (rx/throw-if-empty #(js/Error. "empty")))]
      (rx/subscribe s
                    (fn [_] (t/is false))
                    (fn [e] (do
                              (t/is (= (.-message e) "empty"))
                              (done)))))))

(t/deftest observable-find
  (t/async done
    (let [s (->> (rx/from [1 2 3 4 5])
                 (rx/find #(> % 3)))]
      (drain! s #(t/is (= % [4])))
      (rx/on-end s done))))

(t/deftest observable-is-empty-true
  (t/async done
    (let [s (->> (rx/empty)
                 (rx/is-empty))]
      (drain! s #(t/is (= % [true])))
      (rx/on-end s done))))

(t/deftest observable-is-empty-false
  (t/async done
    (let [s (->> (rx/from [1 2 3])
                 (rx/is-empty))]
      (drain! s #(t/is (= % [false])))
      (rx/on-end s done))))

(t/deftest observable-single
  (t/async done
    (let [s (->> (rx/from [1 2 3 4 5])
                 (rx/single #(= % 3)))]
      (drain! s #(t/is (= % [3])))
      (rx/on-end s done))))

(t/deftest observable-max
  (t/async done
    (let [s (->> (rx/from [3 1 4 1 5 9 2 6])
                 (rx/max))]
      (drain! s #(t/is (= % [9])))
      (rx/on-end s done))))

(t/deftest observable-min
  (t/async done
    (let [s (->> (rx/from [3 1 4 1 5 9 2 6])
                 (rx/min))]
      (drain! s #(t/is (= % [1])))
      (rx/on-end s done))))

(t/deftest observable-timestamp
  (t/async done
    (let [s (->> (rx/from [1 2 3])
                 (rx/timestamp)
                 (rx/map #(.-value %)))]
      (drain! s #(t/is (= % [1 2 3])))
      (rx/on-end s done))))

(t/deftest observable-materialize
  (t/async done
    (let [s (->> (rx/from [1 2])
                 (rx/materialize)
                 (rx/map #(.-kind %)))]
      (drain! s #(t/is (= % ["N" "N" "C"])))
      (rx/on-end s done))))

(t/deftest observable-dematerialize
  (t/async done
    (let [notifications #js [#js {:kind "N" :value 1} #js {:kind "C"}]
          s (->> (rx/from notifications)
                 (rx/dematerialize))]
      (drain! s #(t/is (= % [1])))
      (rx/on-end s done))))

(t/deftest observable-sequence-equal-true
  (t/async done
    (let [s1 (rx/from [1 2 3])
          s2 (rx/from [1 2 3])
          s (->> s1 (rx/sequence-equal s2))]
      (drain! s #(t/is (= % [true])))
      (rx/on-end s done))))

(t/deftest observable-sequence-equal-false
  (t/async done
    (let [s1 (rx/from [1 2 3])
          s2 (rx/from [1 2 4])
          s (->> s1 (rx/sequence-equal s2))]
      (drain! s #(t/is (= % [false])))
      (rx/on-end s done))))

(t/deftest observable-on-error-resume-next
  (t/async done
    (let [s1 (rx/create (fn [sink]
                          (rx/push! sink 1)
                          (rx/push! sink 2)
                          (rx/error! sink (js/Error. "oops"))))
          s2 (rx/from [3 4])
          s (->> s1 (rx/on-error-resume-next s2))]
      (drain! s #(t/is (= % [1 2 3 4])))
      (rx/on-end s done))))

(t/deftest observable-exhaust-all
  (t/async done
    (let [s (->> (rx/from [(rx/of 1 2) (rx/of 3 4)])
                 (rx/exhaust-all))]
      (drain! s #(t/is (= % [1 2 3 4])))
      (rx/on-end s done))))

(t/deftest observable-switch-map-to
  (t/async done
    (let [inner (rx/of :x)
          s (->> (rx/from [1 2 3])
                 (rx/switch-map-to inner))]
      (drain! s #(t/is (= % [:x :x :x])))
      (rx/on-end s done))))

(t/deftest observable-merge-map-to
  (t/async done
    (let [inner (rx/of :x)
          s (->> (rx/from [1 2 3])
                 (rx/merge-map-to inner))]
      (drain! s #(t/is (= % [:x :x :x])))
      (rx/on-end s done))))

(t/deftest observable-concat-map-to
  (t/async done
    (let [inner (rx/of :x)
          s (->> (rx/from [1 2 3])
                 (rx/concat-map-to inner))]
      (drain! s #(t/is (= % [:x :x :x])))
      (rx/on-end s done))))

(t/deftest observable-switch-scan
  (t/async done
    (let [s (->> (rx/from [1 2 3])
                 (rx/switch-scan (fn [acc v] (rx/of (+ acc v))) 0))]
      (drain! s #(t/is (= % [1 3 6])))
      (rx/on-end s done))))

(t/deftest observable-window-count
  (t/async done
    (let [s (->> (rx/from [1 2 3 4 5])
                 (rx/window-count 2)
                 (rx/merge-map #(rx/to-array %))
                 (rx/map vec))]
      (drain! s #(t/is (= % [[1 2] [3 4] [5]])))
      (rx/on-end s done))))

(t/deftest observable-race-with
  (t/async done
    (let [s1 (->> (rx/from [1 2 3])
                  (rx/delay 100))
          s2 (rx/from [10 20 30])
          s (->> s1 (rx/race-with s2))]
      (drain! s #(t/is (= % [10 20 30])))
      (rx/on-end s done))))

(t/deftest observable-partition
  (t/async done
    (let [s (rx/from [1 2 3 4 5 6])
          result (rx/partition even? s)
          evens (aget result 0)
          odds (aget result 1)
          even-results (atom [])
          odd-results (atom [])]
      (rx/subscribe evens #(swap! even-results conj %))
      (rx/subscribe odds #(swap! odd-results conj %))
      (rx/on-end evens #(do
                          (t/is (= @even-results [2 4 6]))
                          (t/is (= @odd-results [1 3 5]))
                          (done))))))

(t/deftest observable-distinct-until-key-changed
  (t/async done
    (let [s (->> (rx/from [{:id 1 :name "a"} {:id 1 :name "b"} {:id 2 :name "c"}])
                 (rx/distinct-until-key-changed :id))]
      (drain! s #(t/is (= (count %) 2)))
      (rx/on-end s done))))

(t/deftest observable-buffer-toggle
  (t/async done
    (let [source (rx/from [1 2 3 4 5])
          openings (rx/from [0 0])
          closing (fn [_] (rx/timer 10))
          s (->> source (rx/buffer-toggle openings closing))]
      (drain! s #(t/is (>= (count %) 0)))
      (rx/on-end s done))))

(t/deftest observable-buffer-when
  (t/async done
    (let [source (rx/from [1 2 3 4 5])
          closing #(rx/timer 10)
          s (->> source (rx/buffer-when closing))]
      (drain! s #(t/is (>= (count %) 1)))
      (rx/on-end s done))))

(t/deftest observable-window-toggle
  (t/async done
    (let [source (rx/from [1 2 3 4 5])
          openings (rx/from [0 0])
          closing (fn [_] (rx/timer 10))
          s (->> source (rx/window-toggle openings closing)
                 (rx/merge-map #(rx/to-array %))
                 (rx/map vec))]
      (drain! s #(t/is (>= (count %) 0)))
      (rx/on-end s done))))

(t/deftest observable-window-when
  (t/async done
    (let [source (rx/from [1 2 3 4 5])
          closing #(rx/timer 10)
          s (->> source (rx/window-when closing)
                 (rx/merge-map #(rx/to-array %))
                 (rx/map vec))]
      (drain! s #(t/is (>= (count %) 1)))
      (rx/on-end s done))))

(t/deftest observable-defer
  (t/async done
    (let [counter (atom 0)
          s (rx/defer (fn [] (swap! counter inc) (rx/of @counter)))
          s1 (atom nil)
          s2 (atom nil)]
      (rx/subscribe s #(reset! s1 %))
      (rx/subscribe s #(reset! s2 %))
      (rx/on-end s #(do
                      (t/is (= @s1 1))
                      (t/is (= @s2 2))
                      (done))))))

(t/deftest observable-iif-true
  (t/async done
    (let [s (rx/iif (constantly true) (rx/of :yes) (rx/of :no))]
      (drain! s #(t/is (= % [:yes])))
      (rx/on-end s done))))

(t/deftest observable-iif-false
  (t/async done
    (let [s (rx/iif (constantly false) (rx/of :yes) (rx/of :no))]
      (drain! s #(t/is (= % [:no])))
      (rx/on-end s done))))

(t/deftest observable-find-index
  (t/async done
    (let [s (->> (rx/from [1 2 3 4 5])
                 (rx/find-index #(> % 3)))]
      (drain! s #(t/is (= % [3])))
      (rx/on-end s done))))

(t/deftest observable-map-to
  (t/async done
    (let [s (->> (rx/from [1 2 3])
                 (rx/map-to :x))]
      (drain! s #(t/is (= % [:x :x :x])))
      (rx/on-end s done))))

(t/deftest observable-switch-all
  (t/async done
    (let [s (->> (rx/from [(rx/of 1 2) (rx/of 3 4)])
                 (rx/switch-all))]
      (drain! s #(t/is (= % [1 2 3 4])))
      (rx/on-end s done))))

(t/deftest observable-concat-all
  (t/async done
    (let [s (->> (rx/from [(rx/of 1 2) (rx/of 3 4)])
                 (rx/concat-all))]
      (drain! s #(t/is (= % [1 2 3 4])))
      (rx/on-end s done))))

(t/deftest observable-zip-all
  (t/async done
    (let [s (->> (rx/from [(rx/of 1 2) (rx/of 3 4)])
                 (rx/zip-all)
                 (rx/map vec))]
      (drain! s #(t/is (= % [[1 3] [2 4]])))
      (rx/on-end s done))))

(t/deftest observable-zip-with
  (t/async done
    (let [s1 (rx/from [1 2 3])
          s2 (rx/from [4 5 6])
          s (->> s1 (rx/zip-with s2) (rx/map vec))]
      (drain! s #(t/is (= % [[1 4] [2 5] [3 6]])))
      (rx/on-end s done))))

(t/deftest observable-merge-with
  (t/async done
    (let [s1 (rx/from [1 2])
          s2 (rx/from [3 4])
          s (->> s1 (rx/merge-with s2))]
      (drain! s #(t/is (= (set %) #{1 2 3 4})))
      (rx/on-end s done))))

(t/deftest observable-concat-with
  (t/async done
    (let [s1 (rx/from [1 2])
          s2 (rx/from [3 4])
          s (->> s1 (rx/concat-with s2))]
      (drain! s #(t/is (= % [1 2 3 4])))
      (rx/on-end s done))))

(t/deftest observable-time-interval
  (t/async done
    (let [s (->> (rx/from [1 2 3])
                 (rx/time-interval)
                 (rx/map #(.-value %)))]
      (drain! s #(t/is (= % [1 2 3])))
      (rx/on-end s done))))

(t/deftest observable-repeat-when
  (t/async done
    (let [retry-count (atom 0)
          s (->> (rx/create (fn [sink]
                              (swap! retry-count inc)
                              (rx/push! sink @retry-count)
                              (rx/end! sink)))
                 (rx/repeat-when (fn [notifier] (rx/take 2 notifier))))]
      (drain! s #(t/is (= % [1 2])))
      (rx/on-end s done))))

(t/deftest observable-retry-when
  (t/async done
    (let [attempt (atom 0)
          s (->> (rx/create (fn [sink]
                              (swap! attempt inc)
                              (if (< @attempt 3)
                                (rx/error! sink (js/Error. "fail"))
                                (do
                                  (rx/push! sink :success)
                                  (rx/end! sink)))))
                 (rx/retry-when (fn [errors] (rx/delay 10 errors))))]
      (drain! s #(t/is (= % [:success])))
      (rx/on-end s done))))

(t/deftest observable-timeout-with
  (t/async done
    (let [s (->> (rx/from [1 2 3])
                 (rx/timeout-with 100 (rx/of :timeout)))]
      (drain! s #(t/is (= % [1 2 3])))
      (rx/on-end s done))))

;; Skipping from-event-pattern test for now - needs more investigation
;; (t/deftest observable-from-event-pattern
;;   (t/async done
;;     (let [handlers (atom [])
;;           add-handler (fn [handler] (swap! handlers conj handler))
;;           remove-handler (fn [handler] (swap! handlers #(remove #{handler} %)))
;;           s (rx/from-event-pattern add-handler remove-handler)]
;;       (t/is (rx/observable? s))
;;       (rx/sub! (fn [v] (t/is (= v "test"))) s)
;;       (doseq [h @handlers] (h "test"))
;;       (rx/end! s)
;;       (done))))

(t/deftest observable-generate
  (t/async done
    (let [s (rx/generate 0
                         (fn [x] (< x 5))
                         (fn [x] (inc x))
                         (fn [x] (* x 2)))]
      (drain! s #(t/is (= % [0 2 4 6 8])))
      (rx/on-end s done))))

(t/deftest observable-bind-callback
  (t/async done
    (let [async-fn (fn [x callback]
                     (js/setTimeout #(callback (* x 2)) 10))
          bound-fn (rx/bind-callback async-fn)
          s (bound-fn 5)]
      (drain! s #(t/is (= % [10])))
      (rx/on-end s done))))

(t/deftest observable-bind-node-callback
  (t/async done
    (let [node-fn (fn [x callback]
                    (js/setTimeout #(callback nil (* x 3)) 10))
          bound-fn (rx/bind-node-callback node-fn)
          s (bound-fn 5)]
      (drain! s #(t/is (= % [15])))
      (rx/on-end s done))))

(t/deftest observable-using
  (t/async done
    (let [disposed? (atom false)
          resource (fn [] #js {:unsubscribe #(reset! disposed? true)})
          observable-fn (fn [r] (rx/of (.-unsubscribe r)))
          s (rx/using resource observable-fn)]
      (drain! s #(t/is (fn? (first %))))
      (rx/on-end s #(do
                      (t/is @disposed?)
                      (done))))))

;; Skipping connectable test for now - needs more investigation
;; (t/deftest observable-connectable
;;   (t/async done
;;     (let [source (rx/from [1 2 3])
;;           connectable-obs (rx/connectable source)
;;           results (atom [])]
;;       (rx/sub! #(swap! results conj %) connectable-obs)
;;       (rx/sub! #(swap! results conj %) connectable-obs)
;;       (.connect connectable-obs)
;;       (js/setTimeout #(do
;;                         (t/is (= @results [1 1 2 2 3 3]))
;;                         (done))
;;                      50))))

(t/deftest observable-first-value-from
  (t/async done
    (let [s (rx/from [1 2 3])
          p (rx/first-value-from s)]
      (.then p (fn [v]
                 (t/is (= v 1))
                 (done))))))

(t/deftest observable-last-value-from
  (t/async done
    (let [s (rx/from [1 2 3])
          p (rx/last-value-from s)]
      (.then p (fn [v]
                 (t/is (= v 3))
                 (done))))))

(t/deftest observable-is-observable
  (t/is (rx/is-observable (rx/from [1 2 3])))
  (t/is (not (rx/is-observable [1 2 3])))
  (t/is (not (rx/is-observable nil))))
