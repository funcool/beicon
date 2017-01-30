(ns beicon.core
  (:refer-clojure :exclude [true? map filter reduce merge repeat mapcat
                            repeatedly zip dedupe drop take take-while
                            concat empty delay range throw do trampoline])
  #?(:cljs (:require [beicon.extern.rxjs]))
  #?(:clj  (:import rx.Observable
                    rx.Observer
                    rx.Scheduler
                    rx.Subscriber
                    rx.Subscription
                    rx.Observable$OnSubscribe
                    rx.schedulers.Schedulers
                    rx.subjects.Subject
                    rx.subjects.PublishSubject
                    rx.subjects.BehaviorSubject
                    rx.observables.AsyncOnSubscribe
                    rx.subscriptions.Subscriptions
                    rx.functions.Action0
                    rx.functions.Action1
                    rx.functions.Func1
                    rx.functions.Func2
                    rx.functions.FuncN
                    java.util.concurrent.Future
                    java.util.concurrent.TimeUnit)))


;; --- Interop Helpers

#?(:clj
   (do
     (defn- rxfn1
       ^Func1 [f]
       (reify Func1
         (call [_ v]
           (f v))))
     (defn- rxfn2
       ^Func2 [f]
       (reify Func2
         (call [_ a b]
           (f a b))))

     (defn- rxfnn
       ^FuncN [f]
       (reify FuncN
         (call [_ objs]
           (apply f objs))))

     (defn- rxaction0
       ^Action0 [f]
       (reify Action0
         (call [_]
           (f))))

     (defn- rxaction1
       ^Action1 [f]
       (reify Action1
         (call [_ a]
           (f a))))))

;; --- Predicates

#?(:cljs
   (do
     (def ^:const Observable js/Rx.Observable)
     (def ^:const Subject js/Rx.Subject)
     (def ^:const BehaviorSubject js/Rx.BehaviorSubject)
     (def ^:const Subscriber js/Rx.Subscriber)
     (def ^:const Observer js/Rx.Subscriber)
     (def ^:const Subscription js/Rx.Subscription)
     (def ^:const Scheduler js/Rx.Scheduler)))

(defn observable?
  "Return true if `ob` is a instance
  of Rx.Observable."
  [ob]
  (instance? Observable ob))

(defn subject?
  "Return true if `b` is a Subject instance."
  [b]
  (instance? Subject b))

(defn observer?
  "Returns true if `o` is an Observer instance."
  [o]
  (instance? Observer o))

(defn bus?
  "Deprecated alias for `subject?`."
  [b]
  (instance? Subject b))

;; --- Observables Constructors

(defprotocol IObservableValue
  (-end? [_] "Returns true if is end value.")
  (-error? [_] "Returns true if is end value.")
  (-next? [_] "Returns true if is end value."))

(def end
  "Mark a value as a final value of the stream."
  #?(:cljs cljs.core/reduced
     :clj  clojure.core/reduced))

#?(:cljs
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
   :clj
   (do
     (extend-type Object
       IObservableValue
       (-next? [_] true)
       (-error? [_] false)
       (-end? [_] false))

     (extend-type clojure.lang.Reduced
       IObservableValue
       (-next? [_] false)
       (-error? [_] false)
       (-end? [_] false))

     (extend-type Throwable
       IObservableValue
       (-next? [_] false)
       (-error? [_] true)
       (-end? [_] false))))

#?(:cljs
   (defn create
     "Creates an observable sequence from a specified
     subscribe method implementation."
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
                                (.complete subs))))
             (factory [subs]
               (try
                 (sf (partial sink subs))
                 (catch js/Error e
                   (.error subs e))))]
       (Observable. factory))))


#?(:clj
   (defn- from-function
     [sf]
     (letfn [(sink [^Subscriber subs v]
               (cond
                 (identical? end v) (.onCompleted subs)
                 (-next? v) (.onNext subs v)
                 (-error? v) (.onError subs v)
                 (-end? v) (.onCompleted subs)
                 (reduced? v) (do
                                (sink subs @v)
                                (.onCompleted subs))))
             (factory [^Subscriber subr]
               (try
                 (let [dispose (sf (partial sink subr))
                       subs (Subscriptions/create
                             (rxaction0 #(when (ifn? dispose)
                                           (dispose))))]
                   (.add subr subs))
                 (catch Exception e
                   (.onError subr e))))]
       (Observable/create (reify Observable$OnSubscribe
                            (call [_ v]
                              (factory v)))))))

