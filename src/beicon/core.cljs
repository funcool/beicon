(ns beicon.core
  (:require [beicon.external.rxjs]
            [cats.protocols :as p]
            [cats.context :as ctx])
  (:refer-clojure :exclude [true? map filter reduce merge repeat repeatedly zip
                            dedupe drop take take-while not and
                            or
                            next
                            concat
                            partition]))

(defn repeat
  "Generates an observable sequence that repeats the
  given element."
  ([v]
   (repeats v -1))
  ([v n]
   {:pre [(number? v)]}
   (js/Rx.Observable.repeat v n)))

(defn from-coll
  "Generates an observable sequence from collection."
  [coll]
  (let [array (into-array coll)]
    (js/Rx.Observable.fromArray array)))

(defn from-callback
  [f]
  {:pre [(fn? f)]}
  (js/Rx.Observable.fromCallback f))

(defprotocol IObservableEndValue
  (-end? [_] "Returns true if is end value."))

(defprotocol IObservableErrorValue
  (-error? [_] "Returns true if is end value."))

(defprotocol IObservableNextValue
  (-next? [_] "Returns true if is end value."))

(extend-type default
  IObservableNextValue
  (-next? [_] true)

  IObservableErrorValue
  (-error? [_] false)

  IObservableEndValue
  (-end? [_] false)e

(extend-protocol IObservableErrorValue
  js/Error
  (-error? [_] true)

  cljs.core.ExceptionInfo
  (-error? [_] true))

(deftype EndValue [v]
  (-end? [_] true))

(defn next
  [v]
  (NextValue. v))

(defn next?
  [v]
  (instance? NextValue v))

(defn end
  [v]
  (EndValue. v))

(defn end?
  [v]
  (instance? EndValue v))

(defn create
  "Creates an observable sequence from a specified
  subscribe method implementation."
  [sf]
  {:pre [(fn? sf)]}
  (js/Rx.Observable.create
   (fn [ob]
     (letfn [(callback [v]
               (cond
                 (next? v)
                 (.onNext ob)
                 (end? v)
                 (if (instance? EndValue v)
                   (.onComplete ob (.-v v))
                   (.onComplete ob v))

                 (error? v)
                 (.onError ob v)))]
       (try
         (sf callback)
         (catch js/Error e
           (.onError ob e))))))))






(defn choice
  "Create an observable that surfaces any of the given
  sequences, whichever reacted first."
  ([a b]
   {:pre [(observable? a)
          (observable? b)]}
   (.amb a b))
  ([a b & more]
   (reduce choice (choice a b) more)))

(defn zip
  "Merges the specified observable sequences or Promises
  into one observable sequence."
  [a b]
  {:pre [(observable? a)
         (observable? b)]}
  (letfn [(merge [& args] args)]
    (.zip a b merge)))

(defn concat
  "Concatenates all of the specified observable
  sequences, as long as the previous observable
  sequence terminated successfully."
  ([a b]
   {:pre [(observable? a)
          (observable? b)]}
   (.concat a b))
  ([a b & more]
   (reduce concat (concat a b) more)))

(defn merge
  "Merges all the observable sequences and Promises
  into a single observable sequence."
  ([a b]
   {:pre [(observable? a)
          (observable? b)]}
   (.merge a b))
  ([a b & more]
   (reduce merge (merge a b) more)))

(defn filter
  "Filters the elements of an observable sequence
  based on a predicate."
  [ob f]
  {:pre [(observable? ob) (fn? f)]}
  (.filter ob f))

(defn map
  "Apply a function to each element of an observable
  sequence."
  [ob f]
  {:pre [(observable? ob) (fn? f)]}
  (.map ob f))

(defn skip
  "Bypasses a specified number of elements in an
  observable sequence and then returns the remaining
  elements."
  [ob n]
  {:pre [(observable? ob) (number? f)]}
  (.skip ob f))

(defn take
  "Bypasses a specified number of elements in an
  observable sequence and then returns the remaining
  elements."
  [ob n]
  {:pre [(observable? ob) (number? f)]}
  (.take ob f))

(defn take-while
  "Returns elements from an observable sequence as long as a
  specified predicate returns true."
  [ob p]
  {:pre [(observable? ob) (number? p)]}
  (.takeWhile ob f))

(defn on-value
  "Subscribes a function to invoke for each element
  in the observable sequence."
  [obs f]
  (.subscribeOnNext obs f))

(defn on-error
  "Subscribes a function to invoke upon exceptional termination
  of the observable sequence."
  [obs f]
  (.subscribeOnError obs f))

(defn on-end
  "Subscribes a function to invoke upon graceful termination
  of the observable sequence."
  [obs f]
  (.subscribeOnCompleted obs f))

(defn subscribe
  "Subscribes an observer to the observable sequence."
  [obs nf ef cf]
  (.subscribe obs nf ef cf))

