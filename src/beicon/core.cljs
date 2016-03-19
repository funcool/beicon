(ns beicon.core
  (:require [beicon.extern.rxjs])
  (:refer-clojure :exclude [true? map filter reduce merge repeat mapcat
                            repeatedly zip dedupe drop take take-while
                            concat empty delay range throw]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Predicates
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(def ^:const Observable js/Rx.Observable)
(def ^:const Bus js/Rx.Subject)
(def ^:const Subscriber js/Rx.Subscriber)
(def ^:const Subscription js/Rx.Subscription)
(def ^:const Scheduler js/Rx.Scheduler)

(defn observable?
  "Return true if `ob` is a instance
  of Rx.Observable."
  [ob]
  (instance? Observable ob))

(defn bus?
  "Return true if `b` is a Subject instance."
  [b]
  (instance? Bus b))

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
  (letfn [(continuation [subs v]
            (cond
              (-next? v) (.next subs v)
              (-end? v) (.complete subs)
              (-error? v) (.error subs v)))
          (factory [subs]
            (try
              (sf #(continuation subs %))
              (catch js/Error e
                (.error subs e))))]
    (Observable. factory)))

(defn range
  "Generates an observable sequence that repeats the
  given element."
  ([b]
   (range 0 b))
  ([a b]
   {:pre [(number? a) (number? b)]}
   (js/Rx.Observable.range a b)))

(defn publish
  "Create a connectable (hot) observable
  from other observable."
  ([ob]
   (publish ob true))
  ([ob connect?]
   {:pre [(observable? ob)]}
   (let [ob' (.publish ob)]
     (when connect?
       (.connect ob'))
     ob')))

(defn share
  "Returns an observable sequence that shares a single
  subscription to the underlying sequence."
  [ob]
  {:pre [(observable? ob)]}
  (.share ob))

(defn connect!
  "Connect the connectable observable."
  [ob]
  {:pre [(observable? ob)]}
  (.connect ob))

(defn from-coll
  "Generates an observable sequence from collection."
  [coll]
  (let [array (into-array coll)]
    (.from Observable array)))

(defn from-atom
  [atm]
  (create (fn [sink]
            (let [key (keyword (gensym "beicon"))]
              (add-watch atm key (fn [_ _ _ val]
                                   (sink val)))
              (fn []
                (remove-watch atm key))))))

(defn from-promise
  "Creates an observable from a promise."
  [p]
  (.fromPromise Observable p))

(defn just
  "Returns an observable sequence that contains
  a single element."
  [v]
  (.of Observable v))

(defn once
  "An alias to `just`."
  [v]
  (just v))

(defn empty
  "Returns an observable sequence that is already
  in end state."
  []
  (.empty Observable))

(def never
  "Alias to 'empty'."
  empty)

(defn throw
  [e]
  ((aget Observable "throw") e))

(def ^:deprecated from-exception
  "A deprecated alias for `throw`."
  throw)

(defn timer
  "Returns an observable sequence that produces a value after
  `ms` has elapsed and then after each period."
  ([ms]
   {:pre [(number? ms)]}
   (.timer Observable ms))
  ([ms interval]
   {:pre [(number? ms) (number? interval)]}
   (.timer Observable ms interval)))

(defn timeout
  "Returns the source observable sequence or the other
  observable sequence if dueTime elapses."
  ([^number ms ob]
   {:pre [(number? ms)]}
   (.timeoutWith ob ms))
  ([^number ms other ob]
   {:pre [(number? ms)]}
   (.timeoutWith ob ms other)))

(defn delay
  "Time shifts the observable sequence by dueTime. The relative
  time intervals between the values are preserved."
  [^number ms ob]
  {:pre [(number? ms)]}
  (.delay ob ms))

(defn delay-when
  "Time shifts the observable sequence based on a subscription
  delay and a delay selector function for each element."
  ([ds ob]
   {:pre [(ifn? ds)]}
   (.delayWhen ob ds))
  ([sd ds ob]
   {:pre [(ifn? ds) (observable? sd)]}
   (.delayWhen ob ds sd)))

(defn interval
  "Returns an observable sequence that produces a
  value after each period."
  [^number ms]
  {:pre [(number? ms)]}
  (.interval Observable ms))

(defn fjoin
  "Runs all observable sequences in parallel and collect
  their last elements."
  [& items]
  (let [[selector items] (if (ifn? (first items))
                           [(first items) (rest items)]
                           [vector items])
        items (if (vector? items) items (into [] items))]
    (apply Observable.forkJoin (conj items selector))))

(def fork-join
  "Alias to fjoin."
  fjoin)

(defn of
  "Converts arguments to an observable sequence."
  ([a]
   (Observable.of a))
  ([a b]
   (Observable.of a b))
  ([a b c]
   (Observable.of a b c))
  ([a b c d]
   (Observable.of a b c d))
  ([a b c d e]
   (Observable.of a b c d e))
  ([a b c d e f]
   (Observable.of a b c d e f))
  ([a b c d e f & more]
   (apply Observable.of a b c d e f more)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Bus
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn bus
  "Bus is an observable sequence that allows you to push
  values into the stream."
  []
  (Bus.))

(defn push!
  "Pushes the given value to the bus stream."
  [^subject b v]
  {:pre [(bus? b)]}
  (.next b v))

(defn error!
  "Pushes the given error to the bus stream."
  [^subject b e]
  {:pre [(bus? b)]}
  (.error b e))

(defn end!
  "Ends the given bus stream."
  [^subject b]
  {:pre [(bus? b)]}
  (.complete b))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Observable Subscription
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(def ^:const noop (constantly nil))

(defn- mk-subscription
  [subs]
  (reify
    cljs.core/IFn
    (-invoke [_]
      (.unsubscribe subs))

    Object
    (unsubscribe [_]
      (.unsubscribe subs))

    (close [_]
      (.unsubscribe subs))))

(defn on-value
  "Subscribes a function to invoke for each element
  in the observable sequence."
  [ob f]
  {:pre [(observable? ob)]}
  (let [subr (Subscriber. #(f %) noop noop)
        subs (.subscribe ob subr)]
    (mk-subscription subs)))

(def on-next
  "A semantic alias for `on-value`."
  on-value)

(defn on-error
  "Subscribes a function to invoke upon exceptional termination
  of the observable sequence."
  [ob f]
  {:pre [(observable? ob)]}
  (let [subr (Subscriber. noop #(f %) noop)
        subs (.subscribe ob subr)]
    (mk-subscription subs)))

(defn on-complete
  "Subscribes a function to invoke upon graceful termination
  of the observable sequence."
  [ob f]
  {:pre [(observable? ob)]}
  (let [subr (Subscriber. noop noop #(f %))
        subs (.subscribe ob subr)]
    (mk-subscription subs)))

(def on-end
  "A semantic alias for `on-complete`."
  on-complete)

(defn subscribe
  "Subscribes an observer to the observable sequence."
  ([ob nf]
   (subscribe ob nf nil nil))
  ([ob nf ef]
   (subscribe ob nf ef nil))
  ([ob nf ef cf]
   {:pre [(observable? ob)]}
   (let [nf #(nf %)
         ef (if (nil? ef) noop #(ef %))
         cf (if (nil? cf) noop #(cf))
         subr (Subscriber. nf ef cf)
         subs (.subscribe ob subr)]
    (mk-subscription subs))))

(defn to-atom
  "Materialize the observable sequence into an atom."
  ([ob]
   (let [a (atom nil)]
     (to-atom ob a)))
  ([ob a]
   {:pre [(observable? ob)]}
   (on-value ob #(reset! a %))
   a)
  ([ob a f]
   {:pre [(observable? ob)]}
   (on-value ob #(swap! a f %))
   a))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Observable Transformations
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn race
  "Create an observable that surfaces any of the given
  sequences, whichever reacted first."
  ([a b]
   {:pre [(observable? a)
          (observable? b)]}
   (.race a b))
  ([a b & more]
   (cljs.core/reduce race (race a b) more)))

(defn zip
  "Merges the specified observable sequences or Promises
  into one observable sequence."
  [& items]
  (let [[selector items] (if (ifn? (first items))
                           [(first items) (rest items)]
                           [vector items])
        items (if (vector? items) items (into [] items))]
    (apply Observable.zip (conj items selector))))

(defn concat
  "Concatenates all of the specified observable
  sequences, as long as the previous observable
  sequence terminated successfully."
  ([a ob]
   {:pre [(observable? a)
          (observable? ob)]}
   (.concat ob a))
  ([a b & more]
   (let [ob (last more)
         obs (into [b] (butlast more))]
     (cljs.core/reduce concat (cljs.core/concat a ob) more))))

(defn merge
  "Merges all the observable sequences and Promises
  into a single observable sequence."
  ([a b]
   {:pre [(observable? a)
          (observable? b)]}
   (.merge b a))
  ([a b & more]
   (let [ob (last more)
         obs (into [b] (butlast more))]
     (cljs.core/reduce concat (merge a ob) more))))

(defn merge-all
  "Merges an observable sequence of observable
  sequences into an observable sequence."
  [ob]
  (.mergeAll ob))

(defn filter
  "Filters the elements of an observable sequence
  based on a predicate."
  [f ob]
  {:pre [(observable? ob)]}
  (.filter ob #(boolean (f %))))

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
  ([ob]
   (flat-map identity ob))
  ([f ob]
   {:pre [(observable? ob)]}
   (.flatMap ob #(f %))))

(defn mapcat
  "Projects each element of an observable sequence to an observable
  sequence and concatenates the resulting observable sequences or
  Promises or array/iterable into one observable sequence."
  [f ob]
  {:pre [(observable? ob) (ifn? f)]}
  (.concatMap ob #(f %)))

(defn skip
  "Bypasses a specified number of elements in an
  observable sequence and then returns the remaining
  elements."
  [^number n ob]
  {:pre [(observable? ob) (number? n)]}
  (.skip ob n))

(defn skip-while
  "Bypasses elements in an observable sequence as long
  as a specified condition is true and then returns the
  remaining elements."
  [f ob]
  {:pre [(observable? ob) (fn? f)]}
  (.skipWhile ob #(boolean (f %))))

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
  [^number n ob]
  {:pre [(observable? ob) (number? n)]}
  (.take ob n))

(defn take-while
  "Returns elements from an observable sequence as long as a
  specified predicate returns true."
  [f ob]
  {:pre [(observable? ob) (fn? f)]}
  (.takeWhile ob f))

(defn take-until
  "Returns the values from the source observable sequence until
  the other observable sequence or Promise produces a value."
  [other ob]
  {:pre [(observable? ob)]}
  (.takeUntil ob other))

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

(defn scan
  "Applies an accumulator function over an observable
  sequence and returns each intermediate result.
  Same as reduce but with intermediate results"
  ([f ob]
   {:pre [(observable? ob) (fn? f)]}
   (.scan ob f))
  ([f seed ob]
   {:pre [(observable? ob) (fn? f)]}
   (.scan ob f seed)))

(defn with-latest-from
  "Merges the specified observable sequences into
  one observable sequence by using the selector
  function only when the source observable sequence
  (the instance) produces an element."
  ([ob' ob]
   (with-latest-from vector ob' ob))
  ([f ob' ob]
   (.withLatestFrom ob ob' f)))

(defn catch
  "Continues an observable sequence that is terminated
  by an exception with the next observable sequence."
  [handler ob]
  {:pre [(or (observable? handler)
             (fn? handler))]}
  (.catch ob handler))

(defn tap
  "Invokes an action for each element in the
  observable sequence."
  ([f ob]
   {:pre [(observable? ob) (fn? f)]}
   (.do ob f))
  ([f g ob]
   {:pre [(observable? ob) (fn? f) (fn? g)]}
   (.do ob f g))
  ([f g e ob]
   {:pre [(observable? ob) (fn? f) (fn? g) (fn? g)]}
   (.do ob f g e)))

(defn log
  "Print all values passed through the given
  observable sequence."
  ([ob]
   (tap #(println %) ob))
  ([prefix ob]
   (tap #(println prefix (str %)) ob)))

(defn pr-log
  "Print all values passed through the given
  observable sequence using pr-str."
  ([ob]
   (tap #(println (pr-str %)) ob))
  ([prefix ob]
   (tap #(println prefix (pr-str %)) ob)))

(defn throttle
  "Returns an observable sequence that emits only the
  first item emitted by the source Observable during
  sequential time windows of a specified duration."
  [ms ob]
  {:pre [(observable? ob) (number? ms)]}
  (.throttleTime ob ms))

(defn debounce
  "Emits an item from the source Observable after a
  particular timespan has passed without the Observable
  omitting any other items."
  [ms ob]
  {:pre [(observable? ob) (number? ms)]}
  (.debounceTime ob ms))

(defn sample
  "Samples the observable sequence at each interval."
  [ms ob]
  {:pre [(observable? ob) (number? ms)]}
  (.sampleTime ob ms))

(defn sample-when
  "Samples the observable sequence at each interval."
  [other ob]
  {:pre [(observable? ob) (observable? other)]}
  (.sample ob other))

(defn ignore
  "Ignores all elements in an observable sequence leaving
  only the termination messages."
  [ob]
  {:pre [(observable? ob)]}
  (.ignoreElements ob))

(defn dedupe
  "Returns an observable sequence that contains only
  distinct contiguous elements."
  ([ob]
   {:pre [(observable? ob)]}
   (.distinctUntilChanged ob =))
  ([f ob]
   {:pre [(observable? ob)]}
   (.distinctUntilChanged ob f)))

(defn dedupe'
  "Returns an observable sequence that contains only d
  istinct elements.
  Usage of this operator should be considered carefully
  due to the maintenance of an internal lookup structure
  which can grow large."
  ([ob]
   {:pre [(observable? ob)]}
   (.distinct ob =))
  ([f ob]
   {:pre [(observable? ob)]}
   (.distinct ob f)))

(defn buffer
  "Projects each element of an observable sequence into zero
  or more buffers which are produced based on element count
  information."
  ([n ob]
   {:pre [(observable? ob)]}
   (.bufferCount ob n))
  ([n skip ob]
   {:pre [(observable? ob)]}
   (.bufferCount ob n skip)))

(defn retry
  "Given an optional number of retries and an observable,
  repeats the source observable the specified number of
  times or until it terminates. If no number of retries
  is given, it will be retried indefinitely."
  ([ob]
   {:pre [(observable? ob)]}
   (.retry ob))
  ([n ob]
   {:pre [(observable? ob)]}
   (.retry ob n)))

(defn transform
  "Transform the observable sequence using transducers."
  [xform stream]
  (letfn [(sink-step [sink]
            (fn
              ([r] (sink nil) r)
              ([_ input] (sink input) input)))]
    (let [ns (create (fn [sink]
                       (let [xsink (xform (sink-step sink))
                             step (fn [input]
                                    (let [v (xsink nil input)]
                                      (when (reduced? v)
                                        (xsink @v))))
                             unsub (on-value stream step)]
                         (on-complete stream #(do (xsink nil)
                                                  (sink nil)))
                         (fn []
                           (unsub)))))]
      ns)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Schedulers
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(def ^:const asap Scheduler.asap)
(def ^:const queue Scheduler.queue)
(def ^:const async Scheduler.async)

(defn observe-on
  [scheduler ob]
  {:pre [(observable? ob)]}
  (.observeOn ob scheduler))

(defn subscribe-on
  [scheduler ob]
  {:pre [(observable? ob)]}
  (.subscribeOn ob scheduler))
