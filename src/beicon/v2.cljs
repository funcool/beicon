(ns beicon.v2
  (:refer-clojure :exclude [true? map filter reduce merge repeat first
                            last mapcat repeatedly zip dedupe drop
                            take take-while map-indexed concat empty
                            take-last delay range throw do trampoline
                            subs flatten comp])
  (:require-macros [beicon.v2 :refer [push! error! end! comp]])

  (:require
   ["./rxjs/index.js" :as rx]
   ["./rxjs/custom/index.js" :as rxc]
   [cljs.core :as c]))

(def ^:const Observable rx/Observable)
(def ^:const Subject rx/Subject)
(def ^:const BehaviorSubject rx/BehaviorSubject)
(def ^:const Subscriber rx/Subscriber)
(def ^:const Disposable rx/Subscription)
(def ^:const Scheduler rx/Scheduler)
(def ^:const internal rx)

(defn- plain-object?
  ^boolean
  [o]
  (and (some? o)
       (identical? (.getPrototypeOf js/Object o)
                   (.-prototype js/Object))))

(defn ^:no-doc internal-call
  [f source]
  (f source))

;; --- Interop Helpers

(declare subject?)

(extend-type BehaviorSubject
  IDeref
  (-deref [self]
    (.getValue ^js self)))

(def ^function noop rx/noop)
(def ^function pipe rxc/pipeWith)
(def ^function comp rxc/pipeComp)

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
  rxc/forkJoin)

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
  rxc/zip)

(def ^function concat
  "Concatenates all of the specified observable
  sequences, as long as the previous observable
  sequence terminated successfully."
  rxc/concat)

(def ^function merge
  "Merges all the observable sequences and Promises
  into a single observable sequence."
  rxc/merge)

(def ^function combine-latest
  "Combines multiple Observables to create an Observable whose values
  are calculated from the latest values of each of its input
  Observables (constructor)."
  rxc/combineLatest)

(defn combine-latest-all
  "Combines multiple Observables to create an Observable whose values
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
  (case type
    :asap rx/asapScheduler
    :async rx/asyncScheduler
    :queue rx/queueScheduler
    :af rx/animationFrameScheduler
    :animation-frame rx/animationFrameScheduler))

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

(defprotocol IDisposable
  (^:no-doc -dispose [_] "dispose resources."))

(defn dispose!
  "Dispose resources acquired by the subscription."
  [v]
  (-dispose v))

(defn- wrap-disposable
  [disposable]
  (specify! disposable
    IFn
    (-invoke ([this] (-dispose this)))
    IDisposable
    (-dispose [this]
      (.unsubscribe this))))

