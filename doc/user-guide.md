# User Guide

## Introduction

_beicon_ is a small and concise library that provides reactive streams
API for ClojureScript, wrapping RxJS 8.x.

### Install

The simplest way to use _beicon_ in a ClojureScript project, is by including
it the following dependency:

```clojure
funcool/beicon2
{:git/tag "v2.0"
 :git/sha "e7135e0"
 :git/url "https://github.com/funcool/beicon.git"}
```

## Creating Streams

This section covers the available methods for creating observable streams.

### From a collection

The most basic way to create a stream is to just take a collection
and convert it into an observable sequence:

```clojure
(require '[beicon.v2.core :as rx])

(def stream (rx/from [1 2 3]))

(rx/sub! stream #(println "v:" %))
;; ==> v: 1
;; ==> v: 2
;; ==> v: 3
```

### From range

Another way to create an observable stream is using the `range` constructor,
which is pretty analogous to Clojure's `range`:

```clojure
(def stream (rx/range 3))

(rx/sub! stream #(println "v:" %))
;; ==> v: 0
;; ==> v: 1
;; ==> v: 2
```

### From Atom

Atoms in Clojure are watchable, so you can listen for their
changes. This method converts those changes into an infinite observable
sequence of atom changes:

```clojure
(def a (atom 1))

(def stream (rx/from-atom a))

(rx/sub! stream #(println "v:" %))
(swap! a inc)
;; ==> v: 2
```

You can also emit the current value immediately:

```clojure
(def a (atom 1))
(def stream (rx/from-atom a {:emit-current-value? true}))
(rx/sub! stream #(println "v:" %))
;; ==> v: 1
```

### From values

There is a way to create an observable sequence from
multiple values, using the `of` constructor:

```clojure
(def stream (rx/of 1 2 3))

(rx/sub! stream #(println "v:" %))
;; ==> v: 1
;; ==> v: 2
;; ==> v: 3
```

### Empty

Sometimes you also want just a terminated stream:

```clojure
(def stream (rx/empty))
```

This stream does not yield any value and just terminates.

### Timer and Interval

Create a stream that emits after a delay:

```clojure
(def stream (rx/timer 1000))
(rx/sub! stream #(println "v:" %))
;; After 1 sec...
;; ==> v: 0
```

Or emit repeatedly at intervals:

```clojure
(def stream (->> (rx/interval 1000)
                 (rx/take 3)))
(rx/sub! stream #(println "v:" %))
;; After 1 sec...
;; ==> v: 0
;; After 2 sec...
;; ==> v: 1
;; After 3 sec...
;; ==> v: 2
```

### From factory

This is the most advanced and flexible way to create an observable
sequence. It allows you to have control about termination and errors, and
is intended to be used for building other kinds of constructors.

```clojure
(def stream
  (rx/create (fn [subs]
               (rx/push! subs 1)          ;; next with `1` as value
               (rx/push! subs 2)          ;; next with `2` as value
               (rx/end! subs)             ;; end the stream
               (fn []
                 ;; function called on unsubscription
                 ))))

(rx/sub! stream #(println "v:" %))
;; ==> v: 1
;; ==> v: 2
```

### Defer

Creates an observable that defers the creation of the actual observable
until a subscriber subscribes. Useful for lazy initialization:

```clojure
(def stream (rx/defer (fn [] (rx/of (rand-int 100)))))

(rx/sub! stream #(println "v:" %))
;; ==> v: 42
(rx/sub! stream #(println "v:" %))
;; ==> v: 17
```

### Generate

Generates an observable sequence by running a state-driven loop:

```clojure
(def stream (rx/generate 0
                         (fn [x] (< x 5))
                         (fn [x] (inc x))
                         (fn [x] (* x 2))))

(rx/sub! stream #(println "v:" %))
;; ==> v: 0
;; ==> v: 2
;; ==> v: 4
;; ==> v: 6
;; ==> v: 8
```

### From Event Pattern

Creates an observable from an event pattern (add/remove handler functions):

```clojure
(def handlers (atom []))

(def add-handler (fn [handler] (swap! handlers conj handler)))
(def remove-handler (fn [handler] (swap! handlers #(remove #{handler} %))))

(def stream (rx/from-event-pattern add-handler remove-handler))

(rx/sub! stream #(println "v:" %))

;; Trigger the event
(doseq [h @handlers] (h "test"))
;; ==> v: test
```

### Bind Callback

Converts a callback-based function to a function that returns an observable:

```clojure
(defn async-fetch [url callback]
  (js/setTimeout #(callback "data from " url) 100))

(def fetch-obs (rx/bind-callback async-fetch))

(rx/sub! (fetch-obs "https://api.example.com")
         #(println "v:" %))
;; After 100ms...
;; ==> v: data from https://api.example.com
```

### Bind Node Callback

Converts a Node.js callback-based function (with error-first callback) to an observable:

```clojure
(defn node-fs-read [path callback]
  (js/setTimeout #(callback nil "file content") 100))

(def read-obs (rx/bind-node-callback node-fs-read))

(rx/sub! (read-obs "/path/to/file")
         #(println "v:" %))
;; ==> v: file content
```

### Using

Creates an observable that depends on a resource, disposing it when unsubscribed:

```clojure
(def resource (fn [] #js {:unsubscribe #(println "disposed!")}))
(def observable-fn (fn [r] (rx/of "value")))

(def stream (rx/using resource observable-fn))

(rx/sub! stream #(println "v:" %))
;; ==> v: value
;; disposed!
```

## Consuming Streams

### The stream states

The observable sequence can be in three different kind of states:
*alive*, *"errored"* or *ended*. If an error is emitted the stream can
be considered ended with an error. So *error* or *end* states are
considered termination states.

And for convenience you can subscribe to any of that states of an
observable sequence.

### General purpose

A general purpose subscription is one that allows you to create one
subscription, that watches all the different possible states of an
observable sequence:

```clojure
(def sub (rx/sub! stream
                  #(println "on-value:" %)
                  #(println "on-error:" %)
                  #(println "on-end:")))
```

The return value of the `subscribe` function is a subscription object,
that identifies the current subscription. It can be cancelled by
executing `(rx/dispose! sub)`.

There is also the `subs!` function useful for `->>` ready call
convention (expects the `observable` on the last argument
position instead of the first position).

### Converting to Promise

You can convert an observable to a promise for the first or last value:

```clojure
(def stream (rx/from [1 2 3]))

(-> (rx/first-value-from stream)
    (.then #(println "first:" %)))
;; ==> first: 1

(-> (rx/last-value-from stream)
    (.then #(println "last:" %)))
;; ==> last: 3
```

## Transformations

There are two call conventions here:

- The familiar fluent API, which just works like any clojure sequence
  transformations functions (`map`, `filter`, ...)
- The RxJS composition API, using `rx/pipe` and `rx/comp`.

Let's see the `filter` example to understand the differences.

### Filter & Map

The main advantage of using reactive streams is that you may treat
them like normal sequences, and in this case apply a function and then
filter them with a predicate. Let's use the fluent API:

```clojure
(def stream
  (->> (rx/from [1 2 3 4 5])
       (rx/map inc)
       (rx/filter #(> % 3))))

(rx/sub! stream
         #(println "on-next:" %)
         #(println "on-error:" %)
         #(println "on-end"))

;; ==> on-next: 4
;; ==> on-next: 5
;; ==> on-next: 6
;; ==> on-end
```

The same can be expressed using the composition API and operators:

```clojure
(require '[beicon.v2.operators :as rxo])

(def stream
  (->> (rx/from [1 2 3 4 5])
       (rx/pipe (rxo/map inc))
       (rx/pipe (rxo/filter #(> % 3)))))

(rx/subs! stream
          #(println "on-next:" %)
          #(println "on-error:" %)
          #(println "on-end"))
```

We also use the `subs!` helper for subscribing to the resulting
observable in a single expression with `->>`.

And finally, you can compose the transformation and later use it
in the same way as transducers:

```clojure
(def rxform
  (rx/comp (rxo/map inc)
           (rxo/filter #(> % 3))))

(def stream
  (->> (rx/from [1 2 3 4 5])
       (rx/pipe rxform)))

(rx/subs! stream
          #(println "on-next:" %)
          #(println "on-error:" %)
          #(println "on-end"))
```

NOTE: Functions in the `beicon.v2.operators` namespace are operator-only.

### Map To

Maps all values to a constant:

```clojure
(def stream (->> (rx/from [1 2 3])
                 (rx/map-to :done)))

(rx/sub! stream #(println "v:" %))
;; ==> v: :done
;; ==> v: :done
;; ==> v: :done
```

### Map Indexed

Same as `map` but also provides the index:

```clojure
(def stream (->> (rx/from [:a :b :c])
                 (rx/map-indexed (fn [i v] [i v]))))

(rx/sub! stream #(println "v:" %))
;; ==> v: [0 :a]
;; ==> v: [1 :b]
;; ==> v: [2 :c]
```

### Find

Finds the first value matching a predicate:

```clojure
(def stream (->> (rx/from [1 2 3 4 5])
                 (rx/find #(> % 3))))

(rx/sub! stream #(println "v:" %))
;; ==> v: 4
```

### Find Index

Finds the index of the first value matching a predicate:

```clojure
(def stream (->> (rx/from [1 2 3 4 5])
                 (rx/find-index #(> % 3))))

(rx/sub! stream #(println "v:" %))
;; ==> v: 3
```

### Element At

Emits the value at a specific index:

```clojure
(def stream (->> (rx/from [10 20 30 40 50])
                 (rx/element-at 2)))

(rx/sub! stream #(println "v:" %))
;; ==> v: 30
```

With a default value if index is out of range:

```clojure
(def stream (->> (rx/from [10 20])
                 (rx/element-at 5 :default)))

(rx/sub! stream #(println "v:" %))
;; ==> v: :default
```

### Distinct

Removes duplicate values:

```clojure
(def stream (->> (rx/from [1 2 1 3 2 4])
                 (rx/distinct)))

(rx/sub! stream #(println "v:" %))
;; ==> v: 1
;; ==> v: 2
;; ==> v: 3
;; ==> v: 4
```

### Distinct Until Key Changed

Filters items that have the same key as the previous item:

```clojure
(def stream (->> (rx/from [{:id 1 :name "a"}
                           {:id 1 :name "b"}
                           {:id 2 :name "c"}])
                 (rx/distinct-until-key-changed :id)))

(rx/sub! stream #(println "v:" %))
;; ==> v: {:id 1 :name "a"}
;; ==> v: {:id 2 :name "c"}
```

### Skip

Also, sometimes you just want to skip values from stream by different criteria.

You can skip the first N values:

```clojure
(def stream (->> (rx/from [1 2 3 4 5 6])
                 (rx/skip 4)))

(rx/sub! stream
         #(println "on-value:" %)
         #(println "on-error:" %)
         #(println "on-end"))

;; ==> on-value: 5
;; ==> on-value: 6
;; ==> on-end
```

Skip while some predicate evaluates to `true`:

```clojure
(def stream (->> (rx/from [1 1 1 1 2 3])
                 (rx/skip-while odd?)))

(rx/sub! stream
         #(println "on-value:" %)
         #(println "on-error:" %)
         #(println "on-end"))

;; ==> on-value: 2
;; ==> on-value: 3
;; ==> on-end
```

Or skip until another observable yields a value with `skip-until`:

```clojure
(def notifier (rx/timer 1000))
(def stream (->> (rx/interval 100)
                 (rx/skip-until notifier)
                 (rx/take 3)))

(rx/sub! stream #(println "v:" %))
;; After 1 sec...
;; ==> v: 10
;; ==> v: 11
;; ==> v: 12
```

Skip the last N values:

```clojure
(def stream (->> (rx/from [1 2 3 4 5])
                 (rx/skip-last 2)))

(rx/sub! stream #(println "v:" %))
;; ==> v: 1
;; ==> v: 2
;; ==> v: 3
```

### Take

You can also limit the observable sequence to a specified number of
elements:

```clojure
(def stream (->> (rx/from [1 1 1 1 2 3])
                 (rx/take 2)))

(rx/sub! stream
         #(println "on-value:" %)
         #(println "on-error:" %)
         #(println "on-end"))

;; ==> on-value: 1
;; ==> on-value: 1
;; ==> on-end
```

Or take while a predicate evaluates to `true`:

```clojure
(def stream (->> (rx/from [1 1 1 1 2 3])
                 (rx/take-while odd?)))

(rx/sub! stream
         #(println "on-value:" %)
         #(println "on-error:" %)
         #(println "on-end"))

;; ==> on-value: 1
;; ==> on-value: 1
;; ==> on-value: 1
;; ==> on-value: 1
;; ==> on-end
```

