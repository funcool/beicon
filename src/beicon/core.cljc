(ns beicon.core
  (:refer-clojure :exclude [true? map filter reduce merge repeat mapcat
                            repeatedly zip dedupe drop take take-while
                            concat empty delay range throw do trampoline])
  #?(:cljs (:require [beicon.extern.rxjs]))
  #?(:clj  (:import io.reactivex.BackpressureStrategy
                    io.reactivex.Emitter
                    io.reactivex.Flowable
                    io.reactivex.Observable
                    io.reactivex.ObservableOnSubscribe
                    io.reactivex.Observer
                    io.reactivex.Scheduler
                    io.reactivex.Single
                    io.reactivex.SingleObserver
                    io.reactivex.disposables.Disposable
                    io.reactivex.exceptions.CompositeException
                    io.reactivex.functions.Action
                    io.reactivex.functions.BiFunction
                    io.reactivex.functions.Cancellable
                    io.reactivex.functions.Consumer
                    io.reactivex.functions.Function
                    io.reactivex.functions.Predicate
                    io.reactivex.internal.functions.Functions
                    io.reactivex.internal.observers.LambdaObserver
                    io.reactivex.internal.subscribers.LambdaSubscriber
                    io.reactivex.observers.ResourceObserver
                    io.reactivex.schedulers.Schedulers
                    io.reactivex.subjects.BehaviorSubject
                    io.reactivex.subjects.PublishSubject
                    io.reactivex.subjects.Subject
                    java.lang.AutoCloseable
                    java.util.concurrent.Callable
                    java.util.concurrent.Future
                    java.util.concurrent.TimeUnit
                    java.util.concurrent.atomic.AtomicReference
                    org.reactivestreams.Subscriber
                    org.reactivestreams.Subscription)))

;; --- Interop Helpers

#?(:clj
   (defn as-consumer
     "Wrap the provided function into a Consumer instance."
     ^Consumer
     [f]
     (reify Consumer
       (accept [_ v]
         (f v)))))

#?(:clj
   (defn as-action
     "Wrap the provided function into a Action instance."
     ^Action
     [f]
     (reify Action
       (run [_]
         (f)))))

#?(:clj
   (defn as-predicate
     "Wrap the provided function into a Predicate instance."
     ^Predicate
     [f]
     (reify Predicate
       (test [_ v]
         (boolean (f v))))))

#?(:clj
   (defn as-function
     "Wrap the provided function into a Function instance."
     ^Function
     [f]
     (reify Function
       (apply [_ v]
         (f v)))))

#?(:clj
   (defn as-bifunction
     "Wrap the provided function into a Function instance."
     ^BiFunction
     [f]
     (reify BiFunction
       (apply [_ v b]
         (f v b)))))

(def ^:private noop (constantly nil))

#?(:clj
   (do
     (def ^:private noop-consumer (as-consumer noop))
     (def ^:private noop-action (as-action noop))))

;; --- Predicates

#?(:cljs
   (do
     (def ^:const Observable js/Rx.Observable)
     (def ^:const Subject js/Rx.Subject)
     (def ^:const BehaviorSubject js/Rx.BehaviorSubject)
     (def ^:const Subscriber js/Rx.Subscriber)
     (def ^:const Observer js/Rx.Subscriber)
     (def ^:const Disposable js/Rx.Subscription)
     (def ^:const Scheduler js/Rx.Scheduler)))

(defn observable?
  "Return true if `ob` is a instance
  of Rx.Observable."
  [ob]
  (instance? Observable ob))

(defn disposable?
  "Check if the provided object is disposable (jvm) or subscription (js)."
  [v]
  #?(:clj (instance? Disposable v)
     :clj (instance? Subscription v)))

#?(:clj
   (defn flowable?
     "Check if the provided value is Flowable instance."
     [o]
     (instance? Flowable o)))