#?(:clj
   (defn create
     "Creates an observable sequence from a specified
     subscribe method implementation."
     [sf]
     (from-function sf)))

(defn range
  "Generates an observable sequence that repeats the
  given element."
  ([b]
   (range 0 b))
  ([a b]
   {:pre [(number? a) (number? b)]}
   #?(:cljs (.range Observable a b)
      :clj (Observable/range a b))))

(defn publish
  "Create a connectable (hot) observable
  from other observable.

  WARNING: the arity 2 is deprecated, if you
  want to connect, just use `connect!` function."
  ([^Observable ob]
   (.publish ob))
  ([^Observable ob connect?]
   {:pre [(observable? ob)]}
   (let [ob' (.publish ob)]
     (when connect?
       (.connect ob'))
     ob')))

(defn share
  "Returns an observable sequence that shares a single
  subscription to the underlying sequence."
  [^Observable ob]
  {:pre [(observable? ob)]}
  (.share ob))

(defn connect!
  "Connect the connectable observable."
  [^Observable ob]
  {:pre [(observable? ob)]}
  (.connect ob))

(defn from-coll
  "Generates an observable sequence from collection."
  [coll]
  #?(:cljs (let [array (into-array coll)]
             (.from Observable array))
     :clj  (Observable/from ^Iterable coll)))

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
  #?(:cljs (.fromPromise Observable p)
     :clj (Observable/from ^Future p)))

(defn just
  "Returns an observable sequence that contains
  a single element."
  [v]
  #?(:cljs (.of Observable v)
     :clj (Observable/just v)))

(defn once
  "An alias to `just`."
  [v]
  (just v))

(defn empty
  "Returns an observable sequence that is already
  in end state."
  []
  #?(:cljs (.empty Observable)
     :clj  (Observable/empty)))

(def never
  "Alias to 'empty'."
  empty)

(defn throw
  [e]
  #?(:cljs ((aget Observable "throw") e)
     :clj  (Observable/error e)))

(def ^:deprecated from-exception
  "A deprecated alias for `throw`."
  throw)

(defn timer
  "Returns an observable sequence that produces a value after
  `ms` has elapsed and then after each period."
  ([delay]
   #?(:cljs (.timer Observable delay)
      :clj  (Observable/timer ^long delay TimeUnit/MILLISECONDS)))
  ([delay period]
   #?(:cljs (.timer Observable delay period)
      :clj  (Observable/interval ^long delay ^long period))))

(defn timeout
  "Returns the source observable sequence or the other
  observable sequence if dueTime elapses."
  ([ms ^Observable ob]
   #?(:cljs (.timeoutWith ob ms)
      :clj  (.timeout ob ^long ms TimeUnit/MILLISECONDS)))
  ([ms ^Observable other ^Observable ob]
   #?(:cljs (.timeoutWith ob ms other)
      :clj  (.timeout ob ^long ms TimeUnit/MILLISECONDS other))))

(defn delay
  "Time shifts the observable sequence by dueTime. The relative
  time intervals between the values are preserved."
  [ms ^Observable ob]
  #?(:cljs (.delay ob ms)
     :clj  (.delay ob ^long ms TimeUnit/MILLISECONDS)))

(defn delay-when
  "Time shifts the observable sequence based on a subscription
  delay and a delay selector function for each element."
  ([sf ^Observable ob]
   #?(:cljs (.delayWhen ob sf)
      :clj  (.delay ob (rxfn1 sf))))

  ([^Observable sd sf ^Observable ob]
   #?(:cljs (.delayWhen ob sf sd)
      :clj  (throw (ex-info "Not implemented" {})))))

(defn interval
  "Returns an observable sequence that produces a
  value after each period."
  [ms]
  #?(:cljs (.interval Observable ms)
     :clj  (Observable/interval ^long ms TimeUnit/MILLISECONDS)))

