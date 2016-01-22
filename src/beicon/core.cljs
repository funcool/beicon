(ns beicon.core
  (:require [beicon.extern.rxjs])
  (:refer-clojure :exclude [true? map filter reduce merge repeat
                            repeatedly zip dedupe drop take take-while
                            concat partition empty delay]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Predicates
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(def ^:static observable js/Rx.Observable)
(def ^:static subject js/Rx.Subject)

(defn observable?
  "Return true if `ob` is a instance
  of Rx.Observable."
  [ob]
  (instance? js/Rx.Observable ob))

(defn connectable?
  "Return true if `ob` is a instance
  of Rx.ConnectableObservable."
  [ob]
  (instance? js/Rx.ConnectableObservable ob))

(defn bus?
  "Return true if `b` is a Subject instance."
  [b]
  (instance? js/Rx.Subject b))

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
   {:pre [(number? n)]}
   (js/Rx.Observable.repeat v n)))

(defn publish
  "Create a connectable (hot) observable
  from other observable."
  ([^observable ob]
   (publish ob true))
  ([^observable ob connect?]
   {:pre [(observable? ob)]}
   (let [ob' (.publish ob)]
     (when connect?
       (.connect ob'))
     ob')))

(defn share
  "Returns an observable sequence that shares a single
  subscription to the underlying sequence."
  [^observable ob]
  (.share ob))

(defn connect!
  "Connect the connectable observable."
  [^observable ob]
  {:pre [(connectable? ob)]}
  (.connect ob))

(defn from-coll
  "Generates an observable sequence from collection."
  [coll]
  (let [array (into-array coll)]
    (js/Rx.Observable.from array)))

(defn from-callback
  "Creates an observable sequence of one unique value
  executing a callback."
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
  (js/Rx.Observable.fromPromise p))

(defn from-exception
  [e]
  (let [func (aget js/Rx.Observable "throw")]
    (func e)))

(defn just
  "Returns an observable sequence that contains
  a single element."
  [v]
  (js/Rx.Observable.just v))

(defn once
  "An alias to `just`."
  [v]
  (just v))

(defn empty
  "Returns an observable sequence that is already
  in end state."
  []
  (js/Rx.Observable.empty))

(def never
  "Alias to 'empty'."
  empty)

(defn timer
  "Returns an observable sequence that produces a value after
  dueTime has elapsed and then after each period."
  ([ms]
   {:pre [(number? ms)]}
   (js/Rx.Observable.timer ms))
  ([ms interval]
   {:pre [(number? ms) (number? interval)]}
   (js/Rx.Observable.timer ms interval)))

(defn timeout
  "Returns the source observable sequence or the other
  observable sequence if dueTime elapses."
  ([^number ms ^observable ob]
   {:pre [(number? ms)]}
   (.timeout ob ms))
  ([^number ms ^observable other ^observable ob]
   {:pre [(number? ms)]}
   (.timeout ob other ms)))

(defn delay
  "Time shifts the observable sequence by dueTime. The relative
  time intervals between the values are preserved."
  [^number ms ^observable ob]
  {:pre [(number? ms)]}
  (.delay ob ms))

(defn delay'
  "Time shifts the observable sequence based on a subscription
  delay and a delay selector function for each element."
  ([ds ^observable ob]
   {:pre [(ifn? ds)]}
   (.delay ob ds))
  ([sd ds ^observable ob]
   {:pre [(ifn? ds) (observable? sd)]}
   (.delay ob sd ds)))

(defn interval
  "Returns an observable sequence that produces a
  value after each period."
  [^number ms]
  {:pre [(number? ms)]}
  (js/Rx.Observable.interval ms))

(defn of
  "Converts arguments to an observable sequence."
  ([a]
   (js/Rx.Observable.of a))
  ([a b]
   (js/Rx.Observable.of a b))
  ([a b c]
   (js/Rx.Observable.of a b c))
  ([a b c d]
   (js/Rx.Observable.of a b c d))
  ([a b c d e]
   (js/Rx.Observable.of a b c d e))
  ([a b c d e f]
   (js/Rx.Observable.of a b c d e f)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Bus
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn bus
  "Bus is an observable sequence that allows you to push
  values into the stream."
  []
  (js/Rx.Subject.))

(defn push!
  "Pushes the given value to the bus stream."
  [^subject b v]
  {:pre [(bus? b)]}
  (.onNext b v))

(defn error!
  "Pushes the given error to the bus stream."
  [^subject b e]
  {:pre [(bus? b)]}
  (.onError b e))

(defn end!
  "Ends the given bus stream."
  [^subject b]
  {:pre [(bus? b)]}
  (.onCompleted b))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Observable Subscription
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn on-value
  "Subscribes a function to invoke for each element
  in the observable sequence."
  [^observable ob f]
  {:pre [(observable? ob)]}
  (let [disposable (.subscribeOnNext ob #(f %))]
    #(.dispose disposable)))

(defn on-error
  "Subscribes a function to invoke upon exceptional termination
  of the observable sequence."
  [^observable ob f]
  {:pre [(observable? ob)]}
  (let [disposable (.subscribeOnError ob #(f %))]
    #(.dispose disposable)))

(defn on-end
  "Subscribes a function to invoke upon graceful termination
  of the observable sequence."
  [^observable ob f]
  {:pre [(observable? ob)]}
  (let [disposable (.subscribeOnCompleted ob #(f %))]
    #(.dispose disposable)))

(defn subscribe
  "Subscribes an observer to the observable sequence."
  ([ob nf]
   (subscribe ob nf nil nil))
  ([ob nf ef]
   (subscribe ob nf ef nil))
  ([^observable ob nf ef cf]
   {:pre [(observable? ob)]}
   (let [disposable (.subscribe ob nf ef cf)]
     #(.dispose disposable))))

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

(defn choice
  "Create an observable that surfaces any of the given
  sequences, whichever reacted first."
  ([^observable a ^observable b]
   {:pre [(observable? a)
          (observable? b)]}
   (.amb a b))
  ([a b & more]
   (cljs.core/reduce choice (choice a b) more)))

(defn zip
  "Merges the specified observable sequences or Promises
  into one observable sequence."
  ([ob' ob]
   (zip vector ob' ob))
  ([f ^observable ob' ^observable ob]
   {:pre [(observable? ob') (observable? ob)]}
   (.zip ob ob' f)))

(defn concat
  "Concatenates all of the specified observable
  sequences, as long as the previous observable
  sequence terminated successfully."
  ([^observable a ^observable ob]
   {:pre [(observable? a)
          (observable? ob)]}
   (.concat ob a))
  ([a b & more]
   (let [ob (last more)
         obs (into [b] (butlast more))]
     (cljs.core/reduce concat (concat a ob) more))))

(defn merge
  "Merges all the observable sequences and Promises
  into a single observable sequence."
  ([^observable a ^observable b]
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
  [^observable ob]
  (.mergeAll ob))

(defn filter
  "Filters the elements of an observable sequence
  based on a predicate."
  [f ^observable ob]
  {:pre [(observable? ob)]}
  (.filter ob #(boolean (f %))))

(defn map
  "Apply a function to each element of an observable
  sequence."
  [f ^observable ob]
  {:pre [(observable? ob)]}
  (.map ob #(f %)))

(defn flat-map
  "Projects each element of an observable sequence to
  an observable sequence and merges the resulting
  observable sequences or Promises or array/iterable
  into one observable sequence."
  ([ob]
   (flat-map identity ob))
  ([f ^observable ob]
   {:pre [(observable? ob)]}
   (.flatMap ob #(f %))))

(defn skip
  "Bypasses a specified number of elements in an
  observable sequence and then returns the remaining
  elements."
  [^number n ^observable ob]
  {:pre [(observable? ob) (number? n)]}
  (.skip ob n))

(defn skip-while
  "Bypasses elements in an observable sequence as long
  as a specified condition is true and then returns the
  remaining elements."
  [f ^observable ob]
  {:pre [(observable? ob) (fn? f)]}
  (.skipWhile ob #(boolean (f %))))

(defn skip-until
  "Returns the values from the source observable sequence
  only after the other observable sequence produces a value."
  [pob ^observable ob]
  {:pre [(observable? ob) (observable? pob)]}
  (.skipUntil ob pob))

(defn take
  "Bypasses a specified number of elements in an
  observable sequence and then returns the remaining
  elements."
  [^number n ^observable ob]
  {:pre [(observable? ob) (number? n)]}
  (.take ob n))

(defn slice
  "Returns a shallow copy of a portion of an Observable
  into a new Observable object."
  ([begin ^observable ob]
   {:pre [(observable? ob) (number? begin)]}
   (.slice ob begin))
  ([begin end ^observable ob]
   {:pre [(observable? ob) (number? begin) (number? end)]}
   (.slice ob begin end)))

(defn take-while
  "Returns elements from an observable sequence as long as a
  specified predicate returns true."
  [f ^observable ob]
  {:pre [(observable? ob) (fn? f)]}
  (.takeWhile ob f))

(defn reduce
  "Applies an accumulator function over an observable
  sequence, returning the result of the aggregation as a
  single element in the result sequence."
  ([f ^observable ob]
   {:pre [(observable? ob) (fn? f)]}
   (.reduce ob f))
  ([f seed ^observable ob]
   {:pre [(observable? ob) (fn? f)]}
   (.reduce ob f seed)))

(defn scan
  "Applies an accumulator function over an observable
  sequence and returns each intermediate result.
  Same as reduce but with intermediate results"
  ([f ^observable ob]
   {:pre [(observable? ob) (fn? f)]}
   (.scan ob f))
  ([f seed ^observable ob]
   {:pre [(observable? ob) (fn? f)]}
   (.scan ob f seed)))

(defn with-latest-from
  "Merges the specified observable sequences into
  one observable sequence by using the selector
  function only when the source observable sequence
  (the instance) produces an element."
  ([ob' ob]
   (with-latest-from vector ob' ob))
  ([f ob' ^observable ob]
   (.withLatestFrom ob ob' f)))

(defn catch
  "Continues an observable sequence that is terminated
  by an exception with the next observable sequence."
  [handler ^observable ob]
  {:pre [(or (observable? handler)
             (fn? handler))]}
  (.catch ob handler))

(defn tap
  "Invokes an action for each element in the
  observable sequence."
  [f ^observable ob]
  {:pre [(observable? ob) (fn? f)]}
  (.tap ob f))

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
  [ms ^observable ob]
  {:pre [(observable? ob) (number? ms)]}
  (.throttle ob ms))

(defn debounce
  "Emits an item from the source Observable after a
  particular timespan has passed without the Observable
  omitting any other items."
  [ms ^observable ob]
  {:pre [(observable? ob) (number? ms)]}
  (.debounce ob ms))

(defn sample
  "Samples the observable sequence at each interval."
  [ms ^observable ob]
  {:pre [(observable? ob) (number? ms)]}
  (.sample ob ms))

(defn ignore
  "Ignores all elements in an observable sequence leaving
  only the termination messages."
  [^observable ob]
  {:pre [(observable? ob)]}
  (.ignoreElements ob))

(defn pausable
  "Pauses the underlying observable sequence based upon the
  observable sequence which yields true/false.

  WARNING: The buffered pausable only works with hot
  observables."
  ([pauser ob]
   (pausable pauser false ob))
  ([pauser buffer? ob]
   {:pre [(observable? ob) (observable? pauser)]}
   (if buffer?
     (.pausableBuffered ob pauser)
     (.pausable ob pauser))))

(defn dedupe
  "Returns an observable sequence that contains only
  distinct contiguous elements."
  ([^observable ob]
   {:pre [(observable? ob)]}
   (.distinctUntilChanged ob))
  ([f ^observable ob]
   {:pre [(observable? ob)]}
   (.distinctUntilChanged ob f)))

(defn dedupe'
  "Returns an observable sequence that contains only d
  istinct elements.
  Usage of this operator should be considered carefully
  due to the maintenance of an internal lookup structure
  which can grow large."
  ([^observable ob]
   {:pre [(observable? ob)]}
   (.distinct ob))
  ([f ^observable ob]
   {:pre [(observable? ob)]}
   (.distinct ob f)))

(defn buffer
  "Projects each element of an observable sequence into zero
  or more buffers which are produced based on element count
  information."
  ([n ^observable ob]
   {:pre [(observable? ob)]}
   (.bufferWithCount ob n))
  ([n skip ^observable ob]
   {:pre [(observable? ob)]}
   (.bufferWithCount ob n skip)))

(defn retry
  "Given an optional number of retries and an observable,
  repeats the source observable the specified number of
  times or until it terminates. If no number of retries
  is given, it will be retried indefinitely."
  ([^observable ob]
   {:pre [(observable? ob)]}
   (.retry ob))
  ([n ^observable ob]
   {:pre [(observable? ob)]}
   (.retry ob n)))

(defn to-observable
  "Hides the identity of an observable sequence."
  [b]
  (.asObservable b))

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
                         (on-end stream #(do (xsink nil)
                                             (sink nil)))
                         (fn []
                           (unsub)))))]
      ns)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Schedulers
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn scheduler?
  "Return true if `s` is scheduler instance."
  [s]
  (js/Rx.Scheduler.isScheduler s))

(def immediate-scheduler js/Rx.Scheduler.immediate)
(def current-thread-scheduler js/Rx.Scheduler.currentThread)

(defn observe-on
  [scheduler ob]
  (.observeOn ob scheduler))

(defn subscribe-on
  [scheduler ob]
  (.subscribeOn ob scheduler))
