(ns beicon.tests.core-tests
  (:require [cljs.test :as t]
            [promesa.core :as p]
            [beicon.core :as s]
            [beicon.tests.helpers
             :refer (no-op drain!)
             :refer-macros (with-timeout)]))

;; event stream

(t/deftest observable-from-values
  (t/async done
    (let [s (s/of 1 2 3 4 5 6 7 8 9)]
      (t/is (s/observable? s))
      (drain! s #(t/is (= % [1 2 3 4 5 6 7 8 9])))
      (s/on-end s done))))

(t/deftest observable-from-vector
  (t/async done
    (let [coll [1 2 3]
          s (s/from-coll coll)]
      (t/is (s/observable? s))
      (drain! s #(t/is (= % coll)))
      (s/on-end s done))))

(t/deftest observable-from-vector-with-take
  (t/async done
    (let [coll [1 2 3 4 5 6]
          s (->> (s/from-coll coll)
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
          s (s/from-coll coll)]
      (t/is (s/observable? s))
      (drain! s #(t/is (= (set %) coll)))
      (s/on-end s done))))

(t/deftest observable-from-create
  (t/async done
    (let [s (s/create (fn [sink]
                        (with-timeout 10
                          (sink 1)
                          (sink 2)
                          (sink 3)
                          (sink nil))))]
      (t/is (s/observable? s))
      (drain! s #(t/is (= % [1 2 3])))
      (s/on-end s done))))

(t/deftest observable-with-timeout
  (t/async done
    (let [s (->> (s/timer 200)
                 (s/timeout 100 (s/just :timeout)))]

      (t/is (s/observable? s))
      (drain! s #(do
                   (t/is (= % [:timeout]))
      (s/on-end s done))))))


(t/deftest observable-errors-from-create
  (t/async done
    (let [s (s/create (fn [sink]
                        (with-timeout 10
                          (sink 1)
                          (sink (ex-info "oh noes" {})))))]
      (t/is (s/observable? s))
      (drain! s
              #(t/is (= % [1]))
              #(t/is (= (ex-message %) "oh noes")))
      (s/on-error s done))))

(t/deftest observable-from-promise
  (t/async done
    (let [p (p/resolved 42)
          s (s/from-promise p)]
      (t/is (s/observable? s))
      (drain! s
              #(t/is (= % [42])))
      (s/on-end s done))))

(t/deftest observable-from-rejected-promise
  (t/async done
    (let [p (p/rejected (ex-info "oh noes" {}))
          s (s/from-promise p)]
      (t/is (s/observable? s))
      (drain! s
              #(t/is (= % []))
              #(t/is (= (ex-message %) "oh noes")))
      (s/on-error s done))))

(t/deftest observable-range
  (t/async done
    (let [s (s/range 5)]
      (t/is (s/observable? s))
      (drain! s #(t/is (= % [0 1 2 3 4])))
      (s/on-end s done))))

(t/deftest observable-once
  (t/async done
    (let [s (s/once 1)]
      (t/is (s/observable? s))
      (drain! s #(t/is (= % [1])))
      (s/on-end s done))))

(t/deftest observable-never
  (t/async done
    (let [n (s/never)]
      (s/on-end n done))))

(t/deftest observable-concat
  (t/async done
    (let [s1 (s/from-coll [1 2 3])
          s2 (s/from-coll [4 5 6])
          cs (s/concat s2 s1)]
      (drain! cs #(t/is (= % [1 2 3 4 5 6])))
      (s/on-end cs done))))

(t/deftest observable-zip
  (t/async done
    (let [s1 (s/from-coll [1 2])
          s2 (s/from-coll [4 5])
          s3 (s/from-coll [7 8])
          cs (s/zip s1 s2 s3)]
      (drain! cs #(t/is (= % [[1 4 7] [2 5 8]])))
      (s/on-end cs done))))

(t/deftest observable-fjoin
  (t/async done
    (let [s1 (s/from-coll [1 2])
          s2 (s/from-coll [4 5])
          s3 (s/from-coll [7 8])
          cs (s/fjoin vector
                      s1 s2 s3)]
      (drain! cs #(t/is (= % [[2 5 8]])))
      (s/on-end cs done))))

(t/deftest observable-merge
  (t/async done
    (let [s1 (s/from-coll [1 2 3])
          s2 (s/from-coll [:1 :2 :3])
          ms (s/merge s1 s2)]
      (drain! ms #(t/is (= (set %) #{:1 1 :2 2 :3 3})))
      (s/on-end ms done))))

(t/deftest observable-skip-while
  (t/async done
    (let [nums (s/from-coll [1 1 1 2 3 4 5])
          sample (s/skip-while odd? nums)]
      (drain! sample #(t/is (= % [2 3 4 5])))
      (s/on-end sample done))))

;; (t/deftest observable-skip-until
;;   (t/async done
;;     (let [s (s/bus)
;;           sv (s/bus)
;;           sample (s/skip-until sv s)]
;;       (drain! sample #(t/is (= % [3 4 5])))
;;       (s/on-end sample done)
;;       ;; push values onto stream
;;       (s/push! s 1)
;;       (s/push! s 2)
;;       ;; open switch
;;       (s/push! sv :value)
;;       ;; push some more
;;       (s/push! s 3)
;;       (s/push! s 4)
;;       (s/push! s 5)
;;       ;; end
;;       (s/end! s)
;;       (s/end! sv))))

(t/deftest bus-push
  (t/async done
    (let [b (s/bus)]
      (t/is (s/bus? b))
      (drain! b #(t/is (= % [1 2 3])))
      (s/push! b 1)
      (s/push! b 2)
      (s/push! b 3)
      (s/end! b)
      (s/on-end b done))))

(t/deftest observable-filter-with-predicate
  (t/async done
    (let [s (s/from-coll [1 2 3 4 5])
          fs (s/filter #{3 5} s)]
      (drain! fs #(t/is (= % [3 5])))
      (s/on-end fs done))))

(t/deftest observable-map-with-ifn
  (t/async done
    (let [s (s/from-coll [{:foo 1} {:foo 2}])
          fs (s/map :foo s)]
      (drain! fs #(do
                    (t/is (= % [1 2]))
                    (done))))))

(t/deftest observable-retry
  (t/async done
    (let [errored? (volatile! false)
          s (s/create (fn [sink]
                        (if @errored?
                          (do
                            (sink 2)
                            (sink 3)
                            (sink nil))
                           (do
                             (vreset! errored? true)
                                   (sink (js/Error.))))))
                 rs (s/retry 2 s)]
      (t/is (s/observable? rs))
      (drain! rs #(t/is (= % [2 3])))
      (s/on-end rs done))))

(t/deftest observable-with-latest-from
  (t/async done
    (let [s1 (s/from-coll [0])
          s2 (s/from-coll [1 2 3])
          s3 (s/with-latest-from s1 s2)]
      (t/is (s/observable? s3))
      (drain! s3 #(t/is (= % [[1 0] [2 0] [3 0]])))
      (s/on-end s3 done))))

(t/deftest observable-catch
  (t/async done
    (let [s1 (s/throw (ex-info "error" {:foo :bar}))
          s2 (s/catch (fn [error]
                        (s/once (ex-data error)))
                 s1)]
      (t/is (s/observable? s2))
      (drain! s2 #(t/is (= % [{:foo :bar}])))
      (s/on-end s2 done))))

(t/deftest observable-to-atom
  (t/async done
    (let [st (s/from-coll [1 2 3])
          a (s/to-atom st)]
      (s/on-end st #(do (t/is (= @a 3))
                        (done))))))

(t/deftest observable-to-atom-with-atom
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
                        (done))))))

(t/deftest observable-to-atom-with-atom-and-function
  (t/async done
    (let [st (s/from-coll [1 2 3])
          a (atom [])]
      (s/to-atom st a conj)
      (s/on-end st #(do (t/is (= @a [1 2 3]))
                        (done))))))

(t/deftest transform-with-stateless-transducers
  (t/async done
    (let [s (s/from-coll [1 2 3 4 5 6])
          ts (s/transform (comp
                           (map inc)
                           (filter odd?))
                          s)]
      (drain! ts #(t/is (= % [3 5 7])))
      (s/on-end ts done))))

(t/deftest transform-with-stateful-transducers
  (t/async done
    (let [s (s/from-coll [1 2 3 4 5 6])
          ts (s/transform (comp
                           (partition-all 2)
                           (take 2))
                          s)]
      (drain! ts #(t/is (= % [[1 2] [3 4]])))
      (s/on-end ts done))))

(t/deftest observe-on
  (t/async done
    (let [coll [1 2 3]
          s (s/observe-on s/async (s/from-coll coll))]
      (t/is (s/observable? s))
      (drain! s #(t/is (= % coll)))
      (s/on-end s done))))

(t/deftest subscribe-on
  (t/async done
    (let [coll [1 2 3]
          s (s/subscribe-on s/queue (s/from-coll coll))]
      (t/is (s/observable? s))
      (drain! s #(t/is (= % coll)))
      (s/on-end s done))))