Take until another observable emits:

```clojure
(def stopper (rx/timer 500))
(def stream (->> (rx/interval 100)
                 (rx/take-until stopper)))

(rx/sub! stream #(println "v:" %))
;; ==> v: 0
;; ==> v: 1
;; ==> v: 2
;; ==> v: 3
;; ==> v: 4
```

Take the last N values:

```clojure
(def stream (->> (rx/from [1 2 3 4 5])
                 (rx/take-last 2)))

(rx/sub! stream #(println "v:" %))
;; ==> v: 4
;; ==> v: 5
```

### Reduce

Allows combining all results of an observable sequence using a
combining function (also called *reducing* function):

```clojure
(def stream (->> (rx/from [1 2 3 4])
                 (rx/reduce + 0)))

(rx/sub! stream
         #(println "on-value:" %)
         #(println "on-error:" %)
         #(println "on-end"))

;; ==> on-value: 10
;; ==> on-end
```

### Scan

Like `reduce` (see above), but returns a stream of each intermediate
result instead (similar to `reductions` in Clojure):

```clojure
(def stream (->> (rx/from [1 2 3 4])
                 (rx/scan + 0)))

(rx/sub! stream
         #(println "on-value:" %)
         #(println "on-error:" %)
         #(println "on-end"))

;; ==> on-value: 1
;; ==> on-value: 3
;; ==> on-value: 6
;; ==> on-value: 10
;; ==> on-end
```

### Count

Counts the number of emissions:

```clojure
(def stream (->> (rx/from [1 2 3 4 5])
                 (rx/count)))

(rx/sub! stream #(println "v:" %))
;; ==> v: 5
```

With a predicate to count matching values:

```clojure
(def stream (->> (rx/from [1 2 3 4 5])
                 (rx/count odd?)))

(rx/sub! stream #(println "v:" %))
;; ==> v: 3
```

### Max and Min

Emits the maximum or minimum value:

```clojure
(def stream (->> (rx/from [3 1 4 1 5 9 2 6])
                 (rx/max)))

(rx/sub! stream #(println "v:" %))
;; ==> v: 9

(def stream (->> (rx/from [3 1 4 1 5 9 2 6])
                 (rx/min)))

(rx/sub! stream #(println "v:" %))
;; ==> v: 1
```

### Every

Tests whether all values match a predicate:

```clojure
(def stream (->> (rx/from [2 4 6 8])
                 (rx/every even?)))

(rx/sub! stream #(println "v:" %))
;; ==> v: true

(def stream (->> (rx/from [2 3 6 8])
                 (rx/every even?)))

(rx/sub! stream #(println "v:" %))
;; ==> v: false
```

### Is Empty

Checks if the observable is empty:

```clojure
(def stream (->> (rx/empty)
                 (rx/is-empty)))

(rx/sub! stream #(println "v:" %))
;; ==> v: true

(def stream (->> (rx/from [1 2 3])
                 (rx/is-empty)))

(rx/sub! stream #(println "v:" %))
;; ==> v: false
```

### To Array

Collects all values into an array:

```clojure
(def stream (->> (rx/from [1 2 3])
                 (rx/to-array)))

(rx/sub! stream #(println "v:" (js->clj %)))
;; ==> v: [1 2 3]
```

### Pairwise

Emits pairs of consecutive values:

```clojure
(def stream (->> (rx/from [1 2 3 4])
                 (rx/pairwise)
                 (rx/map vec)))

(rx/sub! stream #(println "v:" %))
;; ==> v: [1 2]
;; ==> v: [2 3]
;; ==> v: [3 4]
```

### Buffer

This transformer function allows you to accumulate N values in a buffer
and then emit them as one value (similar to `partition` in Clojure):

```clojure
(def stream (->> (rx/from [1 2 3 4])
                 (rx/buffer 2)))

(rx/sub! stream
         #(println "on-value:" %)
         #(println "on-error:" %)
         #(println "on-end"))

;; ==> on-value: [1 2]
;; ==> on-value: [3 4]
;; ==> on-end
```

Buffer by time:

```clojure
(def stream (->> (rx/interval 100)
                 (rx/buffer-time 500)
                 (rx/take 2)))

(rx/sub! stream #(println "v:" (js->clj %)))
;; ==> v: [0 1 2 3 4]
;; ==> v: [5 6 7 8 9]
```

Buffer using opening/closing observables:

```clojure
(def openings (rx/interval 1000))
(def closing (fn [_] (rx/timer 500)))

(def stream (->> (rx/interval 100)
                 (rx/buffer-toggle openings closing)
                 (rx/take 3)))

(rx/sub! stream #(println "v:" (js->clj %)))
;; Emits buffers of values collected during each window
```

Buffer using a closing selector:

```clojure
(def stream (->> (rx/interval 100)
                 (rx/buffer-when (fn [_] (rx/timer 500)))
                 (rx/take 2)))

(rx/sub! stream #(println "v:" (js->clj %)))
```

### Window

Like buffer but emits observables instead of arrays:

```clojure
(def stream (->> (rx/from [1 2 3 4 5])
                 (rx/window-count 2)
                 (rx/merge-map #(rx/to-array %))
                 (rx/map vec)))

(rx/sub! stream #(println "v:" %))
;; ==> v: [1 2]
;; ==> v: [3 4]
;; ==> v: [5]
```

Window by time:

```clojure
(def stream (->> (rx/interval 100)
                 (rx/window-time 500)
                 (rx/merge-map #(rx/to-array %))
                 (rx/take 2)))

(rx/sub! stream #(println "v:" (js->clj %)))
```

Window using opening/closing observables:

```clojure
(def openings (rx/interval 1000))
(def closing (fn [_] (rx/timer 500)))

(def stream (->> (rx/interval 100)
                 (rx/window-toggle openings closing)
                 (rx/merge-map #(rx/to-array %))
                 (rx/take 3)))
```

Window using a closing selector:

```clojure
(def stream (->> (rx/interval 100)
                 (rx/window-when (fn [_] (rx/timer 500)))
                 (rx/merge-map #(rx/to-array %))
                 (rx/take 2)))
```

## Combining Streams

### Zip

This combinator combines two observable sequences in one:

```clojure
(def stream (rx/zip
              (rx/from [1 2 3])
              (rx/from [2 3 4])))

(rx/sub! stream
         #(println "on-value:" %)
         #(println "on-error:" %)
         #(println "on-end"))

;; ==> on-value: [1 2]
;; ==> on-value: [2 3]
;; ==> on-value: [3 4]
;; ==> on-end
```

