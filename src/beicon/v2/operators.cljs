(ns beicon.v2.operators
  "RxJS operators only"
  (:refer-clojure :exclude [map filter reduce last mapcat take take-while comp
                            map-indexed concat take-last delay distinct
                            to-array group-by repeat count find max min
                            partition merge-with])
  (:require
   ["rxjs" :as rx]
   ["rxjs/operators" :as ops]
   [cljs.core :as c]))

(defn scheduler
  {:no-doc true}
  [type]
  (case type
    :asap rx/asapScheduler
    :async rx/asyncScheduler
    :queue rx/queueScheduler
    :af rx/animationFrameScheduler
    :animation-frame rx/animationFrameScheduler))

(defn ^:no-doc plain-object?
  ^boolean
  [o]
  (and (some? o)
       (identical? (.getPrototypeOf js/Object o)
                   (.-prototype js/Object))))

(def ^function share
  "Returns an observable sequence that shares a single subscription to
  the underlying sequence."
  rx/share)

(def ^function if-empty
  "Emits a given value if the source Observable completes without
  emitting any next value, otherwise mirrors the source Observable."
  rx/defaultIfEmpty)

(def ^function merge-all
  "Merges an observable sequence of observable sequences into an
  observable sequence."
  rx/mergeAll)

(defn filter
  "Filters the elements of an observable sequence
  based on a predicate."
  [f]
  (rx/filter #(boolean (f %))))

(defn map
  [f]
  (rx/map #(f %)))

(defn map-indexed
  "Same as `map` but also projects an index."
  ([] (rx/map (fn [a b] #js [a b])))
  ([f] (rx/map #(f %2 %1))))

(defn- safe-project
  "Wraps a projection function so that nil/undefined results are
  replaced with rx/EMPTY. This prevents RxJS 8+ from throwing
  'Cannot read properties of undefined (reading @@observable)'
  when a projection function returns nil."
  [f]
  (fn [v] (or (f v) rx/EMPTY)))

(defn- safe-project-indexed
  "Indexed variant of `safe-project`."
  [f]
  (fn [v i] (or (f v i) rx/EMPTY)))

(defn merge-map
  "Projects each element of an observable sequence to an observable
  sequence and merges the resulting observable sequences or Promises
  or array/iterable into one observable sequence.

  In other languages is called: flatMap or mergeMap."
  ([f] (rx/mergeMap (safe-project f)))
  ([f concurrency] (rx/mergeMap (safe-project f) concurrency)))

(defn switch-map
  [f]
  (rx/switchMap (safe-project f)))

(defn mapcat
  "Projects each element of an observable sequence to an observable
  sequence and concatenates the resulting observable sequences or
  Promises or array/iterable into one observable sequence."
  [f]
  (rx/concatMap (safe-project f)))

(defn mapcat-indexed
  "Indexed variant of `mapcat`"
  [f]
  (rx/concatMap (safe-project-indexed f)))

(def ^function start-with
  "Returns an observable sequence that upon subscription emits the
   specified values before it begins to emit the elements of the
   source observable sequence."
  rx/startWith)

(def ^function end-with
  "Returns an observable sequence that emits the elements of the
   source observable and then emits the specified values after the
   source completes."
  rx/endWith)

(def ^function skip
  "Bypasses a specified number of elements in an
  observable sequence and then returns the remaining
  elements."
  rx/skip)

(defn skip-while
  "Bypasses elements in an observable sequence as long
  as a specified condition is true and then returns the
  remaining elements."
  [f]
  (rx/skipWhile #(boolean (f %))))

(defn skip-until
  "Returns the values from the source observable sequence only after the
  other observable sequence produces a value."
  [pob]
  (rx/skipUntil pob))

(defn skip-last
  "Skip a specified number of values before the completion of an observable."
  [n]
  (rx/skipLast n))

(def ^function take
  "Bypasses a specified number of elements in an observable sequence and
  then returns the remaining elements."
  rx/take)

(def ^function take-last
  rx/takeLast)

(defn take-while
  "Returns elements from an observable sequence as long as a specified
  predicate returns true."
  [f]
  (rx/takeWhile #(boolean (f %))))

(def ^function take-until
  "Returns the values from the source observable sequence until the
  other observable sequence or Promise produces a value."
  rx/takeUntil)

(defn reduce
  "Applies an accumulator function over an observable sequence,
  returning the result of the aggregation as a single element in the
  result sequence."
  ([f]
   (rx/reduce #(f %1 %2)))
  ([f seed]
   (rx/reduce #(f %1 %2) seed)))

(defn scan
  "Applies an accumulator function over an observable sequence and
  returns each intermediate result.  Same as reduce but with
  intermediate results"
  ([f]
   (rx/scan #(f %1 %2)))
  ([f seed]
   (rx/scan #(f %1 %2) seed)))

(defn merge-scan
  "Applies an accumulator function over the source Observable where
  the accumulator function itself returns an Observable, then each
  intermediate Observable returned is merged into the output
  Observable."
  [f seed]
  (rx/mergeScan (fn [acc v] (or (f acc v) rx/EMPTY)) seed))

(defn expand
  "Recursively projects each source value to an Observable
  which is merged in the output Observable."
  ([f] (rx/expand (safe-project f)))
  ([f c] (rx/expand (safe-project f) c)))

(def ^function with-latest
  "Merges the specified observable sequences into one observable
  sequence by using the selector function only when the source
  observable sequence (the instance) produces an element.

  (operator)"
  (js* "function withLatestFrom(...args) {
  const resultSelector = (typeof args[0] === 'function') ? args.shift() : undefined;
  if (resultSelector === undefined) {
    return ~{}(...args);
  } else {
    return ~{}(...args, resultSelector);
  }
}" rx/withLatestFrom rx/withLatestFrom))

(def ^function combine-latest
  "Combines multiple Observables to create an Observable whose values
  are calculated from the latest values of each of its input
  Observables (operator)."
  rx/combineLatestWith)

(defn catch
  "Continues an observable sequence that is terminated
  by an exception with the next observable sequence."
  [handler]
  (rx/catchError (fn [error source]
                   (let [value (handler error source)]
                     (if (instance? rx/Observable value)
                      value
                      rx/EMPTY)))))

(defn tap
  "Invokes an action for each element in the
  observable sequence."
  ([f]
   (if (or (plain-object? f)
           (fn? f))
     (rx/tap f)
     (throw (ex-info "invalid argiments" {:f f}))))
  ([f e]
   (rx/tap #js {:next f :error e :complete rx/noop}))
  ([f e c]
   (rx/tap #js {:next f :error e :complete c})))

(defn throttle
  "Returns an observable sequence that emits only the first item emitted
  by the source Observable during sequential time windows of a
  specified duration.

  (operator only)"
  ([ms]
   (rx/throttleTime ms))
  ([ms config]
   (cond
     (plain-object? config)
     (rx/throttleTime ms config)

     (map? config)
     (rx/throttleTime ms #js {:leading (:leading config true)
                              :trailing (:trailing config false)})
     :else
     (rx/throttleTime ms))))

(def ^function debounce
  "Emits an item from the source Observable after a
  particular timespan has passed without the Observable
  omitting any other items."
  rx/debounceTime)

(def ^function sample
  "Samples the observable sequence at each interval."
  rx/sampleTime)

(def ^function sample-when
  "Samples the observable sequence at each interval."
  rx/sample)

(def ^function ignore
  "Ignores all elements in an observable sequence leaving only the
  termination messages."
  rx/ignoreElements)

(def ^function finalize
  "Returns an Observable that mirrors the source Observable, but will
  call a specified function when the source terminates on complete or
  error."
  rx/finalize)

(defn distinct-contiguous
  "Returns an observable sequence that contains only
  distinct contiguous elements."
  ([] (rx/distinctUntilChanged))
  ([comparator-fn]
   (rx/distinctUntilChanged #(comparator-fn %1 %2)))
  ([comparator-fn key-fn]
   (rx/distinctUntilChanged #(comparator-fn %1 %2) #(key-fn %))))

(defn distinct
  "Returns an observable sequence that contains only distinct
  elements.

  Usage of this operator should be considered carefully due to the
  maintenance of an internal lookup structure which can grow large."
  ([] (rx/distinct))
  ([comparator-fn] (rx/distinct #(comparator-fn %1 %2)))
  ([comparator-fn key-fn] (rx/distinct #(comparator-fn %1 %2) #(key-fn %))))

(def ^function buffer
  "Projects each element of an observable sequence into zero
  or more buffers which are produced based on element count
  information."
  rx/bufferCount)

(def ^function buffer-time
  "Buffers the source Observable values for a specific time period.
  (operator only)"
  rx/bufferTime)

(def ^function buffer-until
  "Buffers the source Observable values until notifier emits."
  rx/buffer)

(def ^function retry
  "Given an optional number of retries and an observable,
  repeats the source observable the specified number of
  times or until it terminates. If no number of retries
  is given, it will be retried indefinitely."
  rx/retry)

(defn transform
  [xform]
  (fn [source]
    (rx/Observable.
     (fn [subs]
       (let [xsubs (xform
                    (fn
                      ([r] (.complete ^js subs) r)
                      ([_ input] (.next ^js subs input) input)))
             obs   #js {:next
                        (fn [input]
                          (let [v (xsubs nil input)]
                            (when (reduced? v)
                              (xsubs @v))))
                        :error
                        (fn [cause]
                          (.error ^js subs cause))

                        :complete
                        (fn []
                          (xsubs nil)
                          (.complete subs))}
             sub   (.subscribe source obs)]
         (fn []
           (.unsubscribe ^js subs)))))))

(defn timeout
  "Returns the source observable sequence or the other
  observable sequence if dueTime elapses."
  ([ms]
   (cond
     (or (number? ms)
         (instance? js/Data ms)
         (plain-object? ms))
     (rx/timeout ms)

     (map? ms)
     (rx/timeout #js {:first (get ms :first)
                      :each  (get ms :each)
                      :with  (get ms :with)})

     :else
     (throw (ex-info "invalid arguments" {:ms ms}))))
  ([ms with]
   (rx/timeout #js {:each ms
                    :with (if (instance? rx/Observable with)
                            #(-> with)
                            with)})))

(def ^function delay
  "Time shifts the observable sequence by dueTime. The relative
  time intervals between the values are preserved."
  rx/delay)

(def ^function delay-when
  "Time shifts the observable sequence based on a subscription
  delay and a delay selector function for each element."
  rx/delayWhen)

(def ^function start-with
  "Emits the specified values before emitting values from the source observable."
  rx/startWith)

(defn exhaust-map
  "Projects each source value to an Observable which is merged in the
  output Observable only if the previous projected Observable has completed."
  [f]
  (rx/exhaustMap (safe-project f)))

(def ^function pairwise
  "Emits an array of the current value and the previous value."
  rx/pairwise)

(def ^function to-array
  "Collects all values from the source into an array and emits that array."
  rx/toArray)

(defn group-by
  "Groups the items emitted by an observable according to a key selector function."
  ([key-fn] (rx/groupBy key-fn))
  ([key-fn element-fn] (rx/groupBy key-fn element-fn)))

(defn repeat
  "Repeat the source observable sequence a specified number of times
  or indefinitely."
  ([] (rx/repeat))
  ([n] (rx/repeat n)))

(defn count
  "Counts the number of emissions from the source and emits that count."
  ([] (rx/count))
  ([pred] (rx/count #(boolean (pred %)))))

(defn every
  "Determines whether all items emitted by an observable meet some criteria."
  [pred]
  (rx/every #(boolean (pred %))))

(defn element-at
  "Emits the single value at the specified index."
  ([n] (rx/elementAt n))
  ([n default-value] (rx/elementAt n default-value)))

(def ^function window-time
  "Time shifts the observable sequence by producing windows of values
  for a specific time period."
  rx/windowTime)

(defn share-replay
  "Share the source observable and replay emissions to new subscribers."
  ([buffer-size] (rx/shareReplay buffer-size))
  ([buffer-size window-time] (rx/shareReplay buffer-size window-time)))

(def ^function audit-time
  "Ignores source values for `ms` milliseconds, then emits the most recent value."
  rx/auditTime)

(def ^function end-with
  "Emits the specified values after the source observable completes."
  rx/endWith)

(defn throw-if-empty
  "Throws an error if the source observable completes without emitting any values."
  ([error-factory] (rx/throwIfEmpty error-factory)))

(defn find
  "Finds the first value emitted by the source that matches the predicate."
  [pred]
  (rx/find #(boolean (pred %))))

(def ^function is-empty
  "Emits true if the source observable completes without emitting any values, false otherwise."
  rx/isEmpty)

(defn single
  "Emits the single value that matches the predicate, or throws if zero or multiple values match."
  ([pred] (rx/single #(boolean (pred %)))))

(defn max
  "Emits the maximum value emitted by the source observable."
  ([] (rx/max))
  ([comparator] (rx/max comparator)))

(defn min
  "Emits the minimum value emitted by the source observable."
  ([] (rx/min))
  ([comparator] (rx/min comparator)))

(def ^function timestamp
  "Attaches a timestamp to each value emitted by the source observable."
  rx/timestamp)

(def ^function materialize
  "Represents all notifications (next, error, complete) as Notification objects."
  rx/materialize)

(def ^function dematerialize
  "Converts Notification objects back to their corresponding emissions."
  rx/dematerialize)

(defn sequence-equal
  "Emits true if both observables emit the same values in the same order."
  [other]
  (rx/sequenceEqual other))

(def ^function on-error-resume-next
  "When the source errors, continues with the provided observable."
  rx/onErrorResumeNextWith)

(def ^function exhaust-all
  "Converts a higher-order observable into a first-order observable by
  concatenating each inner observable, ignoring new inner observables
  until the current one completes."
  rx/exhaustAll)

(defn switch-map-to
  "Projects each source value to the same observable, switching to the new
  observable whenever a new value arrives, discarding previous ones."
  [inner-observable]
  (rx/switchMapTo inner-observable))

(defn merge-map-to
  "Projects each source value to the same observable, merging results."
  ([inner-observable] (rx/mergeMapTo inner-observable))
  ([inner-observable concurrency] (rx/mergeMapTo inner-observable concurrency)))

(defn concat-map-to
  "Projects each source value to the same observable, concatenating results."
  [inner-observable]
  (rx/concatMapTo inner-observable))

(defn switch-scan
  "Like merge-scan but uses switchMap semantics for the inner observable."
  ([f seed] (rx/switchScan (fn [acc v] (or (f acc v) rx/EMPTY)) seed))
  ([f seed concurrent] (rx/switchScan (fn [acc v] (or (f acc v) rx/EMPTY)) seed concurrent)))

(defn window-count
  "Like buffer-count but emits observables instead of arrays."
  ([window-size] (rx/windowCount window-size))
  ([window-size start-window-every] (rx/windowCount window-size start-window-every)))

(def ^function race-with
  "Returns an observable that mirrors the first source observable to emit an item."
  rx/raceWith)

(defn connect
  "Connects to a connectable observable."
  [selector]
  (rx/connect selector))

(defn partition
  "Splits the source observable into two: one for values that match the predicate
  and one for values that don't. Returns a tuple [matching non-matching]."
  ([pred] (ops/partition #(boolean (pred %))))
  ([pred this-arg] (ops/partition #(boolean (pred %)) this-arg)))

(defn distinct-until-key-changed
  "Returns an observable that emits all items emitted by the source, but filters
  out items that have the same key as the previous item. The key-fn should be a
  function that extracts the key from each item."
  ([key-fn] (rx/distinctUntilChanged (fn [a b] (= (key-fn a) (key-fn b)))))
  ([key-fn compare-fn] (rx/distinctUntilChanged (fn [a b] (compare-fn (key-fn a) (key-fn b))))))

(defn buffer-toggle
  "Buffers the source observable values using opening and closing observables."
  [openings closing-selector]
  (rx/bufferToggle openings closing-selector))

(defn buffer-when
  "Buffers the source observable values using a closing selector function."
  [closing-selector]
  (rx/bufferWhen closing-selector))

(defn window-toggle
  "Windows the source observable values using opening and closing observables."
  [openings closing-selector]
  (rx/windowToggle openings closing-selector))

(defn window-when
  "Windows the source observable values using a closing selector function."
  [closing-selector]
  (rx/windowWhen closing-selector))

(def ^function pipe
  (js* "function pipeWith(...fns) { const input = fns.pop(); return fns.reduce((prev, fn) => fn(prev), input); }"))

(def ^function comp
  (js* "function pipeComp(...fns) { return (source) => fns.reduce((prev, fn) => fn(prev), source); }"))

(defn delay-at-least
  "Time shifts at least `ms` milisseconds."
  [ms]
  (comp (combine-latest (rx/timer ms))
        (map c/first)))

(defn observe-on
  ([sch]
   (cond
     (instance? rx/Scheduler sch)
     (rx/observeOn sch)

     (keyword? sch)
     (observe-on (scheduler sch))

     :else
     (throw (ex-info "invalid arguments" {:sch sch}))))
  ([sch delay]
   (cond
     (instance? rx/Scheduler sch)
     (rx/observeOn sch delay)

     (keyword? sch)
     (observe-on (scheduler sch) delay)

     :else
     (throw (ex-info "invalid arguments" {:sch sch :delay delay})))))

(defn subscribe-on
  ([sch]
   (cond
     (instance? rx/Scheduler sch)
     (rx/subscribeOn sch)

     (keyword? sch)
     (subscribe-on (scheduler sch))

     :else
     (throw (ex-info "invalid arguments" {:sch sch}))))
  ([sch delay]
   (cond
     (instance? rx/Scheduler sch)
     (rx/subscribeOn sch delay)

     (keyword? sch)
     (subscribe-on (scheduler sch) delay)

     :else
     (throw (ex-info "invalid arguments" {:sch sch :delay delay})))))

(defn find-index
  "Finds the index of the first value that matches the predicate."
  [pred]
  (rx/findIndex #(boolean (pred %))))

(def ^function map-to
  "Maps all values emitted by the source observable to a constant value."
  rx/mapTo)

(def ^function switch-all
  "Converts a higher-order observable into a first-order observable by
  concatenating each inner observable, switching to the new inner observable
  whenever a new one arrives, ignoring previous inner observables."
  rx/switchAll)

(def ^function concat-all
  "Flattens an observable of observables by concatenating each inner observable."
  rx/concatAll)

(def ^function zip-all
  "Zips all inner observables together, emitting values only when all inner
  observables have emitted a value."
  rx/zipAll)

(defn zip-with
  "Zips the source observable with other observables."
  [& observables]
  (apply rx/zipWith observables))

(defn merge-with
  "Merges the source observable with other observables."
  [& observables]
  (apply rx/mergeWith observables))

(defn concat-with
  "Concatenates the source observable with other observables."
  [& observables]
  (apply rx/concatWith observables))

(def ^function combine-latest-all
  "Combines the latest values from all inner observables, emitting whenever
  any inner observable emits."
  rx/combineLatestAll)

(def ^function time-interval
  "Records the time interval between consecutive values emitted by the source."
  rx/timeInterval)

(defn repeat-when
  "Returns an observable that repeats the source observable when the notifier emits."
  [notifier]
  (rx/repeatWhen notifier))

(defn retry-when
  "Returns an observable that retries the source observable when the notifier emits."
  [notifier]
  (rx/retryWhen notifier))

(defn timeout-with
  "Returns the source observable or the provided observable if the timeout expires."
  [due with-observable]
  (rx/timeoutWith due with-observable))

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
   (rx/connectable source))
  ([source subject-factory]
   (rx/connectable source subject-factory)))

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
