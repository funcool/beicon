(ns beicon.cats
  "A cats integration namespace.

  NOTE: this is a side-effect namespace; you should
  require it for make observables play into the cats
  protocols and abstractions."
  (:require [beicon.extern.rxjs]
            [cats.protocols :as p]
            [cats.context :as ctx]))

(def ^:static observable-context
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
      (.zip ^observable pf pv #(%1 %2)))

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
