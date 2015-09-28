(ns beicon.core
  (:require [beicon.extern.rxjs]
            [cats.protocols :as p]
            [cats.context :as ctx])
  (:refer-clojure :exclude [true? map filter reduce merge repeat repeatedly zip
                            dedupe drop take take-while concat partition]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Predicates
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

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
   {:pre [(number? v)]}
   (js/Rx.Observable.repeat v n)))

(defn publish
  "Create a connectable (hot) observable
  from other observable."
  ([ob]
   (publish ob true))
  ([ob connect?]
   {:pre [(observable? ob)]}
   (let [ob' (.publish ob)]
     (when connect?
       (.connect ob'))
     ob')))

(defn connect!
  "Connect the connectable observable."
  [ob]
  {:pre [(connectable? ob)]}
  (.connect ob))

(defn from-coll
  "Generates an observable sequence from collection."
  [coll]
  (let [array (into-array coll)]
    (js/Rx.Observable.fromArray array)))

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

(defn once
  "Returns an observable sequence that contains
  a single element."
  [v]
  (js/Rx.Observable.just v))

(defn never
  "Returns an observable sequence that is already
  in end state."
  []
  (create (fn [sink]
            (sink nil))))

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
  [b v]
  {:pre [(bus? b)]}
  (.onNext b v))

(defn error!
  "Pushes the given error to the bus stream."
  [b e]
  {:pre [(bus? b)]}
  (.onError b e))

(defn end!
  "Ends the given bus stream."
  [b]
  {:pre [(bus? b)]}
  (.onCompleted b))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Observable Subscription
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn on-value
  "Subscribes a function to invoke for each element
  in the observable sequence."
  [ob f]
  {:pre [(observable? ob) (fn? f)]}
  (let [disposable (.subscribeOnNext ob f)]
    #(.dispose disposable)))

(defn on-error
  "Subscribes a function to invoke upon exceptional termination
  of the observable sequence."
  [ob f]
  {:pre [(observable? ob) (fn? f)]}
  (let [disposable (.subscribeOnError ob f)]
    #(.dispose disposable)))

(defn on-end
  "Subscribes a function to invoke upon graceful termination
  of the observable sequence."
  [ob f]
  {:pre [(observable? ob) (fn? f)]}
  (let [disposable (.subscribeOnCompleted ob f)]
    #(.dispose disposable)))

(defn subscribe
  "Subscribes an observer to the observable sequence."
  [ob nf ef cf]
  {:pre [(observable? ob)]}
  (let [disposable (.subscribe ob nf ef cf)]
    #(.dispose disposable)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Observable Transformations
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn choice
  "Create an observable that surfaces any of the given
  sequences, whichever reacted first."
  ([a b]
   {:pre [(observable? a)
          (observable? b)]}
   (.amb a b))
  ([a b & more]
   (cljs.core/reduce choice (choice a b) more)))

(defn zip
  "Merges the specified observable sequences or Promises
  into one observable sequence."
  [a b]
  {:pre [(observable? a)
         (observable? b)]}
  (.zip a b vector))

(defn concat
  "Concatenates all of the specified observable
  sequences, as long as the previous observable
  sequence terminated successfully."
  ([a b]
   {:pre [(observable? a)
          (observable? b)]}
   (.concat a b))
  ([a b & more]
   (cljs.core/reduce concat (concat a b) more)))

(defn merge
  "Merges all the observable sequences and Promises
  into a single observable sequence."
  ([a b]
   {:pre [(observable? a)
          (observable? b)]}
   (.merge a b))
  ([a b & more]
   (cljs.core/reduce merge (merge a b) more)))

(defn filter
  "Filters the elements of an observable sequence
  based on a predicate."
  [f ob]
  {:pre [(observable? ob)]}
  (.filter ob #(boolean (f %))))

(defn map
  "Apply a function to each element of an observable
  sequence."
  [f ob]
  {:pre [(observable? ob)]}
  (.map ob #(f %)))

(defn flat-map
  "Projects each element of an observable sequence to
  an observable sequence and merges the resulting
  observable sequences or Promises or array/iterable
  into one observable sequence."
  [f ob]
  {:pre [(observable? ob)]}
  (.flatMap ob #(f %)))

(defn skip
  "Bypasses a specified number of elements in an
  observable sequence and then returns the remaining
  elements."
  [n ob]
  {:pre [(observable? ob) (number? n)]}
  (.skip ob n))

(defn skip-while
  "Bypasses elements in an observable sequence as long
  as a specified condition is true and then returns the
  remaining elements."
  [f ob]
  {:pre [(observable? ob) (fn? f)]}
  (.skipWhile ob #(boolean (f %))))

(defn skip-until
  "Returns the values from the source observable sequence
  only after the other observable sequence produces a value."
  [pob ob]
  {:pre [(observable? ob) (observable? pob)]}
  (.skipUntil ob pob))

(defn take
  "Bypasses a specified number of elements in an
  observable sequence and then returns the remaining
  elements."
  [n ob]
  {:pre [(observable? ob) (number? n)]}
  (.take ob n))

(defn slice
  "Returns a shallow copy of a portion of an Observable
  into a new Observable object."
  ([begin ob]
   {:pre [(observable? ob) (number? begin)]}
   (.slice ob begin))
  ([begin end ob]
   {:pre [(observable? ob) (number? begin) (number? end)]}
   (.slice ob begin end)))

(defn take-while
  "Returns elements from an observable sequence as long as a
  specified predicate returns true."
  [f ob]
  {:pre [(observable? ob) (fn? f)]}
  (.takeWhile ob f))

(defn reduce
  "Applies an accumulator function over an observable
  sequence, returning the result of the aggregation as a
  single element in the result sequence."
  ([f ob]
   {:pre [(observable? ob) (fn? f)]}
   (.reduce ob f))
  ([f seed ob]
   {:pre [(observable? ob) (fn? f)]}
   (.reduce ob f seed)))

(defn tap
  "Invokes an action for each element in the
  observable sequence."
  [f ob]
  {:pre [(observable? ob) (fn? f)]}
  (.tap ob f))

(defn throttle
  "Returns an Observable that emits only the first item
  emitted by the source Observable during sequential
  time windows of a specified duration."
  [ms ob]
  {:pre [(observable? ob) (number? ms)]}
  (.throttle ob ms))

(defn ignore
  "Ignores all elements in an observable sequence leaving
  only the termination messages."
  [ob]
  {:pre [(observable? ob)]}
  (.ignoreElements ob))

(defn pausable
  "Pauses the underlying observable sequence based upon the
  observable sequence which yields true/false.

  WARNING: The buffered pausable only works with hot
  observables."
  ([pauser ob]
   (pausable pauser false ob))
   ;; {:pre [(observable? ob) (observable? pauser)]}
   ;; (.pausable ob pauser))
  ([pauser buffer? ob]
   {:pre [(observable? ob) (observable? pauser)]}
   (if buffer?
     (.pausableBuffered ob pauser)
     (.pausable ob pauser))))

(defn dedupe
  "Returns an observable sequence that contains only
  distinct contiguous elements."
  ([ob]
   (.distinctUntilChanged ob))
  ([f ob]
   (.distinctUntilChanged ob f)))

(defn dedupe'
  "Returns an observable sequence that contains only d
  istinct elements.
  Usage of this operator should be considered carefully
  due to the maintenance of an internal lookup structure
  which can grow large."
  ([ob]
   (.distinct ob))
  ([f ob]
   (.distinct ob f)))

(defn buffer
  "Projects each element of an observable sequence into zero
  or more buffers which are produced based on element count
  information."
  ([n ob]
   (.bufferWithCount ob n))
  ([n skip ob]
   (.bufferWithCount ob n skip)))

(defn to-atom
  "Materialize the observable sequence into an atom."
  ([ob]
   (let [a (atom nil)]
     (to-atom a ob)))
  ([a ob]
   {:pre [(observable? ob)]}
   (on-value ob #(reset! a %))
   a)
  ([a ob f]
   {:pre [(observable? ob)]}
   (on-value ob #(swap! a f %))
   a))

(defn to-observable
  "Hides the identity of an observable sequence."
  [b]
  {:pre [(or (observable? b) (bus? b))]}
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
;; Cats Integration
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(def observable-context
  (reify
    p/Context
    (-get-level [_] ctx/+level-default+)

    p/Functor
    (-fmap [_ f obs]
      (map f obs))

    p/Applicative
    (-pure [_ v]
      (once v))

    (-fapply [_ pf pv]
      (.zip pf pv #(%1 %2)))

    p/Monad
    (-mreturn [_ v]
      (once v))

    (-mbind [_ mv f]
      (flat-map f mv))))

(extend-protocol p/Contextual
  js/Rx.Observable
  (-get-context [_] observable-context)

  js/Rx.Subject
  (-get-context [_] observable-context)

  js/Rx.ConnectableObservable
  (-get-context [_] observable-context))
