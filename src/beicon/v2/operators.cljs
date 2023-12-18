(ns beicon.v2.operators
  "RxJS operators only"
  (:refer-clojure :exclude [map filter reduce last mapcat take take-while
                            map-indexed concat take-last delay distinct])
  (:require
   ["rxjs" :as rx]
   ["./impl/index.js" :as rxc]
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

(defn merge-map
  "Projects each element of an observable sequence to an observable
  sequence and merges the resulting observable sequences or Promises
  or array/iterable into one observable sequence.

  In other languages is called: flatMap or mergeMap."
  ([f] (rx/mergeMap #(f %)))
  ([f concurrency] (rx/mergeMap #(f %) concurrency)))

(defn switch-map
  [f]
  (rx/switchMap #(f %)))

(defn mapcat
  "Projects each element of an observable sequence to an observable
  sequence and concatenates the resulting observable sequences or
  Promises or array/iterable into one observable sequence."
  [f]
  (rx/concatMap #(f %)))

(defn mapcat-indexed
  "Indexed variant of `mapcat`"
  [f]
  (rx/concatMap #(f %2 %1)))

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
  (rx/mergeScan #(f %1 %2) seed))

(defn expand
  "Recursively projects each source value to an Observable
  which is merged in the output Observable."
  ([f] (rx/expand #(f %)))
  ([f c] (rx/expand #(f %) c)))

(def ^function with-latest
  "Merges the specified observable sequences into one observable
  sequence by using the selector function only when the source
  observable sequence (the instance) produces an element.

  (operator)"
  rxc/withLatestFrom)

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

(defn delay-at-least
  "Time shifts at least `ms` milisseconds."
  [ms]
  (rxc/pipeComp (combine-latest (rx/timer ms))
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
