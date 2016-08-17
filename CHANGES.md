# Changelog #

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
