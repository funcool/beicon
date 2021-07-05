(ns beicon.core
  (:refer-clojure :exclude [true? map filter reduce merge repeat first
                            last mapcat repeatedly zip dedupe drop
                            take take-while map-indexed concat empty
                            delay range throw do trampoline subs flatten])
  (:require [beicon.impl.rxjs]
            [beicon.impl.rxjs-operators]
            [cljs.core :as c]))

(def rx js/rxjsMain)
(def rxop js/rxjsOperators)

(def ^:const Observable (.-Observable ^js rx))
(def ^:const Subject (.-Subject ^js rx))
(def ^:const BehaviorSubject (.-BehaviorSubject ^js rx))
(def ^:const Subscriber (.-Subscriber ^js rx))
(def ^:const Observer (.-Subscriber ^js rx))
(def ^:const Disposable (.-Subscription ^js rx))
(def ^:const Scheduler (.-Scheduler ^js rx))

;; --- Interop Helpers

(declare subject?)

(defn to-observable
  "Coerce a object to an observable instance."
  [ob]
  (assert (subject? ob) "`ob` should be a Subject instance")
  (.asObservable ^js ob))

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
    (.getValue ^js self)))

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

(defn create
  "Creates an observable sequence from a specified subscribe method
  implementation."
  [sf]
  {:pre [(fn? sf)]}
  (.create ^js Observable
           (fn [subs]
             (try
               (sf subs)
               (catch :default e
                 (.error subs e))))))

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
  (.next ^js b v))

(defn error!
  "Pushes the given error to the bus stream."
  [b e]
  (.error ^js b e))

(defn end!
  "Ends the given bus stream."
  [b]
  (.complete ^js b))

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
  (pipe ob (.share ^js rxop)))

(defn connect!
  "Connect the connectable observable."
  [ob]
  (.connect ob))

(defn from
  "Creates an observable from js arrays, clojurescript collections, and
  promise instance."
  [v]
  (if (nil? v)
    (.empty rx)
    (.from rx v)))

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
  (.fromEvent rx et ev))

(defn empty
  "Returns an observable sequence that is already
  in end state."
  []
  (.empty rx))

(defn if-empty
  "Emits a given value if the source Observable completes without
  emitting any next value, otherwise mirrors the source Observable."
  [default ob]
  (pipe ob (.defaultIfEmpty ^js rxop default)))

;; Alias
(def default-if-empty if-empty)

(defn throw
  [e]
   (.throwError rx e))

;; Alias
(def error throw)

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
  (let [zip-fn (unchecked-get rx "zip")]
    (apply zip-fn items)))

(defn concat
  "Concatenates all of the specified observable
  sequences, as long as the previous observable
  sequence terminated successfully."
  [& more]
  (let [concat-fn (unchecked-get rx "concat")]
    (apply concat-fn (c/filter some? more))))

(defn merge
  "Merges all the observable sequences and Promises
  into a single observable sequence."
  [& more]
  (let [merge-fn (unchecked-get rx "merge")]
    (apply merge-fn (c/filter some? more))))

(defn merge-all
  "Merges an observable sequence of observable sequences into an
  observable sequence."
  ([ob] (pipe ob (.mergeAll ^js rxop)))
  ([^number concurrency ob]
   (pipe ob (.mergeAll ^js rxop concurrency))))