With a projection function:

```clojure
(def stream (rx/zip +
                    (rx/from [1 2 3])
                    (rx/from [10 20 30])))

(rx/sub! stream #(println "v:" %))
;; ==> v: 11
;; ==> v: 22
;; ==> v: 33
```

### Zip With

Operator form of zip:

```clojure
(def stream (->> (rx/from [1 2 3])
                 (rx/zip-with (rx/from [4 5 6]))
                 (rx/map vec)))

(rx/sub! stream #(println "v:" %))
;; ==> v: [1 4]
;; ==> v: [2 5]
;; ==> v: [3 6]
```

### Zip All

Zips all inner observables together:

```clojure
(def stream (->> (rx/from [(rx/of 1 2) (rx/of 3 4)])
                 (rx/zip-all)
                 (rx/map vec)))

(rx/sub! stream #(println "v:" %))
;; ==> v: [1 3]
;; ==> v: [2 4]
```

### Concat

This combinator concatenates two or more observable sequences *in order*:

```clojure
(def stream (rx/concat
              (rx/from [1 2])
              (rx/from [3 4])))

(rx/sub! stream
         #(println "on-value:" %)
         #(println "on-error:" %)
         #(println "on-end"))

;; ==> on-value: 1
;; ==> on-value: 2
;; ==> on-value: 3
;; ==> on-value: 4
;; ==> on-end
```

It ignores nil values from arguments.

### Concat With

Operator form of concat:

```clojure
(def stream (->> (rx/from [1 2])
                 (rx/concat-with (rx/from [3 4]))))

(rx/sub! stream #(println "v:" %))
;; ==> v: 1
;; ==> v: 2
;; ==> v: 3
;; ==> v: 4
```

### Concat All

Flattens an observable of observables by concatenating:

```clojure
(def stream (->> (rx/from [(rx/of 1 2) (rx/of 3 4)])
                 (rx/concat-all)))

(rx/sub! stream #(println "v:" %))
;; ==> v: 1
;; ==> v: 2
;; ==> v: 3
;; ==> v: 4
```

### Merge

This combinator merges two or more observable sequences *at random* (see
`concat` for ordered):

```clojure
(def stream (rx/merge
              (rx/from [1 2])
              (rx/from [3 4])))

(rx/sub! stream
         #(println "on-value:" %)
         #(println "on-error:" %)
         #(println "on-end"))

;; ==> on-value: 1
;; ==> on-value: 3
;; ==> on-value: 2
;; ==> on-value: 4
;; ==> on-end
```

It ignores nil values from arguments.

### Merge With

Operator form of merge:

```clojure
(def stream (->> (rx/from [1 2])
                 (rx/merge-with (rx/from [3 4]))))

(rx/sub! stream #(println "v:" %))
;; ==> v: 1
;; ==> v: 3
;; ==> v: 2
;; ==> v: 4
```

### Merge All

Flattens an observable of observables by merging:

```clojure
(def stream (->> (rx/from [(rx/of 1 2) (rx/of 3 4)])
                 (rx/merge-all)))

(rx/sub! stream #(println "v:" %))
;; ==> v: 1
;; ==> v: 3
;; ==> v: 2
;; ==> v: 4
```

### Combine Latest

Combines the latest values from multiple observables:

```clojure
(def stream (rx/combine-latest
              (rx/of 1)
              (rx/of 2)))

(rx/sub! stream #(println "v:" (js->clj %)))
;; ==> v: [1 2]
```

As an operator:

```clojure
(def stream (->> (rx/from [1 2 3])
                 (rx/combine-latest-with (rx/of :a))
                 (rx/map vec)))

(rx/sub! stream #(println "v:" %))
;; ==> v: [1 :a]
;; ==> v: [2 :a]
;; ==> v: [3 :a]
```

### Combine Latest All

Combines latest values from all inner observables:

```clojure
(def stream (->> (rx/from [(rx/of 1 2) (rx/of :a :b)])
                 (rx/combine-latest-all)
                 (rx/map vec)))

(rx/sub! stream #(println "v:" %))
;; Emits combinations of latest values
```

### Race With

Returns the observable that emits first:

```clojure
(def slow (->> (rx/from [:slow])
               (rx/delay 1000)))
(def fast (rx/of :fast))

(def stream (->> slow (rx/race-with fast)))

(rx/sub! stream #(println "v:" %))
;; ==> v: :fast
```

### Partition

Splits an observable into two based on a predicate:

```clojure
(def stream (rx/from [1 2 3 4 5 6]))
(def result (rx/partition even? stream))
(def evens (aget result 0))
(def odds (aget result 1))

(rx/sub! evens #(println "even:" %))
(rx/sub! odds #(println "odd:" %))
;; ==> even: 2
;; ==> odd: 1
;; ==> even: 4
;; ==> odd: 3
;; ==> even: 6
;; ==> odd: 5
```

### With Latest From

Merges observables, using the latest value from others when source emits:

```clojure
(def stream (->> (rx/from [1 2 3])
                 (rx/with-latest-from (rx/of :a))
                 (rx/map vec)))

(rx/sub! stream #(println "v:" %))
;; ==> v: [1 :a]
;; ==> v: [2 :a]
;; ==> v: [3 :a]
```

## Flattening Higher-Order Observables

### Merge Map

Projects each element to an observable and merges the results:

```clojure
(def stream (->> (rx/from [1 2])
                 (rx/merge-map #(rx/from (range % (+ % 2))))))

(rx/sub! stream
         #(println "on-value:" %)
         #(println "on-error:" %)
         #(println "on-end"))

;; ==> on-value: 1
;; ==> on-value: 2
;; ==> on-value: 2
;; ==> on-value: 3
;; ==> on-end
```

### Merge Map To

Projects each source value to the same observable, merging results:

```clojure
(def stream (->> (rx/from [1 2 3])
                 (rx/merge-map-to (rx/of :x))))

(rx/sub! stream #(println "v:" %))
;; ==> v: :x
;; ==> v: :x
;; ==> v: :x
```

### Switch Map

Projects each element to an observable, switching to the new one and
discarding previous:

```clojure
(def stream (->> (rx/from [1 2 3])
                 (rx/switch-map #(rx/of (* % 10)))))

(rx/sub! stream #(println "v:" %))
;; ==> v: 10
;; ==> v: 20
;; ==> v: 30
```

