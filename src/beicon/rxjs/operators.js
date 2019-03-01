/*
 *****************************************************************************
Copyright (c) Microsoft Corporation. All rights reserved.
Licensed under the Apache License, Version 2.0 (the "License"); you may not use
this file except in compliance with the License. You may obtain a copy of the
License at http://www.apache.org/licenses/LICENSE-2.0

THIS CODE IS PROVIDED ON AN *AS IS* BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
KIND, EITHER EXPRESS OR IMPLIED, INCLUDING WITHOUT LIMITATION ANY IMPLIED
WARRANTIES OR CONDITIONS OF TITLE, FITNESS FOR A PARTICULAR PURPOSE,
MERCHANTABLITY OR NON-INFRINGEMENT.

See the Apache Version 2.0 License for specific language governing permissions
and limitations under the License.
*****************************************************************************/
goog.provide("beicon.rxjs.operators");
var module$output_operators = beicon.rxjs.operators;

module$output_operators.audit = function(c) {
  return function(a) {
    return a.lift(new AuditOperator$$module$output_operators(c));
  };
};
module$output_operators.auditTime = function(c, a) {
  void 0 === a && (a = async$$module$output_operators);
  return (0,module$output_operators.audit)(function() {
    return timer$$module$output_operators(c, a);
  });
};
module$output_operators.buffer = function(c) {
  return function(a) {
    return a.lift(new BufferOperator$$module$output_operators(c));
  };
};
module$output_operators.bufferCount = function(c, a) {
  void 0 === a && (a = null);
  return function(b) {
    return b.lift(new BufferCountOperator$$module$output_operators(c, a));
  };
};
module$output_operators.bufferTime = function(c) {
  var a = arguments.length, b = async$$module$output_operators;
  isScheduler$$module$output_operators(arguments[arguments.length - 1]) && (b = arguments[arguments.length - 1], a--);
  var e = null;
  2 <= a && (e = arguments[1]);
  var d = Number.POSITIVE_INFINITY;
  3 <= a && (d = arguments[2]);
  return function(a) {
    return a.lift(new BufferTimeOperator$$module$output_operators(c, e, d, b));
  };
};
module$output_operators.bufferToggle = function(c, a) {
  return function(b) {
    return b.lift(new BufferToggleOperator$$module$output_operators(c, a));
  };
};
module$output_operators.bufferWhen = function(c) {
  return function(a) {
    return a.lift(new BufferWhenOperator$$module$output_operators(c));
  };
};
module$output_operators.catchError = function(c) {
  return function(a) {
    var b = new CatchOperator$$module$output_operators(c);
    a = a.lift(b);
    return b.caught = a;
  };
};
module$output_operators.combineAll = function(c) {
  return function(a) {
    return a.lift(new CombineLatestOperator$$module$output_operators(c));
  };
};
module$output_operators.combineLatest = function() {
  for (var c = [], a = 0; a < arguments.length; a++) {
    c[a] = arguments[a];
  }
  var b = null;
  "function" === typeof c[c.length - 1] && (b = c.pop());
  1 === c.length && isArray$$module$output_operators(c[0]) && (c = c[0].slice());
  return function(a) {
    return a.lift.call(from$$module$output_operators([a].concat(c)), new CombineLatestOperator$$module$output_operators(b));
  };
};
module$output_operators.map = function(c, a) {
  return function(b) {
    if ("function" !== typeof c) {
      throw new TypeError("argument is not a function. Are you looking for `mapTo()`?");
    }
    return b.lift(new MapOperator$$module$output_operators(c, a));
  };
};
module$output_operators.mergeMap = function(c, a, b) {
  void 0 === b && (b = Number.POSITIVE_INFINITY);
  if ("function" === typeof a) {
    return function(e) {
      return e.pipe((0,module$output_operators.mergeMap)(function(b, e) {
        return from$$module$output_operators(c(b, e)).pipe((0,module$output_operators.map)(function(c, d) {
          return a(b, c, e, d);
        }));
      }, b));
    };
  }
  "number" === typeof a && (b = a);
  return function(a) {
    return a.lift(new MergeMapOperator$$module$output_operators(c, b));
  };
};
module$output_operators.mergeAll = function(c) {
  void 0 === c && (c = Number.POSITIVE_INFINITY);
  return (0,module$output_operators.mergeMap)(identity$$module$output_operators, c);
};
module$output_operators.concatAll = function() {
  return (0,module$output_operators.mergeAll)(1);
};
module$output_operators.concat = function() {
  for (var c = [], a = 0; a < arguments.length; a++) {
    c[a] = arguments[a];
  }
  return function(b) {
    return b.lift.call(concat$1$$module$output_operators.apply(void 0, [b].concat(c)));
  };
};
module$output_operators.concatMap = function(c, a) {
  return (0,module$output_operators.mergeMap)(c, a, 1);
};
module$output_operators.concatMapTo = function(c, a) {
  return (0,module$output_operators.concatMap)(function() {
    return c;
  }, a);
};
module$output_operators.count = function(c) {
  return function(a) {
    return a.lift(new CountOperator$$module$output_operators(c, a));
  };
};
module$output_operators.debounce = function(c) {
  return function(a) {
    return a.lift(new DebounceOperator$$module$output_operators(c));
  };
};
module$output_operators.debounceTime = function(c, a) {
  void 0 === a && (a = async$$module$output_operators);
  return function(b) {
    return b.lift(new DebounceTimeOperator$$module$output_operators(c, a));
  };
};
module$output_operatorsIfEmpty = function(c) {
  void 0 === c && (c = null);
  return function(a) {
    return a.lift(new DefaultIfEmptyOperator$$module$output_operators(c));
  };
};
module$output_operators.delay = function(c, a) {
  void 0 === a && (a = async$$module$output_operators);
  var b = isDate$$module$output_operators(c) ? +c - a.now() : Math.abs(c);
  return function(c) {
    return c.lift(new DelayOperator$$module$output_operators(b, a));
  };
};
module$output_operators.delayWhen = function(c, a) {
  return a ? function(b) {
    return (new SubscriptionDelayObservable$$module$output_operators(b, a)).lift(new DelayWhenOperator$$module$output_operators(c));
  } : function(b) {
    return b.lift(new DelayWhenOperator$$module$output_operators(c));
  };
};
module$output_operators.dematerialize = function() {
  return function(c) {
    return c.lift(new DeMaterializeOperator$$module$output_operators);
  };
};
module$output_operators.distinct = function(c, a) {
  return function(b) {
    return b.lift(new DistinctOperator$$module$output_operators(c, a));
  };
};
module$output_operators.distinctUntilChanged = function(c, a) {
  return function(b) {
    return b.lift(new DistinctUntilChangedOperator$$module$output_operators(c, a));
  };
};
module$output_operators.distinctUntilKeyChanged = function(c, a) {
  return (0,module$output_operators.distinctUntilChanged)(function(b, e) {
    return a ? a(b[c], e[c]) : b[c] === e[c];
  });
};
module$output_operators.filter = function(c, a) {
  return function(b) {
    return b.lift(new FilterOperator$$module$output_operators(c, a));
  };
};
module$output_operators.tap = function(c, a, b) {
  return function(e) {
    return e.lift(new DoOperator$$module$output_operators(c, a, b));
  };
};
module$output_operators.take = function(c) {
  return function(a) {
    return 0 === c ? empty$1$$module$output_operators() : a.lift(new TakeOperator$$module$output_operators(c));
  };
};
module$output_operators.elementAt = function(c, a) {
  if (0 > c) {
    throw new ArgumentOutOfRangeError$$module$output_operators;
  }
  var b = 2 <= arguments.length;
  return function(e) {
    return e.pipe((0,module$output_operators.filter)(function(b, a) {
      return a === c;
    }), (0,module$output_operators.take)(1), b ? (0,module$output_operatorsIfEmpty)(a) : (0,module$output_operators.throwIfEmpty)(function() {
      return new ArgumentOutOfRangeError$$module$output_operators;
    }));
  };
};
module$output_operators.endWith = function() {
  for (var c = [], a = 0; a < arguments.length; a++) {
    c[a] = arguments[a];
  }
  return function(b) {
    var a = c[c.length - 1];
    isScheduler$$module$output_operators(a) ? c.pop() : a = null;
    var d = c.length;
    return 1 !== d || a ? 0 < d ? concat$1$$module$output_operators(b, fromArray$$module$output_operators(c, a)) : concat$1$$module$output_operators(b, empty$1$$module$output_operators(a)) : concat$1$$module$output_operators(b, scalar$$module$output_operators(c[0]));
  };
};
module$output_operators.every = function(c, a) {
  return function(b) {
    return b.lift(new EveryOperator$$module$output_operators(c, a, b));
  };
};
module$output_operators.exhaust = function() {
  return function(c) {
    return c.lift(new SwitchFirstOperator$$module$output_operators);
  };
};
module$output_operators.exhaustMap = function(c, a) {
  return a ? function(b) {
    return b.pipe((0,module$output_operators.exhaustMap)(function(b, d) {
      return from$$module$output_operators(c(b, d)).pipe((0,module$output_operators.map)(function(c, e) {
        return a(b, c, d, e);
      }));
    }));
  } : function(b) {
    return b.lift(new ExhauseMapOperator$$module$output_operators(c));
  };
};
module$output_operators.expand = function(c, a, b) {
  void 0 === a && (a = Number.POSITIVE_INFINITY);
  void 0 === b && (b = void 0);
  a = 1 > (a || 0) ? Number.POSITIVE_INFINITY : a;
  return function(e) {
    return e.lift(new ExpandOperator$$module$output_operators(c, a, b));
  };
};
module$output_operators.finalize = function(c) {
  return function(a) {
    return a.lift(new FinallyOperator$$module$output_operators(c));
  };
};
module$output_operators.find = function(c, a) {
  if ("function" !== typeof c) {
    throw new TypeError("predicate is not a function");
  }
  return function(b) {
    return b.lift(new FindValueOperator$$module$output_operators(c, b, !1, a));
  };
};
module$output_operators.findIndex = function(c, a) {
  return function(b) {
    return b.lift(new FindValueOperator$$module$output_operators(c, b, !0, a));
  };
};
module$output_operators.first = function(c, a) {
  var b = 2 <= arguments.length;
  return function(e) {
    return e.pipe(c ? (0,module$output_operators.filter)(function(b, a) {
      return c(b, a, e);
    }) : identity$$module$output_operators, (0,module$output_operators.take)(1), b ? (0,module$output_operatorsIfEmpty)(a) : (0,module$output_operators.throwIfEmpty)(function() {
      return new EmptyError$$module$output_operators;
    }));
  };
};
module$output_operators.groupBy = function(c, a, b, e) {
  return function(d) {
    return d.lift(new GroupByOperator$$module$output_operators(c, a, b, e));
  };
};
module$output_operators.ignoreElements = function() {
  return function(c) {
    return c.lift(new IgnoreElementsOperator$$module$output_operators);
  };
};
module$output_operators.isEmpty = function() {
  return function(c) {
    return c.lift(new IsEmptyOperator$$module$output_operators);
  };
};
module$output_operators.takeLast = function(c) {
  return function(a) {
    return 0 === c ? empty$1$$module$output_operators() : a.lift(new TakeLastOperator$$module$output_operators(c));
  };
};
module$output_operators.last = function(c, a) {
  var b = 2 <= arguments.length;
  return function(e) {
    return e.pipe(c ? (0,module$output_operators.filter)(function(b, a) {
      return c(b, a, e);
    }) : identity$$module$output_operators, (0,module$output_operators.takeLast)(1), b ? (0,module$output_operatorsIfEmpty)(a) : (0,module$output_operators.throwIfEmpty)(function() {
      return new EmptyError$$module$output_operators;
    }));
  };
};
module$output_operators.mapTo = function(c) {
  return function(a) {
    return a.lift(new MapToOperator$$module$output_operators(c));
  };
};
module$output_operators.materialize = function() {
  return function(c) {
    return c.lift(new MaterializeOperator$$module$output_operators);
  };
};
module$output_operators.scan = function(c, a) {
  var b = !1;
  2 <= arguments.length && (b = !0);
  return function(e) {
    return e.lift(new ScanOperator$$module$output_operators(c, a, b));
  };
};
module$output_operators.reduce = function(c, a) {
  return 2 <= arguments.length ? function(b) {
    return pipe$$module$output_operators((0,module$output_operators.scan)(c, a), (0,module$output_operators.takeLast)(1), (0,module$output_operatorsIfEmpty)(a))(b);
  } : function(b) {
    return pipe$$module$output_operators((0,module$output_operators.scan)(function(b, a, f) {
      return c(b, a, f + 1);
    }), (0,module$output_operators.takeLast)(1))(b);
  };
};
module$output_operators.max = function(c) {
  return (0,module$output_operators.reduce)("function" === typeof c ? function(a, b) {
    return 0 < c(a, b) ? a : b;
  } : function(a, b) {
    return a > b ? a : b;
  });
};
module$output_operators.merge = function() {
  for (var c = [], a = 0; a < arguments.length; a++) {
    c[a] = arguments[a];
  }
  return function(b) {
    return b.lift.call(merge$1$$module$output_operators.apply(void 0, [b].concat(c)));
  };
};
module$output_operators.mergeMapTo = function(c, a, b) {
  void 0 === b && (b = Number.POSITIVE_INFINITY);
  if ("function" === typeof a) {
    return (0,module$output_operators.mergeMap)(function() {
      return c;
    }, a, b);
  }
  "number" === typeof a && (b = a);
  return (0,module$output_operators.mergeMap)(function() {
    return c;
  }, b);
};
module$output_operators.mergeScan = function(c, a, b) {
  void 0 === b && (b = Number.POSITIVE_INFINITY);
  return function(e) {
    return e.lift(new MergeScanOperator$$module$output_operators(c, a, b));
  };
};
module$output_operators.min = function(c) {
  return (0,module$output_operators.reduce)("function" === typeof c ? function(a, b) {
    return 0 > c(a, b) ? a : b;
  } : function(a, b) {
    return a < b ? a : b;
  });
};
module$output_operators.refCount = function() {
  return function(c) {
    return c.lift(new RefCountOperator$1$$module$output_operators(c));
  };
};
module$output_operators.multicast = function(c, a) {
  return function(b) {
    var e = "function" === typeof c ? c : function() {
      return c;
    };
    if ("function" === typeof a) {
      return b.lift(new MulticastOperator$$module$output_operators(e, a));
    }
    var d = Object.create(b, connectableObservableDescriptor$$module$output_operators);
    d.source = b;
    d.subjectFactory = e;
    return d;
  };
};
module$output_operators.observeOn = function(c, a) {
  void 0 === a && (a = 0);
  return function(b) {
    return b.lift(new ObserveOnOperator$$module$output_operators(c, a));
  };
};
module$output_operators.onErrorResumeNext = function() {
  for (var c = [], a = 0; a < arguments.length; a++) {
    c[a] = arguments[a];
  }
  1 === c.length && isArray$$module$output_operators(c[0]) && (c = c[0]);
  return function(b) {
    return b.lift(new OnErrorResumeNextOperator$$module$output_operators(c));
  };
};
module$output_operators.pairwise = function() {
  return function(c) {
    return c.lift(new PairwiseOperator$$module$output_operators);
  };
};
module$output_operators.partition = function(c, a) {
  return function(b) {
    return [(0,module$output_operators.filter)(c, a)(b), (0,module$output_operators.filter)(not$$module$output_operators(c, a))(b)];
  };
};
module$output_operators.pluck = function() {
  for (var c = [], a = 0; a < arguments.length; a++) {
    c[a] = arguments[a];
  }
  var b = c.length;
  if (0 === b) {
    throw Error("list of properties cannot be empty.");
  }
  return function(a) {
    return (0,module$output_operators.map)(plucker$$module$output_operators(c, b))(a);
  };
};
module$output_operators.publish = function(c) {
  return c ? (0,module$output_operators.multicast)(function() {
    return new Subject$$module$output_operators;
  }, c) : (0,module$output_operators.multicast)(new Subject$$module$output_operators);
};
module$output_operators.publishBehavior = function(c) {
  return function(a) {
    return (0,module$output_operators.multicast)(new BehaviorSubject$$module$output_operators(c))(a);
  };
};
module$output_operators.publishLast = function() {
  return function(c) {
    return (0,module$output_operators.multicast)(new AsyncSubject$$module$output_operators)(c);
  };
};
module$output_operators.publishReplay = function(c, a, b, e) {
  b && "function" !== typeof b && (e = b);
  var d = "function" === typeof b ? b : void 0, f = new ReplaySubject$$module$output_operators(c, a, e);
  return function(b) {
    return (0,module$output_operators.multicast)(function() {
      return f;
    }, d)(b);
  };
};
module$output_operators.race = function() {
  for (var c = [], a = 0; a < arguments.length; a++) {
    c[a] = arguments[a];
  }
  return function(b) {
    1 === c.length && isArray$$module$output_operators(c[0]) && (c = c[0]);
    return b.lift.call(race$1$$module$output_operators.apply(void 0, [b].concat(c)));
  };
};
module$output_operators.repeat = function(c) {
  void 0 === c && (c = -1);
  return function(a) {
    return 0 === c ? empty$1$$module$output_operators() : 0 > c ? a.lift(new RepeatOperator$$module$output_operators(-1, a)) : a.lift(new RepeatOperator$$module$output_operators(c - 1, a));
  };
};
module$output_operators.repeatWhen = function(c) {
  return function(a) {
    return a.lift(new RepeatWhenOperator$$module$output_operators(c));
  };
};
module$output_operators.retry = function(c) {
  void 0 === c && (c = -1);
  return function(a) {
    return a.lift(new RetryOperator$$module$output_operators(c, a));
  };
};
module$output_operators.retryWhen = function(c) {
  return function(a) {
    return a.lift(new RetryWhenOperator$$module$output_operators(c, a));
  };
};
module$output_operators.sample = function(c) {
  return function(a) {
    return a.lift(new SampleOperator$$module$output_operators(c));
  };
};
module$output_operators.sampleTime = function(c, a) {
  void 0 === a && (a = async$$module$output_operators);
  return function(b) {
    return b.lift(new SampleTimeOperator$$module$output_operators(c, a));
  };
};
module$output_operators.sequenceEqual = function(c, a) {
  return function(b) {
    return b.lift(new SequenceEqualOperator$$module$output_operators(c, a));
  };
};
module$output_operators.share = function() {
  return function(c) {
    return (0,module$output_operators.refCount)()((0,module$output_operators.multicast)(shareSubjectFactory$$module$output_operators)(c));
  };
};
module$output_operators.shareReplay = function(c, a, b) {
  void 0 === c && (c = Number.POSITIVE_INFINITY);
  void 0 === a && (a = Number.POSITIVE_INFINITY);
  return function(e) {
    return e.lift(shareReplayOperator$$module$output_operators(c, a, b));
  };
};
module$output_operators.single = function(c) {
  return function(a) {
    return a.lift(new SingleOperator$$module$output_operators(c, a));
  };
};
module$output_operators.skip = function(c) {
  return function(a) {
    return a.lift(new SkipOperator$$module$output_operators(c));
  };
};
module$output_operators.skipLast = function(c) {
  return function(a) {
    return a.lift(new SkipLastOperator$$module$output_operators(c));
  };
};
module$output_operators.skipUntil = function(c) {
  return function(a) {
    return a.lift(new SkipUntilOperator$$module$output_operators(c));
  };
};
module$output_operators.skipWhile = function(c) {
  return function(a) {
    return a.lift(new SkipWhileOperator$$module$output_operators(c));
  };
};
module$output_operators.startWith = function() {
  for (var c = [], a = 0; a < arguments.length; a++) {
    c[a] = arguments[a];
  }
  return function(b) {
    var a = c[c.length - 1];
    isScheduler$$module$output_operators(a) ? c.pop() : a = null;
    var d = c.length;
    return 1 !== d || a ? 0 < d ? concat$1$$module$output_operators(fromArray$$module$output_operators(c, a), b) : concat$1$$module$output_operators(empty$1$$module$output_operators(a), b) : concat$1$$module$output_operators(scalar$$module$output_operators(c[0]), b);
  };
};
module$output_operators.subscribeOn = function(c, a) {
  void 0 === a && (a = 0);
  return function(b) {
    return b.lift(new SubscribeOnOperator$$module$output_operators(c, a));
  };
};
module$output_operators.switchMap = function(c, a) {
  return "function" === typeof a ? function(b) {
    return b.pipe((0,module$output_operators.switchMap)(function(b, d) {
      return from$$module$output_operators(c(b, d)).pipe((0,module$output_operators.map)(function(c, e) {
        return a(b, c, d, e);
      }));
    }));
  } : function(b) {
    return b.lift(new SwitchMapOperator$$module$output_operators(c));
  };
};
module$output_operators.switchAll = function() {
  return (0,module$output_operators.switchMap)(identity$$module$output_operators);
};
module$output_operators.switchMapTo = function(c, a) {
  return a ? (0,module$output_operators.switchMap)(function() {
    return c;
  }, a) : (0,module$output_operators.switchMap)(function() {
    return c;
  });
};
module$output_operators.takeUntil = function(c) {
  return function(a) {
    return a.lift(new TakeUntilOperator$$module$output_operators(c));
  };
};
module$output_operators.takeWhile = function(c) {
  return function(a) {
    return a.lift(new TakeWhileOperator$$module$output_operators(c));
  };
};
module$output_operators.throttle = function(c, a) {
  void 0 === a && (a = defaultThrottleConfig$$module$output_operators);
  return function(b) {
    return b.lift(new ThrottleOperator$$module$output_operators(c, a.leading, a.trailing));
  };
};
module$output_operators.throttleTime = function(c, a, b) {
  void 0 === a && (a = async$$module$output_operators);
  void 0 === b && (b = defaultThrottleConfig$$module$output_operators);
  return function(e) {
    return e.lift(new ThrottleTimeOperator$$module$output_operators(c, a, b.leading, b.trailing));
  };
};
module$output_operators.timeInterval = function(c) {
  void 0 === c && (c = async$$module$output_operators);
  return function(a) {
    return defer$$module$output_operators(function() {
      return a.pipe((0,module$output_operators.scan)(function(b, a) {
        b = b.current;
        return {value:a, current:c.now(), last:b};
      }, {current:c.now(), value:void 0, last:void 0}), (0,module$output_operators.map)(function(b) {
        return new TimeInterval$$module$output_operators(b.value, b.current - b.last);
      }));
    });
  };
};
module$output_operators.timeoutWith = function(c, a, b) {
  void 0 === b && (b = async$$module$output_operators);
  return function(e) {
    var d = isDate$$module$output_operators(c), f = d ? +c - b.now() : Math.abs(c);
    return e.lift(new TimeoutWithOperator$$module$output_operators(f, d, a, b));
  };
};
module$output_operators.timeout = function(c, a) {
  void 0 === a && (a = async$$module$output_operators);
  return (0,module$output_operators.timeoutWith)(c, throwError$$module$output_operators(new TimeoutError$$module$output_operators), a);
};
module$output_operators.timestamp = function(c) {
  void 0 === c && (c = async$$module$output_operators);
  return (0,module$output_operators.map)(function(a) {
    return new Timestamp$$module$output_operators(a, c.now());
  });
};
module$output_operators.toArray = function() {
  return (0,module$output_operators.reduce)(toArrayReducer$$module$output_operators, []);
};
module$output_operators.window = function(c) {
  return function(a) {
    return a.lift(new WindowOperator$$module$output_operators(c));
  };
};
module$output_operators.windowCount = function(c, a) {
  void 0 === a && (a = 0);
  return function(b) {
    return b.lift(new WindowCountOperator$$module$output_operators(c, a));
  };
};
module$output_operators.windowTime = function(c, a, b, e) {
  var d = async$$module$output_operators, f = null, g = Number.POSITIVE_INFINITY;
  isScheduler$$module$output_operators(e) && (d = e);
  isScheduler$$module$output_operators(b) ? d = b : isNumeric$$module$output_operators(b) && (g = b);
  isScheduler$$module$output_operators(a) ? d = a : isNumeric$$module$output_operators(a) && (f = a);
  return function(b) {
    return b.lift(new WindowTimeOperator$$module$output_operators(c, f, g, d));
  };
};
module$output_operators.windowToggle = function(c, a) {
  return function(b) {
    return b.lift(new WindowToggleOperator$$module$output_operators(c, a));
  };
};
module$output_operators.windowWhen = function(c) {
  return function(a) {
    return a.lift(new WindowOperator$1$$module$output_operators(c));
  };
};
module$output_operators.withLatestFrom = function() {
  for (var c = [], a = 0; a < arguments.length; a++) {
    c[a] = arguments[a];
  }
  return function(b) {
    var a;
    "function" === typeof c[c.length - 1] && (a = c.pop());
    return b.lift(new WithLatestFromOperator$$module$output_operators(c, a));
  };
};
module$output_operators.zip = function() {
  for (var c = [], a = 0; a < arguments.length; a++) {
    c[a] = arguments[a];
  }
  return function(b) {
    return b.lift.call(zip$1$$module$output_operators.apply(void 0, [b].concat(c)));
  };
};
module$output_operators.zipAll = function(c) {
  return function(a) {
    return a.lift(new ZipOperator$$module$output_operators(c));
  };
};
Object.defineProperty(module$output_operators, "__esModule", {value:!0});
var extendStatics$$module$output_operators = Object.setPrototypeOf || {__proto__:[]} instanceof Array && function(c, a) {
  c.__proto__ = a;
} || function(c, a) {
  for (var b in a) {
    a.hasOwnProperty(b) && (c[b] = a[b]);
  }
};
function __extends$$module$output_operators(c, a) {
  function b() {
    this.constructor = c;
  }
  extendStatics$$module$output_operators(c, a);
  c.prototype = null === a ? Object.create(a) : (b.prototype = a.prototype, new b);
}
function __values$$module$output_operators(c) {
  var a = "function" === typeof Symbol && c[Symbol.iterator], b = 0;
  return a ? a.call(c) : {next:function() {
    c && b >= c.length && (c = void 0);
    return {value:c && c[b++], done:!c};
  }};
}
function __read$$module$output_operators(c, a) {
  var b = "function" === typeof Symbol && c[Symbol.iterator];
  if (!b) {
    return c;
  }
  c = b.call(c);
  var e, d = [];
  try {
    for (; (void 0 === a || 0 < a--) && !(e = c.next()).done;) {
      d.push(e.value);
    }
  } catch (g) {
    var f = {error:g};
  } finally {
    try {
      e && !e.done && (b = c["return"]) && b.call(c);
    } finally {
      if (f) {
        throw f.error;
      }
    }
  }
  return d;
}
function __await$$module$output_operators(c) {
  return this instanceof __await$$module$output_operators ? (this.v = c, this) : new __await$$module$output_operators(c);
}
var errorObject$$module$output_operators = {e:{}}, tryCatchTarget$$module$output_operators;
function tryCatcher$$module$output_operators() {
  try {
    return tryCatchTarget$$module$output_operators.apply(this, arguments);
  } catch (c) {
    return errorObject$$module$output_operators.e = c, errorObject$$module$output_operators;
  }
}
function tryCatch$$module$output_operators(c) {
  tryCatchTarget$$module$output_operators = c;
  return tryCatcher$$module$output_operators;
}
function isFunction$$module$output_operators(c) {
  return "function" === typeof c;
}
var _enable_super_gross_mode_that_will_cause_bad_things$$module$output_operators = !1, config$$module$output_operators = {Promise:void 0, set useDeprecatedSynchronousErrorHandling(c) {
  c ? console.warn("DEPRECATED! RxJS was set to use deprecated synchronous error handling behavior by code at: \n" + Error().stack) : _enable_super_gross_mode_that_will_cause_bad_things$$module$output_operators && console.log("RxJS: Back to a better error behavior. Thank you. <3");
  _enable_super_gross_mode_that_will_cause_bad_things$$module$output_operators = c;
}, get useDeprecatedSynchronousErrorHandling() {
  return _enable_super_gross_mode_that_will_cause_bad_things$$module$output_operators;
}};
function hostReportError$$module$output_operators(c) {
  setTimeout(function() {
    throw c;
  });
}
var empty$$module$output_operators = {closed:!0, next:function(c) {
}, error:function(c) {
  if (config$$module$output_operators.useDeprecatedSynchronousErrorHandling) {
    throw c;
  }
  hostReportError$$module$output_operators(c);
}, complete:function() {
}}, isArray$$module$output_operators = Array.isArray || function(c) {
  return c && "number" === typeof c.length;
};
function isObject$$module$output_operators(c) {
  return null != c && "object" === typeof c;
}
function UnsubscriptionErrorImpl$$module$output_operators(c) {
  Error.call(this);
  this.message = c ? c.length + " errors occurred during unsubscription:\n" + c.map(function(a, b) {
    return b + 1 + ") " + a.toString();
  }).join("\n  ") : "";
  this.name = "UnsubscriptionError";
  this.errors = c;
  return this;
}
UnsubscriptionErrorImpl$$module$output_operators.prototype = Object.create(Error.prototype);
var UnsubscriptionError$$module$output_operators = UnsubscriptionErrorImpl$$module$output_operators, Subscription$$module$output_operators = function() {
  function c(a) {
    this.closed = !1;
    this._subscriptions = this._parents = this._parent = null;
    a && (this._unsubscribe = a);
  }
  c.prototype.unsubscribe = function() {
    var a = !1;
    if (!this.closed) {
      var b = this._parent, c = this._parents, d = this._unsubscribe, f = this._subscriptions;
      this.closed = !0;
      this._subscriptions = this._parents = this._parent = null;
      for (var g = -1, h = c ? c.length : 0; b;) {
        b.remove(this), b = ++g < h && c[g] || null;
      }
      if (isFunction$$module$output_operators(d) && (b = tryCatch$$module$output_operators(d).call(this), b === errorObject$$module$output_operators)) {
        a = !0;
        var k = k || (errorObject$$module$output_operators.e instanceof UnsubscriptionError$$module$output_operators ? flattenUnsubscriptionErrors$$module$output_operators(errorObject$$module$output_operators.e.errors) : [errorObject$$module$output_operators.e]);
      }
      if (isArray$$module$output_operators(f)) {
        for (g = -1, h = f.length; ++g < h;) {
          b = f[g], isObject$$module$output_operators(b) && (b = tryCatch$$module$output_operators(b.unsubscribe).call(b), b === errorObject$$module$output_operators && (a = !0, k = k || [], b = errorObject$$module$output_operators.e, b instanceof UnsubscriptionError$$module$output_operators ? k = k.concat(flattenUnsubscriptionErrors$$module$output_operators(b.errors)) : k.push(b)));
        }
      }
      if (a) {
        throw new UnsubscriptionError$$module$output_operators(k);
      }
    }
  };
  c.prototype.add = function(a) {
    if (!a || a === c.EMPTY) {
      return c.EMPTY;
    }
    if (a === this) {
      return this;
    }
    var b = a;
    switch(typeof a) {
      case "function":
        b = new c(a);
      case "object":
        if (b.closed || "function" !== typeof b.unsubscribe) {
          return b;
        }
        if (this.closed) {
          return b.unsubscribe(), b;
        }
        "function" !== typeof b._addParent && (a = b, b = new c, b._subscriptions = [a]);
        break;
      default:
        throw Error("unrecognized teardown " + a + " added to Subscription.");
    }
    (this._subscriptions || (this._subscriptions = [])).push(b);
    b._addParent(this);
    return b;
  };
  c.prototype.remove = function(a) {
    var b = this._subscriptions;
    b && (a = b.indexOf(a), -1 !== a && b.splice(a, 1));
  };
  c.prototype._addParent = function(a) {
    var b = this._parent, c = this._parents;
    b && b !== a ? c ? -1 === c.indexOf(a) && c.push(a) : this._parents = [a] : this._parent = a;
  };
  c.EMPTY = function(a) {
    a.closed = !0;
    return a;
  }(new c);
  return c;
}();
function flattenUnsubscriptionErrors$$module$output_operators(c) {
  return c.reduce(function(a, b) {
    return a.concat(b instanceof UnsubscriptionError$$module$output_operators ? b.errors : b);
  }, []);
}
var rxSubscriber$$module$output_operators = "function" === typeof Symbol ? Symbol("rxSubscriber") : "@@rxSubscriber_" + Math.random(), Subscriber$$module$output_operators = function(c) {
  function a(b, e, d) {
    var f = c.call(this) || this;
    f.syncErrorValue = null;
    f.syncErrorThrown = !1;
    f.syncErrorThrowable = !1;
    f.isStopped = !1;
    f._parentSubscription = null;
    switch(arguments.length) {
      case 0:
        f.destination = empty$$module$output_operators;
        break;
      case 1:
        if (!b) {
          f.destination = empty$$module$output_operators;
          break;
        }
        if ("object" === typeof b) {
          b instanceof a ? (f.syncErrorThrowable = b.syncErrorThrowable, f.destination = b, b.add(f)) : (f.syncErrorThrowable = !0, f.destination = new SafeSubscriber$$module$output_operators(f, b));
          break;
        }
      default:
        f.syncErrorThrowable = !0, f.destination = new SafeSubscriber$$module$output_operators(f, b, e, d);
    }
    return f;
  }
  __extends$$module$output_operators(a, c);
  a.prototype[rxSubscriber$$module$output_operators] = function() {
    return this;
  };
  a.create = function(b, c, d) {
    b = new a(b, c, d);
    b.syncErrorThrowable = !1;
    return b;
  };
  a.prototype.next = function(b) {
    this.isStopped || this._next(b);
  };
  a.prototype.error = function(b) {
    this.isStopped || (this.isStopped = !0, this._error(b));
  };
  a.prototype.complete = function() {
    this.isStopped || (this.isStopped = !0, this._complete());
  };
  a.prototype.unsubscribe = function() {
    this.closed || (this.isStopped = !0, c.prototype.unsubscribe.call(this));
  };
  a.prototype._next = function(b) {
    this.destination.next(b);
  };
  a.prototype._error = function(b) {
    this.destination.error(b);
    this.unsubscribe();
  };
  a.prototype._complete = function() {
    this.destination.complete();
    this.unsubscribe();
  };
  a.prototype._unsubscribeAndRecycle = function() {
    var b = this._parent, a = this._parents;
    this._parents = this._parent = null;
    this.unsubscribe();
    this.isStopped = this.closed = !1;
    this._parent = b;
    this._parents = a;
    this._parentSubscription = null;
    return this;
  };
  return a;
}(Subscription$$module$output_operators), SafeSubscriber$$module$output_operators = function(c) {
  function a(b, a, d, f) {
    var e = c.call(this) || this;
    e._parentSubscriber = b;
    b = e;
    if (isFunction$$module$output_operators(a)) {
      var h = a;
    } else {
      a && (h = a.next, d = a.error, f = a.complete, a !== empty$$module$output_operators && (b = Object.create(a), isFunction$$module$output_operators(b.unsubscribe) && e.add(b.unsubscribe.bind(b)), b.unsubscribe = e.unsubscribe.bind(e)));
    }
    e._context = b;
    e._next = h;
    e._error = d;
    e._complete = f;
    return e;
  }
  __extends$$module$output_operators(a, c);
  a.prototype.next = function(b) {
    if (!this.isStopped && this._next) {
      var a = this._parentSubscriber;
      config$$module$output_operators.useDeprecatedSynchronousErrorHandling && a.syncErrorThrowable ? this.__tryOrSetError(a, this._next, b) && this.unsubscribe() : this.__tryOrUnsub(this._next, b);
    }
  };
  a.prototype.error = function(b) {
    if (!this.isStopped) {
      var a = this._parentSubscriber, c = config$$module$output_operators.useDeprecatedSynchronousErrorHandling;
      if (this._error) {
        c && a.syncErrorThrowable ? this.__tryOrSetError(a, this._error, b) : this.__tryOrUnsub(this._error, b), this.unsubscribe();
      } else {
        if (a.syncErrorThrowable) {
          c ? (a.syncErrorValue = b, a.syncErrorThrown = !0) : hostReportError$$module$output_operators(b), this.unsubscribe();
        } else {
          this.unsubscribe();
          if (c) {
            throw b;
          }
          hostReportError$$module$output_operators(b);
        }
      }
    }
  };
  a.prototype.complete = function() {
    var b = this;
    if (!this.isStopped) {
      var a = this._parentSubscriber;
      if (this._complete) {
        var c = function() {
          return b._complete.call(b._context);
        };
        config$$module$output_operators.useDeprecatedSynchronousErrorHandling && a.syncErrorThrowable ? this.__tryOrSetError(a, c) : this.__tryOrUnsub(c);
      }
      this.unsubscribe();
    }
  };
  a.prototype.__tryOrUnsub = function(b, a) {
    try {
      b.call(this._context, a);
    } catch (d) {
      this.unsubscribe();
      if (config$$module$output_operators.useDeprecatedSynchronousErrorHandling) {
        throw d;
      }
      hostReportError$$module$output_operators(d);
    }
  };
  a.prototype.__tryOrSetError = function(b, a, c) {
    if (!config$$module$output_operators.useDeprecatedSynchronousErrorHandling) {
      throw Error("bad call");
    }
    try {
      a.call(this._context, c);
    } catch (f) {
      return config$$module$output_operators.useDeprecatedSynchronousErrorHandling ? (b.syncErrorValue = f, b.syncErrorThrown = !0) : hostReportError$$module$output_operators(f), !0;
    }
    return !1;
  };
  a.prototype._unsubscribe = function() {
    var b = this._parentSubscriber;
    this._parentSubscriber = this._context = null;
    b.unsubscribe();
  };
  return a;
}(Subscriber$$module$output_operators), OuterSubscriber$$module$output_operators = function(c) {
  function a() {
    return null !== c && c.apply(this, arguments) || this;
  }
  __extends$$module$output_operators(a, c);
  a.prototype.notifyNext = function(b, a, c, f, g) {
    this.destination.next(a);
  };
  a.prototype.notifyError = function(b, a) {
    this.destination.error(b);
  };
  a.prototype.notifyComplete = function(b) {
    this.destination.complete();
  };
  return a;
}(Subscriber$$module$output_operators), InnerSubscriber$$module$output_operators = function(c) {
  function a(b, a, d) {
    var e = c.call(this) || this;
    e.parent = b;
    e.outerValue = a;
    e.outerIndex = d;
    e.index = 0;
    return e;
  }
  __extends$$module$output_operators(a, c);
  a.prototype._next = function(b) {
    this.parent.notifyNext(this.outerValue, b, this.outerIndex, this.index++, this);
  };
  a.prototype._error = function(b) {
    this.parent.notifyError(b, this);
    this.unsubscribe();
  };
  a.prototype._complete = function() {
    this.parent.notifyComplete(this);
    this.unsubscribe();
  };
  return a;
}(Subscriber$$module$output_operators);
function canReportError$$module$output_operators(c) {
  for (; c;) {
    var a = c.destination, b = c.isStopped;
    if (c.closed || b) {
      return !1;
    }
    c = a && a instanceof Subscriber$$module$output_operators ? a : null;
  }
  return !0;
}
function toSubscriber$$module$output_operators(c, a, b) {
  if (c) {
    if (c instanceof Subscriber$$module$output_operators) {
      return c;
    }
    if (c[rxSubscriber$$module$output_operators]) {
      return c[rxSubscriber$$module$output_operators]();
    }
  }
  return c || a || b ? new Subscriber$$module$output_operators(c, a, b) : new Subscriber$$module$output_operators(empty$$module$output_operators);
}
var observable$$module$output_operators = "function" === typeof Symbol && Symbol.observable || "@@observable";
function noop$$module$output_operators() {
}
function pipe$$module$output_operators() {
  for (var c = [], a = 0; a < arguments.length; a++) {
    c[a] = arguments[a];
  }
  return pipeFromArray$$module$output_operators(c);
}
function pipeFromArray$$module$output_operators(c) {
  return c ? 1 === c.length ? c[0] : function(a) {
    return c.reduce(function(b, a) {
      return a(b);
    }, a);
  } : noop$$module$output_operators;
}
var Observable$$module$output_operators = function() {
  function c(a) {
    this._isScalar = !1;
    a && (this._subscribe = a);
  }
  c.prototype.lift = function(a) {
    var b = new c;
    b.source = this;
    b.operator = a;
    return b;
  };
  c.prototype.subscribe = function(a, b, c) {
    var e = this.operator;
    a = toSubscriber$$module$output_operators(a, b, c);
    e ? e.call(a, this.source) : a.add(this.source || config$$module$output_operators.useDeprecatedSynchronousErrorHandling && !a.syncErrorThrowable ? this._subscribe(a) : this._trySubscribe(a));
    if (config$$module$output_operators.useDeprecatedSynchronousErrorHandling && a.syncErrorThrowable && (a.syncErrorThrowable = !1, a.syncErrorThrown)) {
      throw a.syncErrorValue;
    }
    return a;
  };
  c.prototype._trySubscribe = function(a) {
    try {
      return this._subscribe(a);
    } catch (b) {
      config$$module$output_operators.useDeprecatedSynchronousErrorHandling && (a.syncErrorThrown = !0, a.syncErrorValue = b), canReportError$$module$output_operators(a) ? a.error(b) : console.warn(b);
    }
  };
  c.prototype.forEach = function(a, b) {
    var c = this;
    b = getPromiseCtor$$module$output_operators(b);
    return new b(function(b, e) {
      var d = c.subscribe(function(b) {
        try {
          a(b);
        } catch (k) {
          e(k), d && d.unsubscribe();
        }
      }, e, b);
    });
  };
  c.prototype._subscribe = function(a) {
    var b = this.source;
    return b && b.subscribe(a);
  };
  c.prototype[observable$$module$output_operators] = function() {
    return this;
  };
  c.prototype.pipe = function() {
    for (var a = [], b = 0; b < arguments.length; b++) {
      a[b] = arguments[b];
    }
    return 0 === a.length ? this : pipeFromArray$$module$output_operators(a)(this);
  };
  c.prototype.toPromise = function(a) {
    var b = this;
    a = getPromiseCtor$$module$output_operators(a);
    return new a(function(a, c) {
      var e;
      b.subscribe(function(b) {
        return e = b;
      }, function(b) {
        return c(b);
      }, function() {
        return a(e);
      });
    });
  };
  c.create = function(a) {
    return new c(a);
  };
  return c;
}();
function getPromiseCtor$$module$output_operators(c) {
  c || (c = config$$module$output_operators.Promise || Promise);
  if (!c) {
    throw Error("no Promise impl found");
  }
  return c;
}
var subscribeToArray$$module$output_operators = function(c) {
  return function(a) {
    for (var b = 0, e = c.length; b < e && !a.closed; b++) {
      a.next(c[b]);
    }
    a.closed || a.complete();
  };
}, subscribeToPromise$$module$output_operators = function(c) {
  return function(a) {
    c.then(function(b) {
      a.closed || (a.next(b), a.complete());
    }, function(b) {
      return a.error(b);
    }).then(null, hostReportError$$module$output_operators);
    return a;
  };
};
function getSymbolIterator$$module$output_operators() {
  return "function" === typeof Symbol && Symbol.iterator ? Symbol.iterator : "@@iterator";
}
var iterator$$module$output_operators = getSymbolIterator$$module$output_operators(), subscribeToIterable$$module$output_operators = function(c) {
  return function(a) {
    var b = c[iterator$$module$output_operators]();
    do {
      var e = b.next();
      if (e.done) {
        a.complete();
        break;
      }
      a.next(e.value);
      if (a.closed) {
        break;
      }
    } while (1);
    "function" === typeof b.return && a.add(function() {
      b.return && b.return();
    });
    return a;
  };
}, subscribeToObservable$$module$output_operators = function(c) {
  return function(a) {
    var b = c[observable$$module$output_operators]();
    if ("function" !== typeof b.subscribe) {
      throw new TypeError("Provided object does not correctly implement Symbol.observable");
    }
    return b.subscribe(a);
  };
}, isArrayLike$$module$output_operators = function(c) {
  return c && "number" === typeof c.length && "function" !== typeof c;
};
function isPromise$$module$output_operators(c) {
  return c && "function" !== typeof c.subscribe && "function" === typeof c.then;
}
var subscribeTo$$module$output_operators = function(c) {
  if (c instanceof Observable$$module$output_operators) {
    return function(b) {
      if (c._isScalar) {
        b.next(c.value), b.complete();
      } else {
        return c.subscribe(b);
      }
    };
  }
  if (c && "function" === typeof c[observable$$module$output_operators]) {
    return subscribeToObservable$$module$output_operators(c);
  }
  if (isArrayLike$$module$output_operators(c)) {
    return subscribeToArray$$module$output_operators(c);
  }
  if (isPromise$$module$output_operators(c)) {
    return subscribeToPromise$$module$output_operators(c);
  }
  if (c && "function" === typeof c[iterator$$module$output_operators]) {
    return subscribeToIterable$$module$output_operators(c);
  }
  var a = isObject$$module$output_operators(c) ? "an invalid object" : "'" + c + "'";
  throw new TypeError("You provided " + a + " where a stream was expected. You can provide an Observable, Promise, Array, or Iterable.");
};
function subscribeToResult$$module$output_operators(c, a, b, e, d) {
  void 0 === d && (d = new InnerSubscriber$$module$output_operators(c, b, e));
  if (!d.closed) {
    return subscribeTo$$module$output_operators(a)(d);
  }
}
var AuditOperator$$module$output_operators = function() {
  function c(a) {
    this.durationSelector = a;
  }
  c.prototype.call = function(a, b) {
    return b.subscribe(new AuditSubscriber$$module$output_operators(a, this.durationSelector));
  };
  return c;
}(), AuditSubscriber$$module$output_operators = function(c) {
  function a(b, a) {
    b = c.call(this, b) || this;
    b.durationSelector = a;
    b.hasValue = !1;
    return b;
  }
  __extends$$module$output_operators(a, c);
  a.prototype._next = function(b) {
    this.value = b;
    this.hasValue = !0;
    this.throttled || (b = tryCatch$$module$output_operators(this.durationSelector)(b), b === errorObject$$module$output_operators ? this.destination.error(errorObject$$module$output_operators.e) : (b = subscribeToResult$$module$output_operators(this, b), !b || b.closed ? this.clearThrottle() : this.add(this.throttled = b)));
  };
  a.prototype.clearThrottle = function() {
    var b = this.value, a = this.hasValue, c = this.throttled;
    c && (this.remove(c), this.throttled = null, c.unsubscribe());
    a && (this.value = null, this.hasValue = !1, this.destination.next(b));
  };
  a.prototype.notifyNext = function(b, a, c, f) {
    this.clearThrottle();
  };
  a.prototype.notifyComplete = function() {
    this.clearThrottle();
  };
  return a;
}(OuterSubscriber$$module$output_operators), Action$$module$output_operators = function(c) {
  function a(b, a) {
    return c.call(this) || this;
  }
  __extends$$module$output_operators(a, c);
  a.prototype.schedule = function(b, a) {
    return this;
  };
  return a;
}(Subscription$$module$output_operators), AsyncAction$$module$output_operators = function(c) {
  function a(b, a) {
    var e = c.call(this, b, a) || this;
    e.scheduler = b;
    e.work = a;
    e.pending = !1;
    return e;
  }
  __extends$$module$output_operators(a, c);
  a.prototype.schedule = function(b, a) {
    void 0 === a && (a = 0);
    if (this.closed) {
      return this;
    }
    this.state = b;
    b = this.id;
    var c = this.scheduler;
    null != b && (this.id = this.recycleAsyncId(c, b, a));
    this.pending = !0;
    this.delay = a;
    this.id = this.id || this.requestAsyncId(c, this.id, a);
    return this;
  };
  a.prototype.requestAsyncId = function(b, a, c) {
    void 0 === c && (c = 0);
    return setInterval(b.flush.bind(b, this), c);
  };
  a.prototype.recycleAsyncId = function(b, a, c) {
    void 0 === c && (c = 0);
    if (null !== c && this.delay === c && !1 === this.pending) {
      return a;
    }
    clearInterval(a);
  };
  a.prototype.execute = function(b, a) {
    if (this.closed) {
      return Error("executing a cancelled action");
    }
    this.pending = !1;
    if (b = this._execute(b, a)) {
      return b;
    }
    !1 === this.pending && null != this.id && (this.id = this.recycleAsyncId(this.scheduler, this.id, null));
  };
  a.prototype._execute = function(b, a) {
    a = !1;
    var c = void 0;
    try {
      this.work(b);
    } catch (f) {
      a = !0, c = !!f && f || Error(f);
    }
    if (a) {
      return this.unsubscribe(), c;
    }
  };
  a.prototype._unsubscribe = function() {
    var b = this.id, a = this.scheduler, c = a.actions, f = c.indexOf(this);
    this.state = this.work = null;
    this.pending = !1;
    this.scheduler = null;
    -1 !== f && c.splice(f, 1);
    null != b && (this.id = this.recycleAsyncId(a, b, null));
    this.delay = null;
  };
  return a;
}(Action$$module$output_operators), Scheduler$$module$output_operators = function() {
  function c(a, b) {
    void 0 === b && (b = c.now);
    this.SchedulerAction = a;
    this.now = b;
  }
  c.prototype.schedule = function(a, b, c) {
    void 0 === b && (b = 0);
    return (new this.SchedulerAction(this, a)).schedule(c, b);
  };
  c.now = function() {
    return Date.now();
  };
  return c;
}(), AsyncScheduler$$module$output_operators = function(c) {
  function a(b, e) {
    void 0 === e && (e = Scheduler$$module$output_operators.now);
    var d = c.call(this, b, function() {
      return a.delegate && a.delegate !== d ? a.delegate.now() : e();
    }) || this;
    d.actions = [];
    d.active = !1;
    d.scheduled = void 0;
    return d;
  }
  __extends$$module$output_operators(a, c);
  a.prototype.schedule = function(b, e, d) {
    void 0 === e && (e = 0);
    return a.delegate && a.delegate !== this ? a.delegate.schedule(b, e, d) : c.prototype.schedule.call(this, b, e, d);
  };
  a.prototype.flush = function(b) {
    var a = this.actions;
    if (this.active) {
      a.push(b);
    } else {
      var c;
      this.active = !0;
      do {
        if (c = b.execute(b.state, b.delay)) {
          break;
        }
      } while (b = a.shift());
      this.active = !1;
      if (c) {
        for (; b = a.shift();) {
          b.unsubscribe();
        }
        throw c;
      }
    }
  };
  return a;
}(Scheduler$$module$output_operators), async$$module$output_operators = new AsyncScheduler$$module$output_operators(AsyncAction$$module$output_operators);
function isNumeric$$module$output_operators(c) {
  return !isArray$$module$output_operators(c) && 0 <= c - parseFloat(c) + 1;
}
function isScheduler$$module$output_operators(c) {
  return c && "function" === typeof c.schedule;
}
function timer$$module$output_operators(c, a, b) {
  void 0 === c && (c = 0);
  var e = -1;
  isNumeric$$module$output_operators(a) ? e = 1 > Number(a) && 1 || Number(a) : isScheduler$$module$output_operators(a) && (b = a);
  isScheduler$$module$output_operators(b) || (b = async$$module$output_operators);
  return new Observable$$module$output_operators(function(a) {
    var d = isNumeric$$module$output_operators(c) ? c : +c - b.now();
    return b.schedule(dispatch$$module$output_operators, d, {index:0, period:e, subscriber:a});
  });
}
function dispatch$$module$output_operators(c) {
  var a = c.index, b = c.period, e = c.subscriber;
  e.next(a);
  if (!e.closed) {
    if (-1 === b) {
      return e.complete();
    }
    c.index = a + 1;
    this.schedule(c, b);
  }
}
var BufferOperator$$module$output_operators = function() {
  function c(a) {
    this.closingNotifier = a;
  }
  c.prototype.call = function(a, b) {
    return b.subscribe(new BufferSubscriber$$module$output_operators(a, this.closingNotifier));
  };
  return c;
}(), BufferSubscriber$$module$output_operators = function(c) {
  function a(b, a) {
    b = c.call(this, b) || this;
    b.buffer = [];
    b.add(subscribeToResult$$module$output_operators(b, a));
    return b;
  }
  __extends$$module$output_operators(a, c);
  a.prototype._next = function(b) {
    this.buffer.push(b);
  };
  a.prototype.notifyNext = function(b, a, c, f, g) {
    b = this.buffer;
    this.buffer = [];
    this.destination.next(b);
  };
  return a;
}(OuterSubscriber$$module$output_operators), BufferCountOperator$$module$output_operators = function() {
  function c(a, b) {
    this.bufferSize = a;
    this.subscriberClass = (this.startBufferEvery = b) && a !== b ? BufferSkipCountSubscriber$$module$output_operators : BufferCountSubscriber$$module$output_operators;
  }
  c.prototype.call = function(a, b) {
    return b.subscribe(new this.subscriberClass(a, this.bufferSize, this.startBufferEvery));
  };
  return c;
}(), BufferCountSubscriber$$module$output_operators = function(c) {
  function a(b, a) {
    b = c.call(this, b) || this;
    b.bufferSize = a;
    b.buffer = [];
    return b;
  }
  __extends$$module$output_operators(a, c);
  a.prototype._next = function(b) {
    var a = this.buffer;
    a.push(b);
    a.length == this.bufferSize && (this.destination.next(a), this.buffer = []);
  };
  a.prototype._complete = function() {
    var b = this.buffer;
    0 < b.length && this.destination.next(b);
    c.prototype._complete.call(this);
  };
  return a;
}(Subscriber$$module$output_operators), BufferSkipCountSubscriber$$module$output_operators = function(c) {
  function a(b, a, d) {
    b = c.call(this, b) || this;
    b.bufferSize = a;
    b.startBufferEvery = d;
    b.buffers = [];
    b.count = 0;
    return b;
  }
  __extends$$module$output_operators(a, c);
  a.prototype._next = function(b) {
    var a = this.bufferSize, c = this.startBufferEvery, f = this.buffers, g = this.count;
    this.count++;
    0 === g % c && f.push([]);
    for (c = f.length; c--;) {
      g = f[c], g.push(b), g.length === a && (f.splice(c, 1), this.destination.next(g));
    }
  };
  a.prototype._complete = function() {
    for (var b = this.buffers, a = this.destination; 0 < b.length;) {
      var d = b.shift();
      0 < d.length && a.next(d);
    }
    c.prototype._complete.call(this);
  };
  return a;
}(Subscriber$$module$output_operators), BufferTimeOperator$$module$output_operators = function() {
  function c(a, b, c, d) {
    this.bufferTimeSpan = a;
    this.bufferCreationInterval = b;
    this.maxBufferSize = c;
    this.scheduler = d;
  }
  c.prototype.call = function(a, b) {
    return b.subscribe(new BufferTimeSubscriber$$module$output_operators(a, this.bufferTimeSpan, this.bufferCreationInterval, this.maxBufferSize, this.scheduler));
  };
  return c;
}(), Context$$module$output_operators = function() {
  return function() {
    this.buffer = [];
  };
}(), BufferTimeSubscriber$$module$output_operators = function(c) {
  function a(b, a, d, f, g) {
    b = c.call(this, b) || this;
    b.bufferTimeSpan = a;
    b.bufferCreationInterval = d;
    b.maxBufferSize = f;
    b.scheduler = g;
    b.contexts = [];
    f = b.openContext();
    b.timespanOnly = null == d || 0 > d;
    if (b.timespanOnly) {
      b.add(f.closeAction = g.schedule(dispatchBufferTimeSpanOnly$$module$output_operators, a, {subscriber:b, context:f, bufferTimeSpan:a}));
    } else {
      var e = {bufferTimeSpan:a, bufferCreationInterval:d, subscriber:b, scheduler:g};
      b.add(f.closeAction = g.schedule(dispatchBufferClose$$module$output_operators, a, {subscriber:b, context:f}));
      b.add(g.schedule(dispatchBufferCreation$$module$output_operators, d, e));
    }
    return b;
  }
  __extends$$module$output_operators(a, c);
  a.prototype._next = function(b) {
    for (var a = this.contexts, c = a.length, f, g = 0; g < c; g++) {
      var h = a[g], k = h.buffer;
      k.push(b);
      k.length == this.maxBufferSize && (f = h);
    }
    if (f) {
      this.onBufferFull(f);
    }
  };
  a.prototype._error = function(b) {
    this.contexts.length = 0;
    c.prototype._error.call(this, b);
  };
  a.prototype._complete = function() {
    for (var b = this.contexts, a = this.destination; 0 < b.length;) {
      var d = b.shift();
      a.next(d.buffer);
    }
    c.prototype._complete.call(this);
  };
  a.prototype._unsubscribe = function() {
    this.contexts = null;
  };
  a.prototype.onBufferFull = function(b) {
    this.closeContext(b);
    b = b.closeAction;
    b.unsubscribe();
    this.remove(b);
    if (!this.closed && this.timespanOnly) {
      b = this.openContext();
      var a = this.bufferTimeSpan;
      this.add(b.closeAction = this.scheduler.schedule(dispatchBufferTimeSpanOnly$$module$output_operators, a, {subscriber:this, context:b, bufferTimeSpan:a}));
    }
  };
  a.prototype.openContext = function() {
    var b = new Context$$module$output_operators;
    this.contexts.push(b);
    return b;
  };
  a.prototype.closeContext = function(b) {
    this.destination.next(b.buffer);
    var a = this.contexts;
    0 <= (a ? a.indexOf(b) : -1) && a.splice(a.indexOf(b), 1);
  };
  return a;
}(Subscriber$$module$output_operators);
function dispatchBufferTimeSpanOnly$$module$output_operators(c) {
  var a = c.subscriber, b = c.context;
  b && a.closeContext(b);
  a.closed || (c.context = a.openContext(), c.context.closeAction = this.schedule(c, c.bufferTimeSpan));
}
function dispatchBufferCreation$$module$output_operators(c) {
  var a = c.bufferCreationInterval, b = c.bufferTimeSpan, e = c.subscriber, d = c.scheduler, f = e.openContext();
  e.closed || (e.add(f.closeAction = d.schedule(dispatchBufferClose$$module$output_operators, b, {subscriber:e, context:f})), this.schedule(c, a));
}
function dispatchBufferClose$$module$output_operators(c) {
  c.subscriber.closeContext(c.context);
}
var BufferToggleOperator$$module$output_operators = function() {
  function c(a, b) {
    this.openings = a;
    this.closingSelector = b;
  }
  c.prototype.call = function(a, b) {
    return b.subscribe(new BufferToggleSubscriber$$module$output_operators(a, this.openings, this.closingSelector));
  };
  return c;
}(), BufferToggleSubscriber$$module$output_operators = function(c) {
  function a(b, a, d) {
    b = c.call(this, b) || this;
    b.openings = a;
    b.closingSelector = d;
    b.contexts = [];
    b.add(subscribeToResult$$module$output_operators(b, a));
    return b;
  }
  __extends$$module$output_operators(a, c);
  a.prototype._next = function(b) {
    for (var a = this.contexts, c = a.length, f = 0; f < c; f++) {
      a[f].buffer.push(b);
    }
  };
  a.prototype._error = function(b) {
    for (var a = this.contexts; 0 < a.length;) {
      var d = a.shift();
      d.subscription.unsubscribe();
      d.buffer = null;
      d.subscription = null;
    }
    this.contexts = null;
    c.prototype._error.call(this, b);
  };
  a.prototype._complete = function() {
    for (var b = this.contexts; 0 < b.length;) {
      var a = b.shift();
      this.destination.next(a.buffer);
      a.subscription.unsubscribe();
      a.buffer = null;
      a.subscription = null;
    }
    this.contexts = null;
    c.prototype._complete.call(this);
  };
  a.prototype.notifyNext = function(b, a, c, f, g) {
    b ? this.closeBuffer(b) : this.openBuffer(a);
  };
  a.prototype.notifyComplete = function(b) {
    this.closeBuffer(b.context);
  };
  a.prototype.openBuffer = function(b) {
    try {
      var a = this.closingSelector.call(this, b);
      a && this.trySubscribe(a);
    } catch (d) {
      this._error(d);
    }
  };
  a.prototype.closeBuffer = function(b) {
    var a = this.contexts;
    if (a && b) {
      var c = b.subscription;
      this.destination.next(b.buffer);
      a.splice(a.indexOf(b), 1);
      this.remove(c);
      c.unsubscribe();
    }
  };
  a.prototype.trySubscribe = function(b) {
    var a = this.contexts, c = new Subscription$$module$output_operators, f = {buffer:[], subscription:c};
    a.push(f);
    b = subscribeToResult$$module$output_operators(this, b, f);
    !b || b.closed ? this.closeBuffer(f) : (b.context = f, this.add(b), c.add(b));
  };
  return a;
}(OuterSubscriber$$module$output_operators), BufferWhenOperator$$module$output_operators = function() {
  function c(a) {
    this.closingSelector = a;
  }
  c.prototype.call = function(a, b) {
    return b.subscribe(new BufferWhenSubscriber$$module$output_operators(a, this.closingSelector));
  };
  return c;
}(), BufferWhenSubscriber$$module$output_operators = function(c) {
  function a(b, a) {
    b = c.call(this, b) || this;
    b.closingSelector = a;
    b.subscribing = !1;
    b.openBuffer();
    return b;
  }
  __extends$$module$output_operators(a, c);
  a.prototype._next = function(b) {
    this.buffer.push(b);
  };
  a.prototype._complete = function() {
    var b = this.buffer;
    b && this.destination.next(b);
    c.prototype._complete.call(this);
  };
  a.prototype._unsubscribe = function() {
    this.buffer = null;
    this.subscribing = !1;
  };
  a.prototype.notifyNext = function(b, a, c, f, g) {
    this.openBuffer();
  };
  a.prototype.notifyComplete = function() {
    this.subscribing ? this.complete() : this.openBuffer();
  };
  a.prototype.openBuffer = function() {
    var b = this.closingSubscription;
    b && (this.remove(b), b.unsubscribe());
    (b = this.buffer) && this.destination.next(b);
    this.buffer = [];
    var a = tryCatch$$module$output_operators(this.closingSelector)();
    a === errorObject$$module$output_operators ? this.error(errorObject$$module$output_operators.e) : (this.closingSubscription = b = new Subscription$$module$output_operators, this.add(b), this.subscribing = !0, b.add(subscribeToResult$$module$output_operators(this, a)), this.subscribing = !1);
  };
  return a;
}(OuterSubscriber$$module$output_operators), CatchOperator$$module$output_operators = function() {
  function c(a) {
    this.selector = a;
  }
  c.prototype.call = function(a, b) {
    return b.subscribe(new CatchSubscriber$$module$output_operators(a, this.selector, this.caught));
  };
  return c;
}(), CatchSubscriber$$module$output_operators = function(c) {
  function a(b, a, d) {
    b = c.call(this, b) || this;
    b.selector = a;
    b.caught = d;
    return b;
  }
  __extends$$module$output_operators(a, c);
  a.prototype.error = function(b) {
    if (!this.isStopped) {
      var a = void 0;
      try {
        a = this.selector(b, this.caught);
      } catch (d) {
        c.prototype.error.call(this, d);
        return;
      }
      this._unsubscribeAndRecycle();
      b = new InnerSubscriber$$module$output_operators(this, void 0, void 0);
      this.add(b);
      subscribeToResult$$module$output_operators(this, a, void 0, void 0, b);
    }
  };
  return a;
}(OuterSubscriber$$module$output_operators);
function fromArray$$module$output_operators(c, a) {
  return a ? new Observable$$module$output_operators(function(b) {
    var e = new Subscription$$module$output_operators, d = 0;
    e.add(a.schedule(function() {
      d === c.length ? b.complete() : (b.next(c[d++]), b.closed || e.add(this.schedule()));
    }));
    return e;
  }) : new Observable$$module$output_operators(subscribeToArray$$module$output_operators(c));
}
var NONE$$module$output_operators = {}, CombineLatestOperator$$module$output_operators = function() {
  function c(a) {
    this.resultSelector = a;
  }
  c.prototype.call = function(a, b) {
    return b.subscribe(new CombineLatestSubscriber$$module$output_operators(a, this.resultSelector));
  };
  return c;
}(), CombineLatestSubscriber$$module$output_operators = function(c) {
  function a(b, a) {
    b = c.call(this, b) || this;
    b.resultSelector = a;
    b.active = 0;
    b.values = [];
    b.observables = [];
    return b;
  }
  __extends$$module$output_operators(a, c);
  a.prototype._next = function(b) {
    this.values.push(NONE$$module$output_operators);
    this.observables.push(b);
  };
  a.prototype._complete = function() {
    var b = this.observables, a = b.length;
    if (0 === a) {
      this.destination.complete();
    } else {
      this.toRespond = this.active = a;
      for (var c = 0; c < a; c++) {
        var f = b[c];
        this.add(subscribeToResult$$module$output_operators(this, f, f, c));
      }
    }
  };
  a.prototype.notifyComplete = function(b) {
    0 === --this.active && this.destination.complete();
  };
  a.prototype.notifyNext = function(b, a, c, f, g) {
    b = this.values;
    f = b[c];
    f = this.toRespond ? f === NONE$$module$output_operators ? --this.toRespond : this.toRespond : 0;
    b[c] = a;
    0 === f && (this.resultSelector ? this._tryResultSelector(b) : this.destination.next(b.slice()));
  };
  a.prototype._tryResultSelector = function(b) {
    try {
      var a = this.resultSelector.apply(this, b);
    } catch (d) {
      this.destination.error(d);
      return;
    }
    this.destination.next(a);
  };
  return a;
}(OuterSubscriber$$module$output_operators);
function isInteropObservable$$module$output_operators(c) {
  return c && "function" === typeof c[observable$$module$output_operators];
}
function isIterable$$module$output_operators(c) {
  return c && "function" === typeof c[iterator$$module$output_operators];
}
function fromPromise$$module$output_operators(c, a) {
  return a ? new Observable$$module$output_operators(function(b) {
    var e = new Subscription$$module$output_operators;
    e.add(a.schedule(function() {
      return c.then(function(c) {
        e.add(a.schedule(function() {
          b.next(c);
          e.add(a.schedule(function() {
            return b.complete();
          }));
        }));
      }, function(c) {
        e.add(a.schedule(function() {
          return b.error(c);
        }));
      });
    }));
    return e;
  }) : new Observable$$module$output_operators(subscribeToPromise$$module$output_operators(c));
}
function fromIterable$$module$output_operators(c, a) {
  if (!c) {
    throw Error("Iterable cannot be null");
  }
  return a ? new Observable$$module$output_operators(function(b) {
    var e = new Subscription$$module$output_operators, d;
    e.add(function() {
      d && "function" === typeof d.return && d.return();
    });
    e.add(a.schedule(function() {
      d = c[iterator$$module$output_operators]();
      e.add(a.schedule(function() {
        if (!b.closed) {
          try {
            var a = d.next();
            var c = a.value;
            var e = a.done;
          } catch (k) {
            b.error(k);
            return;
          }
          e ? b.complete() : (b.next(c), this.schedule());
        }
      }));
    }));
    return e;
  }) : new Observable$$module$output_operators(subscribeToIterable$$module$output_operators(c));
}
function fromObservable$$module$output_operators(c, a) {
  return a ? new Observable$$module$output_operators(function(b) {
    var e = new Subscription$$module$output_operators;
    e.add(a.schedule(function() {
      var d = c[observable$$module$output_operators]();
      e.add(d.subscribe({next:function(c) {
        e.add(a.schedule(function() {
          return b.next(c);
        }));
      }, error:function(c) {
        e.add(a.schedule(function() {
          return b.error(c);
        }));
      }, complete:function() {
        e.add(a.schedule(function() {
          return b.complete();
        }));
      }}));
    }));
    return e;
  }) : new Observable$$module$output_operators(subscribeToObservable$$module$output_operators(c));
}
function from$$module$output_operators(c, a) {
  if (!a) {
    return c instanceof Observable$$module$output_operators ? c : new Observable$$module$output_operators(subscribeTo$$module$output_operators(c));
  }
  if (null != c) {
    if (isInteropObservable$$module$output_operators(c)) {
      return fromObservable$$module$output_operators(c, a);
    }
    if (isPromise$$module$output_operators(c)) {
      return fromPromise$$module$output_operators(c, a);
    }
    if (isArrayLike$$module$output_operators(c)) {
      return fromArray$$module$output_operators(c, a);
    }
    if (isIterable$$module$output_operators(c) || "string" === typeof c) {
      return fromIterable$$module$output_operators(c, a);
    }
  }
  throw new TypeError((null !== c && typeof c || c) + " is not observable");
}
var EMPTY$$module$output_operators = new Observable$$module$output_operators(function(c) {
  return c.complete();
});
function empty$1$$module$output_operators(c) {
  return c ? emptyScheduled$$module$output_operators(c) : EMPTY$$module$output_operators;
}
function emptyScheduled$$module$output_operators(c) {
  return new Observable$$module$output_operators(function(a) {
    return c.schedule(function() {
      return a.complete();
    });
  });
}
function scalar$$module$output_operators(c) {
  var a = new Observable$$module$output_operators(function(b) {
    b.next(c);
    b.complete();
  });
  a._isScalar = !0;
  a.value = c;
  return a;
}
function of$$module$output_operators() {
  for (var c = [], a = 0; a < arguments.length; a++) {
    c[a] = arguments[a];
  }
  a = c[c.length - 1];
  isScheduler$$module$output_operators(a) ? c.pop() : a = void 0;
  switch(c.length) {
    case 0:
      return empty$1$$module$output_operators(a);
    case 1:
      return a ? fromArray$$module$output_operators(c, a) : scalar$$module$output_operators(c[0]);
    default:
      return fromArray$$module$output_operators(c, a);
  }
}
var MapOperator$$module$output_operators = function() {
  function c(a, b) {
    this.project = a;
    this.thisArg = b;
  }
  c.prototype.call = function(a, b) {
    return b.subscribe(new MapSubscriber$$module$output_operators(a, this.project, this.thisArg));
  };
  return c;
}(), MapSubscriber$$module$output_operators = function(c) {
  function a(b, a, d) {
    b = c.call(this, b) || this;
    b.project = a;
    b.count = 0;
    b.thisArg = d || b;
    return b;
  }
  __extends$$module$output_operators(a, c);
  a.prototype._next = function(b) {
    try {
      var a = this.project.call(this.thisArg, b, this.count++);
    } catch (d) {
      this.destination.error(d);
      return;
    }
    this.destination.next(a);
  };
  return a;
}(Subscriber$$module$output_operators), MergeMapOperator$$module$output_operators = function() {
  function c(a, b) {
    void 0 === b && (b = Number.POSITIVE_INFINITY);
    this.project = a;
    this.concurrent = b;
  }
  c.prototype.call = function(a, b) {
    return b.subscribe(new MergeMapSubscriber$$module$output_operators(a, this.project, this.concurrent));
  };
  return c;
}(), MergeMapSubscriber$$module$output_operators = function(c) {
  function a(b, a, d) {
    void 0 === d && (d = Number.POSITIVE_INFINITY);
    b = c.call(this, b) || this;
    b.project = a;
    b.concurrent = d;
    b.hasCompleted = !1;
    b.buffer = [];
    b.active = 0;
    b.index = 0;
    return b;
  }
  __extends$$module$output_operators(a, c);
  a.prototype._next = function(b) {
    this.active < this.concurrent ? this._tryNext(b) : this.buffer.push(b);
  };
  a.prototype._tryNext = function(b) {
    var a = this.index++;
    try {
      var c = this.project(b, a);
    } catch (f) {
      this.destination.error(f);
      return;
    }
    this.active++;
    this._innerSub(c, b, a);
  };
  a.prototype._innerSub = function(b, a, c) {
    var e = new InnerSubscriber$$module$output_operators(this, void 0, void 0);
    this.destination.add(e);
    subscribeToResult$$module$output_operators(this, b, a, c, e);
  };
  a.prototype._complete = function() {
    this.hasCompleted = !0;
    0 === this.active && 0 === this.buffer.length && this.destination.complete();
    this.unsubscribe();
  };
  a.prototype.notifyNext = function(b, a, c, f, g) {
    this.destination.next(a);
  };
  a.prototype.notifyComplete = function(b) {
    var a = this.buffer;
    this.remove(b);
    this.active--;
    0 < a.length ? this._next(a.shift()) : 0 === this.active && this.hasCompleted && this.destination.complete();
  };
  return a;
}(OuterSubscriber$$module$output_operators);
function identity$$module$output_operators(c) {
  return c;
}
function concat$1$$module$output_operators() {
  for (var c = [], a = 0; a < arguments.length; a++) {
    c[a] = arguments[a];
  }
  return 1 === c.length || 2 === c.length && isScheduler$$module$output_operators(c[1]) ? from$$module$output_operators(c[0]) : (0,module$output_operators.concatAll)()(of$$module$output_operators.apply(void 0, c));
}
var CountOperator$$module$output_operators = function() {
  function c(a, b) {
    this.predicate = a;
    this.source = b;
  }
  c.prototype.call = function(a, b) {
    return b.subscribe(new CountSubscriber$$module$output_operators(a, this.predicate, this.source));
  };
  return c;
}(), CountSubscriber$$module$output_operators = function(c) {
  function a(b, a, d) {
    b = c.call(this, b) || this;
    b.predicate = a;
    b.source = d;
    b.count = 0;
    b.index = 0;
    return b;
  }
  __extends$$module$output_operators(a, c);
  a.prototype._next = function(b) {
    this.predicate ? this._tryPredicate(b) : this.count++;
  };
  a.prototype._tryPredicate = function(b) {
    try {
      var a = this.predicate(b, this.index++, this.source);
    } catch (d) {
      this.destination.error(d);
      return;
    }
    a && this.count++;
  };
  a.prototype._complete = function() {
    this.destination.next(this.count);
    this.destination.complete();
  };
  return a;
}(Subscriber$$module$output_operators), DebounceOperator$$module$output_operators = function() {
  function c(a) {
    this.durationSelector = a;
  }
  c.prototype.call = function(a, b) {
    return b.subscribe(new DebounceSubscriber$$module$output_operators(a, this.durationSelector));
  };
  return c;
}(), DebounceSubscriber$$module$output_operators = function(c) {
  function a(b, a) {
    b = c.call(this, b) || this;
    b.durationSelector = a;
    b.hasValue = !1;
    b.durationSubscription = null;
    return b;
  }
  __extends$$module$output_operators(a, c);
  a.prototype._next = function(b) {
    try {
      var a = this.durationSelector.call(this, b);
      a && this._tryNext(b, a);
    } catch (d) {
      this.destination.error(d);
    }
  };
  a.prototype._complete = function() {
    this.emitValue();
    this.destination.complete();
  };
  a.prototype._tryNext = function(b, a) {
    var c = this.durationSubscription;
    this.value = b;
    this.hasValue = !0;
    c && (c.unsubscribe(), this.remove(c));
    (c = subscribeToResult$$module$output_operators(this, a)) && !c.closed && this.add(this.durationSubscription = c);
  };
  a.prototype.notifyNext = function(b, a, c, f, g) {
    this.emitValue();
  };
  a.prototype.notifyComplete = function() {
    this.emitValue();
  };
  a.prototype.emitValue = function() {
    if (this.hasValue) {
      var b = this.value, a = this.durationSubscription;
      a && (this.durationSubscription = null, a.unsubscribe(), this.remove(a));
      this.value = null;
      this.hasValue = !1;
      c.prototype._next.call(this, b);
    }
  };
  return a;
}(OuterSubscriber$$module$output_operators), DebounceTimeOperator$$module$output_operators = function() {
  function c(a, b) {
    this.dueTime = a;
    this.scheduler = b;
  }
  c.prototype.call = function(a, b) {
    return b.subscribe(new DebounceTimeSubscriber$$module$output_operators(a, this.dueTime, this.scheduler));
  };
  return c;
}(), DebounceTimeSubscriber$$module$output_operators = function(c) {
  function a(b, a, d) {
    b = c.call(this, b) || this;
    b.dueTime = a;
    b.scheduler = d;
    b.debouncedSubscription = null;
    b.lastValue = null;
    b.hasValue = !1;
    return b;
  }
  __extends$$module$output_operators(a, c);
  a.prototype._next = function(b) {
    this.clearDebounce();
    this.lastValue = b;
    this.hasValue = !0;
    this.add(this.debouncedSubscription = this.scheduler.schedule(dispatchNext$$module$output_operators, this.dueTime, this));
  };
  a.prototype._complete = function() {
    this.debouncedNext();
    this.destination.complete();
  };
  a.prototype.debouncedNext = function() {
    this.clearDebounce();
    if (this.hasValue) {
      var b = this.lastValue;
      this.lastValue = null;
      this.hasValue = !1;
      this.destination.next(b);
    }
  };
  a.prototype.clearDebounce = function() {
    var b = this.debouncedSubscription;
    null !== b && (this.remove(b), b.unsubscribe(), this.debouncedSubscription = null);
  };
  return a;
}(Subscriber$$module$output_operators);
function dispatchNext$$module$output_operators(c) {
  c.debouncedNext();
}
var DefaultIfEmptyOperator$$module$output_operators = function() {
  function c(a) {
    thisValue = a;
  }
  c.prototype.call = function(a, b) {
    return b.subscribe(new DefaultIfEmptySubscriber$$module$output_operators(a, thisValue));
  };
  return c;
}(), DefaultIfEmptySubscriber$$module$output_operators = function(c) {
  function a(b, a) {
    b = c.call(this, b) || this;
    bValue = a;
    b.isEmpty = !0;
    return b;
  }
  __extends$$module$output_operators(a, c);
  a.prototype._next = function(b) {
    this.isEmpty = !1;
    this.destination.next(b);
  };
  a.prototype._complete = function() {
    this.isEmpty && this.destination.next(thisValue);
    this.destination.complete();
  };
  return a;
}(Subscriber$$module$output_operators);
function isDate$$module$output_operators(c) {
  return c instanceof Date && !isNaN(+c);
}
function throwError$$module$output_operators(c, a) {
  return a ? new Observable$$module$output_operators(function(b) {
    return a.schedule(dispatch$1$$module$output_operators, 0, {error:c, subscriber:b});
  }) : new Observable$$module$output_operators(function(b) {
    return b.error(c);
  });
}
function dispatch$1$$module$output_operators(c) {
  c.subscriber.error(c.error);
}
var Notification$$module$output_operators = function() {
  function c(a, b, c) {
    this.kind = a;
    this.value = b;
    this.error = c;
    this.hasValue = "N" === a;
  }
  c.prototype.observe = function(a) {
    switch(this.kind) {
      case "N":
        return a.next && a.next(this.value);
      case "E":
        return a.error && a.error(this.error);
      case "C":
        return a.complete && a.complete();
    }
  };
  c.prototype.do = function(a, b, c) {
    switch(this.kind) {
      case "N":
        return a && a(this.value);
      case "E":
        return b && b(this.error);
      case "C":
        return c && c();
    }
  };
  c.prototype.accept = function(a, b, c) {
    return a && "function" === typeof a.next ? this.observe(a) : this.do(a, b, c);
  };
  c.prototype.toObservable = function() {
    switch(this.kind) {
      case "N":
        return of$$module$output_operators(this.value);
      case "E":
        return throwError$$module$output_operators(this.error);
      case "C":
        return empty$1$$module$output_operators();
    }
    throw Error("unexpected notification kind value");
  };
  c.createNext = function(a) {
    return "undefined" !== typeof a ? new c("N", a) : c.undefinedValueNotification;
  };
  c.createError = function(a) {
    return new c("E", void 0, a);
  };
  c.createComplete = function() {
    return c.completeNotification;
  };
  c.completeNotification = new c("C");
  c.undefinedValueNotification = new c("N", void 0);
  return c;
}(), DelayOperator$$module$output_operators = function() {
  function c(a, b) {
    this.delay = a;
    this.scheduler = b;
  }
  c.prototype.call = function(a, b) {
    return b.subscribe(new DelaySubscriber$$module$output_operators(a, this.delay, this.scheduler));
  };
  return c;
}(), DelaySubscriber$$module$output_operators = function(c) {
  function a(b, a, d) {
    b = c.call(this, b) || this;
    b.delay = a;
    b.scheduler = d;
    b.queue = [];
    b.active = !1;
    b.errored = !1;
    return b;
  }
  __extends$$module$output_operators(a, c);
  a.dispatch = function(b) {
    for (var a = b.source, c = a.queue, f = b.scheduler, g = b.destination; 0 < c.length && 0 >= c[0].time - f.now();) {
      c.shift().notification.observe(g);
    }
    0 < c.length ? (a = Math.max(0, c[0].time - f.now()), this.schedule(b, a)) : (this.unsubscribe(), a.active = !1);
  };
  a.prototype._schedule = function(b) {
    this.active = !0;
    this.destination.add(b.schedule(a.dispatch, this.delay, {source:this, destination:this.destination, scheduler:b}));
  };
  a.prototype.scheduleNotification = function(b) {
    if (!0 !== this.errored) {
      var a = this.scheduler;
      b = new DelayMessage$$module$output_operators(a.now() + this.delay, b);
      this.queue.push(b);
      !1 === this.active && this._schedule(a);
    }
  };
  a.prototype._next = function(b) {
    this.scheduleNotification(Notification$$module$output_operators.createNext(b));
  };
  a.prototype._error = function(b) {
    this.errored = !0;
    this.queue = [];
    this.destination.error(b);
    this.unsubscribe();
  };
  a.prototype._complete = function() {
    this.scheduleNotification(Notification$$module$output_operators.createComplete());
    this.unsubscribe();
  };
  return a;
}(Subscriber$$module$output_operators), DelayMessage$$module$output_operators = function() {
  return function(c, a) {
    this.time = c;
    this.notification = a;
  };
}(), DelayWhenOperator$$module$output_operators = function() {
  function c(a) {
    this.delayDurationSelector = a;
  }
  c.prototype.call = function(a, b) {
    return b.subscribe(new DelayWhenSubscriber$$module$output_operators(a, this.delayDurationSelector));
  };
  return c;
}(), DelayWhenSubscriber$$module$output_operators = function(c) {
  function a(b, a) {
    b = c.call(this, b) || this;
    b.delayDurationSelector = a;
    b.completed = !1;
    b.delayNotifierSubscriptions = [];
    b.index = 0;
    return b;
  }
  __extends$$module$output_operators(a, c);
  a.prototype.notifyNext = function(b, a, c, f, g) {
    this.destination.next(b);
    this.removeSubscription(g);
    this.tryComplete();
  };
  a.prototype.notifyError = function(b, a) {
    this._error(b);
  };
  a.prototype.notifyComplete = function(b) {
    (b = this.removeSubscription(b)) && this.destination.next(b);
    this.tryComplete();
  };
  a.prototype._next = function(b) {
    var a = this.index++;
    try {
      var c = this.delayDurationSelector(b, a);
      c && this.tryDelay(c, b);
    } catch (f) {
      this.destination.error(f);
    }
  };
  a.prototype._complete = function() {
    this.completed = !0;
    this.tryComplete();
    this.unsubscribe();
  };
  a.prototype.removeSubscription = function(b) {
    b.unsubscribe();
    var a = this.delayNotifierSubscriptions.indexOf(b);
    -1 !== a && this.delayNotifierSubscriptions.splice(a, 1);
    return b.outerValue;
  };
  a.prototype.tryDelay = function(b, a) {
    (b = subscribeToResult$$module$output_operators(this, b, a)) && !b.closed && (this.destination.add(b), this.delayNotifierSubscriptions.push(b));
  };
  a.prototype.tryComplete = function() {
    this.completed && 0 === this.delayNotifierSubscriptions.length && this.destination.complete();
  };
  return a;
}(OuterSubscriber$$module$output_operators), SubscriptionDelayObservable$$module$output_operators = function(c) {
  function a(b, a) {
    var e = c.call(this) || this;
    e.source = b;
    e.subscriptionDelay = a;
    return e;
  }
  __extends$$module$output_operators(a, c);
  a.prototype._subscribe = function(b) {
    this.subscriptionDelay.subscribe(new SubscriptionDelaySubscriber$$module$output_operators(b, this.source));
  };
  return a;
}(Observable$$module$output_operators), SubscriptionDelaySubscriber$$module$output_operators = function(c) {
  function a(b, a) {
    var e = c.call(this) || this;
    e.parent = b;
    e.source = a;
    e.sourceSubscribed = !1;
    return e;
  }
  __extends$$module$output_operators(a, c);
  a.prototype._next = function(b) {
    this.subscribeToSource();
  };
  a.prototype._error = function(b) {
    this.unsubscribe();
    this.parent.error(b);
  };
  a.prototype._complete = function() {
    this.unsubscribe();
    this.subscribeToSource();
  };
  a.prototype.subscribeToSource = function() {
    this.sourceSubscribed || (this.sourceSubscribed = !0, this.unsubscribe(), this.source.subscribe(this.parent));
  };
  return a;
}(Subscriber$$module$output_operators), DeMaterializeOperator$$module$output_operators = function() {
  function c() {
  }
  c.prototype.call = function(a, b) {
    return b.subscribe(new DeMaterializeSubscriber$$module$output_operators(a));
  };
  return c;
}(), DeMaterializeSubscriber$$module$output_operators = function(c) {
  function a(b) {
    return c.call(this, b) || this;
  }
  __extends$$module$output_operators(a, c);
  a.prototype._next = function(b) {
    b.observe(this.destination);
  };
  return a;
}(Subscriber$$module$output_operators), DistinctOperator$$module$output_operators = function() {
  function c(a, b) {
    this.keySelector = a;
    this.flushes = b;
  }
  c.prototype.call = function(a, b) {
    return b.subscribe(new DistinctSubscriber$$module$output_operators(a, this.keySelector, this.flushes));
  };
  return c;
}(), DistinctSubscriber$$module$output_operators = function(c) {
  function a(b, a, d) {
    b = c.call(this, b) || this;
    b.keySelector = a;
    b.values = new Set;
    d && b.add(subscribeToResult$$module$output_operators(b, d));
    return b;
  }
  __extends$$module$output_operators(a, c);
  a.prototype.notifyNext = function(b, a, c, f, g) {
    this.values.clear();
  };
  a.prototype.notifyError = function(b, a) {
    this._error(b);
  };
  a.prototype._next = function(b) {
    this.keySelector ? this._useKeySelector(b) : this._finalizeNext(b, b);
  };
  a.prototype._useKeySelector = function(b) {
    var a = this.destination;
    try {
      var c = this.keySelector(b);
    } catch (f) {
      a.error(f);
      return;
    }
    this._finalizeNext(c, b);
  };
  a.prototype._finalizeNext = function(b, a) {
    var c = this.values;
    c.has(b) || (c.add(b), this.destination.next(a));
  };
  return a;
}(OuterSubscriber$$module$output_operators), DistinctUntilChangedOperator$$module$output_operators = function() {
  function c(a, b) {
    this.compare = a;
    this.keySelector = b;
  }
  c.prototype.call = function(a, b) {
    return b.subscribe(new DistinctUntilChangedSubscriber$$module$output_operators(a, this.compare, this.keySelector));
  };
  return c;
}(), DistinctUntilChangedSubscriber$$module$output_operators = function(c) {
  function a(b, a, d) {
    b = c.call(this, b) || this;
    b.keySelector = d;
    b.hasKey = !1;
    "function" === typeof a && (b.compare = a);
    return b;
  }
  __extends$$module$output_operators(a, c);
  a.prototype.compare = function(b, a) {
    return b === a;
  };
  a.prototype._next = function(b) {
    var a = b;
    if (this.keySelector && (a = tryCatch$$module$output_operators(this.keySelector)(b), a === errorObject$$module$output_operators)) {
      return this.destination.error(errorObject$$module$output_operators.e);
    }
    var c = !1;
    if (this.hasKey) {
      if (c = tryCatch$$module$output_operators(this.compare)(this.key, a), c === errorObject$$module$output_operators) {
        return this.destination.error(errorObject$$module$output_operators.e);
      }
    } else {
      this.hasKey = !0;
    }
    !1 === !!c && (this.key = a, this.destination.next(b));
  };
  return a;
}(Subscriber$$module$output_operators);
function ArgumentOutOfRangeErrorImpl$$module$output_operators() {
  Error.call(this);
  this.message = "argument out of range";
  this.name = "ArgumentOutOfRangeError";
  return this;
}
ArgumentOutOfRangeErrorImpl$$module$output_operators.prototype = Object.create(Error.prototype);
var ArgumentOutOfRangeError$$module$output_operators = ArgumentOutOfRangeErrorImpl$$module$output_operators, FilterOperator$$module$output_operators = function() {
  function c(a, b) {
    this.predicate = a;
    this.thisArg = b;
  }
  c.prototype.call = function(a, b) {
    return b.subscribe(new FilterSubscriber$$module$output_operators(a, this.predicate, this.thisArg));
  };
  return c;
}(), FilterSubscriber$$module$output_operators = function(c) {
  function a(b, a, d) {
    b = c.call(this, b) || this;
    b.predicate = a;
    b.thisArg = d;
    b.count = 0;
    return b;
  }
  __extends$$module$output_operators(a, c);
  a.prototype._next = function(b) {
    try {
      var a = this.predicate.call(this.thisArg, b, this.count++);
    } catch (d) {
      this.destination.error(d);
      return;
    }
    a && this.destination.next(b);
  };
  return a;
}(Subscriber$$module$output_operators), DoOperator$$module$output_operators = function() {
  function c(a, b, c) {
    this.nextOrObserver = a;
    this.error = b;
    this.complete = c;
  }
  c.prototype.call = function(a, b) {
    return b.subscribe(new TapSubscriber$$module$output_operators(a, this.nextOrObserver, this.error, this.complete));
  };
  return c;
}(), TapSubscriber$$module$output_operators = function(c) {
  function a(b, a, d, f) {
    b = c.call(this, b) || this;
    b._tapNext = noop$$module$output_operators;
    b._tapError = noop$$module$output_operators;
    b._tapComplete = noop$$module$output_operators;
    b._tapError = d || noop$$module$output_operators;
    b._tapComplete = f || noop$$module$output_operators;
    isFunction$$module$output_operators(a) ? (b._context = b, b._tapNext = a) : a && (b._context = a, b._tapNext = a.next || noop$$module$output_operators, b._tapError = a.error || noop$$module$output_operators, b._tapComplete = a.complete || noop$$module$output_operators);
    return b;
  }
  __extends$$module$output_operators(a, c);
  a.prototype._next = function(b) {
    try {
      this._tapNext.call(this._context, b);
    } catch (e) {
      this.destination.error(e);
      return;
    }
    this.destination.next(b);
  };
  a.prototype._error = function(b) {
    try {
      this._tapError.call(this._context, b);
    } catch (e) {
      this.destination.error(e);
      return;
    }
    this.destination.error(b);
  };
  a.prototype._complete = function() {
    try {
      this._tapComplete.call(this._context);
    } catch (b) {
      this.destination.error(b);
      return;
    }
    return this.destination.complete();
  };
  return a;
}(Subscriber$$module$output_operators);
function EmptyErrorImpl$$module$output_operators() {
  Error.call(this);
  this.message = "no elements in sequence";
  this.name = "EmptyError";
  return this;
}
EmptyErrorImpl$$module$output_operators.prototype = Object.create(Error.prototype);
var EmptyError$$module$output_operators = EmptyErrorImpl$$module$output_operators;
module$output_operators.throwIfEmpty = function(c) {
  void 0 === c && (c = defaultErrorFactory$$module$output_operators);
  return (0,module$output_operators.tap)({hasValue:!1, next:function() {
    this.hasValue = !0;
  }, complete:function() {
    if (!this.hasValue) {
      throw c();
    }
  }});
};
function defaultErrorFactory$$module$output_operators() {
  return new EmptyError$$module$output_operators;
}
var TakeOperator$$module$output_operators = function() {
  function c(a) {
    this.total = a;
    if (0 > this.total) {
      throw new ArgumentOutOfRangeError$$module$output_operators;
    }
  }
  c.prototype.call = function(a, b) {
    return b.subscribe(new TakeSubscriber$$module$output_operators(a, this.total));
  };
  return c;
}(), TakeSubscriber$$module$output_operators = function(c) {
  function a(b, a) {
    b = c.call(this, b) || this;
    b.total = a;
    b.count = 0;
    return b;
  }
  __extends$$module$output_operators(a, c);
  a.prototype._next = function(b) {
    var a = this.total, c = ++this.count;
    c <= a && (this.destination.next(b), c === a && (this.destination.complete(), this.unsubscribe()));
  };
  return a;
}(Subscriber$$module$output_operators), EveryOperator$$module$output_operators = function() {
  function c(a, b, c) {
    this.predicate = a;
    this.thisArg = b;
    this.source = c;
  }
  c.prototype.call = function(a, b) {
    return b.subscribe(new EverySubscriber$$module$output_operators(a, this.predicate, this.thisArg, this.source));
  };
  return c;
}(), EverySubscriber$$module$output_operators = function(c) {
  function a(b, a, d, f) {
    b = c.call(this, b) || this;
    b.predicate = a;
    b.thisArg = d;
    b.source = f;
    b.index = 0;
    b.thisArg = d || b;
    return b;
  }
  __extends$$module$output_operators(a, c);
  a.prototype.notifyComplete = function(b) {
    this.destination.next(b);
    this.destination.complete();
  };
  a.prototype._next = function(b) {
    var a = !1;
    try {
      a = this.predicate.call(this.thisArg, b, this.index++, this.source);
    } catch (d) {
      this.destination.error(d);
      return;
    }
    a || this.notifyComplete(!1);
  };
  a.prototype._complete = function() {
    this.notifyComplete(!0);
  };
  return a;
}(Subscriber$$module$output_operators), SwitchFirstOperator$$module$output_operators = function() {
  function c() {
  }
  c.prototype.call = function(a, b) {
    return b.subscribe(new SwitchFirstSubscriber$$module$output_operators(a));
  };
  return c;
}(), SwitchFirstSubscriber$$module$output_operators = function(c) {
  function a(b) {
    b = c.call(this, b) || this;
    b.hasCompleted = !1;
    b.hasSubscription = !1;
    return b;
  }
  __extends$$module$output_operators(a, c);
  a.prototype._next = function(b) {
    this.hasSubscription || (this.hasSubscription = !0, this.add(subscribeToResult$$module$output_operators(this, b)));
  };
  a.prototype._complete = function() {
    this.hasCompleted = !0;
    this.hasSubscription || this.destination.complete();
  };
  a.prototype.notifyComplete = function(b) {
    this.remove(b);
    this.hasSubscription = !1;
    this.hasCompleted && this.destination.complete();
  };
  return a;
}(OuterSubscriber$$module$output_operators), ExhauseMapOperator$$module$output_operators = function() {
  function c(a) {
    this.project = a;
  }
  c.prototype.call = function(a, b) {
    return b.subscribe(new ExhaustMapSubscriber$$module$output_operators(a, this.project));
  };
  return c;
}(), ExhaustMapSubscriber$$module$output_operators = function(c) {
  function a(b, a) {
    b = c.call(this, b) || this;
    b.project = a;
    b.hasSubscription = !1;
    b.hasCompleted = !1;
    b.index = 0;
    return b;
  }
  __extends$$module$output_operators(a, c);
  a.prototype._next = function(b) {
    this.hasSubscription || this.tryNext(b);
  };
  a.prototype.tryNext = function(b) {
    var a = this.index++;
    try {
      var c = this.project(b, a);
    } catch (f) {
      this.destination.error(f);
      return;
    }
    this.hasSubscription = !0;
    this._innerSub(c, b, a);
  };
  a.prototype._innerSub = function(b, a, c) {
    var e = new InnerSubscriber$$module$output_operators(this, void 0, void 0);
    this.destination.add(e);
    subscribeToResult$$module$output_operators(this, b, a, c, e);
  };
  a.prototype._complete = function() {
    this.hasCompleted = !0;
    this.hasSubscription || this.destination.complete();
    this.unsubscribe();
  };
  a.prototype.notifyNext = function(b, a, c, f, g) {
    this.destination.next(a);
  };
  a.prototype.notifyError = function(b) {
    this.destination.error(b);
  };
  a.prototype.notifyComplete = function(b) {
    this.destination.remove(b);
    this.hasSubscription = !1;
    this.hasCompleted && this.destination.complete();
  };
  return a;
}(OuterSubscriber$$module$output_operators), ExpandOperator$$module$output_operators = function() {
  function c(a, b, c) {
    this.project = a;
    this.concurrent = b;
    this.scheduler = c;
  }
  c.prototype.call = function(a, b) {
    return b.subscribe(new ExpandSubscriber$$module$output_operators(a, this.project, this.concurrent, this.scheduler));
  };
  return c;
}(), ExpandSubscriber$$module$output_operators = function(c) {
  function a(b, a, d, f) {
    b = c.call(this, b) || this;
    b.project = a;
    b.concurrent = d;
    b.scheduler = f;
    b.index = 0;
    b.active = 0;
    b.hasCompleted = !1;
    d < Number.POSITIVE_INFINITY && (b.buffer = []);
    return b;
  }
  __extends$$module$output_operators(a, c);
  a.dispatch = function(b) {
    b.subscriber.subscribeToProjection(b.result, b.value, b.index);
  };
  a.prototype._next = function(b) {
    var c = this.destination;
    if (c.closed) {
      this._complete();
    } else {
      var d = this.index++;
      if (this.active < this.concurrent) {
        c.next(b);
        var f = tryCatch$$module$output_operators(this.project)(b, d);
        f === errorObject$$module$output_operators ? c.error(errorObject$$module$output_operators.e) : this.scheduler ? this.destination.add(this.scheduler.schedule(a.dispatch, 0, {subscriber:this, result:f, value:b, index:d})) : this.subscribeToProjection(f, b, d);
      } else {
        this.buffer.push(b);
      }
    }
  };
  a.prototype.subscribeToProjection = function(b, a, c) {
    this.active++;
    this.destination.add(subscribeToResult$$module$output_operators(this, b, a, c));
  };
  a.prototype._complete = function() {
    (this.hasCompleted = !0, 0 === this.active) && this.destination.complete();
    this.unsubscribe();
  };
  a.prototype.notifyNext = function(b, a, c, f, g) {
    this._next(a);
  };
  a.prototype.notifyComplete = function(b) {
    var a = this.buffer;
    this.destination.remove(b);
    this.active--;
    a && 0 < a.length && this._next(a.shift());
    this.hasCompleted && 0 === this.active && this.destination.complete();
  };
  return a;
}(OuterSubscriber$$module$output_operators), FinallyOperator$$module$output_operators = function() {
  function c(a) {
    this.callback = a;
  }
  c.prototype.call = function(a, b) {
    return b.subscribe(new FinallySubscriber$$module$output_operators(a, this.callback));
  };
  return c;
}(), FinallySubscriber$$module$output_operators = function(c) {
  function a(b, a) {
    b = c.call(this, b) || this;
    b.add(new Subscription$$module$output_operators(a));
    return b;
  }
  __extends$$module$output_operators(a, c);
  return a;
}(Subscriber$$module$output_operators), FindValueOperator$$module$output_operators = function() {
  function c(a, b, c, d) {
    this.predicate = a;
    this.source = b;
    this.yieldIndex = c;
    this.thisArg = d;
  }
  c.prototype.call = function(a, b) {
    return b.subscribe(new FindValueSubscriber$$module$output_operators(a, this.predicate, this.source, this.yieldIndex, this.thisArg));
  };
  return c;
}(), FindValueSubscriber$$module$output_operators = function(c) {
  function a(b, a, d, f, g) {
    b = c.call(this, b) || this;
    b.predicate = a;
    b.source = d;
    b.yieldIndex = f;
    b.thisArg = g;
    b.index = 0;
    return b;
  }
  __extends$$module$output_operators(a, c);
  a.prototype.notifyComplete = function(b) {
    var a = this.destination;
    a.next(b);
    a.complete();
    this.unsubscribe();
  };
  a.prototype._next = function(b) {
    var a = this.predicate, c = this.thisArg, f = this.index++;
    try {
      a.call(c || this, b, f, this.source) && this.notifyComplete(this.yieldIndex ? f : b);
    } catch (g) {
      this.destination.error(g);
    }
  };
  a.prototype._complete = function() {
    this.notifyComplete(this.yieldIndex ? -1 : void 0);
  };
  return a;
}(Subscriber$$module$output_operators);
function ObjectUnsubscribedErrorImpl$$module$output_operators() {
  Error.call(this);
  this.message = "object unsubscribed";
  this.name = "ObjectUnsubscribedError";
  return this;
}
ObjectUnsubscribedErrorImpl$$module$output_operators.prototype = Object.create(Error.prototype);
var ObjectUnsubscribedError$$module$output_operators = ObjectUnsubscribedErrorImpl$$module$output_operators, SubjectSubscription$$module$output_operators = function(c) {
  function a(b, a) {
    var e = c.call(this) || this;
    e.subject = b;
    e.subscriber = a;
    e.closed = !1;
    return e;
  }
  __extends$$module$output_operators(a, c);
  a.prototype.unsubscribe = function() {
    if (!this.closed) {
      this.closed = !0;
      var b = this.subject, a = b.observers;
      this.subject = null;
      !a || 0 === a.length || b.isStopped || b.closed || (b = a.indexOf(this.subscriber), -1 !== b && a.splice(b, 1));
    }
  };
  return a;
}(Subscription$$module$output_operators), SubjectSubscriber$$module$output_operators = function(c) {
  function a(b) {
    var a = c.call(this, b) || this;
    a.destination = b;
    return a;
  }
  __extends$$module$output_operators(a, c);
  return a;
}(Subscriber$$module$output_operators), Subject$$module$output_operators = function(c) {
  function a() {
    var b = c.call(this) || this;
    b.observers = [];
    b.closed = !1;
    b.isStopped = !1;
    b.hasError = !1;
    b.thrownError = null;
    return b;
  }
  __extends$$module$output_operators(a, c);
  a.prototype[rxSubscriber$$module$output_operators] = function() {
    return new SubjectSubscriber$$module$output_operators(this);
  };
  a.prototype.lift = function(b) {
    var a = new AnonymousSubject$$module$output_operators(this, this);
    a.operator = b;
    return a;
  };
  a.prototype.next = function(b) {
    if (this.closed) {
      throw new ObjectUnsubscribedError$$module$output_operators;
    }
    if (!this.isStopped) {
      var a = this.observers, c = a.length;
      a = a.slice();
      for (var f = 0; f < c; f++) {
        a[f].next(b);
      }
    }
  };
  a.prototype.error = function(b) {
    if (this.closed) {
      throw new ObjectUnsubscribedError$$module$output_operators;
    }
    this.hasError = !0;
    this.thrownError = b;
    this.isStopped = !0;
    var a = this.observers, c = a.length;
    a = a.slice();
    for (var f = 0; f < c; f++) {
      a[f].error(b);
    }
    this.observers.length = 0;
  };
  a.prototype.complete = function() {
    if (this.closed) {
      throw new ObjectUnsubscribedError$$module$output_operators;
    }
    this.isStopped = !0;
    var b = this.observers, a = b.length;
    b = b.slice();
    for (var c = 0; c < a; c++) {
      b[c].complete();
    }
    this.observers.length = 0;
  };
  a.prototype.unsubscribe = function() {
    this.closed = this.isStopped = !0;
    this.observers = null;
  };
  a.prototype._trySubscribe = function(b) {
    if (this.closed) {
      throw new ObjectUnsubscribedError$$module$output_operators;
    }
    return c.prototype._trySubscribe.call(this, b);
  };
  a.prototype._subscribe = function(b) {
    if (this.closed) {
      throw new ObjectUnsubscribedError$$module$output_operators;
    }
    if (this.hasError) {
      return b.error(this.thrownError), Subscription$$module$output_operators.EMPTY;
    }
    if (this.isStopped) {
      return b.complete(), Subscription$$module$output_operators.EMPTY;
    }
    this.observers.push(b);
    return new SubjectSubscription$$module$output_operators(this, b);
  };
  a.prototype.asObservable = function() {
    var b = new Observable$$module$output_operators;
    b.source = this;
    return b;
  };
  a.create = function(b, a) {
    return new AnonymousSubject$$module$output_operators(b, a);
  };
  return a;
}(Observable$$module$output_operators), AnonymousSubject$$module$output_operators = function(c) {
  function a(b, a) {
    var e = c.call(this) || this;
    e.destination = b;
    e.source = a;
    return e;
  }
  __extends$$module$output_operators(a, c);
  a.prototype.next = function(b) {
    var a = this.destination;
    a && a.next && a.next(b);
  };
  a.prototype.error = function(b) {
    var a = this.destination;
    a && a.error && this.destination.error(b);
  };
  a.prototype.complete = function() {
    var b = this.destination;
    b && b.complete && this.destination.complete();
  };
  a.prototype._subscribe = function(b) {
    return this.source ? this.source.subscribe(b) : Subscription$$module$output_operators.EMPTY;
  };
  return a;
}(Subject$$module$output_operators), GroupByOperator$$module$output_operators = function() {
  function c(a, b, c, d) {
    this.keySelector = a;
    this.elementSelector = b;
    this.durationSelector = c;
    this.subjectSelector = d;
  }
  c.prototype.call = function(a, b) {
    return b.subscribe(new GroupBySubscriber$$module$output_operators(a, this.keySelector, this.elementSelector, this.durationSelector, this.subjectSelector));
  };
  return c;
}(), GroupBySubscriber$$module$output_operators = function(c) {
  function a(b, a, d, f, g) {
    b = c.call(this, b) || this;
    b.keySelector = a;
    b.elementSelector = d;
    b.durationSelector = f;
    b.subjectSelector = g;
    b.groups = null;
    b.attemptedToUnsubscribe = !1;
    b.count = 0;
    return b;
  }
  __extends$$module$output_operators(a, c);
  a.prototype._next = function(b) {
    try {
      var a = this.keySelector(b);
    } catch (d) {
      this.error(d);
      return;
    }
    this._group(b, a);
  };
  a.prototype._group = function(b, a) {
    var c = this.groups;
    c || (c = this.groups = new Map);
    var e = c.get(a);
    if (this.elementSelector) {
      try {
        var g = this.elementSelector(b);
      } catch (h) {
        this.error(h);
      }
    } else {
      g = b;
    }
    if (!e && (e = this.subjectSelector ? this.subjectSelector() : new Subject$$module$output_operators, c.set(a, e), b = new GroupedObservable$$module$output_operators(a, e, this), this.destination.next(b), this.durationSelector)) {
      b = void 0;
      try {
        b = this.durationSelector(new GroupedObservable$$module$output_operators(a, e));
      } catch (h) {
        this.error(h);
        return;
      }
      this.add(b.subscribe(new GroupDurationSubscriber$$module$output_operators(a, e, this)));
    }
    e.closed || e.next(g);
  };
  a.prototype._error = function(b) {
    var a = this.groups;
    a && (a.forEach(function(a, c) {
      a.error(b);
    }), a.clear());
    this.destination.error(b);
  };
  a.prototype._complete = function() {
    var b = this.groups;
    b && (b.forEach(function(b, a) {
      b.complete();
    }), b.clear());
    this.destination.complete();
  };
  a.prototype.removeGroup = function(b) {
    this.groups.delete(b);
  };
  a.prototype.unsubscribe = function() {
    this.closed || (this.attemptedToUnsubscribe = !0, 0 === this.count && c.prototype.unsubscribe.call(this));
  };
  return a;
}(Subscriber$$module$output_operators), GroupDurationSubscriber$$module$output_operators = function(c) {
  function a(b, a, d) {
    var e = c.call(this, a) || this;
    e.key = b;
    e.group = a;
    e.parent = d;
    return e;
  }
  __extends$$module$output_operators(a, c);
  a.prototype._next = function(b) {
    this.complete();
  };
  a.prototype._unsubscribe = function() {
    var b = this.parent, a = this.key;
    this.key = this.parent = null;
    b && b.removeGroup(a);
  };
  return a;
}(Subscriber$$module$output_operators), GroupedObservable$$module$output_operators = function(c) {
  function a(b, a, d) {
    var e = c.call(this) || this;
    e.key = b;
    e.groupSubject = a;
    e.refCountSubscription = d;
    return e;
  }
  __extends$$module$output_operators(a, c);
  a.prototype._subscribe = function(b) {
    var a = new Subscription$$module$output_operators, c = this.refCountSubscription, f = this.groupSubject;
    c && !c.closed && a.add(new InnerRefCountSubscription$$module$output_operators(c));
    a.add(f.subscribe(b));
    return a;
  };
  return a;
}(Observable$$module$output_operators), InnerRefCountSubscription$$module$output_operators = function(c) {
  function a(b) {
    var a = c.call(this) || this;
    a.parent = b;
    b.count++;
    return a;
  }
  __extends$$module$output_operators(a, c);
  a.prototype.unsubscribe = function() {
    var b = this.parent;
    b.closed || this.closed || (c.prototype.unsubscribe.call(this), --b.count, 0 === b.count && b.attemptedToUnsubscribe && b.unsubscribe());
  };
  return a;
}(Subscription$$module$output_operators), IgnoreElementsOperator$$module$output_operators = function() {
  function c() {
  }
  c.prototype.call = function(a, b) {
    return b.subscribe(new IgnoreElementsSubscriber$$module$output_operators(a));
  };
  return c;
}(), IgnoreElementsSubscriber$$module$output_operators = function(c) {
  function a() {
    return null !== c && c.apply(this, arguments) || this;
  }
  __extends$$module$output_operators(a, c);
  a.prototype._next = function(b) {
  };
  return a;
}(Subscriber$$module$output_operators), IsEmptyOperator$$module$output_operators = function() {
  function c() {
  }
  c.prototype.call = function(a, b) {
    return b.subscribe(new IsEmptySubscriber$$module$output_operators(a));
  };
  return c;
}(), IsEmptySubscriber$$module$output_operators = function(c) {
  function a(b) {
    return c.call(this, b) || this;
  }
  __extends$$module$output_operators(a, c);
  a.prototype.notifyComplete = function(b) {
    var a = this.destination;
    a.next(b);
    a.complete();
  };
  a.prototype._next = function(b) {
    this.notifyComplete(!1);
  };
  a.prototype._complete = function() {
    this.notifyComplete(!0);
  };
  return a;
}(Subscriber$$module$output_operators), TakeLastOperator$$module$output_operators = function() {
  function c(a) {
    this.total = a;
    if (0 > this.total) {
      throw new ArgumentOutOfRangeError$$module$output_operators;
    }
  }
  c.prototype.call = function(a, b) {
    return b.subscribe(new TakeLastSubscriber$$module$output_operators(a, this.total));
  };
  return c;
}(), TakeLastSubscriber$$module$output_operators = function(c) {
  function a(b, a) {
    b = c.call(this, b) || this;
    b.total = a;
    b.ring = [];
    b.count = 0;
    return b;
  }
  __extends$$module$output_operators(a, c);
  a.prototype._next = function(b) {
    var a = this.ring, c = this.total, f = this.count++;
    a.length < c ? a.push(b) : a[f % c] = b;
  };
  a.prototype._complete = function() {
    var b = this.destination, a = this.count;
    if (0 < a) {
      for (var c = this.count >= this.total ? this.total : this.count, f = this.ring, g = 0; g < c; g++) {
        var h = a++ % c;
        b.next(f[h]);
      }
    }
    b.complete();
  };
  return a;
}(Subscriber$$module$output_operators), MapToOperator$$module$output_operators = function() {
  function c(a) {
    this.value = a;
  }
  c.prototype.call = function(a, b) {
    return b.subscribe(new MapToSubscriber$$module$output_operators(a, this.value));
  };
  return c;
}(), MapToSubscriber$$module$output_operators = function(c) {
  function a(b, a) {
    b = c.call(this, b) || this;
    b.value = a;
    return b;
  }
  __extends$$module$output_operators(a, c);
  a.prototype._next = function(b) {
    this.destination.next(this.value);
  };
  return a;
}(Subscriber$$module$output_operators), MaterializeOperator$$module$output_operators = function() {
  function c() {
  }
  c.prototype.call = function(a, b) {
    return b.subscribe(new MaterializeSubscriber$$module$output_operators(a));
  };
  return c;
}(), MaterializeSubscriber$$module$output_operators = function(c) {
  function a(b) {
    return c.call(this, b) || this;
  }
  __extends$$module$output_operators(a, c);
  a.prototype._next = function(b) {
    this.destination.next(Notification$$module$output_operators.createNext(b));
  };
  a.prototype._error = function(b) {
    var a = this.destination;
    a.next(Notification$$module$output_operators.createError(b));
    a.complete();
  };
  a.prototype._complete = function() {
    var b = this.destination;
    b.next(Notification$$module$output_operators.createComplete());
    b.complete();
  };
  return a;
}(Subscriber$$module$output_operators), ScanOperator$$module$output_operators = function() {
  function c(a, b, c) {
    void 0 === c && (c = !1);
    this.accumulator = a;
    this.seed = b;
    this.hasSeed = c;
  }
  c.prototype.call = function(a, b) {
    return b.subscribe(new ScanSubscriber$$module$output_operators(a, this.accumulator, this.seed, this.hasSeed));
  };
  return c;
}(), ScanSubscriber$$module$output_operators = function(c) {
  function a(b, a, d, f) {
    b = c.call(this, b) || this;
    b.accumulator = a;
    b._seed = d;
    b.hasSeed = f;
    b.index = 0;
    return b;
  }
  __extends$$module$output_operators(a, c);
  Object.defineProperty(a.prototype, "seed", {get:function() {
    return this._seed;
  }, set:function(b) {
    this.hasSeed = !0;
    this._seed = b;
  }, enumerable:!0, configurable:!0});
  a.prototype._next = function(b) {
    if (this.hasSeed) {
      return this._tryNext(b);
    }
    this.seed = b;
    this.destination.next(b);
  };
  a.prototype._tryNext = function(b) {
    var a = this.index++;
    try {
      var c = this.accumulator(this.seed, b, a);
    } catch (f) {
      this.destination.error(f);
    }
    this.seed = c;
    this.destination.next(c);
  };
  return a;
}(Subscriber$$module$output_operators);
function merge$1$$module$output_operators() {
  for (var c = [], a = 0; a < arguments.length; a++) {
    c[a] = arguments[a];
  }
  a = Number.POSITIVE_INFINITY;
  var b = null, e = c[c.length - 1];
  isScheduler$$module$output_operators(e) ? (b = c.pop(), 1 < c.length && "number" === typeof c[c.length - 1] && (a = c.pop())) : "number" === typeof e && (a = c.pop());
  return null === b && 1 === c.length && c[0] instanceof Observable$$module$output_operators ? c[0] : (0,module$output_operators.mergeAll)(a)(fromArray$$module$output_operators(c, b));
}
var MergeScanOperator$$module$output_operators = function() {
  function c(a, b, c) {
    this.accumulator = a;
    this.seed = b;
    this.concurrent = c;
  }
  c.prototype.call = function(a, b) {
    return b.subscribe(new MergeScanSubscriber$$module$output_operators(a, this.accumulator, this.seed, this.concurrent));
  };
  return c;
}(), MergeScanSubscriber$$module$output_operators = function(c) {
  function a(b, a, d, f) {
    b = c.call(this, b) || this;
    b.accumulator = a;
    b.acc = d;
    b.concurrent = f;
    b.hasValue = !1;
    b.hasCompleted = !1;
    b.buffer = [];
    b.active = 0;
    b.index = 0;
    return b;
  }
  __extends$$module$output_operators(a, c);
  a.prototype._next = function(b) {
    if (this.active < this.concurrent) {
      var a = this.index++, c = tryCatch$$module$output_operators(this.accumulator)(this.acc, b), f = this.destination;
      c === errorObject$$module$output_operators ? f.error(errorObject$$module$output_operators.e) : (this.active++, this._innerSub(c, b, a));
    } else {
      this.buffer.push(b);
    }
  };
  a.prototype._innerSub = function(b, a, c) {
    var e = new InnerSubscriber$$module$output_operators(this, void 0, void 0);
    this.destination.add(e);
    subscribeToResult$$module$output_operators(this, b, a, c, e);
  };
  a.prototype._complete = function() {
    this.hasCompleted = !0;
    0 === this.active && 0 === this.buffer.length && (!1 === this.hasValue && this.destination.next(this.acc), this.destination.complete());
    this.unsubscribe();
  };
  a.prototype.notifyNext = function(b, a, c, f, g) {
    b = this.destination;
    this.acc = a;
    this.hasValue = !0;
    b.next(a);
  };
  a.prototype.notifyComplete = function(b) {
    var a = this.buffer;
    this.destination.remove(b);
    this.active--;
    0 < a.length ? this._next(a.shift()) : 0 === this.active && this.hasCompleted && (!1 === this.hasValue && this.destination.next(this.acc), this.destination.complete());
  };
  return a;
}(OuterSubscriber$$module$output_operators), RefCountOperator$1$$module$output_operators = function() {
  function c(a) {
    this.connectable = a;
  }
  c.prototype.call = function(a, b) {
    var c = this.connectable;
    c._refCount++;
    a = new RefCountSubscriber$1$$module$output_operators(a, c);
    b = b.subscribe(a);
    a.closed || (a.connection = c.connect());
    return b;
  };
  return c;
}(), RefCountSubscriber$1$$module$output_operators = function(c) {
  function a(b, a) {
    b = c.call(this, b) || this;
    b.connectable = a;
    return b;
  }
  __extends$$module$output_operators(a, c);
  a.prototype._unsubscribe = function() {
    var b = this.connectable;
    if (b) {
      this.connectable = null;
      var a = b._refCount;
      0 >= a ? this.connection = null : (b._refCount = a - 1, 1 < a ? this.connection = null : (a = this.connection, b = b._connection, this.connection = null, !b || a && b !== a || b.unsubscribe()));
    } else {
      this.connection = null;
    }
  };
  return a;
}(Subscriber$$module$output_operators), ConnectableObservable$$module$output_operators = function(c) {
  function a(b, a) {
    var e = c.call(this) || this;
    e.source = b;
    e.subjectFactory = a;
    e._refCount = 0;
    e._isComplete = !1;
    return e;
  }
  __extends$$module$output_operators(a, c);
  a.prototype._subscribe = function(b) {
    return this.getSubject().subscribe(b);
  };
  a.prototype.getSubject = function() {
    var b = this._subject;
    if (!b || b.isStopped) {
      this._subject = this.subjectFactory();
    }
    return this._subject;
  };
  a.prototype.connect = function() {
    var b = this._connection;
    b || (this._isComplete = !1, b = this._connection = new Subscription$$module$output_operators, b.add(this.source.subscribe(new ConnectableSubscriber$$module$output_operators(this.getSubject(), this))), b.closed ? (this._connection = null, b = Subscription$$module$output_operators.EMPTY) : this._connection = b);
    return b;
  };
  a.prototype.refCount = function() {
    return (0,module$output_operators.refCount)()(this);
  };
  return a;
}(Observable$$module$output_operators), connectableProto$$module$output_operators = ConnectableObservable$$module$output_operators.prototype, connectableObservableDescriptor$$module$output_operators = {operator:{value:null}, _refCount:{value:0, writable:!0}, _subject:{value:null, writable:!0}, _connection:{value:null, writable:!0}, _subscribe:{value:connectableProto$$module$output_operators._subscribe}, _isComplete:{value:connectableProto$$module$output_operators._isComplete, writable:!0}, getSubject:{value:connectableProto$$module$output_operators.getSubject},
connect:{value:connectableProto$$module$output_operators.connect}, refCount:{value:connectableProto$$module$output_operators.refCount}}, ConnectableSubscriber$$module$output_operators = function(c) {
  function a(b, a) {
    b = c.call(this, b) || this;
    b.connectable = a;
    return b;
  }
  __extends$$module$output_operators(a, c);
  a.prototype._error = function(b) {
    this._unsubscribe();
    c.prototype._error.call(this, b);
  };
  a.prototype._complete = function() {
    this.connectable._isComplete = !0;
    this._unsubscribe();
    c.prototype._complete.call(this);
  };
  a.prototype._unsubscribe = function() {
    var b = this.connectable;
    if (b) {
      this.connectable = null;
      var a = b._connection;
      b._refCount = 0;
      b._subject = null;
      b._connection = null;
      a && a.unsubscribe();
    }
  };
  return a;
}(SubjectSubscriber$$module$output_operators), RefCountSubscriber$$module$output_operators = function(c) {
  function a(b, a) {
    b = c.call(this, b) || this;
    b.connectable = a;
    return b;
  }
  __extends$$module$output_operators(a, c);
  a.prototype._unsubscribe = function() {
    var b = this.connectable;
    if (b) {
      this.connectable = null;
      var a = b._refCount;
      0 >= a ? this.connection = null : (b._refCount = a - 1, 1 < a ? this.connection = null : (a = this.connection, b = b._connection, this.connection = null, !b || a && b !== a || b.unsubscribe()));
    } else {
      this.connection = null;
    }
  };
  return a;
}(Subscriber$$module$output_operators), MulticastOperator$$module$output_operators = function() {
  function c(a, b) {
    this.subjectFactory = a;
    this.selector = b;
  }
  c.prototype.call = function(a, b) {
    var c = this.selector, d = this.subjectFactory();
    a = c(d).subscribe(a);
    a.add(b.subscribe(d));
    return a;
  };
  return c;
}(), ObserveOnOperator$$module$output_operators = function() {
  function c(a, b) {
    void 0 === b && (b = 0);
    this.scheduler = a;
    this.delay = b;
  }
  c.prototype.call = function(a, b) {
    return b.subscribe(new ObserveOnSubscriber$$module$output_operators(a, this.scheduler, this.delay));
  };
  return c;
}(), ObserveOnSubscriber$$module$output_operators = function(c) {
  function a(b, a, d) {
    void 0 === d && (d = 0);
    b = c.call(this, b) || this;
    b.scheduler = a;
    b.delay = d;
    return b;
  }
  __extends$$module$output_operators(a, c);
  a.dispatch = function(b) {
    b.notification.observe(b.destination);
    this.unsubscribe();
  };
  a.prototype.scheduleMessage = function(b) {
    this.destination.add(this.scheduler.schedule(a.dispatch, this.delay, new ObserveOnMessage$$module$output_operators(b, this.destination)));
  };
  a.prototype._next = function(b) {
    this.scheduleMessage(Notification$$module$output_operators.createNext(b));
  };
  a.prototype._error = function(b) {
    this.scheduleMessage(Notification$$module$output_operators.createError(b));
    this.unsubscribe();
  };
  a.prototype._complete = function() {
    this.scheduleMessage(Notification$$module$output_operators.createComplete());
    this.unsubscribe();
  };
  return a;
}(Subscriber$$module$output_operators), ObserveOnMessage$$module$output_operators = function() {
  return function(c, a) {
    this.notification = c;
    this.destination = a;
  };
}(), OnErrorResumeNextOperator$$module$output_operators = function() {
  function c(a) {
    this.nextSources = a;
  }
  c.prototype.call = function(a, b) {
    return b.subscribe(new OnErrorResumeNextSubscriber$$module$output_operators(a, this.nextSources));
  };
  return c;
}(), OnErrorResumeNextSubscriber$$module$output_operators = function(c) {
  function a(b, a) {
    var e = c.call(this, b) || this;
    e.destination = b;
    e.nextSources = a;
    return e;
  }
  __extends$$module$output_operators(a, c);
  a.prototype.notifyError = function(b, a) {
    this.subscribeToNextSource();
  };
  a.prototype.notifyComplete = function(b) {
    this.subscribeToNextSource();
  };
  a.prototype._error = function(b) {
    this.subscribeToNextSource();
    this.unsubscribe();
  };
  a.prototype._complete = function() {
    this.subscribeToNextSource();
    this.unsubscribe();
  };
  a.prototype.subscribeToNextSource = function() {
    var b = this.nextSources.shift();
    if (b) {
      var a = new InnerSubscriber$$module$output_operators(this, void 0, void 0);
      this.destination.add(a);
      subscribeToResult$$module$output_operators(this, b, void 0, void 0, a);
    } else {
      this.destination.complete();
    }
  };
  return a;
}(OuterSubscriber$$module$output_operators), PairwiseOperator$$module$output_operators = function() {
  function c() {
  }
  c.prototype.call = function(a, b) {
    return b.subscribe(new PairwiseSubscriber$$module$output_operators(a));
  };
  return c;
}(), PairwiseSubscriber$$module$output_operators = function(c) {
  function a(b) {
    b = c.call(this, b) || this;
    b.hasPrev = !1;
    return b;
  }
  __extends$$module$output_operators(a, c);
  a.prototype._next = function(b) {
    this.hasPrev ? this.destination.next([this.prev, b]) : this.hasPrev = !0;
    this.prev = b;
  };
  return a;
}(Subscriber$$module$output_operators);
function not$$module$output_operators(c, a) {
  function b() {
    return !b.pred.apply(b.thisArg, arguments);
  }
  b.pred = c;
  b.thisArg = a;
  return b;
}
function plucker$$module$output_operators(c, a) {
  return function(b) {
    var e = b;
    for (b = 0; b < a; b++) {
      if (e = e[c[b]], "undefined" === typeof e) {
        return;
      }
    }
    return e;
  };
}
var BehaviorSubject$$module$output_operators = function(c) {
  function a(b) {
    var a = c.call(this) || this;
    a._value = b;
    return a;
  }
  __extends$$module$output_operators(a, c);
  Object.defineProperty(a.prototype, "value", {get:function() {
    return this.getValue();
  }, enumerable:!0, configurable:!0});
  a.prototype._subscribe = function(b) {
    var a = c.prototype._subscribe.call(this, b);
    a && !a.closed && b.next(this._value);
    return a;
  };
  a.prototype.getValue = function() {
    if (this.hasError) {
      throw this.thrownError;
    }
    if (this.closed) {
      throw new ObjectUnsubscribedError$$module$output_operators;
    }
    return this._value;
  };
  a.prototype.next = function(b) {
    c.prototype.next.call(this, this._value = b);
  };
  return a;
}(Subject$$module$output_operators), AsyncSubject$$module$output_operators = function(c) {
  function a() {
    var b = null !== c && c.apply(this, arguments) || this;
    b.value = null;
    b.hasNext = !1;
    b.hasCompleted = !1;
    return b;
  }
  __extends$$module$output_operators(a, c);
  a.prototype._subscribe = function(b) {
    return this.hasError ? (b.error(this.thrownError), Subscription$$module$output_operators.EMPTY) : this.hasCompleted && this.hasNext ? (b.next(this.value), b.complete(), Subscription$$module$output_operators.EMPTY) : c.prototype._subscribe.call(this, b);
  };
  a.prototype.next = function(b) {
    this.hasCompleted || (this.value = b, this.hasNext = !0);
  };
  a.prototype.error = function(b) {
    this.hasCompleted || c.prototype.error.call(this, b);
  };
  a.prototype.complete = function() {
    this.hasCompleted = !0;
    this.hasNext && c.prototype.next.call(this, this.value);
    c.prototype.complete.call(this);
  };
  return a;
}(Subject$$module$output_operators), QueueAction$$module$output_operators = function(c) {
  function a(b, a) {
    var e = c.call(this, b, a) || this;
    e.scheduler = b;
    e.work = a;
    return e;
  }
  __extends$$module$output_operators(a, c);
  a.prototype.schedule = function(b, a) {
    void 0 === a && (a = 0);
    if (0 < a) {
      return c.prototype.schedule.call(this, b, a);
    }
    this.delay = a;
    this.state = b;
    this.scheduler.flush(this);
    return this;
  };
  a.prototype.execute = function(b, a) {
    return 0 < a || this.closed ? c.prototype.execute.call(this, b, a) : this._execute(b, a);
  };
  a.prototype.requestAsyncId = function(b, a, d) {
    void 0 === d && (d = 0);
    return null !== d && 0 < d || null === d && 0 < this.delay ? c.prototype.requestAsyncId.call(this, b, a, d) : b.flush(this);
  };
  return a;
}(AsyncAction$$module$output_operators), QueueScheduler$$module$output_operators = function(c) {
  function a() {
    return null !== c && c.apply(this, arguments) || this;
  }
  __extends$$module$output_operators(a, c);
  return a;
}(AsyncScheduler$$module$output_operators), queue$$module$output_operators = new QueueScheduler$$module$output_operators(QueueAction$$module$output_operators), ReplaySubject$$module$output_operators = function(c) {
  function a(b, a, d) {
    void 0 === b && (b = Number.POSITIVE_INFINITY);
    void 0 === a && (a = Number.POSITIVE_INFINITY);
    var e = c.call(this) || this;
    e.scheduler = d;
    e._events = [];
    e._infiniteTimeWindow = !1;
    e._bufferSize = 1 > b ? 1 : b;
    e._windowTime = 1 > a ? 1 : a;
    a === Number.POSITIVE_INFINITY ? (e._infiniteTimeWindow = !0, e.next = e.nextInfiniteTimeWindow) : e.next = e.nextTimeWindow;
    return e;
  }
  __extends$$module$output_operators(a, c);
  a.prototype.nextInfiniteTimeWindow = function(b) {
    var a = this._events;
    a.push(b);
    a.length > this._bufferSize && a.shift();
    c.prototype.next.call(this, b);
  };
  a.prototype.nextTimeWindow = function(b) {
    this._events.push(new ReplayEvent$$module$output_operators(this._getNow(), b));
    this._trimBufferThenGetEvents();
    c.prototype.next.call(this, b);
  };
  a.prototype._subscribe = function(b) {
    var a = this._infiniteTimeWindow, c = a ? this._events : this._trimBufferThenGetEvents(), f = this.scheduler, g = c.length;
    if (this.closed) {
      throw new ObjectUnsubscribedError$$module$output_operators;
    }
    if (this.isStopped || this.hasError) {
      var h = Subscription$$module$output_operators.EMPTY;
    } else {
      this.observers.push(b), h = new SubjectSubscription$$module$output_operators(this, b);
    }
    f && b.add(b = new ObserveOnSubscriber$$module$output_operators(b, f));
    if (a) {
      for (a = 0; a < g && !b.closed; a++) {
        b.next(c[a]);
      }
    } else {
      for (a = 0; a < g && !b.closed; a++) {
        b.next(c[a].value);
      }
    }
    this.hasError ? b.error(this.thrownError) : this.isStopped && b.complete();
    return h;
  };
  a.prototype._getNow = function() {
    return (this.scheduler || queue$$module$output_operators).now();
  };
  a.prototype._trimBufferThenGetEvents = function() {
    for (var b = this._getNow(), a = this._bufferSize, c = this._windowTime, f = this._events, g = f.length, h = 0; h < g && !(b - f[h].time < c);) {
      h++;
    }
    g > a && (h = Math.max(h, g - a));
    0 < h && f.splice(0, h);
    return f;
  };
  return a;
}(Subject$$module$output_operators), ReplayEvent$$module$output_operators = function() {
  return function(c, a) {
    this.time = c;
    this.value = a;
  };
}();
function race$1$$module$output_operators() {
  for (var c = [], a = 0; a < arguments.length; a++) {
    c[a] = arguments[a];
  }
  if (1 === c.length) {
    if (isArray$$module$output_operators(c[0])) {
      c = c[0];
    } else {
      return c[0];
    }
  }
  return fromArray$$module$output_operators(c, void 0).lift(new RaceOperator$$module$output_operators);
}
var RaceOperator$$module$output_operators = function() {
  function c() {
  }
  c.prototype.call = function(a, b) {
    return b.subscribe(new RaceSubscriber$$module$output_operators(a));
  };
  return c;
}(), RaceSubscriber$$module$output_operators = function(c) {
  function a(b) {
    b = c.call(this, b) || this;
    b.hasFirst = !1;
    b.observables = [];
    b.subscriptions = [];
    return b;
  }
  __extends$$module$output_operators(a, c);
  a.prototype._next = function(b) {
    this.observables.push(b);
  };
  a.prototype._complete = function() {
    var b = this.observables, a = b.length;
    if (0 === a) {
      this.destination.complete();
    } else {
      for (var c = 0; c < a && !this.hasFirst; c++) {
        var f = b[c];
        f = subscribeToResult$$module$output_operators(this, f, f, c);
        this.subscriptions && this.subscriptions.push(f);
        this.add(f);
      }
      this.observables = null;
    }
  };
  a.prototype.notifyNext = function(b, a, c, f, g) {
    if (!this.hasFirst) {
      this.hasFirst = !0;
      for (b = 0; b < this.subscriptions.length; b++) {
        b !== c && (f = this.subscriptions[b], f.unsubscribe(), this.remove(f));
      }
      this.subscriptions = null;
    }
    this.destination.next(a);
  };
  return a;
}(OuterSubscriber$$module$output_operators), RepeatOperator$$module$output_operators = function() {
  function c(a, b) {
    this.count = a;
    this.source = b;
  }
  c.prototype.call = function(a, b) {
    return b.subscribe(new RepeatSubscriber$$module$output_operators(a, this.count, this.source));
  };
  return c;
}(), RepeatSubscriber$$module$output_operators = function(c) {
  function a(b, a, d) {
    b = c.call(this, b) || this;
    b.count = a;
    b.source = d;
    return b;
  }
  __extends$$module$output_operators(a, c);
  a.prototype.complete = function() {
    if (!this.isStopped) {
      var b = this.source, a = this.count;
      if (0 === a) {
        return c.prototype.complete.call(this);
      }
      -1 < a && (this.count = a - 1);
      b.subscribe(this._unsubscribeAndRecycle());
    }
  };
  return a;
}(Subscriber$$module$output_operators), RepeatWhenOperator$$module$output_operators = function() {
  function c(a) {
    this.notifier = a;
  }
  c.prototype.call = function(a, b) {
    return b.subscribe(new RepeatWhenSubscriber$$module$output_operators(a, this.notifier, b));
  };
  return c;
}(), RepeatWhenSubscriber$$module$output_operators = function(c) {
  function a(b, a, d) {
    b = c.call(this, b) || this;
    b.notifier = a;
    b.source = d;
    b.sourceIsBeingSubscribedTo = !0;
    return b;
  }
  __extends$$module$output_operators(a, c);
  a.prototype.notifyNext = function(b, a, c, f, g) {
    this.sourceIsBeingSubscribedTo = !0;
    this.source.subscribe(this);
  };
  a.prototype.notifyComplete = function(b) {
    if (!1 === this.sourceIsBeingSubscribedTo) {
      return c.prototype.complete.call(this);
    }
  };
  a.prototype.complete = function() {
    this.sourceIsBeingSubscribedTo = !1;
    if (!this.isStopped) {
      this.retries || this.subscribeToRetries();
      if (!this.retriesSubscription || this.retriesSubscription.closed) {
        return c.prototype.complete.call(this);
      }
      this._unsubscribeAndRecycle();
      this.notifications.next();
    }
  };
  a.prototype._unsubscribe = function() {
    var b = this.notifications, a = this.retriesSubscription;
    b && (b.unsubscribe(), this.notifications = null);
    a && (a.unsubscribe(), this.retriesSubscription = null);
    this.retries = null;
  };
  a.prototype._unsubscribeAndRecycle = function() {
    var b = this._unsubscribe;
    this._unsubscribe = null;
    c.prototype._unsubscribeAndRecycle.call(this);
    this._unsubscribe = b;
    return this;
  };
  a.prototype.subscribeToRetries = function() {
    this.notifications = new Subject$$module$output_operators;
    var b = tryCatch$$module$output_operators(this.notifier)(this.notifications);
    if (b === errorObject$$module$output_operators) {
      return c.prototype.complete.call(this);
    }
    this.retries = b;
    this.retriesSubscription = subscribeToResult$$module$output_operators(this, b);
  };
  return a;
}(OuterSubscriber$$module$output_operators), RetryOperator$$module$output_operators = function() {
  function c(a, b) {
    this.count = a;
    this.source = b;
  }
  c.prototype.call = function(a, b) {
    return b.subscribe(new RetrySubscriber$$module$output_operators(a, this.count, this.source));
  };
  return c;
}(), RetrySubscriber$$module$output_operators = function(c) {
  function a(b, a, d) {
    b = c.call(this, b) || this;
    b.count = a;
    b.source = d;
    return b;
  }
  __extends$$module$output_operators(a, c);
  a.prototype.error = function(b) {
    if (!this.isStopped) {
      var a = this.source, d = this.count;
      if (0 === d) {
        return c.prototype.error.call(this, b);
      }
      -1 < d && (this.count = d - 1);
      a.subscribe(this._unsubscribeAndRecycle());
    }
  };
  return a;
}(Subscriber$$module$output_operators), RetryWhenOperator$$module$output_operators = function() {
  function c(a, b) {
    this.notifier = a;
    this.source = b;
  }
  c.prototype.call = function(a, b) {
    return b.subscribe(new RetryWhenSubscriber$$module$output_operators(a, this.notifier, this.source));
  };
  return c;
}(), RetryWhenSubscriber$$module$output_operators = function(c) {
  function a(b, a, d) {
    b = c.call(this, b) || this;
    b.notifier = a;
    b.source = d;
    return b;
  }
  __extends$$module$output_operators(a, c);
  a.prototype.error = function(b) {
    if (!this.isStopped) {
      var a = this.errors, d = this.retries, f = this.retriesSubscription;
      if (d) {
        this.retriesSubscription = this.errors = null;
      } else {
        a = new Subject$$module$output_operators;
        d = tryCatch$$module$output_operators(this.notifier)(a);
        if (d === errorObject$$module$output_operators) {
          return c.prototype.error.call(this, errorObject$$module$output_operators.e);
        }
        f = subscribeToResult$$module$output_operators(this, d);
      }
      this._unsubscribeAndRecycle();
      this.errors = a;
      this.retries = d;
      this.retriesSubscription = f;
      a.next(b);
    }
  };
  a.prototype._unsubscribe = function() {
    var b = this.errors, a = this.retriesSubscription;
    b && (b.unsubscribe(), this.errors = null);
    a && (a.unsubscribe(), this.retriesSubscription = null);
    this.retries = null;
  };
  a.prototype.notifyNext = function(a, c, d, f, g) {
    a = this._unsubscribe;
    this._unsubscribe = null;
    this._unsubscribeAndRecycle();
    this._unsubscribe = a;
    this.source.subscribe(this);
  };
  return a;
}(OuterSubscriber$$module$output_operators), SampleOperator$$module$output_operators = function() {
  function c(a) {
    this.notifier = a;
  }
  c.prototype.call = function(a, b) {
    a = new SampleSubscriber$$module$output_operators(a);
    b = b.subscribe(a);
    b.add(subscribeToResult$$module$output_operators(a, this.notifier));
    return b;
  };
  return c;
}(), SampleSubscriber$$module$output_operators = function(c) {
  function a() {
    var a = null !== c && c.apply(this, arguments) || this;
    a.hasValue = !1;
    return a;
  }
  __extends$$module$output_operators(a, c);
  a.prototype._next = function(a) {
    this.value = a;
    this.hasValue = !0;
  };
  a.prototype.notifyNext = function(a, c, d, f, g) {
    this.emitValue();
  };
  a.prototype.notifyComplete = function() {
    this.emitValue();
  };
  a.prototype.emitValue = function() {
    this.hasValue && (this.hasValue = !1, this.destination.next(this.value));
  };
  return a;
}(OuterSubscriber$$module$output_operators), SampleTimeOperator$$module$output_operators = function() {
  function c(a, b) {
    this.period = a;
    this.scheduler = b;
  }
  c.prototype.call = function(a, b) {
    return b.subscribe(new SampleTimeSubscriber$$module$output_operators(a, this.period, this.scheduler));
  };
  return c;
}(), SampleTimeSubscriber$$module$output_operators = function(c) {
  function a(a, e, d) {
    a = c.call(this, a) || this;
    a.period = e;
    a.scheduler = d;
    a.hasValue = !1;
    a.add(d.schedule(dispatchNotification$$module$output_operators, e, {subscriber:a, period:e}));
    return a;
  }
  __extends$$module$output_operators(a, c);
  a.prototype._next = function(a) {
    this.lastValue = a;
    this.hasValue = !0;
  };
  a.prototype.notifyNext = function() {
    this.hasValue && (this.hasValue = !1, this.destination.next(this.lastValue));
  };
  return a;
}(Subscriber$$module$output_operators);
function dispatchNotification$$module$output_operators(c) {
  var a = c.period;
  c.subscriber.notifyNext();
  this.schedule(c, a);
}
var SequenceEqualOperator$$module$output_operators = function() {
  function c(a, b) {
    this.compareTo = a;
    this.comparor = b;
  }
  c.prototype.call = function(a, b) {
    return b.subscribe(new SequenceEqualSubscriber$$module$output_operators(a, this.compareTo, this.comparor));
  };
  return c;
}(), SequenceEqualSubscriber$$module$output_operators = function(c) {
  function a(a, e, d) {
    var b = c.call(this, a) || this;
    b.compareTo = e;
    b.comparor = d;
    b._a = [];
    b._b = [];
    b._oneComplete = !1;
    b.destination.add(e.subscribe(new SequenceEqualCompareToSubscriber$$module$output_operators(a, b)));
    return b;
  }
  __extends$$module$output_operators(a, c);
  a.prototype._next = function(a) {
    this._oneComplete && 0 === this._b.length ? this.emit(!1) : (this._a.push(a), this.checkValues());
  };
  a.prototype._complete = function() {
    this._oneComplete ? this.emit(0 === this._a.length && 0 === this._b.length) : this._oneComplete = !0;
    this.unsubscribe();
  };
  a.prototype.checkValues = function() {
    for (var a = this._a, c = this._b, d = this.comparor; 0 < a.length && 0 < c.length;) {
      var f = a.shift(), g = c.shift();
      d ? (f = tryCatch$$module$output_operators(d)(f, g), f === errorObject$$module$output_operators && this.destination.error(errorObject$$module$output_operators.e)) : f = f === g;
      f || this.emit(!1);
    }
  };
  a.prototype.emit = function(a) {
    var b = this.destination;
    b.next(a);
    b.complete();
  };
  a.prototype.nextB = function(a) {
    this._oneComplete && 0 === this._a.length ? this.emit(!1) : (this._b.push(a), this.checkValues());
  };
  a.prototype.completeB = function() {
    this._oneComplete ? this.emit(0 === this._a.length && 0 === this._b.length) : this._oneComplete = !0;
  };
  return a;
}(Subscriber$$module$output_operators), SequenceEqualCompareToSubscriber$$module$output_operators = function(c) {
  function a(a, e) {
    a = c.call(this, a) || this;
    a.parent = e;
    return a;
  }
  __extends$$module$output_operators(a, c);
  a.prototype._next = function(a) {
    this.parent.nextB(a);
  };
  a.prototype._error = function(a) {
    this.parent.error(a);
    this.unsubscribe();
  };
  a.prototype._complete = function() {
    this.parent.completeB();
    this.unsubscribe();
  };
  return a;
}(Subscriber$$module$output_operators);
function shareSubjectFactory$$module$output_operators() {
  return new Subject$$module$output_operators;
}
function shareReplayOperator$$module$output_operators(c, a, b) {
  var e, d = 0, f, g = !1, h = !1;
  return function(k) {
    d++;
    if (!e || g) {
      g = !1, e = new ReplaySubject$$module$output_operators(c, a, b), f = k.subscribe({next:function(a) {
        e.next(a);
      }, error:function(a) {
        g = !0;
        e.error(a);
      }, complete:function() {
        h = !0;
        e.complete();
      }});
    }
    var l = e.subscribe(this);
    return function() {
      d--;
      l.unsubscribe();
      f && 0 === d && h && f.unsubscribe();
    };
  };
}
var SingleOperator$$module$output_operators = function() {
  function c(a, b) {
    this.predicate = a;
    this.source = b;
  }
  c.prototype.call = function(a, b) {
    return b.subscribe(new SingleSubscriber$$module$output_operators(a, this.predicate, this.source));
  };
  return c;
}(), SingleSubscriber$$module$output_operators = function(c) {
  function a(a, e, d) {
    a = c.call(this, a) || this;
    a.predicate = e;
    a.source = d;
    a.seenValue = !1;
    a.index = 0;
    return a;
  }
  __extends$$module$output_operators(a, c);
  a.prototype.applySingleValue = function(a) {
    this.seenValue ? this.destination.error("Sequence contains more than one element") : (this.seenValue = !0, this.singleValue = a);
  };
  a.prototype._next = function(a) {
    var b = this.index++;
    this.predicate ? this.tryNext(a, b) : this.applySingleValue(a);
  };
  a.prototype.tryNext = function(a, c) {
    try {
      this.predicate(a, c, this.source) && this.applySingleValue(a);
    } catch (d) {
      this.destination.error(d);
    }
  };
  a.prototype._complete = function() {
    var a = this.destination;
    0 < this.index ? (a.next(this.seenValue ? this.singleValue : void 0), a.complete()) : a.error(new EmptyError$$module$output_operators);
  };
  return a;
}(Subscriber$$module$output_operators), SkipOperator$$module$output_operators = function() {
  function c(a) {
    this.total = a;
  }
  c.prototype.call = function(a, b) {
    return b.subscribe(new SkipSubscriber$$module$output_operators(a, this.total));
  };
  return c;
}(), SkipSubscriber$$module$output_operators = function(c) {
  function a(a, e) {
    a = c.call(this, a) || this;
    a.total = e;
    a.count = 0;
    return a;
  }
  __extends$$module$output_operators(a, c);
  a.prototype._next = function(a) {
    ++this.count > this.total && this.destination.next(a);
  };
  return a;
}(Subscriber$$module$output_operators), SkipLastOperator$$module$output_operators = function() {
  function c(a) {
    this._skipCount = a;
    if (0 > this._skipCount) {
      throw new ArgumentOutOfRangeError$$module$output_operators;
    }
  }
  c.prototype.call = function(a, b) {
    return 0 === this._skipCount ? b.subscribe(new Subscriber$$module$output_operators(a)) : b.subscribe(new SkipLastSubscriber$$module$output_operators(a, this._skipCount));
  };
  return c;
}(), SkipLastSubscriber$$module$output_operators = function(c) {
  function a(a, e) {
    a = c.call(this, a) || this;
    a._skipCount = e;
    a._count = 0;
    a._ring = Array(e);
    return a;
  }
  __extends$$module$output_operators(a, c);
  a.prototype._next = function(a) {
    var b = this._skipCount, c = this._count++;
    if (c < b) {
      this._ring[c] = a;
    } else {
      b = c % b;
      c = this._ring;
      var f = c[b];
      c[b] = a;
      this.destination.next(f);
    }
  };
  return a;
}(Subscriber$$module$output_operators), SkipUntilOperator$$module$output_operators = function() {
  function c(a) {
    this.notifier = a;
  }
  c.prototype.call = function(a, b) {
    return b.subscribe(new SkipUntilSubscriber$$module$output_operators(a, this.notifier));
  };
  return c;
}(), SkipUntilSubscriber$$module$output_operators = function(c) {
  function a(a, e) {
    a = c.call(this, a) || this;
    a.hasValue = !1;
    var b = new InnerSubscriber$$module$output_operators(a, void 0, void 0);
    a.add(b);
    a.innerSubscription = b;
    subscribeToResult$$module$output_operators(a, e, void 0, void 0, b);
    return a;
  }
  __extends$$module$output_operators(a, c);
  a.prototype._next = function(a) {
    this.hasValue && c.prototype._next.call(this, a);
  };
  a.prototype.notifyNext = function(a, c, d, f, g) {
    this.hasValue = !0;
    this.innerSubscription && this.innerSubscription.unsubscribe();
  };
  a.prototype.notifyComplete = function() {
  };
  return a;
}(OuterSubscriber$$module$output_operators), SkipWhileOperator$$module$output_operators = function() {
  function c(a) {
    this.predicate = a;
  }
  c.prototype.call = function(a, b) {
    return b.subscribe(new SkipWhileSubscriber$$module$output_operators(a, this.predicate));
  };
  return c;
}(), SkipWhileSubscriber$$module$output_operators = function(c) {
  function a(a, e) {
    a = c.call(this, a) || this;
    a.predicate = e;
    a.skipping = !0;
    a.index = 0;
    return a;
  }
  __extends$$module$output_operators(a, c);
  a.prototype._next = function(a) {
    var b = this.destination;
    this.skipping && this.tryCallPredicate(a);
    this.skipping || b.next(a);
  };
  a.prototype.tryCallPredicate = function(a) {
    try {
      this.skipping = !!this.predicate(a, this.index++);
    } catch (e) {
      this.destination.error(e);
    }
  };
  return a;
}(Subscriber$$module$output_operators), nextHandle$$module$output_operators = 1, tasksByHandle$$module$output_operators = {};
function runIfPresent$$module$output_operators(c) {
  (c = tasksByHandle$$module$output_operators[c]) && c();
}
var Immediate$$module$output_operators = {setImmediate:function(c) {
  var a = nextHandle$$module$output_operators++;
  tasksByHandle$$module$output_operators[a] = c;
  Promise.resolve().then(function() {
    return runIfPresent$$module$output_operators(a);
  });
  return a;
}, clearImmediate:function(c) {
  delete tasksByHandle$$module$output_operators[c];
}}, AsapAction$$module$output_operators = function(c) {
  function a(a, e) {
    var b = c.call(this, a, e) || this;
    b.scheduler = a;
    b.work = e;
    return b;
  }
  __extends$$module$output_operators(a, c);
  a.prototype.requestAsyncId = function(a, e, d) {
    void 0 === d && (d = 0);
    if (null !== d && 0 < d) {
      return c.prototype.requestAsyncId.call(this, a, e, d);
    }
    a.actions.push(this);
    return a.scheduled || (a.scheduled = Immediate$$module$output_operators.setImmediate(a.flush.bind(a, null)));
  };
  a.prototype.recycleAsyncId = function(a, e, d) {
    void 0 === d && (d = 0);
    if (null !== d && 0 < d || null === d && 0 < this.delay) {
      return c.prototype.recycleAsyncId.call(this, a, e, d);
    }
    0 === a.actions.length && (Immediate$$module$output_operators.clearImmediate(e), a.scheduled = void 0);
  };
  return a;
}(AsyncAction$$module$output_operators), AsapScheduler$$module$output_operators = function(c) {
  function a() {
    return null !== c && c.apply(this, arguments) || this;
  }
  __extends$$module$output_operators(a, c);
  a.prototype.flush = function(a) {
    this.active = !0;
    this.scheduled = void 0;
    var b = this.actions, c, f = -1, g = b.length;
    a = a || b.shift();
    do {
      if (c = a.execute(a.state, a.delay)) {
        break;
      }
    } while (++f < g && (a = b.shift()));
    this.active = !1;
    if (c) {
      for (; ++f < g && (a = b.shift());) {
        a.unsubscribe();
      }
      throw c;
    }
  };
  return a;
}(AsyncScheduler$$module$output_operators), asap$$module$output_operators = new AsapScheduler$$module$output_operators(AsapAction$$module$output_operators), SubscribeOnObservable$$module$output_operators = function(c) {
  function a(a, e, d) {
    void 0 === e && (e = 0);
    void 0 === d && (d = asap$$module$output_operators);
    var b = c.call(this) || this;
    b.source = a;
    b.delayTime = e;
    b.scheduler = d;
    if (!isNumeric$$module$output_operators(e) || 0 > e) {
      b.delayTime = 0;
    }
    d && "function" === typeof d.schedule || (b.scheduler = asap$$module$output_operators);
    return b;
  }
  __extends$$module$output_operators(a, c);
  a.create = function(b, c, d) {
    void 0 === c && (c = 0);
    void 0 === d && (d = asap$$module$output_operators);
    return new a(b, c, d);
  };
  a.dispatch = function(a) {
    return this.add(a.source.subscribe(a.subscriber));
  };
  a.prototype._subscribe = function(b) {
    return this.scheduler.schedule(a.dispatch, this.delayTime, {source:this.source, subscriber:b});
  };
  return a;
}(Observable$$module$output_operators), SubscribeOnOperator$$module$output_operators = function() {
  function c(a, b) {
    this.scheduler = a;
    this.delay = b;
  }
  c.prototype.call = function(a, b) {
    return (new SubscribeOnObservable$$module$output_operators(b, this.delay, this.scheduler)).subscribe(a);
  };
  return c;
}(), SwitchMapOperator$$module$output_operators = function() {
  function c(a) {
    this.project = a;
  }
  c.prototype.call = function(a, b) {
    return b.subscribe(new SwitchMapSubscriber$$module$output_operators(a, this.project));
  };
  return c;
}(), SwitchMapSubscriber$$module$output_operators = function(c) {
  function a(a, e) {
    a = c.call(this, a) || this;
    a.project = e;
    a.index = 0;
    return a;
  }
  __extends$$module$output_operators(a, c);
  a.prototype._next = function(a) {
    var b = this.index++;
    try {
      var c = this.project(a, b);
    } catch (f) {
      this.destination.error(f);
      return;
    }
    this._innerSub(c, a, b);
  };
  a.prototype._innerSub = function(a, c, d) {
    var b = this.innerSubscription;
    b && b.unsubscribe();
    b = new InnerSubscriber$$module$output_operators(this, void 0, void 0);
    this.destination.add(b);
    this.innerSubscription = subscribeToResult$$module$output_operators(this, a, c, d, b);
  };
  a.prototype._complete = function() {
    var a = this.innerSubscription;
    a && !a.closed || c.prototype._complete.call(this);
    this.unsubscribe();
  };
  a.prototype._unsubscribe = function() {
    this.innerSubscription = null;
  };
  a.prototype.notifyComplete = function(a) {
    this.destination.remove(a);
    this.innerSubscription = null;
    this.isStopped && c.prototype._complete.call(this);
  };
  a.prototype.notifyNext = function(a, c, d, f, g) {
    this.destination.next(c);
  };
  return a;
}(OuterSubscriber$$module$output_operators), TakeUntilOperator$$module$output_operators = function() {
  function c(a) {
    this.notifier = a;
  }
  c.prototype.call = function(a, b) {
    a = new TakeUntilSubscriber$$module$output_operators(a);
    var c = subscribeToResult$$module$output_operators(a, this.notifier);
    return c && !a.seenValue ? (a.add(c), b.subscribe(a)) : a;
  };
  return c;
}(), TakeUntilSubscriber$$module$output_operators = function(c) {
  function a(a) {
    a = c.call(this, a) || this;
    a.seenValue = !1;
    return a;
  }
  __extends$$module$output_operators(a, c);
  a.prototype.notifyNext = function(a, c, d, f, g) {
    this.seenValue = !0;
    this.complete();
  };
  a.prototype.notifyComplete = function() {
  };
  return a;
}(OuterSubscriber$$module$output_operators), TakeWhileOperator$$module$output_operators = function() {
  function c(a) {
    this.predicate = a;
  }
  c.prototype.call = function(a, b) {
    return b.subscribe(new TakeWhileSubscriber$$module$output_operators(a, this.predicate));
  };
  return c;
}(), TakeWhileSubscriber$$module$output_operators = function(c) {
  function a(a, e) {
    a = c.call(this, a) || this;
    a.predicate = e;
    a.index = 0;
    return a;
  }
  __extends$$module$output_operators(a, c);
  a.prototype._next = function(a) {
    var b = this.destination;
    try {
      var c = this.predicate(a, this.index++);
    } catch (f) {
      b.error(f);
      return;
    }
    this.nextOrComplete(a, c);
  };
  a.prototype.nextOrComplete = function(a, c) {
    var b = this.destination;
    c ? b.next(a) : b.complete();
  };
  return a;
}(Subscriber$$module$output_operators), defaultThrottleConfig$$module$output_operators = {leading:!0, trailing:!1}, ThrottleOperator$$module$output_operators = function() {
  function c(a, b, c) {
    this.durationSelector = a;
    this.leading = b;
    this.trailing = c;
  }
  c.prototype.call = function(a, b) {
    return b.subscribe(new ThrottleSubscriber$$module$output_operators(a, this.durationSelector, this.leading, this.trailing));
  };
  return c;
}(), ThrottleSubscriber$$module$output_operators = function(c) {
  function a(a, e, d, f) {
    var b = c.call(this, a) || this;
    b.destination = a;
    b.durationSelector = e;
    b._leading = d;
    b._trailing = f;
    b._hasValue = !1;
    return b;
  }
  __extends$$module$output_operators(a, c);
  a.prototype._next = function(a) {
    this._hasValue = !0;
    this._sendValue = a;
    this._throttled || (this._leading ? this.send() : this.throttle(a));
  };
  a.prototype.send = function() {
    var a = this._sendValue;
    this._hasValue && (this.destination.next(a), this.throttle(a));
    this._hasValue = !1;
    this._sendValue = null;
  };
  a.prototype.throttle = function(a) {
    (a = this.tryDurationSelector(a)) && this.add(this._throttled = subscribeToResult$$module$output_operators(this, a));
  };
  a.prototype.tryDurationSelector = function(a) {
    try {
      return this.durationSelector(a);
    } catch (e) {
      return this.destination.error(e), null;
    }
  };
  a.prototype.throttlingDone = function() {
    var a = this._throttled, c = this._trailing;
    a && a.unsubscribe();
    this._throttled = null;
    c && this.send();
  };
  a.prototype.notifyNext = function(a, c, d, f, g) {
    this.throttlingDone();
  };
  a.prototype.notifyComplete = function() {
    this.throttlingDone();
  };
  return a;
}(OuterSubscriber$$module$output_operators), ThrottleTimeOperator$$module$output_operators = function() {
  function c(a, b, c, d) {
    this.duration = a;
    this.scheduler = b;
    this.leading = c;
    this.trailing = d;
  }
  c.prototype.call = function(a, b) {
    return b.subscribe(new ThrottleTimeSubscriber$$module$output_operators(a, this.duration, this.scheduler, this.leading, this.trailing));
  };
  return c;
}(), ThrottleTimeSubscriber$$module$output_operators = function(c) {
  function a(a, e, d, f, g) {
    a = c.call(this, a) || this;
    a.duration = e;
    a.scheduler = d;
    a.leading = f;
    a.trailing = g;
    a._hasTrailingValue = !1;
    a._trailingValue = null;
    return a;
  }
  __extends$$module$output_operators(a, c);
  a.prototype._next = function(a) {
    this.throttled ? this.trailing && (this._trailingValue = a, this._hasTrailingValue = !0) : (this.add(this.throttled = this.scheduler.schedule(dispatchNext$1$$module$output_operators, this.duration, {subscriber:this})), this.leading && this.destination.next(a));
  };
  a.prototype._complete = function() {
    this._hasTrailingValue && this.destination.next(this._trailingValue);
    this.destination.complete();
  };
  a.prototype.clearThrottle = function() {
    var a = this.throttled;
    a && (this.trailing && this._hasTrailingValue && (this.destination.next(this._trailingValue), this._trailingValue = null, this._hasTrailingValue = !1), a.unsubscribe(), this.remove(a), this.throttled = null);
  };
  return a;
}(Subscriber$$module$output_operators);
function dispatchNext$1$$module$output_operators(c) {
  c.subscriber.clearThrottle();
}
function defer$$module$output_operators(c) {
  return new Observable$$module$output_operators(function(a) {
    try {
      var b = c();
    } catch (e) {
      a.error(e);
      return;
    }
    return (b ? from$$module$output_operators(b) : empty$1$$module$output_operators()).subscribe(a);
  });
}
var TimeInterval$$module$output_operators = function() {
  return function(c, a) {
    this.value = c;
    this.interval = a;
  };
}();
function TimeoutErrorImpl$$module$output_operators() {
  Error.call(this);
  this.message = "Timeout has occurred";
  this.name = "TimeoutError";
  return this;
}
TimeoutErrorImpl$$module$output_operators.prototype = Object.create(Error.prototype);
var TimeoutError$$module$output_operators = TimeoutErrorImpl$$module$output_operators, TimeoutWithOperator$$module$output_operators = function() {
  function c(a, b, c, d) {
    this.waitFor = a;
    this.absoluteTimeout = b;
    this.withObservable = c;
    this.scheduler = d;
  }
  c.prototype.call = function(a, b) {
    return b.subscribe(new TimeoutWithSubscriber$$module$output_operators(a, this.absoluteTimeout, this.waitFor, this.withObservable, this.scheduler));
  };
  return c;
}(), TimeoutWithSubscriber$$module$output_operators = function(c) {
  function a(a, e, d, f, g) {
    a = c.call(this, a) || this;
    a.absoluteTimeout = e;
    a.waitFor = d;
    a.withObservable = f;
    a.scheduler = g;
    a.action = null;
    a.scheduleTimeout();
    return a;
  }
  __extends$$module$output_operators(a, c);
  a.dispatchTimeout = function(a) {
    var b = a.withObservable;
    a._unsubscribeAndRecycle();
    a.add(subscribeToResult$$module$output_operators(a, b));
  };
  a.prototype.scheduleTimeout = function() {
    var b = this.action;
    b ? this.action = b.schedule(this, this.waitFor) : this.add(this.action = this.scheduler.schedule(a.dispatchTimeout, this.waitFor, this));
  };
  a.prototype._next = function(a) {
    this.absoluteTimeout || this.scheduleTimeout();
    c.prototype._next.call(this, a);
  };
  a.prototype._unsubscribe = function() {
    this.withObservable = this.scheduler = this.action = null;
  };
  return a;
}(OuterSubscriber$$module$output_operators), Timestamp$$module$output_operators = function() {
  return function(c, a) {
    this.value = c;
    this.timestamp = a;
  };
}();
function toArrayReducer$$module$output_operators(c, a, b) {
  if (0 === b) {
    return [a];
  }
  c.push(a);
  return c;
}
var WindowOperator$$module$output_operators = function() {
  function c(a) {
    this.windowBoundaries = a;
  }
  c.prototype.call = function(a, b) {
    a = new WindowSubscriber$$module$output_operators(a);
    b = b.subscribe(a);
    b.closed || a.add(subscribeToResult$$module$output_operators(a, this.windowBoundaries));
    return b;
  };
  return c;
}(), WindowSubscriber$$module$output_operators = function(c) {
  function a(a) {
    var b = c.call(this, a) || this;
    b.window = new Subject$$module$output_operators;
    a.next(b.window);
    return b;
  }
  __extends$$module$output_operators(a, c);
  a.prototype.notifyNext = function(a, c, d, f, g) {
    this.openWindow();
  };
  a.prototype.notifyError = function(a, c) {
    this._error(a);
  };
  a.prototype.notifyComplete = function(a) {
    this._complete();
  };
  a.prototype._next = function(a) {
    this.window.next(a);
  };
  a.prototype._error = function(a) {
    this.window.error(a);
    this.destination.error(a);
  };
  a.prototype._complete = function() {
    this.window.complete();
    this.destination.complete();
  };
  a.prototype._unsubscribe = function() {
    this.window = null;
  };
  a.prototype.openWindow = function() {
    var a = this.window;
    a && a.complete();
    a = this.destination;
    var c = this.window = new Subject$$module$output_operators;
    a.next(c);
  };
  return a;
}(OuterSubscriber$$module$output_operators), WindowCountOperator$$module$output_operators = function() {
  function c(a, b) {
    this.windowSize = a;
    this.startWindowEvery = b;
  }
  c.prototype.call = function(a, b) {
    return b.subscribe(new WindowCountSubscriber$$module$output_operators(a, this.windowSize, this.startWindowEvery));
  };
  return c;
}(), WindowCountSubscriber$$module$output_operators = function(c) {
  function a(a, e, d) {
    var b = c.call(this, a) || this;
    b.destination = a;
    b.windowSize = e;
    b.startWindowEvery = d;
    b.windows = [new Subject$$module$output_operators];
    b.count = 0;
    a.next(b.windows[0]);
    return b;
  }
  __extends$$module$output_operators(a, c);
  a.prototype._next = function(a) {
    for (var b = 0 < this.startWindowEvery ? this.startWindowEvery : this.windowSize, c = this.destination, f = this.windowSize, g = this.windows, h = g.length, k = 0; k < h && !this.closed; k++) {
      g[k].next(a);
    }
    a = this.count - f + 1;
    0 <= a && 0 === a % b && !this.closed && g.shift().complete();
    0 !== ++this.count % b || this.closed || (b = new Subject$$module$output_operators, g.push(b), c.next(b));
  };
  a.prototype._error = function(a) {
    var b = this.windows;
    if (b) {
      for (; 0 < b.length && !this.closed;) {
        b.shift().error(a);
      }
    }
    this.destination.error(a);
  };
  a.prototype._complete = function() {
    var a = this.windows;
    if (a) {
      for (; 0 < a.length && !this.closed;) {
        a.shift().complete();
      }
    }
    this.destination.complete();
  };
  a.prototype._unsubscribe = function() {
    this.count = 0;
    this.windows = null;
  };
  return a;
}(Subscriber$$module$output_operators), WindowTimeOperator$$module$output_operators = function() {
  function c(a, b, c, d) {
    this.windowTimeSpan = a;
    this.windowCreationInterval = b;
    this.maxWindowSize = c;
    this.scheduler = d;
  }
  c.prototype.call = function(a, b) {
    return b.subscribe(new WindowTimeSubscriber$$module$output_operators(a, this.windowTimeSpan, this.windowCreationInterval, this.maxWindowSize, this.scheduler));
  };
  return c;
}(), CountedSubject$$module$output_operators = function(c) {
  function a() {
    var a = null !== c && c.apply(this, arguments) || this;
    a._numberOfNextedValues = 0;
    return a;
  }
  __extends$$module$output_operators(a, c);
  a.prototype.next = function(a) {
    this._numberOfNextedValues++;
    c.prototype.next.call(this, a);
  };
  Object.defineProperty(a.prototype, "numberOfNextedValues", {get:function() {
    return this._numberOfNextedValues;
  }, enumerable:!0, configurable:!0});
  return a;
}(Subject$$module$output_operators), WindowTimeSubscriber$$module$output_operators = function(c) {
  function a(a, e, d, f, g) {
    var b = c.call(this, a) || this;
    b.destination = a;
    b.windowTimeSpan = e;
    b.windowCreationInterval = d;
    b.maxWindowSize = f;
    b.scheduler = g;
    b.windows = [];
    a = b.openWindow();
    null !== d && 0 <= d ? (f = {windowTimeSpan:e, windowCreationInterval:d, subscriber:b, scheduler:g}, b.add(g.schedule(dispatchWindowClose$$module$output_operators, e, {subscriber:b, window:a, context:null})), b.add(g.schedule(dispatchWindowCreation$$module$output_operators, d, f))) : b.add(g.schedule(dispatchWindowTimeSpanOnly$$module$output_operators, e, {subscriber:b, window:a, windowTimeSpan:e}));
    return b;
  }
  __extends$$module$output_operators(a, c);
  a.prototype._next = function(a) {
    for (var b = this.windows, c = b.length, f = 0; f < c; f++) {
      var g = b[f];
      g.closed || (g.next(a), g.numberOfNextedValues >= this.maxWindowSize && this.closeWindow(g));
    }
  };
  a.prototype._error = function(a) {
    for (var b = this.windows; 0 < b.length;) {
      b.shift().error(a);
    }
    this.destination.error(a);
  };
  a.prototype._complete = function() {
    for (var a = this.windows; 0 < a.length;) {
      var c = a.shift();
      c.closed || c.complete();
    }
    this.destination.complete();
  };
  a.prototype.openWindow = function() {
    var a = new CountedSubject$$module$output_operators;
    this.windows.push(a);
    this.destination.next(a);
    return a;
  };
  a.prototype.closeWindow = function(a) {
    a.complete();
    var b = this.windows;
    b.splice(b.indexOf(a), 1);
  };
  return a;
}(Subscriber$$module$output_operators);
function dispatchWindowTimeSpanOnly$$module$output_operators(c) {
  var a = c.subscriber, b = c.windowTimeSpan, e = c.window;
  e && a.closeWindow(e);
  c.window = a.openWindow();
  this.schedule(c, b);
}
function dispatchWindowCreation$$module$output_operators(c) {
  var a = c.windowTimeSpan, b = c.subscriber, e = c.scheduler, d = c.windowCreationInterval, f = b.openWindow(), g = {action:this, subscription:null};
  g.subscription = e.schedule(dispatchWindowClose$$module$output_operators, a, {subscriber:b, window:f, context:g});
  this.add(g.subscription);
  this.schedule(c, d);
}
function dispatchWindowClose$$module$output_operators(c) {
  var a = c.subscriber, b = c.window;
  (c = c.context) && c.action && c.subscription && c.action.remove(c.subscription);
  a.closeWindow(b);
}
var WindowToggleOperator$$module$output_operators = function() {
  function c(a, b) {
    this.openings = a;
    this.closingSelector = b;
  }
  c.prototype.call = function(a, b) {
    return b.subscribe(new WindowToggleSubscriber$$module$output_operators(a, this.openings, this.closingSelector));
  };
  return c;
}(), WindowToggleSubscriber$$module$output_operators = function(c) {
  function a(a, e, d) {
    a = c.call(this, a) || this;
    a.openings = e;
    a.closingSelector = d;
    a.contexts = [];
    a.add(a.openSubscription = subscribeToResult$$module$output_operators(a, e, e));
    return a;
  }
  __extends$$module$output_operators(a, c);
  a.prototype._next = function(a) {
    var b = this.contexts;
    if (b) {
      for (var c = b.length, f = 0; f < c; f++) {
        b[f].window.next(a);
      }
    }
  };
  a.prototype._error = function(a) {
    var b = this.contexts;
    this.contexts = null;
    if (b) {
      for (var d = b.length, f = -1; ++f < d;) {
        var g = b[f];
        g.window.error(a);
        g.subscription.unsubscribe();
      }
    }
    c.prototype._error.call(this, a);
  };
  a.prototype._complete = function() {
    var a = this.contexts;
    this.contexts = null;
    if (a) {
      for (var e = a.length, d = -1; ++d < e;) {
        var f = a[d];
        f.window.complete();
        f.subscription.unsubscribe();
      }
    }
    c.prototype._complete.call(this);
  };
  a.prototype._unsubscribe = function() {
    var a = this.contexts;
    this.contexts = null;
    if (a) {
      for (var c = a.length, d = -1; ++d < c;) {
        var f = a[d];
        f.window.unsubscribe();
        f.subscription.unsubscribe();
      }
    }
  };
  a.prototype.notifyNext = function(a, c, d, f, g) {
    if (a === this.openings) {
      f = tryCatch$$module$output_operators(this.closingSelector)(c);
      if (f === errorObject$$module$output_operators) {
        return this.error(errorObject$$module$output_operators.e);
      }
      a = new Subject$$module$output_operators;
      c = new Subscription$$module$output_operators;
      d = {window:a, subscription:c};
      this.contexts.push(d);
      f = subscribeToResult$$module$output_operators(this, f, d);
      f.closed ? this.closeWindow(this.contexts.length - 1) : (f.context = d, c.add(f));
      this.destination.next(a);
    } else {
      this.closeWindow(this.contexts.indexOf(a));
    }
  };
  a.prototype.notifyError = function(a) {
    this.error(a);
  };
  a.prototype.notifyComplete = function(a) {
    a !== this.openSubscription && this.closeWindow(this.contexts.indexOf(a.context));
  };
  a.prototype.closeWindow = function(a) {
    if (-1 !== a) {
      var b = this.contexts, c = b[a], f = c.window;
      c = c.subscription;
      b.splice(a, 1);
      f.complete();
      c.unsubscribe();
    }
  };
  return a;
}(OuterSubscriber$$module$output_operators), WindowOperator$1$$module$output_operators = function() {
  function c(a) {
    this.closingSelector = a;
  }
  c.prototype.call = function(a, b) {
    return b.subscribe(new WindowSubscriber$1$$module$output_operators(a, this.closingSelector));
  };
  return c;
}(), WindowSubscriber$1$$module$output_operators = function(c) {
  function a(a, e) {
    var b = c.call(this, a) || this;
    b.destination = a;
    b.closingSelector = e;
    b.openWindow();
    return b;
  }
  __extends$$module$output_operators(a, c);
  a.prototype.notifyNext = function(a, c, d, f, g) {
    this.openWindow(g);
  };
  a.prototype.notifyError = function(a, c) {
    this._error(a);
  };
  a.prototype.notifyComplete = function(a) {
    this.openWindow(a);
  };
  a.prototype._next = function(a) {
    this.window.next(a);
  };
  a.prototype._error = function(a) {
    this.window.error(a);
    this.destination.error(a);
    this.unsubscribeClosingNotification();
  };
  a.prototype._complete = function() {
    this.window.complete();
    this.destination.complete();
    this.unsubscribeClosingNotification();
  };
  a.prototype.unsubscribeClosingNotification = function() {
    this.closingNotification && this.closingNotification.unsubscribe();
  };
  a.prototype.openWindow = function(a) {
    void 0 === a && (a = null);
    a && (this.remove(a), a.unsubscribe());
    (a = this.window) && a.complete();
    a = this.window = new Subject$$module$output_operators;
    this.destination.next(a);
    a = tryCatch$$module$output_operators(this.closingSelector)();
    a === errorObject$$module$output_operators ? (a = errorObject$$module$output_operators.e, this.destination.error(a), this.window.error(a)) : this.add(this.closingNotification = subscribeToResult$$module$output_operators(this, a));
  };
  return a;
}(OuterSubscriber$$module$output_operators), WithLatestFromOperator$$module$output_operators = function() {
  function c(a, b) {
    this.observables = a;
    this.project = b;
  }
  c.prototype.call = function(a, b) {
    return b.subscribe(new WithLatestFromSubscriber$$module$output_operators(a, this.observables, this.project));
  };
  return c;
}(), WithLatestFromSubscriber$$module$output_operators = function(c) {
  function a(a, e, d) {
    a = c.call(this, a) || this;
    a.observables = e;
    a.project = d;
    a.toRespond = [];
    d = e.length;
    a.values = Array(d);
    for (var b = 0; b < d; b++) {
      a.toRespond.push(b);
    }
    for (b = 0; b < d; b++) {
      var g = e[b];
      a.add(subscribeToResult$$module$output_operators(a, g, g, b));
    }
    return a;
  }
  __extends$$module$output_operators(a, c);
  a.prototype.notifyNext = function(a, c, d, f, g) {
    this.values[d] = c;
    a = this.toRespond;
    0 < a.length && (d = a.indexOf(d), -1 !== d && a.splice(d, 1));
  };
  a.prototype.notifyComplete = function() {
  };
  a.prototype._next = function(a) {
    0 === this.toRespond.length && (a = [a].concat(this.values), this.project ? this._tryProject(a) : this.destination.next(a));
  };
  a.prototype._tryProject = function(a) {
    try {
      var b = this.project.apply(this, a);
    } catch (d) {
      this.destination.error(d);
      return;
    }
    this.destination.next(b);
  };
  return a;
}(OuterSubscriber$$module$output_operators);
function zip$1$$module$output_operators() {
  for (var c = [], a = 0; a < arguments.length; a++) {
    c[a] = arguments[a];
  }
  a = c[c.length - 1];
  "function" === typeof a && c.pop();
  return fromArray$$module$output_operators(c, void 0).lift(new ZipOperator$$module$output_operators(a));
}
var ZipOperator$$module$output_operators = function() {
  function c(a) {
    this.resultSelector = a;
  }
  c.prototype.call = function(a, b) {
    return b.subscribe(new ZipSubscriber$$module$output_operators(a, this.resultSelector));
  };
  return c;
}(), ZipSubscriber$$module$output_operators = function(c) {
  function a(a, e, d) {
    void 0 === d && (d = Object.create(null));
    a = c.call(this, a) || this;
    a.iterators = [];
    a.active = 0;
    a.resultSelector = "function" === typeof e ? e : null;
    a.values = d;
    return a;
  }
  __extends$$module$output_operators(a, c);
  a.prototype._next = function(a) {
    var b = this.iterators;
    isArray$$module$output_operators(a) ? b.push(new StaticArrayIterator$$module$output_operators(a)) : "function" === typeof a[iterator$$module$output_operators] ? b.push(new StaticIterator$$module$output_operators(a[iterator$$module$output_operators]())) : b.push(new ZipBufferIterator$$module$output_operators(this.destination, this, a));
  };
  a.prototype._complete = function() {
    var a = this.iterators, c = a.length;
    this.unsubscribe();
    if (0 === c) {
      this.destination.complete();
    } else {
      this.active = c;
      for (var d = 0; d < c; d++) {
        var f = a[d];
        f.stillUnsubscribed ? this.destination.add(f.subscribe(f, d)) : this.active--;
      }
    }
  };
  a.prototype.notifyInactive = function() {
    this.active--;
    0 === this.active && this.destination.complete();
  };
  a.prototype.checkIterators = function() {
    for (var a = this.iterators, c = a.length, d = this.destination, f = 0; f < c; f++) {
      var g = a[f];
      if ("function" === typeof g.hasValue && !g.hasValue()) {
        return;
      }
    }
    var h = !1, k = [];
    for (f = 0; f < c; f++) {
      g = a[f];
      var l = g.next();
      g.hasCompleted() && (h = !0);
      if (l.done) {
        d.complete();
        return;
      }
      k.push(l.value);
    }
    this.resultSelector ? this._tryresultSelector(k) : d.next(k);
    h && d.complete();
  };
  a.prototype._tryresultSelector = function(a) {
    try {
      var b = this.resultSelector.apply(this, a);
    } catch (d) {
      this.destination.error(d);
      return;
    }
    this.destination.next(b);
  };
  return a;
}(Subscriber$$module$output_operators), StaticIterator$$module$output_operators = function() {
  function c(a) {
    this.iterator = a;
    this.nextResult = a.next();
  }
  c.prototype.hasValue = function() {
    return !0;
  };
  c.prototype.next = function() {
    var a = this.nextResult;
    this.nextResult = this.iterator.next();
    return a;
  };
  c.prototype.hasCompleted = function() {
    var a = this.nextResult;
    return a && a.done;
  };
  return c;
}(), StaticArrayIterator$$module$output_operators = function() {
  function c(a) {
    this.array = a;
    this.length = this.index = 0;
    this.length = a.length;
  }
  c.prototype[iterator$$module$output_operators] = function() {
    return this;
  };
  c.prototype.next = function(a) {
    a = this.index++;
    var b = this.array;
    return a < this.length ? {value:b[a], done:!1} : {value:null, done:!0};
  };
  c.prototype.hasValue = function() {
    return this.array.length > this.index;
  };
  c.prototype.hasCompleted = function() {
    return this.array.length === this.index;
  };
  return c;
}(), ZipBufferIterator$$module$output_operators = function(c) {
  function a(a, e, d) {
    a = c.call(this, a) || this;
    a.parent = e;
    a.observable = d;
    a.stillUnsubscribed = !0;
    a.buffer = [];
    a.isComplete = !1;
    return a;
  }
  __extends$$module$output_operators(a, c);
  a.prototype[iterator$$module$output_operators] = function() {
    return this;
  };
  a.prototype.next = function() {
    var a = this.buffer;
    return 0 === a.length && this.isComplete ? {value:null, done:!0} : {value:a.shift(), done:!1};
  };
  a.prototype.hasValue = function() {
    return 0 < this.buffer.length;
  };
  a.prototype.hasCompleted = function() {
    return 0 === this.buffer.length && this.isComplete;
  };
  a.prototype.notifyComplete = function() {
    0 < this.buffer.length ? (this.isComplete = !0, this.parent.notifyInactive()) : this.destination.complete();
  };
  a.prototype.notifyNext = function(a, c, d, f, g) {
    this.buffer.push(c);
    this.parent.checkIterators();
  };
  a.prototype.subscribe = function(a, c) {
    return subscribeToResult$$module$output_operators(this, this.observable, this, c);
  };
  return a;
}(OuterSubscriber$$module$output_operators);
module$output_operators.flatMap = module$output_operators.mergeMap;