#?(:cljs
   (defn fjoin
     "Runs all observable sequences in parallel and collect
     their last elements."
     [& items]
     (let [[selector items] (if (ifn? (first items))
                              [(first items) (rest items)]
                              [vector items])
           items (if (vector? items) items (into [] items))]
       (apply Observable.forkJoin (conj items selector)))))

#?(:cljs
   (def fork-join
     "Alias to fjoin."
     fjoin))

(defn of
  "Converts arguments to an observable sequence."
  ([a]
   #?(:cljs (Observable.of a)
      :clj  (Observable/from ^Iterable [a])))
  ([a b]
   #?(:cljs (Observable.of a b)
      :clj  (Observable/from ^Iterable [a b])))
  ([a b c]
   #?(:cljs (Observable.of a b c)
      :clj  (Observable/from ^Iterable [a b c])))
  ([a b c d]
   #?(:cljs (Observable.of a b c d)
      :clj  (Observable/from ^Iterable [a b c d])))
  ([a b c d e]
   #?(:cljs (Observable.of a b c d e)
      :clj  (Observable/from ^Iterable [a b c d e])))
  ([a b c d e f]
   #?(:cljs (Observable.of a b c d e f)
      :clj  (Observable/from ^Iterable [a b c d e f])))
  ([a b c d e f & more]
   #?(:cljs (apply Observable.of a b c d e f more)
      :clj  (let [values (into [a b c d e f] more)]
              (Observable/from ^Iterable values)))))

;; --- Bus / Subject

(defn subject
  "Subject that, once an Observer has subscribed, emits all
  subsequently observed items to the subscriber."
  []
  #?(:cljs (Subject.)
     :clj  (PublishSubject/create)))

(defn bus
  "Deprecated alias to `bus`."
  {:deprecated true}
  []
  #?(:cljs (Subject.)
     :clj  (PublishSubject/create)))

(defn behavior-subject
  "Bus that emits the most recent item it has observed and
  all subsequent observed items to each subscribed Observer."
  [v]
  #?(:cljs (BehaviorSubject. v)
     :clj  (BehaviorSubject/create v)))

(defn push!
  "Pushes the given value to the bus stream."
  [b v]
  #?(:cljs (.next b v)
     :clj  (.onNext ^Observer b v)))

(defn error!
  "Pushes the given error to the bus stream."
  [b e]
  #?(:cljs (.error b e)
     :clj  (.onError ^Observer b e)))

(defn end!
  "Ends the given bus stream."
  [b]
  #?(:cljs (.complete b)
     :clj  (.onCompleted ^Observer b)))

;; --- Observable Subscription

(def noop (constantly nil))

#?(:cljs
   (defn- wrap-subscription
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
   :clj
   (defn- wrap-subscription
     [^Subscription subs]
     (reify
       Subscription
       (isUnsubscribed [_]
         (.isUnsubscribed subs))

       (unsubscribe [_]
         (.unsubscribe subs))

       java.lang.AutoCloseable
       (close [_]
         (.unsubscribe subs))

       clojure.lang.IFn
       (invoke [_]
         (.unsubscribe subs)))))

(defn- map->observer
  [{:keys [next error complete]
    :or {next noop error noop complete noop}}]
  #?(:cljs (Subscriber. next error complete)
     :clj  (reify Observer
             (onNext [_ v] (next v))
             (onError [_ e] (error e))
             (onCompleted [_] (complete)))))

(defn- make-observer
  [data]
  (cond
    (map? data)
    (map->observer data)

    (or (subject? data)
        (observer? data))
    data

    :else
    (throw (ex-info "Invalid arguments for build observer." {:data data}))))

(defn on-value
  "Subscribes a function to invoke for each element
  in the observable sequence."
  [^Observable ob f]
  (let [observer (make-observer {:next f})
        subscription (.subscribe ob observer)]
    (wrap-subscription subscription)))

(def on-next
  "A semantic alias for `on-value`."
  on-value)

(defn on-error
  "Subscribes a function to invoke upon exceptional termination
  of the observable sequence."
  [^Observable ob f]
  (let [observer (make-observer {:error f})
        subscription (.subscribe ob observer)]
    (wrap-subscription subscription)))

