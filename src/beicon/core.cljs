(ns beicon.core
  (:require [beicon.extern.rxjs]
            [cats.protocols :as p]
            [cats.context :as ctx])
  (:refer-clojure :exclude [true? map filter reduce merge repeat repeatedly zip
                            dedupe drop take take-while not and
                            or
                            next
                            concat
                            partition]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Observables Constructors
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

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
  (-end? [_] false))

(extend-type nil
  IObservableEndValue
  (-end? [_] true))

(extend-type js/Error
  IObservableErrorValue
  (-error? [_] true))

(extend-type cljs.core.ExceptionInfo
  IObservableErrorValue
  (-error? [_] true))

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
                 (.onNext ob)

                 (-end? v)
                 (.onCompleted ob)

                 (-error? v)
                 (.onError ob v)))]
       (try
         (sf callback)
         (catch js/Error e
           (.onError ob e)))))))

(defn observable?
  "Return true if `ob` is a instance
  of Rx.Observable."
  [ob]
  (instance? js/Rx.Observable ob))

;; (defn from-poll
;;   "Creates an observable sequence polling given
;;   function with given interval."
;;   [ms f]
;;   (create (fn [sick]

;;             (let [sem (js/setInterval
;;                        (fn []
;;                          (let [v (f)]
;;                            (when (or (-end? v) (-error? v))
;;                              (js/clearInterval sem))
;;                            (sick v))))]
;;                    (.onNext ob)

;;                    (-end? v)
;;                  (if (instance? EndValue v)
;;                    (.onComplete ob (.-v v))
;;                    (.onComplete ob v))

;;                  (-error? v)
;;                  (.onError ob v))))))






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
  {:pre [(observable? ob) (fn? f)]}
  (.filter ob f))

(defn map
  "Apply a function to each element of an observable
  sequence."
  [f ob]
  {:pre [(observable? ob) (fn? f)]}
  (.map ob f))

(defn flat-map
  "Projects each element of an observable sequence to
  an observable sequence and merges the resulting
  observable sequences or Promises or array/iterable
  into one observable sequence."
  [f ob]
  {:pre [(observable? ob) (fn? f)]}
  (.flatMap ob f))

(defn skip
  "Bypasses a specified number of elements in an
  observable sequence and then returns the remaining
  elements."
  [n ob]
  {:pre [(observable? ob) (number? n)]}
  (.skip ob n))

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


