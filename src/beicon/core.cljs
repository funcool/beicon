(ns beicon.core
  (:refer-clojure :exclude [true? map filter reduce merge repeat first
                            last mapcat repeatedly zip dedupe drop
                            take take-while map-indexed concat empty
                            delay range throw do trampoline subs])
  (:require [beicon.impl.rxjs]
            [beicon.impl.rxjs-operators]
            [cljs.core :as c]))


(def rx js/rxjsMain)
(def rxop js/rxjsOperators)

(def ^:const Observable (.-Observable rx))
(def ^:const Subject (.-Subject rx))
(def ^:const BehaviorSubject (.-BehaviorSubject rx))
(def ^:const Subscriber (.-Subscriber rx))
(def ^:const Observer (.-Subscriber rx))
(def ^:const Disposable (.-Subscription rx))
(def ^:const Scheduler (.-Scheduler rx))

;; --- Interop Helpers

(declare subject?)

(defn to-observable
  "Coerce a object to an observable instance."
  [ob]
  (assert (subject? ob) "`ob` should be a Subject instance")
  (.asObservable ^Subject ob))

(def ^:private noop
  (constantly nil))

(defn pipe
  ([ob f]
   (.pipe ob f))
  ([ob f1 f2]
   (.pipe ob f1 f2))
  ([ob f1 f2 f3]
   (.pipe ob f1 f2 f3))
  ([ob f1 f2 f3 f4]
   (.pipe ob f1 f2 f3 f4))
  ([ob f1 f2 f3 f4 & rest]
   (let [pipe' (.-pipe ob)]
     (apply pipe' ob f1 f2 f4 rest))))

;; --- Predicates

(extend-type BehaviorSubject
  IDeref
  (-deref [self]
    (.getValue ^BehaviorSubject self)))

(defn observable?
  "Return true if `ob` is a instance
  of Rx.Observable."
  [ob]
  (instance? Observable ob))

(defn disposable?
  "Check if the provided object is disposable (jvm) or subscription (js)."
  [v]
  (instance? Disposable v))

(defn scheduler?
  "Check if the provided value is Scheduler instance."
  [v]
  (instance? Scheduler v))

(defn subject?
  "Check if the provided value is Subject instance."
  [b]
  (instance? Subject b))

;; --- Observables Constructor

(defprotocol IObservableValue
  (-end? [_] "Returns true if is end value.")
  (-error? [_] "Returns true if is end value.")
  (-next? [_] "Returns true if is end value."))

(def end
  "Mark a value as a final value of the stream."
  cljs.core/reduced)

(do
  (extend-type default
    IObservableValue
    (-next? [_] true)
    (-error? [_] false)
    (-end? [_] false))

  (extend-type cljs.core.Reduced
    IObservableValue
    (-next? [_] false)
    (-error? [_] false)
    (-end? [_] false))

  (extend-type js/Error
    IObservableValue
    (-next? [_] false)
    (-error? [_] true)
    (-end? [_] false))

  (extend-type cljs.core.ExceptionInfo
    IObservableValue
    (-next? [_] false)
    (-error? [_] true)
    (-end? [_] false)))

(defn create
  "Creates an observable sequence from a specified subscribe method
  implementation."
  [sf]
  {:pre [(fn? sf)]}
  (letfn [(sink [subs v]
            (cond
              (identical? end v) (.complete subs)
              (-next? v) (.next subs v)
              (-error? v) (.error subs v)
              (-end? v) (.complete subs)
              (reduced? v) (do
                             (sink subs @v)
                             (.complete subs))))]
    (.create ^js Observable
             (fn [subs]
               (try
                 (sf (partial sink subs))
                 (catch js/Error e
                   (.error subs e)))))))

;; --- Observable Subscription

(defprotocol ISubscriber
  "Backpressure aware subscriber abstraction."
  (-on-init [_ s] "Subscription initialization hook.")
  (-on-next [_ s value] "Subscription data notification hook.")
  (-on-error [_ error] "Subscription error notification hook.")
  (-on-end [_] "Subscription termination notification hook."))

(defprotocol IDisposable
  (-dispose [_] "dispose resources."))

(defn dispose!
  "Dispose resources acquired by the subscription."
  [v]
  (-dispose v))

;; Alias
(def unsub! dispose!)
(def cancel! dispose!)

(defn- wrap-disposable
  [disposable]
  (specify! disposable
    IFn
    (-invoke ([this] (-dispose this)))
    IDisposable
    (-dispose [this]
      (.unsubscribe this))))

(defn subscribe-with
  "Subscribes an observer or subscriber to the observable sequence."
  [ob observer]
  {:pre [(subject? observer)]}
  (wrap-disposable (.subscribe ob observer)))

(defn subscribe
  "Subscribes an observer to the observable sequence."
  ([ob nf] (subscribe ob nf nil nil))
  ([ob nf ef] (subscribe ob nf ef nil))
  ([ob nf ef cf]
   (let [observer (.create ^js Subscriber
                           (if (fn? nf) nf noop)
                           (if (fn? ef) ef noop)
                           (if (fn? cf) cf noop))]
     (wrap-disposable (.subscribe ob observer)))))

(defn subs
  "A specialized version of `subscribe` with inverted arguments."
  ([nf ob] (subscribe ob nf nil nil))
  ([nf ef ob] (subscribe ob nf ef nil))
  ([nf ef cf ob] (subscribe ob nf ef cf)))

;; Alias
(def sub! subscribe)

(defn on-value
  "Subscribes a function to invoke for each element
  in the observable sequence."
  [ob f]
  (subscribe ob f nil nil))

(def on-next
  "A semantic alias for `on-value`."
  on-value)

(defn on-error
  "Subscribes a function to invoke upon exceptional termination
  of the observable sequence."
  [ob f]
  (subscribe ob nil f nil))

(defn on-end
  "Subscribes a function to invoke upon graceful termination
  of the observable sequence."
  [ob f]
  (subscribe ob nil nil f))

(def on-complete
  "A semantic alias for `on-end`."
  on-end)

;; --- Bus / Subject

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

(defn push!
  "Pushes the given value to the bus stream."
  [b v]
  (.next b v))

(defn error!
  "Pushes the given error to the bus stream."
  [b e]
  (.error b e))

(defn end!
  "Ends the given bus stream."
  [b]
  (.complete b))

;; --- Observable Factory Helpers

(defn range
  "Generates an observable sequence that repeats the
  given element."
  ([b] (range 0 b))
  ([a b] (.range rx a b)))

(defn publish
  "Create a connectable (hot) observable
  from other observable."
  [ob]
  (.publish ob))

(defn share
  "Returns an observable sequence that shares a single
  subscription to the underlying sequence."
  [ob]
  (pipe ob (.share rxop)))

(defn connect!
  "Connect the connectable observable."
  [ob]
  (.connect ob))

(defn from
  "Creates an observable from js arrays, clojurescript collections, and
  promise instance."
  [v]
  (.from rx v))

(defn from-atom
  [atm]
  (create (fn [sink]
            (let [key (keyword (gensym "beicon"))]
              (add-watch atm key (fn [_ _ _ val]
                                   (sink val)))
              (fn []
                (remove-watch atm key))))))

(defn from-event
  "Creates an Observable by attaching an event listener to an event target"
  [et ev]
  (.fromEvent rx et ev))

(defn empty
  "Returns an observable sequence that is already
  in end state."
  []
  (.empty rx))

(defn throw
  [e]
   (.throwError rx e))

;; Alias
(def error throw)

(defn timer
  "Returns an observable sequence that produces a value after
  `ms` has elapsed and then after each period."
  ([delay] (.timer rx delay))
  ([delay period] (.timer rx delay period)))

(defn timeout
  "Returns the source observable sequence or the other
  observable sequence if dueTime elapses."
  ([ms ob] (pipe ob (.timeout rxop ms)))
  ([ms other ob] (pipe ob (.timeoutWith rxop ms other))))

(defn delay
  "Time shifts the observable sequence by dueTime. The relative
  time intervals between the values are preserved."
  [ms ob]
  (pipe ob (.delay rxop ms)))

(defn delay-when
  "Time shifts the observable sequence based on a subscription
  delay and a delay selector function for each element."
  ([sf ob] (pipe ob (.delayWhen rxop sf)))
  ([sd sf ob] (pipe ob (.delayWhen rxop sf sd))))

(defn interval
  "Returns an observable sequence that produces a
  value after each period."
  [ms]
  (.interval rx ms))

(defn fjoin
  "Runs all observable sequences in parallel and collect their last
  elements."
  [& items]
  (let [[selector items] (if (ifn? (c/first items))
                           [(c/first items) (rest items)]
                           [vector items])
        items (if (vector? items) items (into [] items))]
    (apply (.-forkJoin rx) (conj items selector))))

;; Alias
(def fork-join fjoin)

(defn of
  "Converts arguments to an observable sequence."
  ([a] (.of rx a))
  ([a b] (.of rx a b))
  ([a b c] (.of rx a b c))
  ([a b c d] (.of rx a b c d))
  ([a b c d e] (.of rx a b c d e))
  ([a b c d e f] (.of rx a b c d e f))
  ([a b c d e f & more] (apply (.-of rx) a b c d e f more)))

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
   (let [disposable (on-value ob #(reset! a %))]
     (disposable-atom a disposable)))
  ([ob a f]
   (let [disposable (on-value ob #(swap! a f %))]
     (disposable-atom a disposable))))

;; --- Observable Transformations

(defn race
  "Create an observable that surfaces any of the given
  sequences, whichever reacted first."
  ([a b] (.race rx a b))
  ([a b & more] (apply (.-race rx) a b more)))

(defn zip
  "Merges the specified observable sequences or Promises (cljs) into one
  observable sequence."
  [& items]
  (let [[selector items] (if (ifn? (c/first items))
                           [(c/first items) (rest items)]
                           [vector items])
        items (if (vector? items) items (vec items))]
    (apply (.-zip rx) (conj items selector))))

(defn concat
  "Concatenates all of the specified observable
  sequences, as long as the previous observable
  sequence terminated successfully."
  [& more]
  (let [more (cljs.core/filter identity more)]
    (apply (.-concat rx) more)))

(defn merge
  "Merges all the observable sequences and Promises
  into a single observable sequence."
  [& more]
  (let [more (cljs.core/filter identity more)]
    (apply (.-merge rx) more)))

(defn merge-all
  "Merges an observable sequence of observable sequences into an
  observable sequence."
  ([ob] (pipe ob (.mergeAll rxop)))
  ([^number concurrency ob]
   (pipe ob (.mergeAll rxop concurrency))))

(defn filter
  "Filters the elements of an observable sequence
  based on a predicate."
  [f ob]
  (pipe ob (.filter rxop #(boolean (f %)))))

(defn map
  "Apply a function to each element of an observable
  sequence."
  [f ob]
  (pipe ob (.map rxop #(f %))))

(defn map-indexed
  [f ob]
  (pipe ob (.map rxop #(f %2 %1))))

(defn merge-map
  "Projects each element of an observable sequence to an observable
  sequence and merges the resulting observable sequences or Promises
  or array/iterable into one observable sequence.

  In other languages is called: flatMap or mergeMap."
  [f ob]
  (pipe ob (.flatMap rxop #(f %))))

(defn switch-map
  [f obj]
  (pipe obj (.switchMap rxop #(f %))))

;; Aliases
(def fmap merge-map)
(def flat-map merge-map)

(defn mapcat
  "Projects each element of an observable sequence to an observable
  sequence and concatenates the resulting observable sequences or
  Promises or array/iterable into one observable sequence."
  [f ob]
  (pipe ob (.concatMap rxop #(f %))))

(defn mapcat-indexed
  [f ob]
  (pipe ob (.concatMap rxop #(f %2 %1))))

(defn concat-all
  [ob]
  (pipe ob (.concatAll rxop)))

(defn skip
  "Bypasses a specified number of elements in an
  observable sequence and then returns the remaining
  elements."
  [n ob]
  (pipe ob (.skip rxop (int n))))

(defn skip-while
  "Bypasses elements in an observable sequence as long
  as a specified condition is true and then returns the
  remaining elements."
  [f ob]
  (pipe ob (.skipWhile rxop #(boolean (f %)))))

(defn skip-until
  "Returns the values from the source observable sequence only after the
  other observable sequence produces a value."
  [pob ob]
  (pipe ob (.skipUntil rxop pob)))

(defn take
  "Bypasses a specified number of elements in an observable sequence and
  then returns the remaining elements."
  [n ob]
  (pipe ob (.take rxop n)))

(defn first
  "Return an observable that only has the first value of the provided
  observable. You can optionally pass a predicate and default value."
  ([ob] (pipe ob (.first rxop)))
  ([f ob] (pipe ob (.first rxop #(boolean (f %)))))
  ([f default ob] (pipe ob (.first rxop #(boolean (f %)) default))))

(defn last
  "Return an observable that only has the last value of the provided
  observable. You can optionally pass a predicate and default value."
  ([ob] (pipe ob (.last rxop)))
  ([f ob] (pipe ob (.last rxop #(boolean (f %)))))
  ([f default ob] (pipe ob (.last rxop #(boolean (f %)) default))))

(defn take-while
  "Returns elements from an observable sequence as long as a specified
  predicate returns true."
  [f ob]
  (pipe ob (.takeWhile rxop #(boolean (f %)))))

(defn take-until
  "Returns the values from the source observable sequence until the
  other observable sequence or Promise produces a value."
  [other ob]
  (pipe ob (.takeUntil rxop other)))

(defn reduce
  "Applies an accumulator function over an observable sequence,
  returning the result of the aggregation as a single element in the
  result sequence."
  ([f ob] (pipe ob (.reduce rxop #(f %1 %2))))
  ([f seed ob] (pipe ob (.reduce rxop #(f %1 %2) seed))))

(defn scan
  "Applies an accumulator function over an observable sequence and
  returns each intermediate result.  Same as reduce but with
  intermediate results"
  ([f ob] (pipe ob (.scan rxop #(f %1 %2))))
  ([f seed ob] (pipe ob (.scan rxop #(f %1 %2) seed))))

(defn with-latest
  "Merges the specified observable sequences into one observable
  sequence by using the selector function only when the source
  observable sequence (the instance) produces an element."
  [f other source]
  (pipe source (.withLatestFrom rxop other f)))

(defn combine-latest
  "Combines multiple Observables to create an Observable whose values
  are calculated from the latest values of each of its input
  Observables."
  ([other ob]
   (combine-latest vector other ob))
  ([f other ob]
   (pipe ob (.combineLatest rxop other f))))

(defn catch
  "Continues an observable sequence that is terminated
  by an exception with the next observable sequence."
  ([handler ob]
   (pipe ob (.catchError rxop
                         (fn [error source]
                           (let [value (handler error source)]
                             (cond
                               (observable? value) value
                               (nil? value) (empty)
                               (-end? value) (empty)
                               (-error? value) (throw value)
                               (-next? value) (of value)))))))
  ([pred handler ob]
   (catch (fn [value]
            (if (pred value)
              (handler value)
              (throw value)))
       ob)))

(defn tap
  "Invokes an action for each element in the
  observable sequence."
  ([f ob] (pipe ob (.tap rxop f)))
  ([f g ob] (pipe ob (.tap rxop f g)))
  ([f g e ob] (pipe ob (.tap rxop f g e))))

;; Aliases
(def do tap)

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
  (pipe ob (.throttleTime rxop ms)))

(defn debounce
  "Emits an item from the source Observable after a
  particular timespan has passed without the Observable
  omitting any other items."
  [ms ob]
  (pipe ob (.debounceTime rxop ms)))

(defn sample
  "Samples the observable sequence at each interval."
  [ms ob]
  (pipe ob (.sampleTime rxop ms)))

(defn sample-when
  "Samples the observable sequence at each interval."
  [other ob]
  (pipe ob (.sample rxop other)))

(defn ignore
  "Ignores all elements in an observable sequence leaving only the
  termination messages."
  [ob]
  (pipe ob (.ignoreElements rxop)))

(defn finalize
  "Returns an Observable that mirrors the source Observable, but will
  call a specified function when the source terminates on complete or
  error."
  [f ob]
  (pipe ob (.finalize rxop #(f))))

(defn dedupe
  "Returns an observable sequence that contains only
  distinct contiguous elements."
  ([ob] (dedupe identity ob))
  ([f ob] (pipe ob (.distinctUntilChanged rxop identical? f))))

(defn dedupe'
  "Returns an observable sequence that contains only d istinct
  elements.

  Usage of this operator should be considered carefully due to the
  maintenance of an internal lookup structure which can grow large."
  ([ob] (pipe ob (.distinct rxop identical?)))
  ([f ob] (pipe ob (.distinct rxop identical? f))))

(defn buffer
  "Projects each element of an observable sequence into zero
  or more buffers which are produced based on element count
  information."
  ([n ob] (pipe ob (.bufferCount rxop n)))
  ([n skip ob] (pipe ob (.bufferCount rxop n skip))))

(defn buffer-time
  "Buffers the source Observable values for a specific time period."
  ([ms ob] (pipe ob (.bufferTime rxop ms)))
  ([ms start ob] (pipe ob (.bufferTime rxop ms start)))
  ([ms start max ob] (pipe ob (.bufferTime rxop ms start max))))

(defn buffer-until
  "Buffers the source Observable values until notifier emits."
  [notifier ob] (pipe ob (.buffer rxop notifier)))

(defn retry
  "Given an optional number of retries and an observable,
  repeats the source observable the specified number of
  times or until it terminates. If no number of retries
  is given, it will be retried indefinitely."
  ([ob] (pipe ob (.retry rxop)))
  ([n ob] (pipe ob (.retry rxop ^long n))))

(defn transform
  "Transform the observable sequence using transducers."
  [xform ob]
  (when-not (observable? ob)
    (throw (ex-info "Only observables are supported" {})))
  (letfn [(sink-step [sink]
            (fn
              ([r] (sink end) r)
              ([_ input] (sink input) input)))]
    (create (fn [sink]
              (let [xsink (xform (sink-step sink))
                    step (fn [input]
                           (let [v (xsink nil input)]
                             (when (reduced? v)
                               (xsink @v))))
                    disposable (on-value ob step)]
                (on-complete ob #(do (xsink nil)
                                     (sink end)))
                (fn []
                  (cancel! disposable)))))))

;; --- Schedulers

(defn scheduler
  "Get the scheduler instance by type. The posible types are: `:asap`,
  `:async`, `:queue`.  Old `:trampoline` type is renamed as `:queue`
  and is deprecated."
  [type]
  (case type
    :asap (.-asapScheduler rx)
    :async (.-asyncScheduler rx)
    :queue (.-queueScheduler rx)
    :af (.-animationFrameScheduler rx)
    :animation-frame (.-animationFrameScheduler rx)))

(defn observe-on
  [schd ob]
  (cond
    (scheduler? schd)
    (pipe ob (.observeOn rxop schd))

    (keyword? schd)
    (pipe ob (.observeOn rxop (scheduler schd)))

    :else
    (throw (ex-info "Invalid argument" {:type ::invalid-argument}))))

(defn subscribe-on
  [schd ob]
  (cond
    (scheduler? schd)
    (pipe ob (.subscribeOn rxop schd))

    (keyword? schd)
    (pipe ob (.subscribeOn rxop (scheduler schd)))

    :else
    (throw (ex-info "Invalid argument" {:type ::invalid-argument}))))
