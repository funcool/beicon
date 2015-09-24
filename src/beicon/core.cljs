(ns beicon.core
  (:require [beicon.extern.rxjs]
            [cats.protocols :as p]
            [cats.context :as ctx])
  (:refer-clojure :exclude [true? map filter reduce merge repeat repeatedly zip
                            dedupe drop take take-while concat partition]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Observables Constructors
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defprotocol IObservableValue
  (-end? [_] "Returns true if is end value.")
  (-error? [_] "Returns true if is end value.")
  (-next? [_] "Returns true if is end value."))

(extend-type default
  IObservableValue
  (-next? [_] true)
  (-error? [_] false)
  (-end? [_] false))

(extend-type nil
  IObservableValue
  (-next? [_] false)
  (-error? [_] false)
  (-end? [_] true))

(extend-type js/Error
  IObservableValue
  (-next? [_] false)
  (-error? [_] true)
  (-end? [_] false))

(extend-type cljs.core.ExceptionInfo
  IObservableValue
  (-next? [_] false)
  (-error? [_] true)
  (-end? [_] false))

(defn create
  "Creates an observable sequence from a specified
  subscribe method implementation."
  [sf]
  {:pre [(fn? sf)]}
  (js/Rx.Observable.create
   (fn [ob]
     (letfn [(callback [v]
               (cond
                 (-next? v)
                 (.onNext ob v)

                 (-end? v)
                 (.onCompleted ob)

                 (-error? v)
                 (.onError ob v)))]
       (try
         (sf callback)
         (catch js/Error e
           (.onError ob e)))))))

(defn repeat
  "Generates an observable sequence that repeats the
  given element."
  ([v]
   (repeat v -1))
  ([v n]
   {:pre [(number? v)]}
   (js/Rx.Observable.repeat v n)))

(defn from-coll
  "Generates an observable sequence from collection."
  [coll]
  (let [array (into-array coll)]
    (js/Rx.Observable.fromArray array)))

(defn from-callback
  [f & args]
  {:pre [(fn? f)]}
  (create (fn [sink]
            (apply f sink args)
            (sink nil))))

(defn from-poll
  "Creates an observable sequence polling given
  function with given interval."
  [ms f]
  (create (fn [sick]
            (let [semholder (volatile! nil)
                  sem (js/setInterval
                       (fn []
                         (let [v (f)]
                           (when (or (-end? v) (-error? v))
                             (js/clearInterval @semholder))
                           (sick v)))
                       ms)]
              (vreset! semholder sem)))))

(defn once
  "Returns an observable sequence that contains
  a single element."
  [v]
  (js/Rx.Observable.just v))

(defn never
  "Returns an observable sequence that is already
  in end state."
  []
  (create (fn [sink]
            (sink nil))))

(defn observable?
  "Return true if `ob` is a instance
  of Rx.Observable."
  [ob]
  (instance? js/Rx.Observable ob))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Bus
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn bus
  "Bus is an observable sequence that allows you to push
  values into the stream."
  []
  (js/Rx.Subject.))

(defn bus?
  "Return true if `b` is a Subject instance."
  [b]
  (instance? js/Rx.Subject b))

(defn push!
  "Pushes the given value to the bus stream."
  [b v]
  {:pre [(bus? b)]}
  (.onNext b v))

(defn error!
  "Pushes the given error to the bus stream."
  [b e]
  {:pre [(bus? b)]}
  (.onError b e))

(defn end!
  "Ends the given bus stream."
  [b]
  {:pre [(bus? b)]}
  (.onCompleted b))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Observable Subscription
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn on-value
  "Subscribes a function to invoke for each element
  in the observable sequence."
  [ob f]
  {:pre [(observable? ob) (fn? f)]}
  (.subscribeOnNext ob f))

(defn on-error
  "Subscribes a function to invoke upon exceptional termination
  of the observable sequence."
  [ob f]
  {:pre [(observable? ob) (fn? f)]}
  (.subscribeOnError ob f))

(defn on-end
  "Subscribes a function to invoke upon graceful termination
  of the observable sequence."
  [ob f]
  {:pre [(observable? ob) (fn? f)]}
  (.subscribeOnCompleted ob f))

(defn subscribe
  "Subscribes an observer to the observable sequence."
  [ob nf ef cf]
  {:pre [(observable? ob)]}
  (.subscribe ob nf ef cf))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Observable Transformations
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn choice
  "Create an observable that surfaces any of the given
  sequences, whichever reacted first."
  ([a b]
   {:pre [(observable? a)
          (observable? b)]}
   (.amb a b))
  ([a b & more]
   (cljs.core/reduce choice (choice a b) more)))