#?(:clj
   (defn single?
     "Check if the provided value is Single instance."
     [o]
     (instance? Single o)))

(defn scheduler?
  "Check if the provided value is Scheduler instance."
  [v]
  #?(:clj (instance? Scheduler v)
     :cljs (and v (fn? (.-schedule v)))))

(defn subject?
  "Check if the provided value is Subject instance."
  [b]
  (instance? Subject b))

(defn observer?
  "Check if the provided value is Observer instance."
  [o]
  (instance? Observer o))

;; --- Observables Constructor

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

     (extend-type nil
       IObservableValue
       (-next? [_] false)
       (-error? [_] false)
       (-end? [_] true))

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
                                (.complete subs))))]
       (Observable.
        (fn [subs]
          (try
            (sf (partial sink subs))
            (catch js/Error e
              (.error subs e)))))))

   :clj
   (defn create
     "Creates an observable sequence from a specified
     subscribe method implementation."
     [factory]
     (letfn [(sink [^Emitter emitter v]
               (cond
                 (identical? end v) (.onComplete emitter)
                 (-next? v) (.onNext emitter v)
                 (-error? v) (.onError emitter v)
                 (-end? v) (.onComplete emitter)
                 (reduced? v) (do
                                (sink emitter @v)
                                (.onComplete emitter))))]
       (Observable/create
        (reify ObservableOnSubscribe
          (subscribe [_ emitter]
            (try
              (let [disposefn (factory (partial sink emitter))
                    cancellable (reify Cancellable
                                  (cancel [_]
                                    (when (fn? disposefn)
                                      (disposefn))))]
                (.setCancellable emitter cancellable))
              (catch Exception e
                (.onError emitter e)))))))))


#?(:clj
   (defn generate
     "Returns a cold, synchronous, stateful and backpressure-aware
     generator of values."
     ([next] (generate next nil nil))
     ([next setup] (generate next setup nil))
     ([next setup dispose]
      (let [setup (if (fn? setup) setup (constantly setup))
            dispose (if (fn? dispose) (as-consumer dispose) noop-consumer)
            next (as-bifunction
                  (fn [state ^Emitter emitter]
                    (let [sink (fn sink [v]
                                 (cond
                                   (identical? end v) (.onComplete emitter)
                                   (-next? v) (.onNext emitter v)
                                   (-error? v) (.onError emitter v)
                                   (-end? v) (.onComplete emitter)
                                   (reduced? v) (do
                                                  (sink @v)
                                                  (.onComplete emitter)))
                                 v)]
                      (next state sink))))]
        (Flowable/generate ^Callable setup
                           ^BiFunction next
                           ^Consumer dispose)))))


;; --- Observable Subscription

(defprotocol ISubscriber
  "Backpressure aware subscriber abstraction."
  (-on-init [_ s] "Subscription initialization hook.")
  (-on-next [_ s value] "Subscription data notification hook.")
  (-on-error [_ error] "Subscription error notification hook.")
  (-on-end [_] "Subscription termination notification hook."))

(defprotocol ISubscription
  (-request [_ n] "request 1 or n items to the subscription."))

(defprotocol ICancellable
  (-cancel [_] "dispose resources."))

(defn request!
  [s n]
  (-request s n))

(defn cancel!
  "Dispose resources acquired by the subscription."
  [v]
  (-cancel v))

#?(:cljs
   (defn- wrap-disposable
     [disposable]
     (specify! disposable
       ICancellable
       (-cancel [this]
         (.unsubscribe this))))

   :clj
   (defn- wrap-disposable
     [^Disposable disposable]
     (reify
       ICancellable
       (-cancel [_]
         (.dispose disposable))

       Disposable
       (dispose [_]
         (.dispose disposable))

       java.lang.AutoCloseable
       (close [_]
         (.dispose disposable)))))