### Switch Map To

Projects each source value to the same observable, switching:

```clojure
(def stream (->> (rx/from [1 2 3])
                 (rx/switch-map-to (rx/of :x))))

(rx/sub! stream #(println "v:" %))
;; ==> v: :x
;; ==> v: :x
;; ==> v: :x
```

### Switch All

Flattens using switchMap semantics:

```clojure
(def stream (->> (rx/from [(rx/of 1 2) (rx/of 3 4)])
                 (rx/switch-all)))

(rx/sub! stream #(println "v:" %))
;; ==> v: 1
;; ==> v: 2
;; ==> v: 3
;; ==> v: 4
```

### Mapcat (Concat Map)

Projects each element to an observable and concatenates:

```clojure
(def stream (->> (rx/from [1 2])
                 (rx/mapcat #(rx/from (range % (+ % 2))))))

(rx/sub! stream #(println "v:" %))
;; ==> v: 1
;; ==> v: 2
;; ==> v: 2
;; ==> v: 3
```

### Concat Map To

Projects each source value to the same observable, concatenating:

```clojure
(def stream (->> (rx/from [1 2 3])
                 (rx/concat-map-to (rx/of :x))))

(rx/sub! stream #(println "v:" %))
;; ==> v: :x
;; ==> v: :x
;; ==> v: :x
```

### Exhaust Map

Ignores new emissions while the inner observable is running:

```clojure
(def stream (->> (rx/from [1 2 3])
                 (rx/exhaust-map (fn [v]
                                   (->> (rx/of (* v 10))
                                        (rx/delay 100))))))

(rx/sub! stream #(println "v:" %))
;; ==> v: 10
;; (2 and 3 are ignored because inner observable is still running)
```

### Exhaust All

Flattens using exhaustMap semantics:

```clojure
(def stream (->> (rx/from [(rx/of 1 2) (rx/of 3 4)])
                 (rx/exhaust-all)))

(rx/sub! stream #(println "v:" %))
;; ==> v: 1
;; ==> v: 2
;; ==> v: 3
;; ==> v: 4
```

### Switch Scan

Like merge-scan but uses switchMap semantics:

```clojure
(def stream (->> (rx/from [1 2 3])
                 (rx/switch-scan (fn [acc v] (rx/of (+ acc v))) 0)))

(rx/sub! stream #(println "v:" %))
;; ==> v: 1
;; ==> v: 3
;; ==> v: 6
```

### Merge Scan

Accumulator that returns an observable, merging intermediate results:

```clojure
(def stream (->> (rx/from [4 5 6])
                 (rx/merge-scan (fn [acc i] (rx/of (conj acc i))) [1])))

(rx/sub! stream #(println "v:" %))
;; ==> v: [1 4]
;; ==> v: [1 4 5]
;; ==> v: [1 4 5 6]
```

## Error Handling

### Catch

Continues with another observable on error:

```clojure
(def stream (->> (rx/throw (ex-info "error" {:type :bad}))
                 (rx/catch (fn [e]
                             (rx/of (ex-data e))))))

(rx/sub! stream #(println "v:" %))
;; ==> v: {:type :bad}
```

With predicate filtering:

```clojure
(def type1? #(= 1 (:type (ex-data %))))

(def stream (->> (rx/throw (ex-info "error" {:type 1}))
                 (rx/catch type1? #(rx/of :recovered))))

(rx/sub! stream #(println "v:" %))
;; ==> v: :recovered
```

### On Error Resume Next

Continues with another observable when the source errors:

```clojure
(def stream (->> (rx/create (fn [sink]
                              (rx/push! sink 1)
                              (rx/push! sink 2)
                              (rx/error! sink (js/Error. "oops"))))
                 (rx/on-error-resume-next (rx/from [3 4]))))

(rx/sub! stream #(println "v:" %))
;; ==> v: 1
;; ==> v: 2
;; ==> v: 3
;; ==> v: 4
```

### Retry

Retries the source observable on error:

```clojure
(def errored? (volatile! false))

(def stream (rx/create (fn [sink]
                         (if @errored?
                           (do
                             (rx/push! sink 2)
                             (rx/push! sink 3)
                             (rx/end! sink))
                           (do
                             (vreset! errored? true)
                             (rx/error! sink (js/Error.)))))))

(def stream (->> stream (rx/retry 2)))

(rx/sub! stream #(println "v:" %))
;; ==> v: 2
;; ==> v: 3
```

### Retry When

Retries when the notifier emits:

```clojure
(def attempt (atom 0))

(def stream (->> (rx/create (fn [sink]
                              (swap! attempt inc)
                              (if (< @attempt 3)
                                (rx/error! sink (js/Error. "fail"))
                                (do
                                  (rx/push! sink :success)
                                  (rx/end! sink)))))
                 (rx/retry-when (fn [errors] (rx/delay 100 errors)))))

(rx/sub! stream #(println "v:" %))
;; ==> v: :success
```

### Repeat

Repeats the source observable:

```clojure
(def stream (->> (rx/from [1 2])
                 (rx/repeat 3)))

(rx/sub! stream #(println "v:" %))
;; ==> v: 1
;; ==> v: 2
;; ==> v: 1
;; ==> v: 2
;; ==> v: 1
;; ==> v: 2
```

### Repeat When

Repeats when the notifier emits:

```clojure
(def retry-count (atom 0))

(def stream (->> (rx/create (fn [sink]
                              (swap! retry-count inc)
                              (rx/push! sink @retry-count)
                              (rx/end! sink)))
                 (rx/repeat-when (fn [notifier] (rx/take 2 notifier)))))

(rx/sub! stream #(println "v:" %))
;; ==> v: 1
;; ==> v: 2
```

### Throw If Empty

Throws an error if the source completes without emitting:

```clojure
(def stream (->> (rx/from [1 2 3])
                 (rx/throw-if-empty #(js/Error. "empty"))))

(rx/sub! stream #(println "v:" %))
;; ==> v: 1
;; ==> v: 2
;; ==> v: 3

(def stream (->> (rx/empty)
                 (rx/throw-if-empty #(js/Error. "empty"))))

(rx/sub! stream
         (fn [_] (println "v:" %))
         (fn [e] (println "error:" (.-message e))))
;; ==> error: empty
```

## Time-Based Operators

### Delay

Delays emissions by a specified time:

```clojure
(def stream (->> (rx/from [1 2 3])
                 (rx/delay 1000)))

(rx/sub! stream #(println "v:" %))
;; After 1 sec...
;; ==> v: 1
;; ==> v: 2
;; ==> v: 3
```

### Delay When

Delays each emission based on a selector function:

```clojure
(def stream (->> (rx/from [1 2 3])
                 (rx/delay-when (fn [v] (rx/timer (* v 100))))))

(rx/sub! stream #(println "v:" %))
;; ==> v: 1 (after 100ms)
;; ==> v: 2 (after 200ms)
;; ==> v: 3 (after 300ms)
```

### Delay At Least

Ensures at least a minimum delay:

```clojure
(def stream (->> (rx/from [1 2 3])
                 (rx/delay-at-least 500)))

(rx/sub! stream #(println "v:" %))
;; All values emitted with at least 500ms delay
```

### Timeout

Emits an error if the source doesn't emit within a time window:

```clojure
(def stream (->> (rx/timer 2000)
                 (rx/timeout 1000 (rx/of :timeout))))

(rx/sub! stream
         (fn [v] (println "v:" v))
         (fn [e] (println "error:" e)))
;; ==> v: :timeout
```

### Timeout With

Falls back to another observable on timeout:

```clojure
(def stream (->> (rx/timer 2000)
                 (rx/timeout-with 1000 (rx/of :fallback))))

(rx/sub! stream #(println "v:" %))
;; ==> v: :fallback
```

### Throttle

Emits the first value, then ignores subsequent values for a time window:

```clojure
(def stream (->> (rx/interval 100)
                 (rx/throttle 500)
                 (rx/take 3)))

(rx/sub! stream #(println "v:" %))
;; ==> v: 0
;; (after 500ms)
;; ==> v: 5
;; (after 500ms)
;; ==> v: 10
```

With leading/trailing options:

```clojure
(def stream (->> (rx/interval 100)
                 (rx/throttle 500 {:leading false :trailing true})
                 (rx/take 3)))
```

### Audit Time

Ignores source values for a time period, then emits the most recent:

```clojure
(def stream (->> (rx/interval 100)
                 (rx/audit-time 500)
                 (rx/take 3)))

(rx/sub! stream #(println "v:" %))
;; Emits the most recent value every 500ms
```

### Debounce

Emits a value only after a specified time has passed without emissions:

```clojure
(def stream (->> (rx/interval 100)
                 (rx/debounce 300)
                 (rx/take 3)))

(rx/sub! stream #(println "v:" %))
;; Emits values only after 300ms of silence
```

### Sample

Samples the observable at regular intervals:

```clojure
(def stream (->> (rx/interval 100)
                 (rx/sample 500)
                 (rx/take 3)))

(rx/sub! stream #(println "v:" %))
;; Emits the most recent value every 500ms
```

### Sample When

Samples when another observable emits:

```clojure
(def sampler (rx/interval 500))
(def stream (->> (rx/interval 100)
                 (rx/sample-when sampler)
                 (rx/take 3)))

(rx/sub! stream #(println "v:" %))
```

### Timestamp

Attaches a timestamp to each emission:

```clojure
(def stream (->> (rx/from [1 2 3])
                 (rx/timestamp)))

(rx/sub! stream (fn [v]
                  (println "value:" (.-value v)
                           "time:" (.-timestamp v))))
;; ==> value: 1 time: 1234567890
;; ==> value: 2 time: 1234567891
;; ==> value: 3 time: 1234567892
```

### Time Interval

Records the time interval between consecutive emissions:

```clojure
(def stream (->> (rx/from [1 2 3])
                 (rx/time-interval)))

(rx/sub! stream (fn [v]
                  (println "value:" (.-value v)
                           "interval:" (.-interval v))))
;; ==> value: 1 interval: 0
;; ==> value: 2 interval: 5
;; ==> value: 3 interval: 3
```

## Utility Operators

### Tap

Performs side effects without modifying the stream:

```clojure
(def stream (->> (rx/from [1 2 3])
                 (rx/tap #(println "processing:" %))
                 (rx/map inc)))

(rx/sub! stream #(println "v:" %))
;; ==> processing: 1
;; ==> v: 2
;; ==> processing: 2
;; ==> v: 3
;; ==> processing: 3
;; ==> v: 4
```

With separate handlers for next, error, and complete:

```clojure
(def stream (->> (rx/from [1 2 3])
                 (rx/tap #(println "next:" %)
                         #(println "error:" %)
                         #(println "complete"))))
```

### Start With

Emits values before the source:

```clojure
(def stream (->> (rx/from [3 4 5])
                 (rx/start-with 1 2)))

(rx/sub! stream #(println "v:" %))
;; ==> v: 1
;; ==> v: 2
;; ==> v: 3
;; ==> v: 4
;; ==> v: 5
```

### End With

Emits values after the source completes:

```clojure
(def stream (->> (rx/from [1 2 3])
                 (rx/end-with 4 5)))

(rx/sub! stream #(println "v:" %))
;; ==> v: 1
;; ==> v: 2
;; ==> v: 3
;; ==> v: 4
;; ==> v: 5
```

### If Empty

Provides a default value if the source is empty:

```clojure
(def stream (->> (rx/empty)
                 (rx/if-empty :default)))

(rx/sub! stream #(println "v:" %))
;; ==> v: :default
```

### Materialize

Converts notifications to objects:

```clojure
(def stream (->> (rx/from [1 2])
                 (rx/materialize)))

(rx/sub! stream (fn [v]
                  (println "kind:" (.-kind v)
                           "value:" (.-value v))))
;; ==> kind: N value: 1
;; ==> kind: N value: 2
;; ==> kind: C value: undefined
```

### Dematerialize

Converts notification objects back to emissions:

```clojure
(def notifications #js [#js {:kind "N" :value 1}
                        #js {:kind "C"}])

(def stream (->> (rx/from notifications)
                 (rx/dematerialize)))

(rx/sub! stream #(println "v:" %))
;; ==> v: 1
```

### Sequence Equal

Compares two observables for equality:

```clojure
(def stream (->> (rx/from [1 2 3])
                 (rx/sequence-equal (rx/from [1 2 3]))))

(rx/sub! stream #(println "v:" %))
;; ==> v: true

(def stream (->> (rx/from [1 2 3])
                 (rx/sequence-equal (rx/from [1 2 4]))))

(rx/sub! stream #(println "v:" %))
;; ==> v: false
```

### Is Observable

Checks if a value is an observable:

```clojure
(rx/is-observable (rx/from [1 2 3]))
;; ==> true

(rx/is-observable [1 2 3])
;; ==> false
```

## Sharing & Multicasting

### Share

Shares a single subscription to the underlying sequence:

```clojure
(def source (->> (rx/from [1 2 3])
                 (rx/tap #(println "emitting:" %))))

(def shared (rx/share source))

(rx/sub! shared #(println "sub1:" %))
(rx/sub! shared #(println "sub2:" %))
;; ==> emitting: 1
;; ==> sub1: 1
;; ==> sub2: 1
;; ==> emitting: 2
;; ==> sub1: 2
;; ==> sub2: 2
;; ==> emitting: 3
;; ==> sub1: 3
;; ==> sub2: 3
```

### Share Replay

Shares and replays emissions to new subscribers:

```clojure
(def source (->> (rx/from [1 2 3])
                 (rx/share-replay 2)))

(rx/sub! source #(println "sub1:" %))
;; ==> sub1: 1
;; ==> sub1: 2
;; ==> sub1: 3

(rx/sub! source #(println "sub2:" %))
;; ==> sub2: 2
;; ==> sub2: 3
```

### Connectable

Creates a connectable observable:

```clojure
(def source (rx/from [1 2 3]))
(def connectable (rx/connectable source))

(rx/sub! connectable #(println "sub1:" %))
(rx/sub! connectable #(println "sub2:" %))

(.connect connectable)
;; ==> sub1: 1
;; ==> sub2: 1
;; ==> sub1: 2
;; ==> sub2: 2
;; ==> sub1: 3
;; ==> sub2: 3
```

### Connect

Connects to a connectable observable:

```clojure
(def source (rx/from [1 2 3]))
(def connectable (rx/connectable source))

(->> connectable
     (rx/connect identity)
     (rx/sub! #(println "v:" %)))
```

## Subjects

This is an abstraction that combines observable sequence with the
observer. So you can push values into it and transform and subscribe
to it like any other sequence.

### Creating a subject

You can create a subject instance using the `subject` constructor
function.

This is an example of using `subject` for two things: push values and
subscribe to it.

```clojure
(def subject (rx/subject))
(def stream (->> subject
                 (rx/skip 1)
                 (rx/map inc)
                 (rx/take 2)))

(rx/sub! stream
         #(println "on-value:" %)
         #(println "on-error:" %)
         #(println "on-end"))

(rx/push! subject 1)
(rx/push! subject 2)
(rx/push! subject 1)
(rx/push! subject 2)

;; ==> on-value: 3
;; ==> on-value: 2
;; ==> on-end
```

### Behavior Subject

A subject that remembers its current value:

```clojure
(def subject (rx/behavior-subject 0))

(rx/sub! subject #(println "v:" %))
;; ==> v: 0

(rx/push! subject 1)
;; ==> v: 1

(rx/push! subject 2)
;; ==> v: 2
```

New subscribers immediately receive the current value:

```clojure
(rx/sub! subject #(println "new sub:" %))
;; ==> new sub: 2
```

You can deref a behavior subject:

```clojure
@subject
;; ==> 2
```

### Ending a subject

You can end a subject at any moment just by executing the `end!` function:

```clojure
(def subject (rx/subject))

(rx/sub! subject
         #(println "on-value:" %)
         #(println "on-error:" %)
         #(println "on-end"))

(rx/end! subject)
;; ==> on-end
```

## Advanced Patterns

### Pipe and Comp

Compose operators using `pipe` and `comp`:

```clojure
(require '[beicon.v2.operators :as rxo])

(def transformation
  (rx/comp (rxo/map inc)
           (rxo/filter odd?)
           (rxo/take 3)))

(def stream (->> (rx/from [1 2 3 4 5 6 7 8 9])
                 (rx/pipe transformation)))

(rx/sub! stream #(println "v:" %))
;; ==> v: 3
;; ==> v: 5
;; ==> v: 7
```

### Transducers

Use Clojure transducers with observables:

```clojure
(def xf (comp (map inc) (filter odd?)))

(def stream (->> (rx/from [1 2 3 4 5 6])
                 (rx/transform xf)))

(rx/sub! stream #(println "v:" %))
;; ==> v: 3
;; ==> v: 5
;; ==> v: 7
```

### Group By

Groups emissions by a key function:

```clojure
(def stream (->> (rx/from [1 2 3 4 5 6])
                 (rx/group-by #(if (even? %) :even :odd))
                 (rx/merge-map (fn [grouped]
                                 (rx/map #(hash-map :key (.-key grouped)
                                                   :val %)
                                        grouped)))))

(rx/sub! stream #(println "v:" %))
;; ==> v: {:key :odd :val 1}
;; ==> v: {:key :even :val 2}
;; ==> v: {:key :odd :val 3}
;; ==> v: {:key :even :val 4}
;; ==> v: {:key :odd :val 5}
;; ==> v: {:key :even :val 6}
```

### Iif

Conditional observable based on a predicate:

```clojure
(def stream (rx/iif (constantly true)
                    (rx/of :yes)
                    (rx/of :no)))

(rx/sub! stream #(println "v:" %))
;; ==> v: :yes
```

### Observe On

Controls which scheduler is used for emissions:

```clojure
(def stream (->> (rx/from [1 2 3])
                 (rx/observe-on :async)))

(rx/sub! stream #(println "v:" %))
```

### Subscribe On

Controls which scheduler is used for subscription:

```clojure
(def stream (->> (rx/from [1 2 3])
                 (rx/subscribe-on :queue)))

(rx/sub! stream #(println "v:" %))
```

## API Reference

### Constructors

| Function | Description |
|----------|-------------|
| `from` | Creates observable from collection, promise, or iterable |
| `of` | Creates observable from multiple values |
| `range` | Generates sequence of numbers |
| `empty` | Creates empty observable that completes immediately |
| `throw` / `error` | Creates observable that errors immediately |
| `timer` | Emits after delay, optionally repeatedly |
| `interval` | Emits at regular intervals |
| `create` | Creates observable from subscribe function |
| `defer` | Defers observable creation until subscription |
| `generate` | Generates observable via state-driven loop |
| `from-atom` | Creates observable from atom changes |
| `from-event` | Creates observable from DOM events |
| `from-event-pattern` | Creates observable from event pattern |
| `bind-callback` | Converts callback function to observable factory |
| `bind-node-callback` | Converts Node.js callback to observable factory |
| `using` | Creates observable with disposable resource |
| `iif` | Conditional observable |
| `race` | Returns first observable to emit |
| `concat` | Concatenates observables in order |
| `merge` | Merges observables concurrently |
| `zip` | Combines observables by index |
| `combine-latest` | Combines latest values from observables |
| `fjoin` | Joins on completion of all observables |

