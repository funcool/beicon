# User Guide

## Introduction

_beicon_ is a small and concise library that provides reactive streams
API for ClojureScript.

### Install

The simplest way to use _beicon_ in a Clojure project, is by including
it the following dependency:

```clojure
funcool/beicon2
{:git/tag "v2.0"
 :git/sha "e7135e0"
 :git/url "https://github.com/funcool/beicon.git"}
```

or

```clojure
funcool/beicon2 {:mvn/version "2.0.0"}
```

## Creating Streams

This section will give you the available methods for create observable streams.


### From a collection

The most basic way to create a stream is to just take a collection
and convert it into an observable sequence:

```clojure
(require '[beicon.v2 :as rx])

(def stream (rx/from [1 2 3]))

(rx/sub! stream #(println "v:" %))
;; ==> v: 1
;; ==> v: 2
;; ==> v: 3
```

### From range

An other way to create an observable stream is using the `range` constructor,
which is pretty analogous to the Clojures one:

```clojure
(def stream (rx/range 3))

(rx/sub! stream #(println "v:" %))
;; ==> v: 0
;; ==> v: 1
;; ==> v: 2
```

### From Atom

Atoms in Clojure are watchable, so you can listen for their
changes. This method converts that changes into an infinite observable
sequence of atom changes:

```clojure
(def a (atom 1))

(def stream (rx/from-atom a))

(rx/sub! stream #(println "v:" %))
(swap! a inc)
;; ==> v: 2
```


### From values.

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

Some times you also want just a terminated stream:

```clojure
(def stream (rx/empty))
```

This stream does not yield any value and just terminates.


### With timeout

This allows to create an observable sequence of one unique value, that
will be emitted after a specified amount of time:

```clojure
(def stream (rx/timeout 1000 10))

(rx/sub! stream #(println "v:" %))
;; After 1 sec...
;; ==> v: 10
```


### From factory

This is the most advanced and flexible way to create an observable
sequence. It allows to have control about termination and errors, and
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


## Consuming streams

### The stream states

The observable sequence can be in three different kind of states:
*alive*, *"errored"* or *ended*. If an error is emitted the stream can
be considered ended with an error.  So *error* or *end* states are
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

There is also the `subs!` function usefull for `->>` ready call
convention (expects receive the `observable` on the last argument
position instead of the first position).


## Transformations

There are two call conventions here:

- The familiar fluent API, which just works like any clojure sequence
  transformations functions (`map`, `filter`, ...)
- The RxJS composition API, using `rx/pipe` and `rx/comp`.

Let see the `filter` example to understand the differences.


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
(require '[beicon.v2.ops :as rxo])

(def stream
  (->> (rx/from [1 2 3 4 5])
       (rx/pipe (rxo/map inc))
       (rx/pipe (rxo/filter #(> % 3)))
       (rx/subs! stream
                 #(println "on-next:" %)
                 #(println "on-error:" %)
                 #(println "on-end"))))
```

We also use the `subs!` helper for subscribe to the resulting
observable in a single expression with `->>`.

And finally, you can compose the transformation and later use it
in the same way as transducers:

```clojure
(def rxform
  (rx/comp (rxo/map inc)
           (rxo/filter #(> % 3))))

(def stream
  (->> (rx/from [1 2 3 4 5])
       (rx/pipe rxform)
       (rx/subs! stream
                 #(println "on-next:" %)
                 #(println "on-error:" %)
                 #(println "on-end"))))
```

NOTE: Functions in the `beicon.v2.ops` are operator only.


### Merge Map

Converts an observable sequence, that can contain other observable sequences, into a
new observable sequence, that emits just plain values.

The result is similar to concatenating all the underlying sequences.

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

Aliases: `fmap`, `flat-map`.


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

Or skip until another observable yields a value with `skip-until` (no
example at this moment).


### Take

You can also limit the observable sequence to an specified number of
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

Or a predicate evaluates to `true`:

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


### Buffer

This transformer function allows to accumulate N values in a buffer
and then emits them as one value (similar to `partition` in Clojure)

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


## Combinators

### Zip

This combinator combines two observable sequences in one.

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

### Concat

This combinator concatenates two or more observable sequences *in
order*.

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

It ignores nil values from arguments


### Merge

This combinator merges two or more observable sequences *at random* (see
`concat` for ordered).

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

It ignores nil values from arguments


## Subject

This is an abstraction that combines observable sequence with the
observer. So you can push values into it and transform and subscribe
to it like any other sequence.

### Creating a subject.

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


## Developers Guide

### Source Code

_beicon_ is open source and can be found on
link:https://github.com/funcool/beicon[github].

You can clone the public repository with this command:

```
git clone https://github.com/funcool/beicon
```


### Run tests

For running tests just execute this:

```bash
yarn run test:watch
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