#?(:clj
   (defn- aref->subscription
     [^AtomicReference ref]
     (reify
       ISubscription
       (-request [_ n]
         (when-let [^Subscription sub (.get ref)]
           (.request sub ^long n)))

       ICancellable
       (-cancel [_]
         (when-let [^Subscription sub (.get ref)]
           (.cancel sub)))

       Disposable
       (dispose [_]
         (when-let [^Subscription sub (.get ref)]
           (.cancel sub)))

       AutoCloseable
       (close [_]
         (when-let [^Subscription sub (.get ref)]
           (.cancel sub))))))

#?(:clj
   (defn- aref->disposable
     [^AtomicReference ref]
     (reify
       ICancellable
       (-cancel [_]
         (when-let [^Disposable disp (.get ref)]
           (.dispose disp)))

       Disposable
       (dispose [_]
         (when-let [^Disposable disp (.get ref)]
           (.dispose disp)))

       AutoCloseable
       (close [_]
         (when-let [^Disposable disp (.get ref)]
           (.dispose disp))))))

#?(:clj
   (defn- subscribe-flowable-with-isubscriber
     [ob subscriber]
     (let [subref (AtomicReference. nil)
           subs (aref->subscription subref)]
       (.subscribe ob (reify Subscriber
                        (onSubscribe [_ subscription]
                          (.compareAndSet subref nil subscription)
                          (-on-init subscriber subs))
                        (onNext [_ value]
                          (-on-next subscriber subs value))
                        (onError [_ error]
                          (-on-error subscriber error))
                        (onComplete [_]
                          (-on-end subscriber))))
       subs)))

#?(:clj
   (defn- subscribe-observable-with-isubscriber
     [ob subscriber]
     (let [dispref (AtomicReference. nil)
           disp (aref->disposable dispref)]
       (.subscribe ob (reify Subscriber
                        (onSubscribe [_ subscription]
                          (.compareAndSet dispref nil subscription)
                          (-on-init subscriber disp))
                        (onNext [_ value]
                          (-on-next subscriber disp value))
                        (onError [_ error]
                          (-on-error subscriber error))
                        (onComplete [_]
                          (-on-end subscriber))))
       disp)))

#?(:clj
   (defn- subscribe-with-observer
     [ob ^Observer observer]
     (let [dispref (AtomicReference. nil)
           disp (aref->disposable dispref)]
       (.subscribe ob (reify Observer
                        (onSubscribe [_ disposable]
                          (.compareAndSet dispref nil disposable)
                          (.onSubscribe observer disp))
                        (onNext [_ value]
                          (.onNext observer value))
                        (onError [_ error]
                          (.onError observer ^Throwable error))
                        (onComplete [_]
                          (.onComplete observer))))
       disp)))

#?(:clj
   (defn subscribe-with
     "Subscribes an observer or subscriber to the observable/flowable sequence."
     [ob subscriber]
     (cond
       (satisfies? ISubscriber subscriber)
       (if (flowable? ob)
         (subscribe-flowable-with-isubscriber ob subscriber)
         (subscribe-observable-with-isubscriber ob subscriber))

       (and (observable? ob)
            (observer? subscriber))
       (subscribe-with-observer ob subscriber)

       :else
       (throw (ex-info "Invalid arguments." {}))))

   :cljs
   (defn subscribe-with
     "Subscribes an observer or subscriber to the observable sequence."
     [ob observer]
     {:pre [(or (observer? observer)
                (subject? observer))]}
     (wrap-disposable (.subscribe ob observer))))

#?(:clj
   (defn- subscribe-to-observable
     [ob nf ef cf sf]
     (let [observer (LambdaObserver.
                     (if (fn? nf) (as-consumer nf) noop-consumer)
                     (if (fn? ef) (as-consumer ef) noop-consumer)
                     (if (fn? cf) (as-action cf) noop-action)
                     (if (fn? sf) (as-consumer sf) noop-consumer))]
       (wrap-disposable (.subscribeWith ^Observable ob
                                        ^Observer observer)))))

