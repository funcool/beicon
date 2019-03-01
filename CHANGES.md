# Changelog #

## Version 5.0.0 ##

Date: 2019-03-01

- Update to RxJS 6.4.0
- Update ro RxJava 2.2.7

This release reduces the rxjs bundle size including only the rxjs core
and all operators (excluding all tests, ajax, websockets and other
unrelated and not exposted api). Now it is 23k minified and gziped.


## Version 4.1.0 ##

Date: 2017-11-17

- Update cljs compiler to the latest version.
- Update RxJS bundle to 5.5.2
- Update RxJava dependency to 2.1.6
- Make `ignore` return an instance of `Observable`.

## Version 4.0.0 ##

Date: 2017-08-01

- Update cljs compiler to the latest version.
- Update RxJS bundle to 5.4.2
- Update RxJava dependency to 2.1.2.
- Fix `concat` operator with flowables.
- Fix `from-promise` is renamed to `from-future` in jvm (BREAKING CHANGE).


## Version 3.5.0 ##

Date: 2017-05-28

- The `:trampoline` scheduler is renamed to `:queue` for consistency
  (backward compatibility preserved, but `:trampoline` is deprected
  for now).
- Add `from-event` function to the cljs.
- Update RxJS to 5.4.0


## Version 3.4.0 ##

Date: 2017-04-29

- Upgrade RxJava to 2.1.0
- Fix unexpected exception on two-arity version of `timer` function.


## Version 3.3.0 ##

Date: 2017-04-25

- Update RxJS to 5.3.0
- Update RxJava to 2.0.9


## Version 3.2.0 ##

Date: 2017-03-12

- Add `to-serialized` function(jvm).
- Add `single?` predicate (jvm).
- Fix unexpected exception when subscribing to Single (jvm).


## Version 3.1.1 ##

Date: 2017-02-26

- Wrap function on `reduce` and `scan` because some functions
  such as conj does not works if they are passed as is (and
  produces wrong results).


## Version 3.1.0 ##

Date: 2017-02-22

- Upgrade to RxJS 5.2.0
- Upgrade to RxJava 2.0.6


## Version 3.0.0 ##

Date: 2017-02-13

WARNING: many changes are backward incompatible with the previous version.
They are introduced because for make the library more concise and more
consistent between clj and cljs.

A list of relevant changes:

- Upgrade to RxJava2 (2.0.5)
- Scheduler vars are removed in favor to `scheduler` function.
- The functions `subscribe-on` and `observe-on` now accept keywords
  as argument that automatically resolves to proper scheduler or raises
  an exception if no scheduler found for the provided keyword).
- Add `cancel!` function for cancel subscriptions more conciselly.
- Remove the ability to call the subscription in order to cancel it.
- Introduce backpressure support with rxjava2 flowables through the
  new `generate` function, see documentation for more info.
- `to-atom` now return a cancellable variant of atom (a wrapper that
  implements the atom interface and ICancellable protocol).
- Remove deprecated `bus?` predicate.
- Remove deprecated `bus` function (subject constructor).
- Remove deprecated second arity of `publish` function.
- Remove deprecated `from-exception` function.
- Remove deprecated `with-latest-from` (replaced by `with-latest`).
- Remove `.close` method on disposable on clojurescript.


## Version 2.9.0 ##

Date: 2017-01-30

- Fix wrong behavior of concat combinator.
- The convenience arity for automatically connect on `publish` is
  **deprecated**. If you want to connect, just use the `connect!`
  function. **The arity will be removed in the next version.**
- The `with-latest-from` function is deprecated in favor of the new
  `with-latest` function that has the project function mandatory in
  contrast to the deprecated function. **The deprecated function will
  be removed in the next version.**
- Update to RxJS 5.0.3 (master at d4533c40)
- Update to RxJava 1.2.5


## Version 2.8.0 ##

Date: 2016-12-18

- Add `buffer-time` operator.


## Version 2.7.0 ##

Date: 2016-12-14

- Update to RxJS 5.0.1.


## Version 2.6.1 ##

Date: 2016-12-07

- Add missing externs.


## Version 2.6.0 ##

Date: 2016-12-07

- Update to RxJS 5.0.0.rc5
- Rename `bus` constructor to `subject` (for name consistency with rxjs).
- Add `behavior-subject` constructor.
- Add the ability to use subject's and Observers as parameter to subscribe.


## Version 2.5.0 ##

Date: 2016-11-27

- Update to RxJS 5.0.0.rc4
- Update to RxJava 1.2.3
- Add combine-latest combinator.


## Version 2.4.0 ##

Date: 2016-11-03

