(ns beicon.core-spec
  (:require [cljs.test :as t]
            [cats.core :as m]
            [beicon.core :as s]))

;; --- helpers for testing

(def no-op (fn [& args]))

(defmacro with-timeout
  [ms & body]
  `(js/setTimeout
    (fn []
      (do
        ~@body))
    ~ms))

(defn drain!
  ([obs cb]
   (drain! obs cb #(println "Error: " %)))
  ([obs cb errb]
   (let [values (volatile! [])]
     (s/on-value obs #(vswap! values conj %))
     (s/on-error obs #(errb %))
     (s/on-end obs #(cb @values)))))

(defn tick
  [interval]
  (s/from-poll interval #(.getTime (js/Date.))))

;; event stream

(defn log
  [e ob]
  (s/tap (fn [v]
           (println "[log]:" e ":" v)
           v) ob))

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

(t/deftest observable-from-callback
  (t/async done
    (let [s (s/from-callback (fn [sink]
                               (with-timeout 10
                                 (sink 1)
                                 nil)))]
      (t/is (s/observable? s))
      (drain! s #(t/is (= % [1])))
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

(t/deftest observable-errors-from-binder
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

(t/deftest observable-repeat
  (t/async done
    (let [s (s/repeat 1 2)]
      (t/is (s/observable? s))
      (drain! s #(t/is (= % [1 1])))
      (s/on-end s done))))

(t/deftest observable-once
  (t/async done
    (let [s (s/once 1)]
      (t/is (s/observable? s))
      (drain! s #(t/is (= % [1])))
      (s/on-end s done))))

;; (t/deftest observable-never
;;   (t/async done
;;     (let [n (s/never)]
;;       (s/on-end n done))))

;; (t/deftest observable-on-value
;;   (t/async done
;;     (let [s (s/from-coll [1 2 3])
;;           vacc (volatile! [])]
;;       (s/on-value s #(vswap! vacc conj %))
;;       (s/on-end s #(do (t/is (= @vacc [1 2 3]))
;;                        (done))))))

(t/deftest observable-concat
  (t/async done
    (let [s1 (s/bus)
          s2 (s/bus)
          cs (s/concat s1 s2)]
      (drain! cs #(t/is (= % [1 2 3 4])))
      (s/on-end cs done)
      (s/push! s1 1)
      (s/push! s1 2)
      (s/push! s2 :discarded)
      (s/push! s2 :discarded)
      (s/end! s1)
      (s/push! s2 3)
      (s/push! s2 4)
      (s/end! s2))))

(t/deftest event-stream-merge
  (t/async done
    (let [s1 (s/from-coll [1 2 3])
          s2 (s/from-coll [:1 :2 :3])
          ms (s/merge s1 s2)]
      (drain! ms #(t/is (= % [1 :1 2 :2 3 :3])))
      (s/on-end ms done))))

(t/deftest event-stream-skip-while
  (t/async done
    (let [nums (s/from-coll [1 1 1 2 3 4 5])
          sample (s/skip-while odd? nums)]
      (drain! sample #(t/is (= % [2 3 4 5])))
      (s/on-end sample done))))

(t/deftest event-stream-skip-until
  (t/async done
    (let [s (s/bus)
          sv (s/bus)
          sample (s/skip-until sv s)]
      (drain! sample #(t/is (= % [3 4 5])))
      (s/on-end sample done)
      ;; push values onto stream
      (s/push! s 1)
      (s/push! s 2)
      ;; open switch
      (s/push! sv :value)
      ;; push some more
      (s/push! s 3)
      (s/push! s 4)
      (s/push! s 5)
      ;; end
      (s/end! s)
      (s/end! sv))))

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

(t/deftest filter-with-predicate
  (t/async done
    (let [s (s/from-coll [1 2 3 4 5])
          fs (s/filter #{3 5} s)]
      (drain! fs #(t/is (= % [3 5])))
      (s/on-end fs done))))

(t/deftest map-with-ifn
  (t/async done
    (let [s (s/from-coll [{:foo 1} {:foo 2}])
          fs (s/map :foo s)]
      (drain! fs #(do
                    (t/is (= % [1 2]))
                    (done))))))

(t/deftest event-stream-as-functor
 (t/async done
   (let [s (s/from-coll [0 1 2])
         s2 (m/fmap inc s)]
     (t/is (s/observable? s))
     (t/is (s/observable? s2))
     (drain! s2 #(do (t/is (= % [1 2 3]))
                      (done))))))

(t/deftest event-stream-as-applicative
 (t/async done
   (let [pinc (m/pure s/observable-context inc)
         pval (m/pure s/observable-context 41)
         life (m/fapply pinc pval)]
     (t/is (s/observable? life))
     (drain! life #(do (t/is (= % [42]))
                       (done))))))

(t/deftest event-stream-as-monad
  (t/async done
    (let [sn (s/from-coll [1 2 3])
          snks (m/mlet [n sn
                        k (s/from-coll (map (comp keyword str) (range 1 (inc n))))]
                 (m/return [n k]))
          sample (s/take 6 snks)]
      (t/is (s/observable? snks))
      (drain! sample #(t/is (= % [[1 :1]
                                  [2 :1]
                                  [2 :2]
                                  [3 :1]
                                  [3 :2]
                                  [3 :3]])))
      (s/on-end sample done))))

(t/deftest pipe-to-atom
  (t/async done
    (let [st (s/from-coll [1 2 3])
          a (s/pipe-to-atom st)]
      (s/on-end st #(do (t/is (= @a 3))
                        (done))))))

(t/deftest pipe-to-atom-with-atom
  (t/async done
    (let [st (s/from-coll [1 2 3])
          vacc (volatile! [])
          a (atom 0)]
      (add-watch a
                 :acc
                 (fn [_ _ _ v]
                   (vswap! vacc conj v)))
      (s/pipe-to-atom a st)
      (s/on-end st #(do (t/is (= @a 3))
                        (t/is (= @vacc [1 2 3]))
                        (done))))))

(t/deftest pipe-to-atom-with-atom-and-function
  (t/async done
    (let [st (s/from-coll [1 2 3])
          a (atom [])]
      (s/pipe-to-atom a st conj)
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