#?(:clj
   (defn- subscribe-to-flowable
     [ob nf ef cf sf]
     (let [on-subscribe (as-consumer #(.request % Long/MAX_VALUE))
           subscriber (LambdaSubscriber.
                       (if (fn? nf) (as-consumer nf) noop-consumer)
                       (if (fn? ef) (as-consumer ef) noop-consumer)
                       (if (fn? cf) (as-action cf) noop-action)
                       (if (fn? sf) (as-consumer sf) on-subscribe))]
       (wrap-disposable (.subscribeWith ^Flowable ob
                                        ^Subscriber subscriber)))))

#?(:cljs
   (defn subscribe
     "Subscribes an observer to the observable sequence."
     ([ob nf]
      (subscribe ob nf nil nil))
     ([ob nf ef]
      (subscribe ob nf ef nil))
     ([ob nf ef cf]
      (let [observer (Subscriber. (if (fn? nf) nf noop)
                                  (if (fn? ef) ef noop)
                                  (if (fn? cf) cf noop))]
        (wrap-disposable (.subscribe ob observer)))))
   :clj
   (defn subscribe
     "Subscribes an observer to the observable sequence."
     ([ob nf]
      (subscribe ob nf nil nil nil))
     ([ob nf ef]
      (subscribe ob nf ef nil nil))
     ([ob nf ef cf]
      (subscribe ob nf ef cf nil))
     ([ob nf ef cf sf]
      (cond
        (flowable? ob) (subscribe-to-flowable ob nf ef cf sf)
        (single? ob) (subscribe-to-observable (.toObservable ob) nf ef cf sf)
        :else (subscribe-to-observable ob nf ef cf sf)))))

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
  #?(:cljs (Subject.)
     :clj  (PublishSubject/create)))

(defn behavior-subject
  "Bus that emits the most recent item it has observed and
  all subsequent observed items to each subscribed Observer."
  [v]
  #?(:cljs (BehaviorSubject. v)
     :clj  (BehaviorSubject/createDefault v)))

#?(:clj
  (defn to-serialized
    "Converts an Subject into a Serialized Subject if not
    already Serialized."
    [s]
    {:pre [(subject? s)]}
    (.toSerialized s)))

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
     :clj  (.onComplete ^Observer b)))

;; --- Observable Factory Helpers

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
  from other observable."
  [ob]
  (.publish ob))

(defn share
  "Returns an observable sequence that shares a single
  subscription to the underlying sequence."
  [ob]
  (.share ob))

(defn connect!
  "Connect the connectable observable."
  [ob]
  (.connect ob))

(defn from-coll
  "Generates an observable sequence from collection."
  [coll]
  #?(:cljs (let [array (into-array coll)]
             (.from Observable array))
     :clj  (Observable/fromIterable ^Iterable coll)))

(defn from-atom
  [atm]
  (create (fn [sink]
            (let [key (keyword (gensym "beicon"))]
              (add-watch atm key (fn [_ _ _ val]
                                   (sink val)))
              (fn []
                (remove-watch atm key))))))

#?(:cljs
   (defn from-promise
    "Creates an observable from a promise."
     [p]
     (.fromPromise Observable p))
   :clj
   (defn from-future
     "Creates an observable from a future."
     [p]
     (Observable/fromFuture ^Future p)))

#?(:clj
   (defn from-publisher
     "Converts an arbitrary Reactive-Streams Publisher into a
     Flowable if not already a Flowable."
     [v]
     (Flowable/fromPublisher ^Publisher v)))

#?(:cljs
   (defn from-event
     "Creates an Observable by attaching an event listener to an event target"
     [et ev]
     (.fromEvent Observable et ev)))

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