- Update to RxJS 5.0.0.rc1
- Update to RxJava 1.2.1


## Version 2.3.0 ##

Date: 2016-08-17

- Update to RxJS 5.0.0.beta11
- Update to RxJava 1.1.9


## Version 2.2.0 ##

Date: 2016-07-10

- Update to RxJS 5.0.0.beta10
- Update to RxJava 1.1.7


## Version 2.1.0 ##

Date: 2016-06-15

- Add missing ignoreElements to externs.
- Update to RxJS 5.0.0.beta9


## Version 2.0.0 ##

Date: 2016-06-04

- Add support for clojure using rxjava as underlying implementation.


## Version 1.4.0 ##

Date: 2016-05-28

- Update bundled rxjs to commit ceb9990 (some commits over 5.0.0.beta8).


## Version 1.3.0 ##

Date: 2016-05-10

- Update bundled rxjs to 5.0.0.beta7.


## Version 1.2.0 ##

Date: 2016-04-13

- Add `merge-map` alias for flat-map.
- Fix wrong impl of merge implementation.
- Simplify impl of `merge` and `concat`.
- Strip `nil` values from `merge` and `concat` func args.
- Add the ability to add predicate for catch function.
- Update bundled rxjs.


## Version 1.1.1 ##

Date: 2016-03-19

- Fix wrong parameters order on `dedupe` and `dedupe'` functions.


## Version 1.1.0 ##

Date: 2016-03-19

- Rename `from-exception` to `throw`.
- Add backward compatible alias for `from-exception`.
- Update cljs compiler versiont o 1.8.34
- Update promise dependency to 1.1.1


## Version 1.0.3 ##

Date: 2016-03-16

- Fix unexpected exception on subscribe function.


## Version 1.0.2 ##

Date: 2016-03-16

- Fix wrong call on buffer function impl.


## Version 1.0.1 ##

Date: 2016-03-16

- Fix minified rxjs bundle.


## Version 1.0.0 ##

Date: 2016-03-16

This is a major release due to big internal changes and some
backward incompatibilities introduced in this version.

- Switch to RxJS 5.x (5.0.0-beta2)
- Remove `from-callback` observable constructor.
- Remove `from-poll` observable constructor.
- Remove `repeat` operator.
- Remove `slice` operator.
- Remove `to-observable` operator.
- Remove `pausable` operator.
- Remove `immediate` scheduler.
- Add `mapcat` operator (similar to flatmap but maintains the order).
- Add `fjoin` a rxjs forkjoin operator (similar to promise `.all` method).
- Add `range` constructor.
- Add `async` scheduler.
- Change `zip` call signature.
- Rename `delay'` operator to `delay-when`.
- Rename `choice` operator to `race`.
- Rename `sample'` operator to `sample-when`.
- Make `of` consturctor accept more than 6 parameters.


## Version 0.6.1 ##

Date: 2016-01-28

- Add `take-until` function.


## Version 0.6.0 ##

Date: 2016-01-22

- The old `timeout` function becomes `timer`.
- Add proper `timeout` function.
- Add `delay` function.
- Add `interval` function.
- Add support for schedulers (`subscribe-on` and `observe-on`).
- Make cats dependency optional (only if you require `beicon.monad` ns).
  (Is responsability of the user include the appropriate cats version).
- Start using clojure 1.8 and clojurescript 1.7.228.

## Version 0.5.1 ##

Date: 2016-01-08

- Fix wrong path to the minified version of bundled rxjs.


## Version 0.5.0 ##

Date: 2015-12-23

- Add `sample` function.


## Version 0.4.0 ##

Date: 2015-12-23

- Add `debounce` function.
- Allow multimethods on `on-value`, `on-error` and `on-end`.


## Version 0.3.0 ##

Date: 2015-12-08

- Fix wrong precondition on `repeat` function.
- Add `scan` function.
- Add `from-promise` function.
- Add `retry` function.
- Add `with-latest-from` function.
- Add `catch` function.
- Add `from-exception` function.
- Add `empty` function.
- Add `share` function.
- Add `merge-all` function.
- Add `of` function.
- Add `just` function (once is now an alias for just).
- Implement `never` in function of empty.
- Improve `zip` function allowing passing user defined
  join functon.
- Changed call signature of `to-atom` for consistency
  with the subscribe related functions.


## Version 0.2.0 ##

Date: 2015-12-03

- Fix incompatibilities with advanced compilations.
- Add new and improved externs.
- Update to rxjs 4.0.7


## Version 0.1.1

Date: 2015-11-03

- Update bundled rxjs to 4.0.6.


## Version 0.1.0

Date: 2015-11-02

- Initial release.