(defn on-complete
  "Subscribes a function to invoke upon graceful termination
  of the observable sequence."
  [^Observable ob f]
  (let [observer (make-observer {:complete f})
        subscription (.subscribe ob observer)]
    (wrap-subscription subscription)))

(def on-end
  "A semantic alias for `on-complete`."
  on-complete)

(defn subscribe
  "Subscribes an observer to the observable sequence."
  ([ob nf]
   (subscribe ob nf nil nil))
  ([ob nf ef]
   (subscribe ob nf ef nil))
  ([^Observable ob nf ef cf]
   (let [observer (make-observer {:next nf
                                  :error ef
                                  :complete cf})
         subscription (.subscribe ob observer)]
     (wrap-subscription subscription))))

(defn to-atom
  "Materialize the observable sequence into an atom."
  ([ob]
   (let [a (atom nil)]
     (to-atom ob a)))
  ([ob a]
   (on-value ob #(reset! a %))
   a)
  ([ob a f]
   (on-value ob #(swap! a f %))
   a))

;; --- Observable Transformations

(defn race
  "Create an observable that surfaces any of the given
  sequences, whichever reacted first."
  ([^Observable a ^Observable b]
   #?(:cljs (.race a b)
      :clj  (Observable/amb a b)))
  ([a b & more]
   #?(:cljs (cljs.core/reduce race (race a b) more)
      :clj  (let [values (into [a b] more)]
              (Observable/amb ^Iterable values)))))

(defn zip
  "Merges the specified observable sequences or Promises
  into one observable sequence."
  [& items]
  (let [[selector items] (if (ifn? (first items))
                           [(first items) (rest items)]
                           [vector items])
        items (if (vector? items) items (into [] items))]
    #?(:cljs (apply Observable.zip (conj items selector))
       :clj  (Observable/zip ^Iterable items (rxfnn selector)))))

(defn concat
  "Concatenates all of the specified observable
  sequences, as long as the previous observable
  sequence terminated successfully."
  [& more]
  #?(:cljs (let [more (cljs.core/filter identity more)]
             (cljs.core/reduce #(.concat %1 %2) more))
     :clj  (let [more (clojure.core/filter identity more)]
             (clojure.core/reduce (fn [^Observable a ^Observable b]
                                    (Observable/concat a b))
                                  more))))

(defn merge
  "Merges all the observable sequences and Promises
  into a single observable sequence."
  [& more]
  #?(:cljs (let [more (cljs.core/filter identity more)]
             (cljs.core/reduce #(.merge %1 %2) more))
     :clj  (let [more (clojure.core/filter identity more)]
             (clojure.core/reduce (fn [^Observable a ^Observable b]
                                    (Observable/merge a b))
                                  more))))

#?(:cljs
   (defn merge-all
     "Merges an observable sequence of observable
     sequences into an observable sequence."
     [ob]
     (.mergeAll ob)))

(defn filter
  "Filters the elements of an observable sequence
  based on a predicate."
  [f ^Observable ob]
  #?(:cljs (.filter ob #(boolean (f %)))
     :clj  (.filter ob (rxfn1 #(boolean (f %))))))

(defn map
  "Apply a function to each element of an observable
  sequence."
  [f ^Observable ob]
  #?(:cljs (.map ob #(f %))
     :clj  (.map ob (rxfn1 f))))

(defn flat-map
  "Projects each element of an observable sequence to
  an observable sequence and merges the resulting
  observable sequences or Promises or array/iterable
  into one observable sequence."
  ([ob]
   (flat-map identity ob))
  ([f ^Observable ob]
   #?(:cljs (.flatMap ob #(f %))
      :clj  (.flatMap ob (rxfn1 f)))))

(def merge-map
  "An idiomatic alias for flat-map."
  flat-map)

(defn mapcat
  "Projects each element of an observable sequence to an observable
  sequence and concatenates the resulting observable sequences or
  Promises or array/iterable into one observable sequence."
  [f ^Observable ob]
  #?(:cljs (.concatMap ob #(f %))
     :clj  (.concatMap ob (rxfn1 f))))