(defn timer
  "Returns an observable sequence that produces a value after
  `ms` has elapsed and then after each period."
  ([delay]
   #?(:cljs (.timer Observable delay)
      :clj  (Observable/timer ^long delay TimeUnit/MILLISECONDS)))
  ([delay period]
   #?(:cljs (.timer Observable delay period)
      :clj  (Observable/interval ^long delay ^long period TimeUnit/MILLISECONDS))))

(defn timeout
  "Returns the source observable sequence or the other
  observable sequence if dueTime elapses."
  ([ms ob]
   #?(:cljs (.timeoutWith ob ms)
      :clj  (.timeout ob ^long ms TimeUnit/MILLISECONDS)))
  ([ms other ob]
   #?(:cljs (.timeoutWith ob ms other)
      :clj  (.timeout ob ^long ms TimeUnit/MILLISECONDS other))))

(defn delay
  "Time shifts the observable sequence by dueTime. The relative
  time intervals between the values are preserved."
  [ms ob]
  #?(:cljs (.delay ob ms)
     :clj  (.delay ob ^long ms TimeUnit/MILLISECONDS)))

(defn delay-when
  "Time shifts the observable sequence based on a subscription
  delay and a delay selector function for each element."
  ([sf ob]
   #?(:cljs (.delayWhen ob sf)
      :clj  (.delay ob (as-function sf))))

  ([sd sf ob]
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
      :clj  (Observable/just a)))
  ([a b]
   #?(:cljs (Observable.of a b)
      :clj  (Observable/just a b)))
  ([a b c]
   #?(:cljs (Observable.of a b c)
      :clj  (Observable/just a b c)))
  ([a b c d]
   #?(:cljs (Observable.of a b c d)
      :clj  (Observable/just a b c d)))
  ([a b c d e]
   #?(:cljs (Observable.of a b c d e)
      :clj  (Observable/just a b c d e)))
  ([a b c d e f]
   #?(:cljs (Observable.of a b c d e f)
      :clj  (Observable/just a b c d e f)))
  ([a b c d e f & more]
   #?(:cljs (apply Observable.of a b c d e f more)
      :clj  (let [values (into [a b c d e f] more)]
              (Observable/fromIterable ^Iterable values)))))

#?(:clj
   (defn- disposable-atom
     [^clojure.lang.IAtom ref ^Disposable disposable]
     (reify
       ICancellable
       (-cancel [_]
         (.dispose disposable))

       clojure.lang.IDeref
       (deref [_] (deref ref))

       clojure.lang.IAtom
       (reset [_ newval] (.reset ref newval))
       (swap [_ f] (.swap ref f))
       (swap [_ f x] (.swap ref f x))
       (swap [_ f x y] (.swap ref f x y))
       (swap [_ f x y more] (.swap ref f x y more))

       clojure.lang.IRef
       (setValidator [_ cb]
         (.setValidator ^clojure.lang.IRef ref cb))

       (getValidator [_]
         (.getValidator ^clojure.lang.IRef ref))

       (getWatches [_]
         (.getWatches ^clojure.lang.IRef ref))

       (addWatch [_ key cb]
         (.addWatch ^clojure.lang.IRef ref key cb))

       (removeWatch [self key]
         (.removeWatch ^clojure.lang.IRef ref key))))

   :cljs
   (defn- disposable-atom
     [ref disposable]
     (specify! ref
       ICancellable
       (-cancel [_]
         (.unsubscribe disposable)))))

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

#?(:clj
   (defn- as-backpressure-strategy
     [strategy]
     (if (instance? BackpressureStrategy strategy)
       strategy
       (case strategy
         :buffer BackpressureStrategy/BUFFER
         :drop BackpressureStrategy/DROP
         :missing BackpressureStrategy/MISSING
         :error BackpressureStrategy/ERROR
         (throw (ex-info "Unexpected option" {:strategy strategy}))))))