(defn subscribe
  "Subscribes an observer to the observable sequence."
  ([ob nf]
   (cond
     (or (plain-object? nf)
         (subject? nf))
     (wrap-disposable (.subscribe ^js ob nf))

     (map? nf)
     (subscribe ob
                (get nf :next noop)
                (get nf :error noop)
                (get nf :complete noop))


     :else
     (do
       (wrap-disposable (.subscribe ^js ob #js {:next nf})))))

  ([ob next-fn error-fn]
   (let [observer #js {:next next-fn
                       :error error-fn}]
     (wrap-disposable
      (.subscribe ^js ob observer))))

  ([ob next-fn error-fn complete-fn]
   (let [observer #js {:next next-fn
                       :error error-fn
                       :complete complete-fn}]
     (wrap-disposable
      (.subscribe ^js ob observer)))))

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

(def ^function share*
  "Returns an observable sequence that shares a single subscription to
  the underlying sequence."
  rx/share)

(defn share
  "Returns an observable sequence that shares a single
  subscription to the underlying sequence."
  ([] (share*))
  ([ob] (pipe (share*) ob)))

(def ^function if-empty*
  "Emits a given value if the source Observable completes without
  emitting any next value, otherwise mirrors the source Observable."
  rx/defaultIfEmpty)

(defn if-empty
  "Emits a given value if the source Observable completes without
  emitting any next value, otherwise mirrors the source Observable."
  ([default] (if-empty* default))
  ([default ob] (pipe (if-empty* default) ob)))

;; --- Observable Transformations

(def ^function merge-all*
  "Merges an observable sequence of observable sequences into an
  observable sequence."
  rx/mergeAll)

(defn merge-all
  "Merges an observable sequence of observable sequences into an
  observable sequence."
  ([] (merge-all*))
  ([ob] (pipe (merge-all*) ob))
  ([concurrency ob] (pipe (merge-all* concurrency) ob)))

(defn filter*
  "Filters the elements of an observable sequence
  based on a predicate."
  [f]
  (rx/filter #(boolean (f %))))

(defn filter
  "Filters the elements of an observable sequence
  based on a predicate."
  ([f] (filter* f))
  ([f ob] (pipe (filter* f) ob)))

(defn map*
  [f]
  (rx/map #(f %)))

(defn map
  "Apply a function to each element of an observable
  sequence."
  ([f] (map* f))
  ([f ob] (pipe (map* f) ob)))

(defn map-indexed*
  "Same as `map*` but also projects an index."
  [f]
  (rx/map #(f %2 %1)))

(defn imap
  "Same as `map` but also projects an index."
  ([f] (map-indexed* f))
  ([f ob] (pipe (map-indexed* f) ob)))

(defn merge-map*
  "Projects each element of an observable sequence to an observable
  sequence and merges the resulting observable sequences or Promises
  or array/iterable into one observable sequence.

  In other languages is called: flatMap or mergeMap."
  ([f] (rx/mergeMap #(f %)))
  ([f concurrency] (rx/mergeMap #(f %) concurrency)))

(defn merge-map
  "Projects each element of an observable sequence to an observable
  sequence and merges the resulting observable sequences or Promises
  or array/iterable into one observable sequence.

  In other languages is called: flatMap or mergeMap."
  ([f] (merge-map* f))
  ([f ob] (pipe (merge-map* f) ob)))

(defn switch-map*
  [f]
  (rx/switchMap #(f %)))

(defn switch-map
  ([f] (switch-map* f))
  ([f ob] (pipe (switch-map* f) ob)))

(defn mapcat*
  "Projects each element of an observable sequence to an observable
  sequence and concatenates the resulting observable sequences or
  Promises or array/iterable into one observable sequence."
  [f]
  (rx/concatMap #(f %)))

(defn mapcat
  "Projects each element of an observable sequence to an observable
  sequence and concatenates the resulting observable sequences or
  Promises or array/iterable into one observable sequence."
  ([f] (mapcat* f))
  ([f ob] (pipe (mapcat* f) ob)))

(defn indexed-mapcat*
  "Indexed variant of `mcat*`"
  [f]
  (rx/concatMap #(f %2 %1)))

(defn concat-all
  ([] (merge-all* 1))
  ([ob] (pipe (merge-all* 1) ob)))

(def ^function skip*
  "Bypasses a specified number of elements in an
  observable sequence and then returns the remaining
  elements."
  rx/skip)

(defn skip
  "Bypasses a specified number of elements in an
  observable sequence and then returns the remaining
  elements."
  ([n] (skip* n))
  ([n ob] (pipe (skip* n) ob)))

(defn skip-while*
  "Bypasses elements in an observable sequence as long
  as a specified condition is true and then returns the
  remaining elements."
  [f]
  (rx/skipWhile #(boolean (f %))))

(defn skip-while
  "Bypasses elements in an observable sequence as long
  as a specified condition is true and then returns the
  remaining elements."
  ([f] (skip-while* f))
  ([f ob] (pipe (skip-while* f) ob)))

(defn skip-until*
  "Returns the values from the source observable sequence only after the
  other observable sequence produces a value."
  [pob]
  (rx/skipUntil pob))

(defn skip-until
  "Returns the values from the source observable sequence only after the
  other observable sequence produces a value."
  ([pob] (skip-until* pob))
  ([pob ob] (pipe (skip-until* pob) ob)))

(defn skip-last*
  "Skip a specified number of values before the completion of an observable."
  [n]
  (rx/skipLast (int n)))

(defn skip-last
  "Skip a specified number of values before the completion of an observable."
  ([n] (skip-last* n))
  ([n ob] (pipe (skip-last* n) ob)))

(def ^function take*
  "Bypasses a specified number of elements in an observable sequence and
  then returns the remaining elements."
  rx/take)

(defn take
  "Bypasses a specified number of elements in an observable sequence and
  then returns the remaining elements."
  ([n] (take* (int n)))
  ([n ob] (pipe (take* n) ob)))

(def ^function take-last*
  rx/takeLast)

(defn take-last
  ([n] (take-last* n))
  ([n ob] (pipe (take-last* n) ob)))

(defn take-while*
  "Returns elements from an observable sequence as long as a specified
  predicate returns true."
  [f]
  (rx/takeWhile #(boolean (f %))))

(defn take-while
  "Returns elements from an observable sequence as long as a specified
  predicate returns true."
  ([f] (take-while* f))
  ([f ob] (pipe (take-while* f) ob)))

(def ^function take-until*
  "Returns the values from the source observable sequence until the
  other observable sequence or Promise produces a value."
  rx/takeUntil)

(defn take-until
  "Returns the values from the source observable sequence until the
  other observable sequence or Promise produces a value."
  ([other] (take-until* other))
  ([other ob] (pipe (take-until* other) ob)))

(defn first
  "Return an observable that only has the first value of the provided
  observable. You can optionally pass a predicate and default value."
  ([] (take* 1))
  ([ob] (pipe (take* 1) ob)))

(defn last
  "Return an observable that only has the last value of the provided
  observable. You can optionally pass a predicate and default value."
  ([] (take-last* 1))
  ([ob] (pipe (take-last* 1) ob)))

(defn reduce*
  "Applies an accumulator function over an observable sequence,
  returning the result of the aggregation as a single element in the
  result sequence."
  [f seed]
  (rx/reduce #(f %1 %2) seed))

(defn reduce
  "Applies an accumulator function over an observable sequence,
  returning the result of the aggregation as a single element in the
  result sequence."
  ([f seed] (reduce* f seed))
  ([f seed ob] (pipe (reduce* f seed) ob)))

(defn scan*
  "Applies an accumulator function over an observable sequence and
  returns each intermediate result.  Same as reduce but with
  intermediate results"
  [f seed]
  (rx/scan #(f %1 %2) seed))

(defn scan
  "Applies an accumulator function over an observable sequence and
  returns each intermediate result.  Same as reduce but with
  intermediate results"
  ([f seed] (scan* f seed))
  ([f seed ob] (pipe (scan* f seed) ob)))


(defn merge-scan*
  "Applies an accumulator function over the source Observable where
  the accumulator function itself returns an Observable, then each
  intermediate Observable returned is merged into the output
  Observable."
  [f seed]
  (rx/mergeScan #(f %1 %2) seed))

(defn merge-scan
  "Applies an accumulator function over the source Observable where
  the accumulator function itself returns an Observable, then each
  intermediate Observable returned is merged into the output
  Observable."
  ([f seed] (merge-scan* f seed))
  ([f seed ob] (pipe (merge-scan* f seed) ob)))

(defn expand*
  "Recursively projects each source value to an Observable
  which is merged in the output Observable."
  ([f] (rx/expand #(f %)))
  ([f c] (rx/expand #(f %) c)))

(defn expand
  "Recursively projects each source value to an Observable
  which is merged in the output Observable."
  ([f] (expand* f))
  ([f ob] (pipe (expand* f) ob)))

;; ;; FIXME
;; (defn with-latest*
;;   "Merges the specified observable sequences into one observable
;;   sequence by using the selector function only when the source
;;   observable sequence (the instance) produces an element."
;;   ([f other] (.withLatestFrom ^js rxop other #(f %1 %2)))
;;   ([f other ob] (pipe (with-latest f other) ob)))

(def ^function with-latest*
  "Merges the specified observable sequences into one observable
  sequence by using the selector function only when the source
  observable sequence (the instance) produces an element.

  (operator)"
  rxc/withLatestFrom)

(defn with-latest-from
  "Merges the specified observable sequences into one observable
  sequence by using the selector function only when the source
  observable sequence (the instance) produces an element."
  ([other] (with-latest* other))
  ([other source] (pipe (with-latest* other) source)))

(def ^function combine-latest-with*
  "Combines multiple Observables to create an Observable whose values
  are calculated from the latest values of each of its input
  Observables (operator)."
  rx/combineLatestWith)

(defn combine-latest-with
  "Combines multiple Observables to create an Observable whose values
  are calculated from the latest values of each of its input
  Observables (operator)."
  ([other] (combine-latest-with* other))
  ([other ob] (pipe (combine-latest-with* other) ob))
  ([o1 o2 ob] (pipe (combine-latest-with* o1 o2) ob))
  ([o1 o2 o3 ob] (pipe (combine-latest-with* o1 o2 o3) ob))
  ([o1 o2 o3 o4 ob] (pipe (combine-latest-with* o1 o2 o3 o4) ob))
  ([o1 o2 o3 o4 o5 ob] (pipe (combine-latest-with* o1 o2 o3 o4 o5) ob))
  ([o1 o2 o3 o4 o5 o6 ob] (pipe (combine-latest-with* o1 o2 o3 o4 o5 o6) ob)))


(defn catch*
  "Continues an observable sequence that is terminated
  by an exception with the next observable sequence."
  [handler]
  (rx/catchError (fn [error source]
                   (let [value (handler error source)]
                     (if (observable? value)
                      value
                      rx/EMPTY)))))

(defn catch
  "Continues an observable sequence that is terminated
  by an exception with the next observable sequence."
  ([handler] (catch* handler))
  ([handler ob] (pipe (catch* handler) ob))
  ([pred handler ob]
   (pipe (catch* (fn [value]
                   (if (pred value)
                     (handler value)
                     (throw value))))
         ob)))

(defn tap*
  "Invokes an action for each element in the
  observable sequence.

  (operator only)"
  ([f]
   (if (or (plain-object? f)
           (fn? f))
     (rx/tap f)
     (throw (ex-info "invalid argiments" {:f f}))))
  ([f e]
   (rx/tap #js {:next f :error e :complete noop}))
  ([f e c]
   (rx/tap #js {:next f :error e :complete c})))

(defn tap
  "Invokes an action for each element in the
  observable sequence."
  ([f] (tap* f))
  ([f ob] (pipe (tap* f) ob))
  ([f e ob] (pipe (tap* f e) ob))
  ([f e c ob] (pipe (tap* f e c) ob)))

(defn throttle*
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

(defn throttle
  "Returns an observable sequence that emits only the first item emitted
  by the source Observable during sequential time windows of a
  specified duration."
  ([ms] (throttle* ms))
  ([ms ob] (pipe (throttle* ms) ob))
  ([ms config ob] (pipe (throttle* ms config) ob)))

(def ^function debounce*
  "Emits an item from the source Observable after a
  particular timespan has passed without the Observable
  omitting any other items."
  rx/debunceTime)

(defn debounce
  "Emits an item from the source Observable after a
  particular timespan has passed without the Observable
  omitting any other items."
  ([ms] (debounce* ms))
  ([ms ob] (pipe (debounce* ms) ob)))

(def ^function sample*
  "Samples the observable sequence at each interval."
  rx/sampleTime)

(defn sample
  "Samples the observable sequence at each interval."
  ([ms] (sample* ms))
  ([ms ob] (pipe (sample* ms) ob)))


(def ^function sample-when*
  "Samples the observable sequence at each interval."
  rx/sample)

(defn sample-when
  "Samples the observable sequence at each interval."
  ([other] (sample-when* other))
  ([other ob] (pipe (sample-when* other) ob)))

(def ^function ignore*
  "Ignores all elements in an observable sequence leaving only the
  termination messages."
  rx/ignoreElements)

(defn ignore
  "Ignores all elements in an observable sequence leaving only the
  termination messages."
  ([] (ignore*))
  ([ob] (pipe (ignore*) ob)))

(def ^function finalize*
  "Returns an Observable that mirrors the source Observable, but will
  call a specified function when the source terminates on complete or
  error."
  rx/finalize)

(defn finalize
  "Returns an Observable that mirrors the source Observable, but will
  call a specified function when the source terminates on complete or
  error."
  ([f] (finalize* f))
  ([f ob] (pipe (finalize* f) ob)))

(defn distinct-contiguous*
  "Returns an observable sequence that contains only
  distinct contiguous elements.

  (operator only)"
  ([] (rx/distinctUntilChanged))
  ([comparator-fn]
   (rx/distinctUntilChanged #(comparator-fn %1 %2)))
  ([comparator-fn key-fn]
   (rx/distinctUntilChanged #(comparator-fn %1 %2) #(key-fn %))))

(defn distinct*
  "Returns an observable sequence that contains only distinct
  elements.

  Usage of this operator should be considered carefully due to the
  maintenance of an internal lookup structure which can grow large.

  (operator only)"
  ([] (rx/distinct))
  ([comparator-fn] (rx/distinct #(comparator-fn %1 %2)))
  ([comparator-fn key-fn] (rx/distinct #(comparator-fn %1 %2) #(key-fn %))))

(def ^function buffer*
  "Projects each element of an observable sequence into zero
  or more buffers which are produced based on element count
  information."
  rx/bufferCount)

(defn buffer
  "Projects each element of an observable sequence into zero
  or more buffers which are produced based on element count
  information."
  ([n] (buffer* n))
  ([n ob] (pipe (buffer* n) ob))
  ([n o ob] (pipe (buffer* n o) ob)))

(def ^function buffer-time*
  "Buffers the source Observable values for a specific time period.
  (operator only)"
  rx/bufferTime)

(defn buffer-time
  "Buffers the source Observable values for a specific time period."
  ([ms] (buffer-time* ms))
  ([ms ob] (pipe (buffer-time* ms) ob))
  ([ms start ob] (pipe (buffer-time* ms start) ob))
  ([ms start max ob] (pipe (buffer-time* ms start max) ob)))

(def ^function buffer-until*
  "Buffers the source Observable values until notifier emits."
  rx/buffer)

(defn buffer-until
  "Buffers the source Observable values until notifier emits."
  ([notifier] (buffer-until* notifier))
  ([notifier ob] (pipe (buffer-until* notifier) ob)))

(def ^function retry*
  "Given an optional number of retries and an observable,
  repeats the source observable the specified number of
  times or until it terminates. If no number of retries
  is given, it will be retried indefinitely."
  rx/retry)

(defn retry
  "Given an optional number of retries and an observable,
  repeats the source observable the specified number of
  times or until it terminates. If no number of retries
  is given, it will be retried indefinitely."
  ([ob] (pipe (retry*) ob))
  ([n ob] (pipe (retry* n) ob)))


(defn transform*
  [xform]
  (fn [source]
    (create (fn [subs]
              (let [xsubs (xform
                           (fn
                             ([r] (end! subs) r)
                             ([_ input] (push! subs input) input)))

                    disposable (subscribe source
                                          (fn [input]
                                            (let [v (xsubs nil input)]
                                              (when (reduced? v)
                                                (xsubs @v))))
                                          (fn [cause]
                                            (rx/error! subs cause))
                                          (fn []
                                            (xsubs nil)
                                            (end! subs)))]
                (fn []
                  (dispose! disposable)))))))

(defn transform
  "Transform the observable sequence using transducers."
  [xform ob]
  (pipe (transform* xform) ob))

(defn timeout*
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
                    :with (if (observable? with)
                            #(-> with)
                            with)})))

(defn timeout
  "Returns the source observable sequence or the other
  observable sequence if dueTime elapses."
  ([ms] (timeout* ms))
  ([ms ob] (pipe (timeout* ms) ob))
  ([ms with ob] (pipe (timeout* ms with) ob)))

(def ^function delay*
  "Time shifts the observable sequence by dueTime. The relative
  time intervals between the values are preserved."
  rx/delay)

(defn delay
  "Time shifts the observable sequence by dueTime. The relative
  time intervals between the values are preserved."
  ([ms] (delay* ms))
  ([ms ob] (pipe (delay* ms) ob)))

;; (defn delay-emit
;;   "Time shift the observable but also increase the relative time between emisions."
;;   [ms ob]
;;   (mapcat #(delay ms (of %)) ob))

(def ^function delay-when*
  "Time shifts the observable sequence based on a subscription
  delay and a delay selector function for each element."
  rx/delayWhen)

(defn delay-when
  "Time shifts the observable sequence based on a subscription
  delay and a delay selector function for each element."
  ([sf] (delay-when* sf))
  ([sf ob] (pipe (delay-when* sf) ob))
  ([sf sd ob] (pipe (delay-when* sf sd) ob)))

;; FIXME: define as operator
(defn delay-at-least
  "Time shifts at least `ms` milisseconds."
  [ms ob]
  (pipe (fn [source]
          (->> source
               (combine-latest-with (timer ms))
               (map c/first)))
        ob))

(defn flatten
  "Just like clojure collections flatten but for rx streams. Given a stream
  off collections will emit every value separately"
  [ob]
  (pipe (rx/concatMap #(-> %1)) ob))

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


(defn observe-on*
  ([sch]
   (cond
     (scheduler? sch)
     (rx/observeOn sch)

     (keyword? sch)
     (observe-on* (scheduler sch))

     :else
     (throw (ex-info "invalid arguments" {:sch sch}))))
  ([sch delay]
   (cond
     (scheduler? sch)
     (rx/observeOn sch delay)

     (keyword? sch)
     (observe-on* (scheduler sch) delay)

     :else
     (throw (ex-info "invalid arguments" {:sch sch :delay delay})))))

(defn observe-on
  [sch ob]
  (pipe (observe-on* sch) ob))

(defn subscribe-on*
  ([sch]
   (cond
     (scheduler? sch)
     (rx/subscribeOn sch)

     (keyword? sch)
     (subscribe-on* (scheduler sch))

     :else
     (throw (ex-info "invalid arguments" {:sch sch}))))
  ([sch delay]
   (cond
     (scheduler? sch)
     (rx/subscribeOn sch delay)

     (keyword? sch)
     (subscribe-on* (scheduler sch) delay)

     :else
     (throw (ex-info "invalid arguments" {:sch sch :delay delay})))))

(defn subscribe-on
  [sch ob]
  (pipe (subscribe-on* sch) ob))