(defn skip
  "Bypasses a specified number of elements in an
  observable sequence and then returns the remaining
  elements."
  [n ^Observable ob]
  (.skip ob (int n)))

(defn skip-while
  "Bypasses elements in an observable sequence as long
  as a specified condition is true and then returns the
  remaining elements."
  [f ^Observable ob]
  #?(:cljs (.skipWhile ob #(boolean (f %)))
     :clj  (.skipWhile ob (rxfn1 #(boolean (f %))))))

(defn skip-until
  "Returns the values from the source observable sequence
  only after the other observable sequence produces a value."
  [^Observable pob ^Observable ob]
  (.skipUntil ob pob))

(defn take
  "Bypasses a specified number of elements in an
  observable sequence and then returns the remaining
  elements."
  [n ^Observable ob]
  (.take ob (int n)))

(defn take-while
  "Returns elements from an observable sequence as long as a
  specified predicate returns true."
  [f ob]
  #?(:cljs (.takeWhile ob #(boolean (f %)))
     :clj  (.takeWhile ob (rxfn1 #(boolean (f %))))))

(defn take-until
  "Returns the values from the source observable sequence until
  the other observable sequence or Promise produces a value."
  [^Observable other ^Observable ob]
  (.takeUntil ob other))

(defn reduce
  "Applies an accumulator function over an observable
  sequence, returning the result of the aggregation as a
  single element in the result sequence."
  ([f ^Observable ob]
   #?(:cljs (.reduce ob f)
      :clj  (.reduce ob (rxfn2 f))))
  ([f seed ^Observable ob]
   #?(:cljs (.reduce ob f seed)
      :clj  (.reduce ob seed (rxfn2 f)))))

(defn scan
  "Applies an accumulator function over an observable
  sequence and returns each intermediate result.
  Same as reduce but with intermediate results"
  ([f ^Observable ob]
   #?(:cljs (.scan ob f)
      :clj  (.scan ob (rxfn2 f))))
  ([f seed ^Observable ob]
   #?(:cljs (.scan ob f seed)
      :clj  (.scan ob seed (rxfn2 f)))))

(defn with-latest-from
  "Merges the specified observable sequences into
  one observable sequence by using the selector
  function only when the source observable sequence
  (the instance) produces an element."
  ([other source]
   (with-latest-from vector other source))
  ([f ^Observable other ^Observable source]
   #?(:cljs (.withLatestFrom source other f)
      :clj  (.withLatestFrom source other (rxfn2 f)))))

(defn combine-latest
  "Combines multiple Observables to create an Observable
  whose values are calculated from the latest values of
  each of its input Observables."
  ([other ob]
   (combine-latest vector other ob))
  ([f ^Observable other ^Observable ob]
   #?(:cljs (.combineLatest ob other f)
      :clj  (Observable/combineLatest [ob other] (rxfnn f)))))

(defn catch
  "Continues an observable sequence that is terminated
  by an exception with the next observable sequence."
  ([handler ^Observable ob]
   #?(:cljs (.catch ob (fn [value]
                         (let [value (handler value)]
                           (cond
                             (observable? value) value
                             (-end? value) (empty)
                             (-error? value) (throw value)
                             (-next? value) (of value)))))
      :clj  (.onErrorResumeNext ob (rxfn1 (fn [value]
                                            (let [value (handler value)]
                                              (cond
                                                (observable? value) value
                                                (-end? value) (empty)
                                                (-error? value) (throw value)
                                                (-next? value) (of value))))))))
  ([pred handler ob]
   (catch (fn [value]
            (if (pred value)
              (handler value)
              (throw value)))
       ob)))

(defn tap
  "Invokes an action for each element in the
  observable sequence."
  ([f ^Observable ob]
   #?(:cljs (.do ob f)
      :clj  (.doOnNext ob (rxaction1 f))))
  ([f g ^Observable ob]
   #?(:cljs (.do ob f g)
      :clj  (-> ob
                (.doOnNext (rxaction1 f))
                (.doOnError (rxaction1 g)))))
  ([f g e ob]
   #?(:cljs (.do ob f g e)
      :clj  (-> ob
                (.doOnNext (rxaction1 f))
                (.doOnError (rxaction1 g))
                (.doOnCompleted (rxaction0 e))))))