#?(:clj
   (defn to-flowable
     "Convert an observable into backpressure-aware Flowable instance."
     ([ob]
      (to-flowable :buffer ob))
     ([strategy ^Observable ob]
      (.toFlowable ob ^BackpressureStrategy (as-backpressure-strategy strategy)))))

;; --- Observable Transformations

(defn race
  "Create an observable that surfaces any of the given
  sequences, whichever reacted first."
  ([a b]
   #?(:cljs (.race a b)
      :clj  (Observable/amb ^Iterable (list a b))))
  ([a b & more]
   #?(:cljs (cljs.core/reduce race (race a b) more)
      :clj  (let [values (cons a (cons b more))]
              (Observable/amb ^Iterable values)))))

(defn zip
  "Merges the specified observable sequences or Promises
  into one observable sequence."
  [& items]
  (let [[selector items] (if (ifn? (first items))
                           [(first items) (rest items)]
                           [vector items])
        items (if (vector? items) items (vec items))]
    #?(:cljs (apply Observable.zip (conj items selector))
       :clj  (Observable/zip ^Iterable items (as-function #(apply selector (seq %)))))))

(defn concat
  "Concatenates all of the specified observable
  sequences, as long as the previous observable
  sequence terminated successfully."
  [& more]
  #?(:cljs (let [more (cljs.core/filter identity more)]
             (cljs.core/reduce #(.concat %1 %2) more))
     :clj  (let [more (clojure.core/filter identity more)]
             (cond
               (every? observable? more)
               (clojure.core/reduce #(Observable/concat %1 %2) more)

               (every? flowable? more)
               (clojure.core/reduce #(Flowable/concat %1 %2) more)

               :else
               (throw (ex-info "Invalid arguments." {}))))))

(defn merge
  "Merges all the observable sequences and Promises
  into a single observable sequence."
  [& more]
  #?(:cljs (let [more (cljs.core/filter identity more)]
             (cljs.core/reduce #(.merge %1 %2) more))
     :clj  (let [more (clojure.core/filter identity more)]
             (clojure.core/reduce (fn [a b]
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
  [f ob]
  #?(:cljs (.filter ob #(boolean (f %)))
     :clj  (.filter ob (as-predicate f))))

(defn map
  "Apply a function to each element of an observable
  sequence."
  [f ob]
  #?(:cljs (.map ob #(f %))
     :clj  (.map ob (as-function f))))

(defn flat-map
  "Projects each element of an observable sequence to
  an observable sequence and merges the resulting
  observable sequences or Promises or array/iterable
  into one observable sequence."
  ([ob]
   (flat-map identity ob))
  ([f ob]
   #?(:cljs (.flatMap ob #(f %))
      :clj  (.flatMap ob (as-function f)))))

(def merge-map
  "An idiomatic alias for flat-map."
  flat-map)

(defn mapcat
  "Projects each element of an observable sequence to an observable
  sequence and concatenates the resulting observable sequences or
  Promises or array/iterable into one observable sequence."
  [f ob]
  #?(:cljs (.concatMap ob #(f %))
     :clj  (.concatMap ob (as-function f))))

(defn skip
  "Bypasses a specified number of elements in an
  observable sequence and then returns the remaining
  elements."
  [n ob]
  (.skip ob (int n)))

(defn skip-while
  "Bypasses elements in an observable sequence as long
  as a specified condition is true and then returns the
  remaining elements."
  [f ob]
  #?(:cljs (.skipWhile ob #(boolean (f %)))
     :clj  (.skipWhile ob (as-predicate f))))

(defn skip-until
  "Returns the values from the source observable sequence
  only after the other observable sequence produces a value."
  [pob ob]
  (.skipUntil ob pob))

(defn take
  "Bypasses a specified number of elements in an
  observable sequence and then returns the remaining
  elements."
  [n ob]
  (.take ob (int n)))

(defn take-while
  "Returns elements from an observable sequence as long as a
  specified predicate returns true."
  [f ob]
  #?(:cljs (.takeWhile ob #(boolean (f %)))
     :clj  (.takeWhile ob (as-predicate f))))

(defn take-until
  "Returns the values from the source observable sequence until
  the other observable sequence or Promise produces a value."
  [other ob]
  (.takeUntil ob other))

(defn reduce
  "Applies an accumulator function over an observable
  sequence, returning the result of the aggregation as a
  single element in the result sequence."
  ([f ob]
   #?(:cljs (.reduce ob #(f %1 %2))
      :clj  (.reduce ob (as-bifunction f))))
  ([f seed ob]
   #?(:cljs (.reduce ob #(f %1 %2) seed)
      :clj  (.reduce ob seed (as-bifunction f)))))

(defn scan
  "Applies an accumulator function over an observable
  sequence and returns each intermediate result.
  Same as reduce but with intermediate results"
  ([f ob]
   #?(:cljs (.scan ob #(f %1 %2))
      :clj  (.scan ob (as-bifunction f))))
  ([f seed ob]
   #?(:cljs (.scan ob #(f %1 %2) seed)
      :clj  (.scan ob seed (as-bifunction f)))))

