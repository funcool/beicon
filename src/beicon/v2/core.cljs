(ns beicon.v2.core
  (:refer-clojure :exclude [map filter reduce merge repeat first
                            last mapcat repeatedly zip take take-while
                            map-indexed concat empty take-last delay
                            range throw flatten comp to-array group-by
                            count find max min partition merge-with])
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
(def ^:const TimeoutError rx/TimeoutError)

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

(defn timeout-error?
  ^boolean
  [o]
  (instance? TimeoutError o))

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

(defn defer
  "Creates an observable that defers the creation of the actual observable
  until a subscriber subscribes."
  [factory]
  (rx/defer factory))

(defn iif
  "Returns an observable that subscribes to either the first or second
  observable based on a condition."
  ([condition on-true on-false]
   (rx/iif #(boolean (condition)) on-true on-false)))

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

(defn exhaust-map
  "Maps each value from the source Observable to an Observable, but
   ignores subsequent values until the inner Observable completes.
   Args:
     f: a function that takes a value from the source observable and
        returns an Observable
     ob: the source Observable"
  [f ob]
  (ops/pipe (ops/exhaust-map f) ob))

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

(defn start-with
  "Emits the specified values before emitting values from the source observable."
  ([v ob] (ops/pipe (ops/start-with v) ob))
  ([v1 v2 ob] (ops/pipe (ops/start-with v1 v2) ob))
  ([v1 v2 v3 ob] (ops/pipe (ops/start-with v1 v2 v3) ob))
  ([v1 v2 v3 v4 ob] (ops/pipe (ops/start-with v1 v2 v3 v4) ob)))

(defn exhaust-map
  "Projects each source value to an Observable which is merged in the
  output Observable only if the previous projected Observable has completed."
  [f ob]
  (ops/pipe (ops/exhaust-map f) ob))

(defn pairwise
  "Emits an array of the current value and the previous value."
  [ob]
  (ops/pipe (ops/pairwise) ob))

(defn to-array
  "Collects all values from the source into an array and emits that array."
  [ob]
  (ops/pipe (ops/to-array) ob))

(defn group-by
  "Groups the items emitted by an observable according to a key selector function."
  ([key-fn ob] (ops/pipe (ops/group-by key-fn) ob))
  ([key-fn element-fn ob] (ops/pipe (ops/group-by key-fn element-fn) ob)))

(defn repeat
  "Repeat the source observable sequence a specified number of times
  or indefinitely."
  ([ob] (ops/pipe (ops/repeat) ob))
  ([n ob] (ops/pipe (ops/repeat n) ob)))

(defn count
  "Counts the number of emissions from the source and emits that count."
  ([ob] (ops/pipe (ops/count) ob))
  ([pred ob] (ops/pipe (ops/count pred) ob)))

(defn every
  "Determines whether all items emitted by an observable meet some criteria."
  [pred ob]
  (ops/pipe (ops/every pred) ob))

(defn element-at
  "Emits the single value at the specified index."
  ([n ob] (ops/pipe (ops/element-at n) ob))
  ([n default-value ob] (ops/pipe (ops/element-at n default-value) ob)))

(defn window-time
  "Time shifts the observable sequence by producing windows of values
  for a specific time period."
  ([ms ob] (ops/pipe (ops/window-time ms) ob))
  ([ms start ob] (ops/pipe (ops/window-time ms start) ob))
  ([ms start max ob] (ops/pipe (ops/window-time ms start max) ob)))

(defn share-replay
  "Share the source observable and replay emissions to new subscribers."
  ([buffer-size ob] (ops/pipe (ops/share-replay buffer-size) ob))
  ([buffer-size window-time ob] (ops/pipe (ops/share-replay buffer-size window-time) ob)))

(defn audit-time
  "Ignores source values for `ms` milliseconds, then emits the most recent value."
  [ms ob]
  (ops/pipe (ops/audit-time ms) ob))

(defn end-with
  "Emits the specified values after the source observable completes."
  ([v ob] (ops/pipe (ops/end-with v) ob))
  ([v1 v2 ob] (ops/pipe (ops/end-with v1 v2) ob))
  ([v1 v2 v3 ob] (ops/pipe (ops/end-with v1 v2 v3) ob))
  ([v1 v2 v3 v4 ob] (ops/pipe (ops/end-with v1 v2 v3 v4) ob)))

(defn throw-if-empty
  "Throws an error if the source observable completes without emitting any values."
  ([error-factory ob] (ops/pipe (ops/throw-if-empty error-factory) ob)))

(defn find
  "Finds the first value emitted by the source that matches the predicate."
  [pred ob]
  (ops/pipe (ops/find pred) ob))

(defn is-empty
  "Emits true if the source observable completes without emitting any values, false otherwise."
  [ob]
  (ops/pipe (ops/is-empty) ob))

(defn single
  "Emits the single value that matches the predicate, or throws if zero or multiple values match."
  ([pred ob] (ops/pipe (ops/single pred) ob)))

(defn max
  "Emits the maximum value emitted by the source observable."
  ([ob] (ops/pipe (ops/max) ob))
  ([comparator ob] (ops/pipe (ops/max comparator) ob)))

(defn min
  "Emits the minimum value emitted by the source observable."
  ([ob] (ops/pipe (ops/min) ob))
  ([comparator ob] (ops/pipe (ops/min comparator) ob)))

(defn timestamp
  "Attaches a timestamp to each value emitted by the source observable."
  [ob]
  (ops/pipe (ops/timestamp) ob))

(defn materialize
  "Represents all notifications (next, error, complete) as Notification objects."
  [ob]
  (ops/pipe (ops/materialize) ob))

(defn dematerialize
  "Converts Notification objects back to their corresponding emissions."
  [ob]
  (ops/pipe (ops/dematerialize) ob))

(defn sequence-equal
  "Emits true if both observables emit the same values in the same order."
  [other ob]
  (ops/pipe (ops/sequence-equal other) ob))

(defn on-error-resume-next
  "When the source errors, continues with the provided observable."
  ([other ob] (ops/pipe (ops/on-error-resume-next other) ob))
  ([o1 o2 ob] (ops/pipe (ops/on-error-resume-next o1 o2) ob))
  ([o1 o2 o3 ob] (ops/pipe (ops/on-error-resume-next o1 o2 o3) ob)))

(defn exhaust-all
  "Converts a higher-order observable into a first-order observable by
  concatenating each inner observable, ignoring new inner observables
  until the current one completes."
  [ob]
  (ops/pipe (ops/exhaust-all) ob))

(defn switch-map-to
  "Projects each source value to the same observable, switching to the new
  observable whenever a new value arrives, discarding previous ones."
  [inner-observable ob]
  (ops/pipe (ops/switch-map-to inner-observable) ob))

(defn merge-map-to
  "Projects each source value to the same observable, merging results."
  ([inner-observable ob] (ops/pipe (ops/merge-map-to inner-observable) ob))
  ([inner-observable concurrency ob] (ops/pipe (ops/merge-map-to inner-observable concurrency) ob)))

(defn concat-map-to
  "Projects each source value to the same observable, concatenating results."
  [inner-observable ob]
  (ops/pipe (ops/concat-map-to inner-observable) ob))

(defn switch-scan
  "Like merge-scan but uses switchMap semantics for the inner observable."
  ([f seed ob] (ops/pipe (ops/switch-scan f seed) ob))
  ([f seed concurrent ob] (ops/pipe (ops/switch-scan f seed concurrent) ob)))

(defn window-count
  "Like buffer-count but emits observables instead of arrays."
  ([window-size ob] (ops/pipe (ops/window-count window-size) ob))
  ([window-size start-window-every ob] (ops/pipe (ops/window-count window-size start-window-every) ob)))

(defn race-with
  "Returns an observable that mirrors the first source observable to emit an item."
  ([o1 ob] (ops/pipe (ops/race-with o1) ob))
  ([o1 o2 ob] (ops/pipe (ops/race-with o1 o2) ob))
  ([o1 o2 o3 ob] (ops/pipe (ops/race-with o1 o2 o3) ob))
  ([o1 o2 o3 o4 ob] (ops/pipe (ops/race-with o1 o2 o3 o4) ob)))

(defn connect
  "Connects to a connectable observable."
  [selector ob]
  (ops/pipe (ops/connect selector) ob))

(defn partition
  "Splits the source observable into two: one for values that match the predicate
  and one for values that don't. Returns a tuple [matching non-matching]."
  ([pred ob] ((ops/partition pred) ob))
  ([pred this-arg ob] ((ops/partition pred this-arg) ob)))

(defn distinct-until-key-changed
  "Returns an observable that emits all items emitted by the source, but filters
  out items that have the same key as the previous item."
  ([key-fn ob] (ops/pipe (ops/distinct-until-key-changed key-fn) ob))
  ([key-fn compare-fn ob] (ops/pipe (ops/distinct-until-key-changed key-fn compare-fn) ob)))

(defn buffer-toggle
  "Buffers the source observable values using opening and closing observables."
  [openings closing-selector ob]
  (ops/pipe (ops/buffer-toggle openings closing-selector) ob))

(defn buffer-when
  "Buffers the source observable values using a closing selector function."
  [closing-selector ob]
  (ops/pipe (ops/buffer-when closing-selector) ob))

(defn window-toggle
  "Windows the source observable values using opening and closing observables."
  [openings closing-selector ob]
  (ops/pipe (ops/window-toggle openings closing-selector) ob))

(defn window-when
  "Windows the source observable values using a closing selector function."
  [closing-selector ob]
  (ops/pipe (ops/window-when closing-selector) ob))

(defn observe-on
  [sch ob]
  (ops/pipe (ops/observe-on sch) ob))

(defn subscribe-on
  [sch ob]
  (ops/pipe (ops/subscribe-on sch) ob))

(defn find-index
  "Finds the index of the first value that matches the predicate."
  [pred ob]
  (ops/pipe (ops/find-index pred) ob))

(defn map-to
  "Maps all values emitted by the source observable to a constant value."
  [v ob]
  (ops/pipe (ops/map-to v) ob))

(defn switch-all
  "Converts a higher-order observable into a first-order observable by
  concatenating each inner observable, switching to the new inner observable
  whenever a new one arrives, ignoring previous inner observables."
  [ob]
  (ops/pipe (ops/switch-all) ob))

(defn zip-all
  "Zips all inner observables together, emitting values only when all inner
  observables have emitted a value."
  [ob]
  (ops/pipe (ops/zip-all) ob))

(defn zip-with
  "Zips the source observable with other observables."
  ([o1 ob] (ops/pipe (ops/zip-with o1) ob))
  ([o1 o2 ob] (ops/pipe (ops/zip-with o1 o2) ob))
  ([o1 o2 o3 ob] (ops/pipe (ops/zip-with o1 o2 o3) ob))
  ([o1 o2 o3 o4 ob] (ops/pipe (ops/zip-with o1 o2 o3 o4) ob)))

(defn merge-with
  "Merges the source observable with other observables."
  ([o1 ob] (ops/pipe (ops/merge-with o1) ob))
  ([o1 o2 ob] (ops/pipe (ops/merge-with o1 o2) ob))
  ([o1 o2 o3 ob] (ops/pipe (ops/merge-with o1 o2 o3) ob))
  ([o1 o2 o3 o4 ob] (ops/pipe (ops/merge-with o1 o2 o3 o4) ob)))

(defn concat-with
  "Concatenates the source observable with other observables."
  ([o1 ob] (ops/pipe (ops/concat-with o1) ob))
  ([o1 o2 ob] (ops/pipe (ops/concat-with o1 o2) ob))
  ([o1 o2 o3 ob] (ops/pipe (ops/concat-with o1 o2 o3) ob))
  ([o1 o2 o3 o4 ob] (ops/pipe (ops/concat-with o1 o2 o3 o4) ob)))

(defn time-interval
  "Records the time interval between consecutive values emitted by the source."
  [ob]
  (ops/pipe (ops/time-interval) ob))

(defn repeat-when
  "Returns an observable that repeats the source observable when the notifier emits."
  [notifier ob]
  (ops/pipe (ops/repeat-when notifier) ob))

(defn retry-when
  "Returns an observable that retries the source observable when the notifier emits."
  [notifier ob]
  (ops/pipe (ops/retry-when notifier) ob))

(defn timeout-with
  "Returns the source observable or the provided observable if the timeout expires."
  [due with-observable ob]
  (ops/pipe (ops/timeout-with due with-observable) ob))

(defn from-event-pattern
  "Creates an observable from an event pattern (add/remove handler functions)."
  ([add-handler remove-handler]
   (rx/fromEventPattern add-handler remove-handler))
  ([add-handler remove-handler transform]
   (rx/fromEventPattern add-handler remove-handler transform)))

(defn generate
  "Generates an observable sequence by running a state-driven loop."
  ([initial-state condition iterate-fn]
   (rx/generate initial-state condition iterate-fn))
  ([initial-state condition iterate-fn result-selector]
   (rx/generate initial-state condition iterate-fn result-selector))
  ([initial-state condition iterate-fn result-selector scheduler]
   (rx/generate initial-state condition iterate-fn result-selector scheduler)))

(defn bind-callback
  "Converts a callback-based function to a function that returns an observable."
  ([func]
   (rx/bindCallback func))
  ([func scheduler]
   (rx/bindCallback func scheduler)))

(defn bind-node-callback
  "Converts a Node.js callback-based function to a function that returns an observable."
  ([func]
   (rx/bindNodeCallback func))
  ([func scheduler]
   (rx/bindNodeCallback func scheduler)))

(defn using
  "Creates an observable that depends on a resource, disposing it when unsubscribed."
  [resource-factory observable-factory]
  (rx/using resource-factory observable-factory))

(defn connectable
  "Creates a connectable observable from a source observable."
  ([source]
   (rx/connectable source #js {:connector #(rx/Subject.)}))
  ([source subject-factory]
   (rx/connectable source #js {:connector subject-factory})))

(defn first-value-from
  "Returns a promise that resolves with the first value emitted by the observable."
  [observable]
  (rx/firstValueFrom observable))

(defn last-value-from
  "Returns a promise that resolves with the last value emitted by the observable."
  [observable]
  (rx/lastValueFrom observable))

(defn is-observable
  "Returns true if the value is an observable, false otherwise."
  [value]
  (rx/isObservable value))