(defn zip
  "Merges the specified observable sequences or Promises
  into one observable sequence."
  [a b]
  {:pre [(observable? a)
         (observable? b)]}
  (.zip a b vector))

(defn concat
  "Concatenates all of the specified observable
  sequences, as long as the previous observable
  sequence terminated successfully."
  ([a b]
   {:pre [(observable? a)
          (observable? b)]}
   (.concat a b))
  ([a b & more]
   (cljs.core/reduce concat (concat a b) more)))

(defn merge
  "Merges all the observable sequences and Promises
  into a single observable sequence."
  ([a b]
   {:pre [(observable? a)
          (observable? b)]}
   (.merge a b))
  ([a b & more]
   (cljs.core/reduce merge (merge a b) more)))

(defn filter
  "Filters the elements of an observable sequence
  based on a predicate."
  [f ob]
  {:pre [(observable? ob)]}
  (.filter ob #(f %)))

(defn map
  "Apply a function to each element of an observable
  sequence."
  [f ob]
  {:pre [(observable? ob)]}
  (.map ob #(f %)))

(defn flat-map
  "Projects each element of an observable sequence to
  an observable sequence and merges the resulting
  observable sequences or Promises or array/iterable
  into one observable sequence."
  [f ob]
  {:pre [(observable? ob)]}
  (.flatMap ob #(f %)))

(defn skip
  "Bypasses a specified number of elements in an
  observable sequence and then returns the remaining
  elements."
  [n ob]
  {:pre [(observable? ob) (number? n)]}
  (.skip ob n))

(defn skip-while
  "Bypasses elements in an observable sequence as long
  as a specified condition is true and then returns the
  remaining elements."
  [f ob]
  {:pre [(observable? ob) (fn? f)]}
  (.skipWhile ob f))

(defn skip-until
  "Returns the values from the source observable sequence
  only after the other observable sequence produces a value."
  [pob ob]
  {:pre [(observable? ob) (observable? pob)]}
  (.skipUntil ob pob))

(defn take
  "Bypasses a specified number of elements in an
  observable sequence and then returns the remaining
  elements."
  [n ob]
  {:pre [(observable? ob) (number? n)]}
  (.take ob n))

(defn take-while
  "Returns elements from an observable sequence as long as a
  specified predicate returns true."
  [f ob]
  {:pre [(observable? ob) (fn? f)]}
  (.takeWhile ob f))

(defn reduce
  "Applies an accumulator function over an observable
  sequence, returning the result of the aggregation as a
  single element in the result sequence."
  ([f ob]
   {:pre [(observable? ob) (fn? f)]}
   (.reduce ob f))
  ([f seed ob]
   {:pre [(observable? ob) (fn? f)]}
   (.reduce ob f seed)))

(defn tap
  "Invokes an action for each element in the
  observable sequence."
  [f ob]
  {:pre [(observable? ob) (fn? f)]}
  (.tap ob f))

(defn throttle
  "Returns an Observable that emits only the first item
  emitted by the source Observable during sequential
  time windows of a specified duration."
  [ms ob]
  {:pre [(observable? ob) (number? ms)]}
  (.throttle ob ms))

(defn ignore
  "Ignores all elements in an observable sequence leaving
  only the termination messages."
  [ob]
  {:pre [(observable? ob)]}
  (.ignoreElements ob))

(defn pausable
  [pauser ob]
  {:pre [(observable? ob) (observable? pauser)]}
  (.pausable ob pauser))

(defn dedupe
  "Returns an observable sequence that contains only
  distinct contiguous elements."
  ([ob]
   (.distinctUntilChanged ob))
  ([f ob]
   (.distinctUntilChanged ob f)))

(defn dedupe'
  "Returns an observable sequence that contains only d
  istinct elements.
  Usage of this operator should be considered carefully
  due to the maintenance of an internal lookup structure
  which can grow large."
  ([ob]
   (.distinct ob))
  ([f ob]
   (.distinct ob f)))

(defn buffer
  "Projects each element of an observable sequence into zero
  or more buffers which are produced based on element count
  information."
  ([n ob]
   (.bufferWithCount ob n))
  ([n skip ob]
   (.bufferWithCount ob n skip)))