(defn with-latest
  "Merges the specified observable sequences into
  one observable sequence by using the selector
  function only when the source observable sequence
  (the instance) produces an element."
  [f other source]
  #?(:cljs (.withLatestFrom source other f)
     :clj  (.withLatestFrom source other (as-bifunction f))))

(defn combine-latest
  "Combines multiple Observables to create an Observable
  whose values are calculated from the latest values of
  each of its input Observables."
  ([other ob]
   (combine-latest vector other ob))
  ([f other ob]
   #?(:cljs (.combineLatest ob other f)
      :clj  (let [^Iterable sources (list ob other)
                  ^Function combiner (as-function #(apply f (seq %)))]
              (Observable/combineLatest sources combiner)))))

(defn- unwrap-composite-exception
  [exc]
  #?(:clj
     (if (instance? io.reactivex.exceptions.CompositeException exc)
       (first (.getExceptions exc))
       exc)
     :cljs exc))

(defn catch
  "Continues an observable sequence that is terminated
  by an exception with the next observable sequence."
  ([handler ob]
   #?(:cljs (.catch ob (fn [value]
                         (let [value (handler value)]
                           (cond
                             (observable? value) value
                             (-end? value) (empty)
                             (-error? value) (throw value)
                             (-next? value) (just value)))))
      :clj  (.onErrorResumeNext ob (as-function
                                    (fn [value]
                                      (let [value (unwrap-composite-exception value)
                                            value (handler value)]
                                        (cond
                                          (observable? value) value
                                          (-end? value) (empty)
                                          (-error? value) (throw value)
                                          (-next? value) (just value))))))))
  ([pred handler ob]
   (catch (fn [value]
            (let [value (unwrap-composite-exception value)]
              (if (pred value)
                (handler value)
                (throw value))))
       ob)))

(defn tap
  "Invokes an action for each element in the
  observable sequence."
  ([f ob]
   #?(:cljs (.do ob f)
      :clj  (.doOnNext ob (as-consumer f))))
  ([f g ob]
   #?(:cljs (.do ob f g)
      :clj  (-> ob
                (.doOnNext (as-consumer f))
                (.doOnError (as-consumer g)))))
  ([f g e ob]
   #?(:cljs (.do ob f g e)
      :clj  (-> ob
                (.doOnNext (as-consumer f))
                (.doOnError (as-consumer g))
                (.doOnComplete (as-action e))))))

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
  [ms ob]
  #?(:cljs (.throttleTime ob ms)
     :clj  (.throttleFirst ob ^long ms TimeUnit/MILLISECONDS)))

(defn debounce
  "Emits an item from the source Observable after a
  particular timespan has passed without the Observable
  omitting any other items."
  [ms ob]
  #?(:cljs (.debounceTime ob ms)
     :clj  (.debounce ob ^long ms TimeUnit/MILLISECONDS)))