### Transformation Operators

| Function | Description |
|----------|-------------|
| `map` | Transforms each value |
| `map-indexed` | Transforms with index |
| `map-to` | Maps all values to constant |
| `filter` | Filters values by predicate |
| `take` | Takes first N values |
| `take-while` | Takes while predicate is true |
| `take-until` | Takes until other observable emits |
| `take-last` | Takes last N values |
| `skip` | Skips first N values |
| `skip-while` | Skips while predicate is true |
| `skip-until` | Skips until other observable emits |
| `skip-last` | Skips last N values |
| `first` | Takes first value |
| `last` | Takes last value |
| `element-at` | Takes value at index |
| `find` | Finds first matching value |
| `find-index` | Finds index of first match |
| `distinct` | Removes duplicates |
| `distinct-until-key-changed` | Removes consecutive duplicates by key |
| `pairwise` | Emits consecutive pairs |
| `scan` | Accumulates with intermediate results |
| `reduce` | Accumulates to single value |
| `count` | Counts emissions |
| `max` | Emits maximum value |
| `min` | Emits minimum value |
| `every` | Tests if all match predicate |
| `is-empty` | Tests if observable is empty |
| `to-array` | Collects all values into array |
| `materialize` | Converts to notification objects |
| `dematerialize` | Converts notifications back |
| `timestamp` | Adds timestamp to emissions |
| `time-interval` | Records time between emissions |
| `buffer` | Buffers N values |
| `buffer-time` | Buffers by time |
| `buffer-toggle` | Buffers using opening/closing |
| `buffer-when` | Buffers using closing selector |
| `window-count` | Windows N values |
| `window-time` | Windows by time |
| `window-toggle` | Windows using opening/closing |
| `window-when` | Windows using closing selector |

### Flattening Operators

| Function | Description |
|----------|-------------|
| `merge-map` | Projects and merges (flatMap) |
| `merge-map-to` | Projects to same observable, merges |
| `merge-all` | Flattens by merging |
| `switch-map` | Projects and switches |
| `switch-map-to` | Projects to same observable, switches |
| `switch-all` | Flattens by switching |
| `mapcat` | Projects and concatenates |
| `concat-map-to` | Projects to same observable, concatenates |
| `concat-all` | Flattens by concatenating |
| `exhaust-map` | Projects, ignores while running |
| `exhaust-all` | Flattens, ignoring new while running |
| `merge-scan` | Accumulator returning observable, merges |
| `switch-scan` | Accumulator returning observable, switches |
| `flatten` | Flattens collections |
| `expand` | Recursively projects |

### Combination Operators

| Function | Description |
|----------|-------------|
| `concat-with` | Concatenates with other observables |
| `merge-with` | Merges with other observables |
| `zip-with` | Zips with other observables |
| `zip-all` | Zips all inner observables |
| `combine-latest-with` | Combines latest with others |
| `combine-latest-all` | Combines latest of all inner |
| `with-latest-from` | Uses latest from others when source emits |
| `race-with` | Races with other observables |
| `partition` | Splits by predicate |
| `group-by` | Groups by key function |
| `sequence-equal` | Compares for equality |

### Error Handling Operators

| Function | Description |
|----------|-------------|
| `catch` | Catches errors |
| `on-error-resume-next` | Continues on error |
| `retry` | Retries on error |
| `retry-when` | Retries when notifier emits |
| `repeat` | Repeats on completion |
| `repeat-when` | Repeats when notifier emits |
| `throw-if-empty` | Errors if empty |

### Time-Based Operators

| Function | Description |
|----------|-------------|
| `delay` | Delays emissions |
| `delay-when` | Delays using selector |
| `delay-at-least` | Ensures minimum delay |
| `timeout` | Errors on timeout |
| `timeout-with` | Falls back on timeout |
| `throttle` | Rate limits |
| `audit-time` | Emits most recent after period |
| `debounce` | Waits for silence |
| `sample` | Samples at intervals |
| `sample-when` | Samples when other emits |

### Utility Operators

| Function | Description |
|----------|-------------|
| `tap` | Side effects |
| `start-with` | Emits before source |
| `end-with` | Emits after source |
| `if-empty` | Default if empty |
| `share` | Shares subscription |
| `share-replay` | Shares with replay |
| `connectable` | Creates connectable |
| `connect` | Connects connectable |
| `observe-on` | Changes emission scheduler |
| `subscribe-on` | Changes subscription scheduler |
| `transform` | Uses transducers |
| `pipe` | Composes operators |
| `comp` | Composes operators (right-to-left) |

### Subscription Functions

| Function | Description |
|----------|-------------|
| `subscribe` | Subscribes to observable |
| `sub!` | Subscribes (observable last) |
| `subs!` | Subscribes (observable last) |
| `dispose!` | Cancels subscription |
| `on-error` | Subscribes to errors only |
| `on-end` | Subscribes to completion only |
| `to-atom` | Materializes to atom |
| `first-value-from` | Promise for first value |
| `last-value-from` | Promise for last value |

### Predicates

| Function | Description |
|----------|-------------|
| `observable?` | Checks if observable |
| `disposable?` | Checks if disposable |
| `subject?` | Checks if subject |
| `subscriber?` | Checks if subscriber |
| `scheduler?` | Checks if scheduler |
| `timeout-error?` | Checks if timeout error |
| `is-observable` | Checks if observable (RxJS) |

## Developers Guide

### Source Code

_beicon_ is open source and can be found on
[github](https://github.com/funcool/beicon).

You can clone the public repository with this command:

```bash
git clone https://github.com/funcool/beicon
```

### Run tests

For running tests:

```bash
pnpm test
```

For watch mode:

```bash
pnpm test:watch
```

### License

_beicon_ is licensed under BSD (2-Clause) license:

```
Copyright (c) 2015-2024 Andrey Antukh <niwi@niwi.nz>

All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:

* Redistributions of source code must retain the above copyright notice, this
  list of conditions and the following disclaimer.

* Redistributions in binary form must reproduce the above copyright notice,
  this list of conditions and the following disclaimer in the documentation
  and/or other materials provided with the distribution.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
```
