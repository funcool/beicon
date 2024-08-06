(ns beicon.v2.core
  (:refer-clojure :exclude [map filter reduce merge repeat first
                            last mapcat repeatedly zip take take-while
                            map-indexed concat empty take-last delay
                            range throw flatten comp])
  (:require-macros [beicon.v2.core :refer [push! error! end! comp]])

  (:require
   ["rxjs" :as rx]
   [beicon.v2.operators :as ops]
   [cljs.core :as c]))

(def ^:const Observable rx/Observable)
(def ^:const Subject rx/Subject)
(def ^:const BehaviorSubject rx/BehaviorSubject)
(def ^:const Subscriber rx/Subscriber)
(def ^:const Disposable rx/Subscription)
(def ^:const Scheduler rx/Scheduler)

(defn ^:no-doc internal-call
  [f source]
  (f source))

;; --- Interop Helpers

(declare subject?)

(def ^function noop rx/noop)
(def ^function comp ops/comp)
(def ^function pipe ops/pipe)

(defn push!
  "Pushes the given value to the bus stream."
  [b v]
  (.next ^js b v))

(defn error!
  "Pushes the given error to the bus stream."
  [b e]
  (.error ^js b e))

(defn end!
  "Ends the given bus stream."
  [b]
  (.complete ^js b))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; PREDICATES
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn observable?
  "Return true if `ob` is a instance
  of Rx.Observable."
  ^boolean
  [ob]
  (instance? Observable ob))

(defn disposable?
  "Check if the provided object is disposable (jvm) or subscription (js)."
  ^boolean
  [v]
  (instance? Disposable v))

(defn scheduler?
  "Check if the provided value is Scheduler instance."
  ^boolean
  [v]
  (instance? Scheduler v))

(defn subject?
  "Check if the provided value is Subject instance."
  ^boolean
  [b]
  (instance? Subject b))

(defn subscriber?
  ^boolean
  [o]
  (instance? Subscriber o))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; CONSTRUCTORS
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn create
  "Creates an observable sequence from a specified subscribe method
  implementation."
  [sf]
  (assert (fn? sf) "expected a plain function")
  (Observable. (fn [subs]
                 (try
                   (sf subs)
                   (catch :default e
                     (.error subs e))))))

(defn subject
  "Subject that, once an Observer has subscribed, emits all
  subsequently observed items to the subscriber."
  []
  (Subject.))

(defn behavior-subject
  "Bus that emits the most recent item it has observed and
  all subsequent observed items to each subscribed Observer."
  [v]
  (BehaviorSubject. v))

(defn range
  "Generates an observable sequence that repeats the
  given element."
  ([b] (range 0 b))
  ([a b] (rx/range a b)))

(defn from
  "Creates an observable from js arrays, clojurescript collections, and
  promise instance."
  [v]
  (if (nil? v)
    rx/EMPTY
    (rx/from v)))

(defn from-atom
  ([atm] (from-atom atm nil))
  ([atm {:keys [emit-current-value?] :or {emit-current-value? false}}]
   (create (fn [subs]
             (let [key (keyword (gensym "beicon"))]
               (when emit-current-value? (push! subs @atm))
               (add-watch atm key (fn [_ _ _ val] (push! subs val)))
               (fn [] (remove-watch atm key)))))))

(defn from-event
  "Creates an Observable by attaching an event listener to an event target"
  [et ev]
  (rx/fromEvent et ev))

(def ^function timer
  "Returns an observable sequence that produces a value after
  `ms` has elapsed and then after each period."
  rx/timer)

(defn interval
  "Returns an observable sequence that produces a
  value after each period."
  [ms]
  (rx/interval ms))

(defn empty
  "Returns an observable sequence that is already
  in end state."
  []
  rx/EMPTY)