(defn sample
  "Samples the observable sequence at each interval."
  [ms ob]
  #?(:cljs (.sampleTime ob ms)
     :clj  (.sample ob ^long ms TimeUnit/MILLISECONDS)))

(defn sample-when
  "Samples the observable sequence at each interval."
  [other ob]
  (.sample ob other))

(defn ignore
  "Ignores all elements in an observable sequence leaving
  only the termination messages."
  [ob]
  (.ignoreElements ob))

(defn dedupe
  "Returns an observable sequence that contains only
  distinct contiguous elements."
  ([ob]
   #?(:cljs (dedupe identity ob)
      :clj  (.distinctUntilChanged ob)))
  ([f ob]
   #?(:cljs (.distinctUntilChanged ob = f)
      :clj  (.distinctUntilChanged ob (as-function f)))))

(defn dedupe'
  "Returns an observable sequence that contains only d
  istinct elements.
  Usage of this operator should be considered carefully
  due to the maintenance of an internal lookup structure
  which can grow large."
  ([ob]
   #?(:cljs (.distinct ob =)
      :clj  (.distinct ob)))
  ([f ob]
   #?(:cljs (.distinct ob = f)
      :clj  (.distinct ob (as-function f)))))

(defn buffer
  "Projects each element of an observable sequence into zero
  or more buffers which are produced based on element count
  information."
  ([n ob]
   #?(:cljs (.bufferCount ob n)
      :clj  (.buffer ob (int n))))
  ([n skip ob]
   #?(:cljs (.bufferCount ob n skip)
      :clj  (.buffer ob (int n) (int skip)))))

(defn buffer-time
  "Buffers the source Observable values for a specific time period."
  [ms ob]
  #?(:cljs (.bufferTime ob ms)
     :clj (.buffer ob ms TimeUnit/MILLISECONDS)))

(defn retry
  "Given an optional number of retries and an observable,
  repeats the source observable the specified number of
  times or until it terminates. If no number of retries
  is given, it will be retried indefinitely."
  ([ob]
   (.retry ob))
  ([n ob]
   (.retry ob ^long n)))

(defn transform
  "Transform the observable sequence using transducers."
  [xform stream]
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
                    disposable (on-value stream step)]
                (on-complete stream #(do (xsink nil)
                                         (sink end)))
                (fn []
                  (cancel! disposable)))))))

;; --- Schedulers

#?(:cljs
   (defn scheduler
     "Get the scheduler instance by type.
     The posible types are: `:asap`, `:async`, `:queue`.
     Old `:trampoline` type is renamed as `:queue` and is deprecated."
     [type]
     (case type
       :asap (.-asap Scheduler)
       :async (.-async Scheduler)
       :queue (.-queue Scheduler)
       :trampoline (.-queue Scheduler)))
   :clj
   (defn scheduler
     "Get the scheduler instance by type. The possible
     types are: `:computation`, `:io`, `:single`,
     `:thread` and `:trampoline`."
     [type]
     (case type
       :computation (Schedulers/computation)
       :io (Schedulers/io)
       :single (Schedulers/single)
       :thread (Schedulers/newThread)
       :trampoline (Schedulers/trampoline))))

(defn observe-on
  [schd ob]
  (cond
    (scheduler? schd)
    (.observeOn ob ^Scheduler schd)

    (keyword? schd)
    (.observeOn ob ^Scheduler (scheduler schd))

    :else
    (throw (ex-info "Invalid argument" {:type ::invalid-argument}))))

(defn subscribe-on
  [schd ob]
  (cond
    (scheduler? schd)
    (.subscribeOn ob ^Scheduler schd)

    (keyword? schd)
    (.subscribeOn ob ^Scheduler (scheduler schd))

    :else
    (throw (ex-info "Invalid argument" {:type ::invalid-argument}))))