(def do
  "An idiomatic alias for `tap`."
  tap)

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
  [ms ^Observable ob]
  #?(:cljs (.throttleTime ob ms)
     :clj  (.throttleFirst ob ^long ms TimeUnit/MILLISECONDS)))

(defn debounce
  "Emits an item from the source Observable after a
  particular timespan has passed without the Observable
  omitting any other items."
  [ms ^Observable ob]
  #?(:cljs (.debounceTime ob ms)
     :clj  (.debounce ob ^long ms TimeUnit/MILLISECONDS)))

(defn sample
  "Samples the observable sequence at each interval."
  [ms ^Observable ob]
  #?(:cljs (.sampleTime ob ms)
     :clj  (.sample ob ^long ms TimeUnit/MILLISECONDS)))

(defn sample-when
  "Samples the observable sequence at each interval."
  [^Observable other ^Observable ob]
  (.sample ob other))

(defn ignore
  "Ignores all elements in an observable sequence leaving
  only the termination messages."
  [^Observable ob]
  (.ignoreElements ob))

(defn dedupe
  "Returns an observable sequence that contains only
  distinct contiguous elements."
  ([^Observable ob]
   #?(:cljs (dedupe identity ob)
      :clj  (.distinctUntilChanged ob)))
  ([f ^Observable ob]
   #?(:cljs (.distinctUntilChanged ob = f)
      :clj  (.distinctUntilChanged ob (rxfn1 f)))))

(defn dedupe'
  "Returns an observable sequence that contains only d
  istinct elements.
  Usage of this operator should be considered carefully
  due to the maintenance of an internal lookup structure
  which can grow large."
  ([^Observable ob]
   #?(:cljs (.distinct ob =)
      :clj  (.distinct ob)))
  ([f ^Observable ob]
   #?(:cljs (.distinct ob = f)
      :clj  (.distinct ob (rxfn1 f)))))

(defn buffer
  "Projects each element of an observable sequence into zero
  or more buffers which are produced based on element count
  information."
  ([n ^Observable ob]
   #?(:cljs (.bufferCount ob n)
      :clj  (.buffer ob (int n))))
  ([n skip ^Observable ob]
   #?(:cljs (.bufferCount ob n skip)
      :clj  (.buffer ob (int n) (int skip)))))

(defn buffer-time
  "Buffers the source Observable values for a specific time period."
  [ms ^Observable ob]
  #?(:cljs (.bufferTime ob ms)
     :clj (.buffer ob ms TimeUnit/MILLISECONDS)))

(defn retry
  "Given an optional number of retries and an observable,
  repeats the source observable the specified number of
  times or until it terminates. If no number of retries
  is given, it will be retried indefinitely."
  ([^Observable ob]
   (.retry ob))
  ([n ^Observable ob]
   (.retry ob (int n))))

(defn transform
  "Transform the observable sequence using transducers."
  [xform stream]
  (letfn [(sink-step [sink]
            (fn
              ([r] (sink end) r)
              ([_ input] (sink input) input)))]
    (let [ns (create (fn [sink]
                       (let [xsink (xform (sink-step sink))
                             step (fn [input]
                                    (let [v (xsink nil input)]
                                      (when (reduced? v)
                                        (xsink @v))))
                             sub (on-value stream step)]
                         (on-complete stream #(do (xsink nil)
                                                  (sink end)))
                         (fn []
                           (.close sub)))))]
      ns)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Schedulers
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

#?(:cljs
   (do
     (def ^:const asap Scheduler.asap)
     (def ^:const queue Scheduler.queue)
     (def ^:const async Scheduler.async))
   :clj
   (do
     (def computation (Schedulers/computation))
     (def io (Schedulers/io))
     (def immediate (Schedulers/immediate))
     (def trampoline (Schedulers/trampoline))))

(defn observe-on
  [^Scheduler scheduler ^Observable ob]
  (.observeOn ob scheduler))

(defn subscribe-on
  [^Scheduler scheduler ^Observable ob]
  (.subscribeOn ob scheduler))