(defn throw
  "Returns an exceptionally terminated observable with provided cause."
  [e]
  (if (fn? e)
    (rx/throwError e)
    (rx/throwError #(-> e))))

(defn error
  "Same as `throw`"
  [e]
  (throw e))

(def ^function fjoin
  "Runs all observable sequences in parallel and collect their last
  elements."
  (js* "function forkJoin(...args) {
  const resultSelector = (typeof args[0] === 'function') ? args.shift() : undefined;
  if (resultSelector === undefined) {
    return ~{}(...args);
  } else {
    return ~{}(...args, resultSelector);
  }
}" rx/forkJoin rx/forkJoin))

(def ^function of
  "Converts arguments to an observable sequence"
  rx/of)

(def ^function race
  "Create an observable that surfaces any of the given
  sequences, whichever reacted first."
  rx/race)

(def ^function zip
  "Merges the specified observable sequences or Promises (cljs) into one
  observable sequence."
  (js* "function zip(...sources) {
  const projectFunction = (typeof sources[0] === 'function') ? sources.shift() : undefined;

  if (projectFunction === undefined) {
    return ~{}(...sources);
  } else {
    return ~{}(...sources, projectFunction);
  }
}" rx/zip rx/zip))


(def ^function concat
  "Concatenates all of the specified observable
  sequences, as long as the previous observable
  sequence terminated successfully."
  (js* "function(...args) { args = args.filter(~{}); return ~{}(...args); }" some? rx/concat))

(def ^function merge
  "Merges all the observable sequences and Promises
  into a single observable sequence."
  (js* "function(...args) {
  const sources = args.filter(~{});
  return !sources.length ? ~{} : sources.length === 1 ? ~{}(sources[0]) : ~{}(Infinity)(~{}(sources))
}" some? rx/EMPTY rx/from rx/mergeAll rx/from))


(def ^function combine-latest
  "Combines multiple Observables to create an Observable whose values
  are calculated from the latest values of each of its input
  Observables (constructor)."
  (js* "function(...sources) {
  const projectFunction = (typeof sources[0] === 'function') ? sources.shift() : undefined;
  return ~{}(sources, projectFunction);
}" rx/combineLatest))

(defn combine-latest-all
  "Comboines multiple Observables to create an Observable whose values
  are calculated from the latest values of each of its input
  Observables (constructor).

  A variant of `conbine-latest*` that accepts an array or sequential"
  [obs]
  (cond
    (array? obs)
    (rx/combineLatest obs)

    (sequential? obs)
    (rx/combineLatest (into-array obs))

    :else
    (throw (ex-info "unexpected arguments" {:obs obs}))))

(defn scheduler
  "Get the scheduler instance by type. The posible types are: `:asap`,
  `:async`, `:queue`.  Old `:trampoline` type is renamed as `:queue`
  and is deprecated."
  [type]
  (ops/scheduler type))

(defn publish!
  "Create a connectable (hot) observable
  from other observable."
  [ob]
  (.publish ^Observable ob))

(defn connect!
  "Connect the connectable observable."
  [ob]
  (.connect ^Observable ob))

(defn to-observable
  "Coerce a object to an observable instance."
  [ob]
  (assert (subject? ob) "`ob` should be a Subject instance")
  (.asObservable ^Subject ob))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; SUBSCRIPTIONS
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defprotocol ^:no-doc IDisposable
  (^:no-doc -dispose [_] "dispose resources."))

(defn dispose!
  "Dispose resources acquired by the subscription."
  [v]
  (-dispose v))

(extend-type Subscriber
  cljs.core/IFn
  (-invoke ([this] (.unsubscribe ^Subscriber this)))

  IDisposable
  (-dispose [this] (.unsubscribe ^Subscriber this)))

(extend-type BehaviorSubject
  cljs.core/IDeref
  (-deref [self]
    (.getValue ^js self)))

(defn subscribe
  "Subscribes an observer to the observable sequence."
  ([ob nf]
   (cond
     (or (ops/plain-object? nf)
         (subject? nf))
     (.subscribe ^js ob nf)

     (map? nf)
     (subscribe ob
                (get nf :next noop)
                (get nf :error noop)
                (get nf :complete noop))


     :else
     (do
       (.subscribe ^js ob #js {:next nf}))))

  ([ob next-fn error-fn]
   (let [observer #js {:next next-fn
                       :error error-fn}]
     (.subscribe ^js ob observer)))

  ([ob next-fn error-fn complete-fn]
   (let [observer #js {:next next-fn
                       :error error-fn
                       :complete complete-fn}]
     (.subscribe ^js ob observer))))

(defn sub!
  "Subscribes an observer to the observable sequence."
  ([ob nf] (subscribe ob nf))
  ([ob next-fn error-fn] (subscribe ob next-fn error-fn))
  ([ob next-fn error-fn complete-fn] (subscribe ob next-fn error-fn complete-fn)))

(defn ^:no-doc on-error
  [ob on-error]
  (subscribe ob noop on-error))

(defn ^:no-doc on-end
  [ob on-complete]
  (subscribe ob noop noop on-complete))

(defn subs!
  "A specialized version of `subscribe` with inverted arguments."
  ([nf ob] (subscribe ob nf))
  ([nf ef ob] (subscribe ob nf ef))
  ([nf ef cf ob] (subscribe ob nf ef cf)))

(defn- disposable-atom
  [ref disposable]
  (specify! ref
    IFn
    (-invoke ([this] (-dispose this)))

    IDisposable
    (-dispose [_]
      (.unsubscribe disposable))))

(defn to-atom
  "Materialize the observable sequence into an atom."
  ([ob]
   (let [a (atom nil)]
     (to-atom ob a)))
  ([ob a]
   (let [disposable (subscribe ob #(reset! a %))]
     (disposable-atom a disposable)))
  ([ob a f]
   (let [disposable (subscribe ob #(swap! a f %))]
     (disposable-atom a disposable))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; OPERATORS
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn share
  "Returns an observable sequence that shares a single
  subscription to the underlying sequence."
  [ob]
  (ops/pipe (ops/share) ob))

(defn if-empty
  "Emits a given value if the source Observable completes without
  emitting any next value, otherwise mirrors the source Observable."
  [default ob]
  (ops/pipe (ops/if-empty default) ob))

(defn merge-all
  "Merges an observable sequence of observable sequences into an
  observable sequence."
  ([ob] (ops/pipe (ops/merge-all) ob))
  ([concurrency ob] (ops/pipe (ops/merge-all concurrency) ob)))

(defn filter
  "Filters the elements of an observable sequence
  based on a predicate."
  [f ob]
  (ops/pipe (ops/filter f) ob))

(defn map
  "Apply a function to each element of an observable
  sequence."
  [f ob]
  (ops/pipe (ops/map f) ob))

(defn map-indexed
  "Same as `map` but also projects an index."
  [f ob]
  (ops/pipe (ops/map-indexed f) ob))

(defn merge-map
  "Projects each element of an observable sequence to an observable
  sequence and merges the resulting observable sequences or Promises
  or array/iterable into one observable sequence.

  In other languages is called: flatMap or mergeMap."
  [f ob]
  (ops/pipe (ops/merge-map f) ob))

(defn switch-map
  [f ob]
  (ops/pipe (ops/switch-map f) ob))

(defn mapcat
  "Projects each element of an observable sequence to an observable
  sequence and concatenates the resulting observable sequences or
  Promises or array/iterable into one observable sequence."
  [f ob]
  (ops/pipe (ops/mapcat f) ob))

(defn concat-all
  [ob]
  (ops/pipe (ops/merge-all 1) ob))

(defn skip
  "Bypasses a specified number of elements in an
  observable sequence and then returns the remaining
  elements."
  [n ob]
  (ops/pipe (ops/skip n) ob))

(defn skip-while
  "Bypasses elements in an observable sequence as long
  as a specified condition is true and then returns the
  remaining elements."
  [f ob]
  (ops/pipe (ops/skip-while f) ob))

(defn skip-until
  "Returns the values from the source observable sequence only after the
  other observable sequence produces a value."
  [pob ob]
  (ops/pipe (ops/skip-until pob) ob))

(defn skip-last
  "Skip a specified number of values before the completion of an observable."
  [n ob]
  (ops/pipe (ops/skip-last n) ob))

(defn take
  "Bypasses a specified number of elements in an observable sequence and
  then returns the remaining elements."
  [n ob]
  (ops/pipe (ops/take n) ob))

(defn take-last
  [n ob]
  (ops/pipe (ops/take-last n) ob))

(defn take-while
  "Returns elements from an observable sequence as long as a specified
  predicate returns true."
  [f ob]
  (ops/pipe (ops/take-while f) ob))

(defn take-until
  "Returns the values from the source observable sequence until the
  other observable sequence or Promise produces a value."
  [other ob]
  (ops/pipe (ops/take-until other) ob))

(defn first
  "Return an observable that only has the first value of the provided
  observable. You can optionally pass a predicate and default value."
  [ob]
  (ops/pipe (ops/take 1) ob))

(defn last
  "Return an observable that only has the last value of the provided
  observable. You can optionally pass a predicate and default value."
  [ob]
  (ops/pipe (ops/take-last 1) ob))

(defn reduce
  "Applies an accumulator function over an observable sequence,
  returning the result of the aggregation as a single element in the
  result sequence."
  ([f ob]
   (ops/pipe (ops/reduce f) ob))
  ([f seed ob]
   (ops/pipe (ops/reduce f seed) ob)))

(defn scan
  "Applies an accumulator function over an observable sequence and
  returns each intermediate result.  Same as reduce but with
  intermediate results"
  ([f ob]
   (ops/pipe (ops/scan f) ob))
  ([f seed ob]
   (ops/pipe (ops/scan f seed) ob)))

(defn merge-scan
  "Applies an accumulator function over the source Observable where
  the accumulator function itself returns an Observable, then each
  intermediate Observable returned is merged into the output
  Observable."
  [f seed ob]
  (ops/pipe (ops/merge-scan f seed) ob))

(defn expand
  "Recursively projects each source value to an Observable
  which is merged in the output Observable."
  [f ob]
  (ops/pipe (ops/expand f) ob))

(defn with-latest-from
  "Merges the specified observable sequences into one observable
  sequence by using the selector function only when the source
  observable sequence (the instance) produces an element."
  ([o1 source] (ops/pipe (ops/with-latest o1) source))
  ([o1 o2 source] (ops/pipe (ops/with-latest o1 o2) source))
  ([o1 o2 o3 source] (ops/pipe (ops/with-latest o1 o2 o3) source))
  ([o1 o2 o3 o4 source] (ops/pipe (ops/with-latest o1 o2 o3 o4) source))
  ([o1 o2 o3 o4 o5 source] (ops/pipe (ops/with-latest o1 o2 o3 o4 o5) source))
  ([o1 o2 o3 o4 o5 o6 source] (ops/pipe (ops/with-latest o1 o2 o3 o4 o5 o6) source)))

(defn combine-latest-with
  "Combines multiple Observables to create an Observable whose values
  are calculated from the latest values of each of its input
  Observables (operator)."
  ([o1 ob] (ops/pipe (ops/combine-latest o1) ob))
  ([o1 o2 ob] (ops/pipe (ops/combine-latest o1 o2) ob))
  ([o1 o2 o3 ob] (ops/pipe (ops/combine-latest o1 o2 o3) ob))
  ([o1 o2 o3 o4 ob] (ops/pipe (ops/combine-latest o1 o2 o3 o4) ob))
  ([o1 o2 o3 o4 o5 ob] (ops/pipe (ops/combine-latest o1 o2 o3 o4 o5) ob))
  ([o1 o2 o3 o4 o5 o6 ob] (ops/pipe (ops/combine-latest o1 o2 o3 o4 o5 o6) ob)))

(defn catch
  "Continues an observable sequence that is terminated
  by an exception with the next observable sequence."
  ([handler ob] (ops/pipe (ops/catch handler) ob))
  ([pred handler ob]
   (ops/pipe (ops/catch (fn [value]
                      (if (pred value)
                        (handler value)
                        (throw value))))
         ob)))

(defn tap
  "Invokes an action for each element in the
  observable sequence."
  ([f ob] (ops/pipe (ops/tap f) ob))
  ([f e ob] (ops/pipe (ops/tap f e) ob))
  ([f e c ob] (ops/pipe (ops/tap f e c) ob)))

(defn throttle
  "Returns an observable sequence that emits only the first item emitted
  by the source Observable during sequential time windows of a
  specified duration."
  ([ms ob] (ops/pipe (ops/throttle ms) ob))
  ([ms config ob] (ops/pipe (ops/throttle ms config) ob)))

(defn debounce
  "Emits an item from the source Observable after a
  particular timespan has passed without the Observable
  omitting any other items."
  [ms ob]
  (ops/pipe (ops/debounce ms) ob))

(defn sample
  "Samples the observable sequence at each interval."
  [ms ob]
  (ops/pipe (ops/sample ms) ob))

(defn sample-when
  "Samples the observable sequence at each interval."
  [other ob]
  (ops/pipe (ops/sample-when other) ob))

(defn ignore
  "Ignores all elements in an observable sequence leaving only the
  termination messages."
  [ob]
  (ops/pipe (ops/ignore) ob))

(defn finalize
  "Returns an Observable that mirrors the source Observable, but will
  call a specified function when the source terminates on complete or
  error."
  [f ob]
  (ops/pipe (ops/finalize f) ob))

(defn buffer
  "Projects each element of an observable sequence into zero
  or more buffers which are produced based on element count
  information."
  ([n ob] (ops/pipe (ops/buffer n) ob))
  ([n o ob] (ops/pipe (ops/buffer n o) ob)))

(defn buffer-time
  "Buffers the source Observable values for a specific time period."
  ([ms ob] (ops/pipe (ops/buffer-time ms) ob))
  ([ms start ob] (ops/pipe (ops/buffer-time ms start) ob))
  ([ms start max ob] (ops/pipe (ops/buffer-time ms start max) ob)))

(defn buffer-until
  "Buffers the source Observable values until notifier emits."
  [notifier ob]
  (ops/pipe (ops/buffer-until notifier) ob))

(defn retry
  "Given an optional number of retries and an observable,
  repeats the source observable the specified number of
  times or until it terminates. If no number of retries
  is given, it will be retried indefinitely."
  ([ob] (ops/pipe (ops/retry) ob))
  ([n ob] (ops/pipe (ops/retry n) ob)))

(defn transform
  "Transform the observable sequence using transducers."
  [xform ob]
  (ops/pipe (ops/transform xform) ob))

(defn timeout
  "Returns the source observable sequence or the other
  observable sequence if dueTime elapses."
  ([ms ob] (ops/pipe (ops/timeout ms) ob))
  ([ms with ob] (ops/pipe (ops/timeout ms with) ob)))

(defn delay
  "Time shifts the observable sequence by dueTime. The relative
  time intervals between the values are preserved."
  [ms ob]
  (ops/pipe (ops/delay ms) ob))

(defn delay-at-least
  "Time shifts at least `ms` milisseconds."
  [ms ob]
  (ops/pipe (ops/delay-at-least ms) ob))

(defn delay-when
  "Time shifts the observable sequence based on a subscription
  delay and a delay selector function for each element."
  ([sf ob] (ops/pipe (ops/delay-when sf) ob))
  ([sf sd ob] (ops/pipe (ops/delay-when sf sd) ob)))

(defn flatten
  "Just like clojure collections flatten but for rx streams. Given a stream
  off collections will emit every value separately"
  [ob]
  (ops/pipe (rx/concatMap #(-> %1)) ob))

(defn concat-reduce
  "Like reduce but accepts a function that returns a stream. Will use as
  value for the next step in the reduce the last valued emited by the stream
  in the function."
  [f seed ob]
  (let [current-acc (volatile! seed)]
    (->> (concat
          (of seed)
          (->> ob
               (mapcat #(f @current-acc %))
               (tap #(vreset! current-acc %))))
         (last))))

(defn observe-on
  [sch ob]
  (ops/pipe (ops/observe-on sch) ob))

(defn subscribe-on
  [sch ob]
  (ops/pipe (ops/subscribe-on sch) ob))