(defn filter
  "Filters the elements of an observable sequence
  based on a predicate."
  [f ob]
  (pipe ob (.filter ^js rxop #(boolean (f %)))))

(defn map
  "Apply a function to each element of an observable
  sequence."
  [f ob]
  (pipe ob (.map ^js rxop #(f %))))

(defn map-indexed
  [f ob]
  (pipe ob (.map ^js rxop #(f %2 %1))))

(defn merge-map
  "Projects each element of an observable sequence to an observable
  sequence and merges the resulting observable sequences or Promises
  or array/iterable into one observable sequence.

  In other languages is called: flatMap or mergeMap."
  [f ob]
  (pipe ob (.flatMap ^js rxop #(f %))))

(defn switch-map
  [f obj]
  (pipe obj (.switchMap ^js rxop #(f %))))

;; Aliases
(def fmap merge-map)
(def flat-map merge-map)

(defn mapcat
  "Projects each element of an observable sequence to an observable
  sequence and concatenates the resulting observable sequences or
  Promises or array/iterable into one observable sequence."
  [f ob]
  (pipe ob (.concatMap ^js rxop #(f %))))

(defn mapcat-indexed
  [f ob]
  (pipe ob (.concatMap ^js rxop #(f %2 %1))))

(defn concat-all
  [ob]
  (pipe ob (.concatAll ^js rxop)))

(defn skip
  "Bypasses a specified number of elements in an
  observable sequence and then returns the remaining
  elements."
  [n ob]
  (pipe ob (.skip ^js rxop (int n))))

(defn skip-while
  "Bypasses elements in an observable sequence as long
  as a specified condition is true and then returns the
  remaining elements."
  [f ob]
  (pipe ob (.skipWhile ^js rxop #(boolean (f %)))))

(defn skip-until
  "Returns the values from the source observable sequence only after the
  other observable sequence produces a value."
  [pob ob]
  (pipe ob (.skipUntil ^js rxop pob)))

(defn skip-last
  "Skip a specified number of values before the completion of an observable."
  [n ob]
  (.pipe ob (.skipLast ^js rxop (int n))))

(defn take
  "Bypasses a specified number of elements in an observable sequence and
  then returns the remaining elements."
  [n ob]
  (pipe ob (.take ^js rxop n)))

(defn first
  "Return an observable that only has the first value of the provided
  observable. You can optionally pass a predicate and default value."
  ([ob] (pipe ob (.first ^js rxop)))
  ([f ob] (pipe ob (.first ^js rxop #(boolean (f %)))))
  ([f default ob] (pipe ob (.first ^js rxop #(boolean (f %)) default))))

(defn last
  "Return an observable that only has the last value of the provided
  observable. You can optionally pass a predicate and default value."
  ([ob] (pipe ob (.last ^js rxop)))
  ([f ob] (pipe ob (.last ^js rxop #(boolean (f %)))))
  ([f default ob] (pipe ob (.last ^js rxop #(boolean (f %)) default))))

(defn take-while
  "Returns elements from an observable sequence as long as a specified
  predicate returns true."
  [f ob]
  (pipe ob (.takeWhile ^js rxop #(boolean (f %)))))

(defn take-until
  "Returns the values from the source observable sequence until the
  other observable sequence or Promise produces a value."
  [other ob]
  (pipe ob (.takeUntil ^js rxop other)))

(defn reduce
  "Applies an accumulator function over an observable sequence,
  returning the result of the aggregation as a single element in the
  result sequence."
  ([f ob] (pipe ob (.reduce ^js rxop #(f %1 %2))))
  ([f seed ob] (pipe ob (.reduce ^js rxop #(f %1 %2) seed))))

(defn scan
  "Applies an accumulator function over an observable sequence and
  returns each intermediate result.  Same as reduce but with
  intermediate results"
  ([f ob] (pipe ob (.scan ^js rxop #(f %1 %2))))
  ([f seed ob] (pipe ob (.scan ^js rxop #(f %1 %2) seed))))

(defn merge-scan
  "Applies an accumulator function over the source Observable where
  the accumulator function itself returns an Observable, then each
  intermediate Observable returned is merged into the output
  Observable."
  [f seed ob]
  (let [merge-scan-fn (unchecked-get rxop "mergeScan")]
    (pipe ob (merge-scan-fn #(f %1 %2) seed))))

(defn expand
  "Recursively projects each source value to an Observable
  which is merged in the output Observable."
  [f ob]
  (pipe ob (.expand ^js rxop f)))

(defn with-latest
  "Merges the specified observable sequences into one observable
  sequence by using the selector function only when the source
  observable sequence (the instance) produces an element."
  {:deprecated true}
  [f other source]
  (pipe source (.withLatestFrom ^js rxop other f)))

(defn with-latest-from
  "Merges the specified observable sequences into one observable
  sequence by using the selector function only when the source
  observable sequence (the instance) produces an element."
  ([other source]
   (let [wlf (.-withLatestFrom ^js rxop)
         cmb (cond
               (observable? other) (wlf other)
               (array? other)      (.apply wlf nil other)
               (sequential? other) (apply wlf other)
               :else               (throw (ex-info "Invalid argument" {:type ::invalid-argument})))]
     (pipe source cmb)))
  ([o1 o2 source]
   (pipe source (.withLatestFrom ^js rxop o1 o2)))
  ([o1 o2 o3 source]
   (pipe source (.withLatestFrom ^js rxop o1 o2 o3)))
  ([o1 o2 o3 o4 source]
   (pipe source (.withLatestFrom ^js rxop o1 o2 o3 o4)))
  ([o1 o2 o3 o4 o5 source]
   (pipe source (.withLatestFrom ^js rxop o1 o2 o3 o4 o5)))
  ([o1 o2 o3 o4 o5 o6 source]
   (pipe source (.withLatestFrom ^js rxop o1 o2 o3 o4 o5 o6))))

(defn combine-latest
  "Combines multiple Observables to create an Observable whose values
  are calculated from the latest values of each of its input
  Observables (constructor)."
  ([obs]
   (cond
     (array? obs)
     (.combineLatest rx obs)

     (sequential? obs)
     (.combineLatest rx (into-array obs))

     :else
     (throw (ex-info "Invalid argument" {:type ::invalid-argument}))))
  ([o1 o2]
   (.combineLatest rx o1 o2))
  ([o1 o2 o3]
   (.combineLatest rx o1 o2 o3))
  ([o1 o2 o3 o4]
   (.combineLatest rx o1 o2 o3 o4))
  ([o1 o2 o3 o4 & other]
   (combine-latest (into-array (into [o1 o2 o3 o4] other)))))

(defn combine-latest-with
  "Combines multiple Observables to create an Observable whose values
  are calculated from the latest values of each of its input
  Observables (operator)."
  [other ob]
  (pipe ob (.combineLatestWith ^js rxop other)))

(defn catch
  "Continues an observable sequence that is terminated
  by an exception with the next observable sequence."
  ([handler ob]
   (pipe ob (.catchError ^js rxop
                         (fn [error source]
                           (let [value (handler error source)]
                             (if (observable? value)
                               value
                               (empty)))))))
  ([pred handler ob]
   (catch (fn [value]
            (if (pred value)
              (handler value)
              (throw value)))
       ob)))

(defn tap
  "Invokes an action for each element in the
  observable sequence."
  ([f ob] (pipe ob (.tap ^js rxop f)))
  ([f g ob] (pipe ob (.tap ^js rxop f g)))
  ([f g e ob] (pipe ob (.tap ^js rxop f g e))))

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
  "Returns an observable sequence that emits only the first item emitted
  by the source Observable during sequential time windows of a
  specified duration."
  ([ms ob]
   (pipe ob (.throttleTime ^js rxop ms)))
  ([ms config ob]
   (let [{:keys [leading trailing]
          :or {leading true trailing false}} config]
     (pipe ob (.throttleTime ^js rxop ms #js {:leading leading :trailing trailing})))))

(defn debounce
  "Emits an item from the source Observable after a
  particular timespan has passed without the Observable
  omitting any other items."
  [ms ob]
  (pipe ob (.debounceTime ^js rxop ms)))

(defn sample
  "Samples the observable sequence at each interval."
  [ms ob]
  (pipe ob (.sampleTime ^js rxop ms)))

(defn sample-when
  "Samples the observable sequence at each interval."
  [other ob]
  (pipe ob (.sample ^js rxop other)))

(defn ignore
  "Ignores all elements in an observable sequence leaving only the
  termination messages."
  [ob]
  (pipe ob (.ignoreElements ^js rxop)))

(defn finalize
  "Returns an Observable that mirrors the source Observable, but will
  call a specified function when the source terminates on complete or
  error."
  [f ob]
  (pipe ob (.finalize ^js rxop #(f))))

(defn dedupe
  "Returns an observable sequence that contains only
  distinct contiguous elements."
  ([ob] (dedupe identity ob))
  ([f ob] (pipe ob (.distinctUntilChanged ^js rxop identical? f))))

(defn dedupe'
  "Returns an observable sequence that contains only d istinct
  elements.

  Usage of this operator should be considered carefully due to the
  maintenance of an internal lookup structure which can grow large."
  ([ob] (pipe ob (.distinct ^js rxop identical?)))
  ([f ob] (pipe ob (.distinct ^js rxop identical? f))))

(defn buffer
  "Projects each element of an observable sequence into zero
  or more buffers which are produced based on element count
  information."
  ([n ob] (pipe ob (.bufferCount ^js rxop n)))
  ([n skip ob] (pipe ob (.bufferCount ^js rxop n skip))))

(defn buffer-time
  "Buffers the source Observable values for a specific time period."
  ([ms ob] (pipe ob (.bufferTime ^js rxop ms)))
  ([ms start ob] (pipe ob (.bufferTime ^js rxop ms start)))
  ([ms start max ob] (pipe ob (.bufferTime ^js rxop ms start max))))

(defn buffer-until
  "Buffers the source Observable values until notifier emits."
  [notifier ob]
  (pipe ob (.buffer ^js rxop notifier)))

(defn retry
  "Given an optional number of retries and an observable,
  repeats the source observable the specified number of
  times or until it terminates. If no number of retries
  is given, it will be retried indefinitely."
  ([ob] (pipe ob (.retry ^js rxop)))
  ([n ob] (pipe ob (.retry ^js rxop ^long n))))

(defn transform
  "Transform the observable sequence using transducers."
  [xform ob]
  (when-not (observable? ob)
    (throw (ex-info "Only observables are supported" {})))
  (letfn [(subs-step [subs]
            (fn
              ([r] (end! subs) r)
              ([_ input] (push! subs input) input)))]
    (create (fn [subs]
              (let [xsubs (xform (subs-step subs))
                    step  (fn [input]
                            (let [v (xsubs nil input)]
                              (when (reduced? v)
                                (xsubs @v))))
                    disposable (on-value ob step)]
                (on-complete ob #(do (xsubs nil)
                                     (end! subs)))
                (fn []
                  (dispose! disposable)))))))


(defn timer
  "Returns an observable sequence that produces a value after
  `ms` has elapsed and then after each period."
  ([delay] (.timer rx delay))
  ([delay period] (.timer rx delay period)))

(defn timeout
  "Returns the source observable sequence or the other
  observable sequence if dueTime elapses."
  ([ms ob] (pipe ob (.timeout ^js rxop ms)))
  ([ms other ob] (pipe ob (.timeoutWith ^js rxop ms other))))

(defn delay
  "Time shifts the observable sequence by dueTime. The relative
  time intervals between the values are preserved."
  [ms ob]
  (pipe ob (.delay ^js rxop ms)))

(defn delay-emit
  "Time shift the observable but also increase the relative time between emisions."
  [ms ob]
  (mapcat #(delay ms (of %)) ob))

(defn delay-when
  "Time shifts the observable sequence based on a subscription
  delay and a delay selector function for each element."
  ([sf ob] (pipe ob (.delayWhen ^js rxop sf)))
  ([sd sf ob] (pipe ob (.delayWhen ^js rxop sf sd))))

(defn delay-at-least
  "Time shifts at least `ms` milisseconds."
  [ms ob]
  (pipe ob (fn [source]
             (->> source
                  (combine-latest-with (timer ms))
                  (map c/first)))))

(defn interval
  "Returns an observable sequence that produces a
  value after each period."
  [ms]
  (.interval rx ms))

(defn flatten
  "Just like clojure collections flatten but for rx streams. Given a stream
  off collections will emit every value separately"
  [ob]
  (pipe ob (.concatMap ^js rxop identity)))

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
    (pipe ob (.observeOn ^js rxop schd))

    (keyword? schd)
    (pipe ob (.observeOn ^js rxop (scheduler schd)))

    :else
    (throw (ex-info "Invalid argument" {:type ::invalid-argument}))))

(defn subscribe-on
  [schd ob]
  (cond
    (scheduler? schd)
    (pipe ob (.subscribeOn ^js rxop schd))

    (keyword? schd)
    (pipe ob (.subscribeOn ^js rxop (scheduler schd)))

    :else
    (throw (ex-info "Invalid argument" {:type ::invalid-argument}))))
