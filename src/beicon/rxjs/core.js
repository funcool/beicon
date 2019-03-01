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
goog.provide("beicon.rxjs.core");
var module$output_core = beicon.rxjs.core;
module$output_core.noop = function() {
};
module$output_core.pipe = function() {
  for (var b = [], a = 0; a < arguments.length; a++) {
    b[a] = arguments[a];
  }
  return pipeFromArray$$module$output_core(b);
};
module$output_core.empty = function(b) {
  return b ? emptyScheduled$$module$output_core(b) : module$output_core.EMPTY;
};
module$output_core.of = function() {
  for (var b = [], a = 0; a < arguments.length; a++) {
    b[a] = arguments[a];
  }
  a = b[b.length - 1];
  isScheduler$$module$output_core(a) ? b.pop() : a = void 0;
  switch(b.length) {
    case 0:
      return (0,module$output_core.empty)(a);
    case 1:
      return a ? fromArray$$module$output_core(b, a) : scalar$$module$output_core(b[0]);
    default:
      return fromArray$$module$output_core(b, a);
  }
};
module$output_core.throwError = function(b, a) {
  return a ? new module$output_core.Observable(function(c) {
    return a.schedule(dispatch$$module$output_core, 0, {error:b, subscriber:c});
  }) : new module$output_core.Observable(function(c) {
    return c.error(b);
  });
};
module$output_core.identity = function(b) {
  return b;
};
module$output_core.isObservable = function(b) {
  return !!b && (b instanceof module$output_core.Observable || "function" === typeof b.lift && "function" === typeof b.subscribe);
};
module$output_core.bindCallback = function(b, a, c) {
  if (a) {
    if (isScheduler$$module$output_core(a)) {
      c = a;
    } else {
      return function() {
        for (var d = [], e = 0; e < arguments.length; e++) {
          d[e] = arguments[e];
        }
        return (0,module$output_core.bindCallback)(b, c).apply(void 0, d).pipe(map$$module$output_core(function(c) {
          return isArray$$module$output_core(c) ? a.apply(void 0, c) : a(c);
        }));
      };
    }
  }
  return function() {
    for (var a = [], e = 0; e < arguments.length; e++) {
      a[e] = arguments[e];
    }
    var f = this, g, h = {context:f, subject:g, callbackFunc:b, scheduler:c};
    return new module$output_core.Observable(function(d) {
      if (c) {
        return c.schedule(dispatch$1$$module$output_core, 0, {args:a, subscriber:d, params:h});
      }
      if (!g) {
        g = new module$output_core.AsyncSubject;
        var e = function() {
          for (var c = [], a = 0; a < arguments.length; a++) {
            c[a] = arguments[a];
          }
          g.next(1 >= c.length ? c[0] : c);
          g.complete();
        };
        try {
          b.apply(f, a.concat([e]));
        } catch (l) {
          canReportError$$module$output_core(g) ? g.error(l) : console.warn(l);
        }
      }
      return g.subscribe(d);
    });
  };
};
module$output_core.bindNodeCallback = function(b, a, c) {
  if (a) {
    if (isScheduler$$module$output_core(a)) {
      c = a;
    } else {
      return function() {
        for (var d = [], e = 0; e < arguments.length; e++) {
          d[e] = arguments[e];
        }
        return (0,module$output_core.bindNodeCallback)(b, c).apply(void 0, d).pipe(map$$module$output_core(function(c) {
          return isArray$$module$output_core(c) ? a.apply(void 0, c) : a(c);
        }));
      };
    }
  }
  return function() {
    for (var a = [], e = 0; e < arguments.length; e++) {
      a[e] = arguments[e];
    }
    var f = {subject:void 0, args:a, callbackFunc:b, scheduler:c, context:this};
    return new module$output_core.Observable(function(d) {
      var e = f.context, g = f.subject;
      if (c) {
        return c.schedule(dispatch$2$$module$output_core, 0, {params:f, subscriber:d, context:e});
      }
      if (!g) {
        g = f.subject = new module$output_core.AsyncSubject;
        var m = function() {
          for (var c = [], a = 0; a < arguments.length; a++) {
            c[a] = arguments[a];
          }
          (a = c.shift()) ? g.error(a) : (g.next(1 >= c.length ? c[0] : c), g.complete());
        };
        try {
          b.apply(e, a.concat([m]));
        } catch (l) {
          canReportError$$module$output_core(g) ? g.error(l) : console.warn(l);
        }
      }
      return g.subscribe(d);
    });
  };
};
module$output_core.combineLatest = function() {
  for (var b = [], a = 0; a < arguments.length; a++) {
    b[a] = arguments[a];
  }
  var c = a = null;
  isScheduler$$module$output_core(b[b.length - 1]) && (c = b.pop());
  "function" === typeof b[b.length - 1] && (a = b.pop());
  1 === b.length && isArray$$module$output_core(b[0]) && (b = b[0]);
  return fromArray$$module$output_core(b, c).lift(new CombineLatestOperator$$module$output_core(a));
};
module$output_core.from = function(b, a) {
  if (!a) {
    return b instanceof module$output_core.Observable ? b : new module$output_core.Observable(subscribeTo$$module$output_core(b));
  }
  if (null != b) {
    if (isInteropObservable$$module$output_core(b)) {
      return fromObservable$$module$output_core(b, a);
    }
    if (isPromise$$module$output_core(b)) {
      return fromPromise$$module$output_core(b, a);
    }
    if (isArrayLike$$module$output_core(b)) {
      return fromArray$$module$output_core(b, a);
    }
    if (isIterable$$module$output_core(b) || "string" === typeof b) {
      return fromIterable$$module$output_core(b, a);
    }
  }
  throw new TypeError((null !== b && typeof b || b) + " is not observable");
};
module$output_core.concat = function() {
  for (var b = [], a = 0; a < arguments.length; a++) {
    b[a] = arguments[a];
  }
  return 1 === b.length || 2 === b.length && isScheduler$$module$output_core(b[1]) ? (0,module$output_core.from)(b[0]) : concatAll$$module$output_core()(module$output_core.of.apply(void 0, b));
};
module$output_core.defer = function(b) {
  return new module$output_core.Observable(function(a) {
    try {
      var c = b();
    } catch (d) {
      a.error(d);
      return;
    }
    return (c ? (0,module$output_core.from)(c) : (0,module$output_core.empty)()).subscribe(a);
  });
};
module$output_core.forkJoin = function() {
  for (var b = [], a = 0; a < arguments.length; a++) {
    b[a] = arguments[a];
  }
  var c;
  "function" === typeof b[b.length - 1] && (c = b.pop());
  1 === b.length && isArray$$module$output_core(b[0]) && (b = b[0]);
  return 0 === b.length ? module$output_core.EMPTY : c ? (0,module$output_core.forkJoin)(b).pipe(map$$module$output_core(function(a) {
    return c.apply(void 0, a);
  })) : new module$output_core.Observable(function(c) {
    return new ForkJoinSubscriber$$module$output_core(c, b);
  });
};
module$output_core.fromEvent = function(b, a, c, d) {
  isFunction$$module$output_core(c) && (d = c, c = void 0);
  return d ? (0,module$output_core.fromEvent)(b, a, c).pipe(map$$module$output_core(function(c) {
    return isArray$$module$output_core(c) ? d.apply(void 0, c) : d(c);
  })) : new module$output_core.Observable(function(d) {
    setupSubscription$$module$output_core(b, a, function(c) {
      1 < arguments.length ? d.next(Array.prototype.slice.call(arguments)) : d.next(c);
    }, d, c);
  });
};
module$output_core.fromEventPattern = function(b, a, c) {
  return c ? (0,module$output_core.fromEventPattern)(b, a).pipe(map$$module$output_core(function(a) {
    return isArray$$module$output_core(a) ? c.apply(void 0, a) : c(a);
  })) : new module$output_core.Observable(function(c) {
    var d = function() {
      for (var a = [], b = 0; b < arguments.length; b++) {
        a[b] = arguments[b];
      }
      return c.next(1 === a.length ? a[0] : a);
    };
    try {
      var f = b(d);
    } catch (g) {
      c.error(g);
      return;
    }
    if (isFunction$$module$output_core(a)) {
      return function() {
        return a(d, f);
      };
    }
  });
};
module$output_core.generate = function(b, a, c, d, e) {
  if (1 == arguments.length) {
    var f = b.initialState;
    a = b.condition;
    c = b.iterate;
    var g = b.resultSelector || module$output_core.identity;
    e = b.scheduler;
  } else {
    void 0 === d || isScheduler$$module$output_core(d) ? (f = b, g = module$output_core.identity, e = d) : (f = b, g = d);
  }
  return new module$output_core.Observable(function(b) {
    var d = f;
    if (e) {
      return e.schedule(dispatch$3$$module$output_core, 0, {subscriber:b, iterate:c, condition:a, resultSelector:g, state:d});
    }
    do {
      if (a) {
        var h = void 0;
        try {
          h = a(d);
        } catch (l) {
          b.error(l);
          break;
        }
        if (!h) {
          b.complete();
          break;
        }
      }
      h = void 0;
      try {
        h = g(d);
      } catch (l) {
        b.error(l);
        break;
      }
      b.next(h);
      if (b.closed) {
        break;
      }
      try {
        d = c(d);
      } catch (l) {
        b.error(l);
        break;
      }
    } while (1);
  });
};
module$output_core.iif = function(b, a, c) {
  void 0 === a && (a = module$output_core.EMPTY);
  void 0 === c && (c = module$output_core.EMPTY);
  return (0,module$output_core.defer)(function() {
    return b() ? a : c;
  });
};
module$output_core.interval = function(b, a) {
  void 0 === b && (b = 0);
  void 0 === a && (a = module$output_core.asyncScheduler);
  if (!isNumeric$$module$output_core(b) || 0 > b) {
    b = 0;
  }
  a && "function" === typeof a.schedule || (a = module$output_core.asyncScheduler);
  return new module$output_core.Observable(function(c) {
    c.add(a.schedule(dispatch$4$$module$output_core, b, {subscriber:c, counter:0, period:b}));
    return c;
  });
};
module$output_core.merge = function() {
  for (var b = [], a = 0; a < arguments.length; a++) {
    b[a] = arguments[a];
  }
  a = Number.POSITIVE_INFINITY;
  var c = null, d = b[b.length - 1];
  isScheduler$$module$output_core(d) ? (c = b.pop(), 1 < b.length && "number" === typeof b[b.length - 1] && (a = b.pop())) : "number" === typeof d && (a = b.pop());
  return null === c && 1 === b.length && b[0] instanceof module$output_core.Observable ? b[0] : mergeAll$$module$output_core(a)(fromArray$$module$output_core(b, c));
};
module$output_core.never = function() {
  return module$output_core.NEVER;
};
module$output_core.onErrorResumeNext = function() {
  for (var b = [], a = 0; a < arguments.length; a++) {
    b[a] = arguments[a];
  }
  if (0 === b.length) {
    return module$output_core.EMPTY;
  }
  var c = b[0], d = b.slice(1);
  return 1 === b.length && isArray$$module$output_core(c) ? module$output_core.onErrorResumeNext.apply(void 0, c) : new module$output_core.Observable(function(a) {
    var b = function() {
      return a.add(module$output_core.onErrorResumeNext.apply(void 0, d).subscribe(a));
    };
    return (0,module$output_core.from)(c).subscribe({next:function(c) {
      a.next(c);
    }, error:b, complete:b});
  });
};
module$output_core.pairs = function(b, a) {
  return a ? new module$output_core.Observable(function(c) {
    var d = Object.keys(b), e = new module$output_core.Subscription;
    e.add(a.schedule(dispatch$5$$module$output_core, 0, {keys:d, index:0, subscriber:c, subscription:e, obj:b}));
    return e;
  }) : new module$output_core.Observable(function(c) {
    for (var a = Object.keys(b), e = 0; e < a.length && !c.closed; e++) {
      var f = a[e];
      b.hasOwnProperty(f) && c.next([f, b[f]]);
    }
    c.complete();
  });
};
module$output_core.race = function() {
  for (var b = [], a = 0; a < arguments.length; a++) {
    b[a] = arguments[a];
  }
  if (1 === b.length) {
    if (isArray$$module$output_core(b[0])) {
      b = b[0];
    } else {
      return b[0];
    }
  }
  return fromArray$$module$output_core(b, void 0).lift(new RaceOperator$$module$output_core);
};
module$output_core.range = function(b, a, c) {
  void 0 === b && (b = 0);
  void 0 === a && (a = 0);
  return new module$output_core.Observable(function(d) {
    var e = 0, f = b;
    if (c) {
      return c.schedule(dispatch$6$$module$output_core, 0, {index:e, count:a, start:b, subscriber:d});
    }
    do {
      if (e++ >= a) {
        d.complete();
        break;
      }
      d.next(f++);
      if (d.closed) {
        break;
      }
    } while (1);
  });
};
module$output_core.timer = function(b, a, c) {
  void 0 === b && (b = 0);
  var d = -1;
  isNumeric$$module$output_core(a) ? d = 1 > Number(a) && 1 || Number(a) : isScheduler$$module$output_core(a) && (c = a);
  isScheduler$$module$output_core(c) || (c = module$output_core.asyncScheduler);
  return new module$output_core.Observable(function(a) {
    var e = isNumeric$$module$output_core(b) ? b : +b - c.now();
    return c.schedule(dispatch$7$$module$output_core, e, {index:0, period:d, subscriber:a});
  });
};
module$output_core.using = function(b, a) {
  return new module$output_core.Observable(function(c) {
    try {
      var d = b();
    } catch (g) {
      c.error(g);
      return;
    }
    try {
      var e = a(d);
    } catch (g) {
      c.error(g);
      return;
    }
    var f = (e ? (0,module$output_core.from)(e) : module$output_core.EMPTY).subscribe(c);
    return function() {
      f.unsubscribe();
      d && d.unsubscribe();
    };
  });
};
module$output_core.zip = function() {
  for (var b = [], a = 0; a < arguments.length; a++) {
    b[a] = arguments[a];
  }
  a = b[b.length - 1];
  "function" === typeof a && b.pop();
  return fromArray$$module$output_core(b, void 0).lift(new ZipOperator$$module$output_core(a));
};
Object.defineProperty(module$output_core, "__esModule", {value:!0});
var extendStatics$$module$output_core = Object.setPrototypeOf || {__proto__:[]} instanceof Array && function(b, a) {
  b.__proto__ = a;
} || function(b, a) {
  for (var c in a) {
    a.hasOwnProperty(c) && (b[c] = a[c]);
  }
};
function __extends$$module$output_core(b, a) {
  function c() {
    this.constructor = b;
  }
  extendStatics$$module$output_core(b, a);
  b.prototype = null === a ? Object.create(a) : (c.prototype = a.prototype, new c);
}
function __values$$module$output_core(b) {
  var a = "function" === typeof Symbol && b[Symbol.iterator], c = 0;
  return a ? a.call(b) : {next:function() {
    b && c >= b.length && (b = void 0);
    return {value:b && b[c++], done:!b};
  }};
}
function __read$$module$output_core(b, a) {
  var c = "function" === typeof Symbol && b[Symbol.iterator];
  if (!c) {
    return b;
  }
  b = c.call(b);
  var d, e = [];
  try {
    for (; (void 0 === a || 0 < a--) && !(d = b.next()).done;) {
      e.push(d.value);
    }
  } catch (g) {
    var f = {error:g};
  } finally {
    try {
      d && !d.done && (c = b["return"]) && c.call(b);
    } finally {
      if (f) {
        throw f.error;
      }
    }
  }
  return e;
}
function __await$$module$output_core(b) {
  return this instanceof __await$$module$output_core ? (this.v = b, this) : new __await$$module$output_core(b);
}
function isFunction$$module$output_core(b) {
  return "function" === typeof b;
}
var _enable_super_gross_mode_that_will_cause_bad_things$$module$output_core = !1;
module$output_core.config = {Promise:void 0, set useDeprecatedSynchronousErrorHandling(b) {
  b ? console.warn("DEPRECATED! RxJS was set to use deprecated synchronous error handling behavior by code at: \n" + Error().stack) : _enable_super_gross_mode_that_will_cause_bad_things$$module$output_core && console.log("RxJS: Back to a better error behavior. Thank you. <3");
  _enable_super_gross_mode_that_will_cause_bad_things$$module$output_core = b;
}, get useDeprecatedSynchronousErrorHandling() {
  return _enable_super_gross_mode_that_will_cause_bad_things$$module$output_core;
}};
function hostReportError$$module$output_core(b) {
  setTimeout(function() {
    throw b;
  });
}
var empty$$module$output_core = {closed:!0, next:function(b) {
}, error:function(b) {
  if (module$output_core.config.useDeprecatedSynchronousErrorHandling) {
    throw b;
  }
  hostReportError$$module$output_core(b);
}, complete:function() {
}}, isArray$$module$output_core = Array.isArray || function(b) {
  return b && "number" === typeof b.length;
};
function isObject$$module$output_core(b) {
  return null != b && "object" === typeof b;
}
var errorObject$$module$output_core = {e:{}}, tryCatchTarget$$module$output_core;
function tryCatcher$$module$output_core() {
  try {
    return tryCatchTarget$$module$output_core.apply(this, arguments);
  } catch (b) {
    return errorObject$$module$output_core.e = b, errorObject$$module$output_core;
  }
}
function tryCatch$$module$output_core(b) {
  tryCatchTarget$$module$output_core = b;
  return tryCatcher$$module$output_core;
}
function UnsubscriptionErrorImpl$$module$output_core(b) {
  Error.call(this);
  this.message = b ? b.length + " errors occurred during unsubscription:\n" + b.map(function(a, c) {
    return c + 1 + ") " + a.toString();
  }).join("\n  ") : "";
  this.name = "UnsubscriptionError";
  this.errors = b;
  return this;
}
UnsubscriptionErrorImpl$$module$output_core.prototype = Object.create(Error.prototype);
module$output_core.UnsubscriptionError = UnsubscriptionErrorImpl$$module$output_core;
module$output_core.Subscription = function() {
  function b(a) {
    this.closed = !1;
    this._subscriptions = this._parents = this._parent = null;
    a && (this._unsubscribe = a);
  }
  b.prototype.unsubscribe = function() {
    var a = !1;
    if (!this.closed) {
      var c = this._parent, b = this._parents, e = this._unsubscribe, f = this._subscriptions;
      this.closed = !0;
      this._subscriptions = this._parents = this._parent = null;
      for (var g = -1, h = b ? b.length : 0; c;) {
        c.remove(this), c = ++g < h && b[g] || null;
      }
      if (isFunction$$module$output_core(e) && (c = tryCatch$$module$output_core(e).call(this), c === errorObject$$module$output_core)) {
        a = !0;
        var k = k || (errorObject$$module$output_core.e instanceof module$output_core.UnsubscriptionError ? flattenUnsubscriptionErrors$$module$output_core(errorObject$$module$output_core.e.errors) : [errorObject$$module$output_core.e]);
      }
      if (isArray$$module$output_core(f)) {
        for (g = -1, h = f.length; ++g < h;) {
          c = f[g], isObject$$module$output_core(c) && (c = tryCatch$$module$output_core(c.unsubscribe).call(c), c === errorObject$$module$output_core && (a = !0, k = k || [], c = errorObject$$module$output_core.e, c instanceof module$output_core.UnsubscriptionError ? k = k.concat(flattenUnsubscriptionErrors$$module$output_core(c.errors)) : k.push(c)));
        }
      }
      if (a) {
        throw new module$output_core.UnsubscriptionError(k);
      }
    }
  };
  b.prototype.add = function(a) {
    if (!a || a === b.EMPTY) {
      return b.EMPTY;
    }
    if (a === this) {
      return this;
    }
    var c = a;
    switch(typeof a) {
      case "function":
        c = new b(a);
      case "object":
        if (c.closed || "function" !== typeof c.unsubscribe) {
          return c;
        }
        if (this.closed) {
          return c.unsubscribe(), c;
        }
        "function" !== typeof c._addParent && (a = c, c = new b, c._subscriptions = [a]);
        break;
      default:
        throw Error("unrecognized teardown " + a + " added to Subscription.");
    }
    (this._subscriptions || (this._subscriptions = [])).push(c);
    c._addParent(this);
    return c;
  };
  b.prototype.remove = function(a) {
    var c = this._subscriptions;
    c && (a = c.indexOf(a), -1 !== a && c.splice(a, 1));
  };
  b.prototype._addParent = function(a) {
    var c = this._parent, b = this._parents;
    c && c !== a ? b ? -1 === b.indexOf(a) && b.push(a) : this._parents = [a] : this._parent = a;
  };
  b.EMPTY = function(a) {
    a.closed = !0;
    return a;
  }(new b);
  return b;
}();
function flattenUnsubscriptionErrors$$module$output_core(b) {
  return b.reduce(function(a, c) {
    return a.concat(c instanceof module$output_core.UnsubscriptionError ? c.errors : c);
  }, []);
}
var rxSubscriber$$module$output_core = "function" === typeof Symbol ? Symbol("rxSubscriber") : "@@rxSubscriber_" + Math.random();
module$output_core.Subscriber = function(b) {
  function a(c, d, e) {
    var f = b.call(this) || this;
    f.syncErrorValue = null;
    f.syncErrorThrown = !1;
    f.syncErrorThrowable = !1;
    f.isStopped = !1;
    f._parentSubscription = null;
    switch(arguments.length) {
      case 0:
        f.destination = empty$$module$output_core;
        break;
      case 1:
        if (!c) {
          f.destination = empty$$module$output_core;
          break;
        }
        if ("object" === typeof c) {
          c instanceof a ? (f.syncErrorThrowable = c.syncErrorThrowable, f.destination = c, c.add(f)) : (f.syncErrorThrowable = !0, f.destination = new SafeSubscriber$$module$output_core(f, c));
          break;
        }
      default:
        f.syncErrorThrowable = !0, f.destination = new SafeSubscriber$$module$output_core(f, c, d, e);
    }
    return f;
  }
  __extends$$module$output_core(a, b);
  a.prototype[rxSubscriber$$module$output_core] = function() {
    return this;
  };
  a.create = function(c, b, e) {
    c = new a(c, b, e);
    c.syncErrorThrowable = !1;
    return c;
  };
  a.prototype.next = function(c) {
    this.isStopped || this._next(c);
  };
  a.prototype.error = function(c) {
    this.isStopped || (this.isStopped = !0, this._error(c));
  };
  a.prototype.complete = function() {
    this.isStopped || (this.isStopped = !0, this._complete());
  };
  a.prototype.unsubscribe = function() {
    this.closed || (this.isStopped = !0, b.prototype.unsubscribe.call(this));
  };
  a.prototype._next = function(c) {
    this.destination.next(c);
  };
  a.prototype._error = function(c) {
    this.destination.error(c);
    this.unsubscribe();
  };
  a.prototype._complete = function() {
    this.destination.complete();
    this.unsubscribe();
  };
  a.prototype._unsubscribeAndRecycle = function() {
    var c = this._parent, a = this._parents;
    this._parents = this._parent = null;
    this.unsubscribe();
    this.isStopped = this.closed = !1;
    this._parent = c;
    this._parents = a;
    this._parentSubscription = null;
    return this;
  };
  return a;
}(module$output_core.Subscription);
var SafeSubscriber$$module$output_core = function(b) {
  function a(c, a, e, f) {
    var d = b.call(this) || this;
    d._parentSubscriber = c;
    c = d;
    if (isFunction$$module$output_core(a)) {
      var h = a;
    } else {
      a && (h = a.next, e = a.error, f = a.complete, a !== empty$$module$output_core && (c = Object.create(a), isFunction$$module$output_core(c.unsubscribe) && d.add(c.unsubscribe.bind(c)), c.unsubscribe = d.unsubscribe.bind(d)));
    }
    d._context = c;
    d._next = h;
    d._error = e;
    d._complete = f;
    return d;
  }
  __extends$$module$output_core(a, b);
  a.prototype.next = function(c) {
    if (!this.isStopped && this._next) {
      var a = this._parentSubscriber;
      module$output_core.config.useDeprecatedSynchronousErrorHandling && a.syncErrorThrowable ? this.__tryOrSetError(a, this._next, c) && this.unsubscribe() : this.__tryOrUnsub(this._next, c);
    }
  };
  a.prototype.error = function(c) {
    if (!this.isStopped) {
      var a = this._parentSubscriber, b = module$output_core.config.useDeprecatedSynchronousErrorHandling;
      if (this._error) {
        b && a.syncErrorThrowable ? this.__tryOrSetError(a, this._error, c) : this.__tryOrUnsub(this._error, c), this.unsubscribe();
      } else {
        if (a.syncErrorThrowable) {
          b ? (a.syncErrorValue = c, a.syncErrorThrown = !0) : hostReportError$$module$output_core(c), this.unsubscribe();
        } else {
          this.unsubscribe();
          if (b) {
            throw c;
          }
          hostReportError$$module$output_core(c);
        }
      }
    }
  };
  a.prototype.complete = function() {
    var c = this;
    if (!this.isStopped) {
      var a = this._parentSubscriber;
      if (this._complete) {
        var b = function() {
          return c._complete.call(c._context);
        };
        module$output_core.config.useDeprecatedSynchronousErrorHandling && a.syncErrorThrowable ? this.__tryOrSetError(a, b) : this.__tryOrUnsub(b);
      }
      this.unsubscribe();
    }
  };
  a.prototype.__tryOrUnsub = function(c, a) {
    try {
      c.call(this._context, a);
    } catch (e) {
      this.unsubscribe();
      if (module$output_core.config.useDeprecatedSynchronousErrorHandling) {
        throw e;
      }
      hostReportError$$module$output_core(e);
    }
  };
  a.prototype.__tryOrSetError = function(c, a, b) {
    if (!module$output_core.config.useDeprecatedSynchronousErrorHandling) {
      throw Error("bad call");
    }
    try {
      a.call(this._context, b);
    } catch (f) {
      return module$output_core.config.useDeprecatedSynchronousErrorHandling ? (c.syncErrorValue = f, c.syncErrorThrown = !0) : hostReportError$$module$output_core(f), !0;
    }
    return !1;
  };
  a.prototype._unsubscribe = function() {
    var c = this._parentSubscriber;
    this._parentSubscriber = this._context = null;
    c.unsubscribe();
  };
  return a;
}(module$output_core.Subscriber);
function canReportError$$module$output_core(b) {
  for (; b;) {
    var a = b.destination, c = b.isStopped;
    if (b.closed || c) {
      return !1;
    }
    b = a && a instanceof module$output_core.Subscriber ? a : null;
  }
  return !0;
}
function toSubscriber$$module$output_core(b, a, c) {
  if (b) {
    if (b instanceof module$output_core.Subscriber) {
      return b;
    }
    if (b[rxSubscriber$$module$output_core]) {
      return b[rxSubscriber$$module$output_core]();
    }
  }
  return b || a || c ? new module$output_core.Subscriber(b, a, c) : new module$output_core.Subscriber(empty$$module$output_core);
}
module$output_core.observable = "function" === typeof Symbol && Symbol.observable || "@@observable";
function pipeFromArray$$module$output_core(b) {
  return b ? 1 === b.length ? b[0] : function(a) {
    return b.reduce(function(c, a) {
      return a(c);
    }, a);
  } : module$output_core.noop;
}
module$output_core.Observable = function() {
  function b(a) {
    this._isScalar = !1;
    a && (this._subscribe = a);
  }
  b.prototype.lift = function(a) {
    var c = new b;
    c.source = this;
    c.operator = a;
    return c;
  };
  b.prototype.subscribe = function(a, c, b) {
    var d = this.operator;
    a = toSubscriber$$module$output_core(a, c, b);
    d ? d.call(a, this.source) : a.add(this.source || module$output_core.config.useDeprecatedSynchronousErrorHandling && !a.syncErrorThrowable ? this._subscribe(a) : this._trySubscribe(a));
    if (module$output_core.config.useDeprecatedSynchronousErrorHandling && a.syncErrorThrowable && (a.syncErrorThrowable = !1, a.syncErrorThrown)) {
      throw a.syncErrorValue;
    }
    return a;
  };
  b.prototype._trySubscribe = function(a) {
    try {
      return this._subscribe(a);
    } catch (c) {
      module$output_core.config.useDeprecatedSynchronousErrorHandling && (a.syncErrorThrown = !0, a.syncErrorValue = c), canReportError$$module$output_core(a) ? a.error(c) : console.warn(c);
    }
  };
  b.prototype.forEach = function(a, c) {
    var b = this;
    c = getPromiseCtor$$module$output_core(c);
    return new c(function(c, d) {
      var e = b.subscribe(function(c) {
        try {
          a(c);
        } catch (k) {
          d(k), e && e.unsubscribe();
        }
      }, d, c);
    });
  };
  b.prototype._subscribe = function(a) {
    var c = this.source;
    return c && c.subscribe(a);
  };
  b.prototype[module$output_core.observable] = function() {
    return this;
  };
  b.prototype.pipe = function() {
    for (var a = [], c = 0; c < arguments.length; c++) {
      a[c] = arguments[c];
    }
    return 0 === a.length ? this : pipeFromArray$$module$output_core(a)(this);
  };
  b.prototype.toPromise = function(a) {
    var c = this;
    a = getPromiseCtor$$module$output_core(a);
    return new a(function(a, b) {
      var d;
      c.subscribe(function(c) {
        return d = c;
      }, function(c) {
        return b(c);
      }, function() {
        return a(d);
      });
    });
  };
  b.create = function(a) {
    return new b(a);
  };
  return b;
}();
function getPromiseCtor$$module$output_core(b) {
  b || (b = module$output_core.config.Promise || Promise);
  if (!b) {
    throw Error("no Promise impl found");
  }
  return b;
}
function ObjectUnsubscribedErrorImpl$$module$output_core() {
  Error.call(this);
  this.message = "object unsubscribed";
  this.name = "ObjectUnsubscribedError";
  return this;
}
ObjectUnsubscribedErrorImpl$$module$output_core.prototype = Object.create(Error.prototype);
module$output_core.ObjectUnsubscribedError = ObjectUnsubscribedErrorImpl$$module$output_core;
var SubjectSubscription$$module$output_core = function(b) {
  function a(c, a) {
    var d = b.call(this) || this;
    d.subject = c;
    d.subscriber = a;
    d.closed = !1;
    return d;
  }
  __extends$$module$output_core(a, b);
  a.prototype.unsubscribe = function() {
    if (!this.closed) {
      this.closed = !0;
      var c = this.subject, a = c.observers;
      this.subject = null;
      !a || 0 === a.length || c.isStopped || c.closed || (c = a.indexOf(this.subscriber), -1 !== c && a.splice(c, 1));
    }
  };
  return a;
}(module$output_core.Subscription), SubjectSubscriber$$module$output_core = function(b) {
  function a(c) {
    var a = b.call(this, c) || this;
    a.destination = c;
    return a;
  }
  __extends$$module$output_core(a, b);
  return a;
}(module$output_core.Subscriber);
module$output_core.Subject = function(b) {
  function a() {
    var c = b.call(this) || this;
    c.observers = [];
    c.closed = !1;
    c.isStopped = !1;
    c.hasError = !1;
    c.thrownError = null;
    return c;
  }
  __extends$$module$output_core(a, b);
  a.prototype[rxSubscriber$$module$output_core] = function() {
    return new SubjectSubscriber$$module$output_core(this);
  };
  a.prototype.lift = function(c) {
    var a = new AnonymousSubject$$module$output_core(this, this);
    a.operator = c;
    return a;
  };
  a.prototype.next = function(c) {
    if (this.closed) {
      throw new module$output_core.ObjectUnsubscribedError;
    }
    if (!this.isStopped) {
      var a = this.observers, b = a.length;
      a = a.slice();
      for (var f = 0; f < b; f++) {
        a[f].next(c);
      }
    }
  };
  a.prototype.error = function(c) {
    if (this.closed) {
      throw new module$output_core.ObjectUnsubscribedError;
    }
    this.hasError = !0;
    this.thrownError = c;
    this.isStopped = !0;
    var a = this.observers, b = a.length;
    a = a.slice();
    for (var f = 0; f < b; f++) {
      a[f].error(c);
    }
    this.observers.length = 0;
  };
  a.prototype.complete = function() {
    if (this.closed) {
      throw new module$output_core.ObjectUnsubscribedError;
    }
    this.isStopped = !0;
    var c = this.observers, a = c.length;
    c = c.slice();
    for (var b = 0; b < a; b++) {
      c[b].complete();
    }
    this.observers.length = 0;
  };
  a.prototype.unsubscribe = function() {
    this.closed = this.isStopped = !0;
    this.observers = null;
  };
  a.prototype._trySubscribe = function(c) {
    if (this.closed) {
      throw new module$output_core.ObjectUnsubscribedError;
    }
    return b.prototype._trySubscribe.call(this, c);
  };
  a.prototype._subscribe = function(c) {
    if (this.closed) {
      throw new module$output_core.ObjectUnsubscribedError;
    }
    if (this.hasError) {
      return c.error(this.thrownError), module$output_core.Subscription.EMPTY;
    }
    if (this.isStopped) {
      return c.complete(), module$output_core.Subscription.EMPTY;
    }
    this.observers.push(c);
    return new SubjectSubscription$$module$output_core(this, c);
  };
  a.prototype.asObservable = function() {
    var c = new module$output_core.Observable;
    c.source = this;
    return c;
  };
  a.create = function(c, a) {
    return new AnonymousSubject$$module$output_core(c, a);
  };
  return a;
}(module$output_core.Observable);
var AnonymousSubject$$module$output_core = function(b) {
  function a(c, a) {
    var d = b.call(this) || this;
    d.destination = c;
    d.source = a;
    return d;
  }
  __extends$$module$output_core(a, b);
  a.prototype.next = function(c) {
    var a = this.destination;
    a && a.next && a.next(c);
  };
  a.prototype.error = function(c) {
    var a = this.destination;
    a && a.error && this.destination.error(c);
  };
  a.prototype.complete = function() {
    var c = this.destination;
    c && c.complete && this.destination.complete();
  };
  a.prototype._subscribe = function(c) {
    return this.source ? this.source.subscribe(c) : module$output_core.Subscription.EMPTY;
  };
  return a;
}(module$output_core.Subject);
function refCount$$module$output_core() {
  return function(b) {
    return b.lift(new RefCountOperator$1$$module$output_core(b));
  };
}
var RefCountOperator$1$$module$output_core = function() {
  function b(a) {
    this.connectable = a;
  }
  b.prototype.call = function(a, c) {
    var b = this.connectable;
    b._refCount++;
    a = new RefCountSubscriber$1$$module$output_core(a, b);
    c = c.subscribe(a);
    a.closed || (a.connection = b.connect());
    return c;
  };
  return b;
}(), RefCountSubscriber$1$$module$output_core = function(b) {
  function a(c, a) {
    c = b.call(this, c) || this;
    c.connectable = a;
    return c;
  }
  __extends$$module$output_core(a, b);
  a.prototype._unsubscribe = function() {
    var c = this.connectable;
    if (c) {
      this.connectable = null;
      var a = c._refCount;
      0 >= a ? this.connection = null : (c._refCount = a - 1, 1 < a ? this.connection = null : (a = this.connection, c = c._connection, this.connection = null, !c || a && c !== a || c.unsubscribe()));
    } else {
      this.connection = null;
    }
  };
  return a;
}(module$output_core.Subscriber);
module$output_core.ConnectableObservable = function(b) {
  function a(c, a) {
    var d = b.call(this) || this;
    d.source = c;
    d.subjectFactory = a;
    d._refCount = 0;
    d._isComplete = !1;
    return d;
  }
  __extends$$module$output_core(a, b);
  a.prototype._subscribe = function(c) {
    return this.getSubject().subscribe(c);
  };
  a.prototype.getSubject = function() {
    var c = this._subject;
    if (!c || c.isStopped) {
      this._subject = this.subjectFactory();
    }
    return this._subject;
  };
  a.prototype.connect = function() {
    var c = this._connection;
    c || (this._isComplete = !1, c = this._connection = new module$output_core.Subscription, c.add(this.source.subscribe(new ConnectableSubscriber$$module$output_core(this.getSubject(), this))), c.closed ? (this._connection = null, c = module$output_core.Subscription.EMPTY) : this._connection = c);
    return c;
  };
  a.prototype.refCount = function() {
    return refCount$$module$output_core()(this);
  };
  return a;
}(module$output_core.Observable);
var ConnectableSubscriber$$module$output_core = function(b) {
  function a(c, a) {
    c = b.call(this, c) || this;
    c.connectable = a;
    return c;
  }
  __extends$$module$output_core(a, b);
  a.prototype._error = function(c) {
    this._unsubscribe();
    b.prototype._error.call(this, c);
  };
  a.prototype._complete = function() {
    this.connectable._isComplete = !0;
    this._unsubscribe();
    b.prototype._complete.call(this);
  };
  a.prototype._unsubscribe = function() {
    var c = this.connectable;
    if (c) {
      this.connectable = null;
      var a = c._connection;
      c._refCount = 0;
      c._subject = null;
      c._connection = null;
      a && a.unsubscribe();
    }
  };
  return a;
}(SubjectSubscriber$$module$output_core), RefCountSubscriber$$module$output_core = function(b) {
  function a(c, a) {
    c = b.call(this, c) || this;
    c.connectable = a;
    return c;
  }
  __extends$$module$output_core(a, b);
  a.prototype._unsubscribe = function() {
    var c = this.connectable;
    if (c) {
      this.connectable = null;
      var a = c._refCount;
      0 >= a ? this.connection = null : (c._refCount = a - 1, 1 < a ? this.connection = null : (a = this.connection, c = c._connection, this.connection = null, !c || a && c !== a || c.unsubscribe()));
    } else {
      this.connection = null;
    }
  };
  return a;
}(module$output_core.Subscriber), GroupByOperator$$module$output_core = function() {
  function b(a, c, b, e) {
    this.keySelector = a;
    this.elementSelector = c;
    this.durationSelector = b;
    this.subjectSelector = e;
  }
  b.prototype.call = function(a, c) {
    return c.subscribe(new GroupBySubscriber$$module$output_core(a, this.keySelector, this.elementSelector, this.durationSelector, this.subjectSelector));
  };
  return b;
}(), GroupBySubscriber$$module$output_core = function(b) {
  function a(c, a, e, f, g) {
    c = b.call(this, c) || this;
    c.keySelector = a;
    c.elementSelector = e;
    c.durationSelector = f;
    c.subjectSelector = g;
    c.groups = null;
    c.attemptedToUnsubscribe = !1;
    c.count = 0;
    return c;
  }
  __extends$$module$output_core(a, b);
  a.prototype._next = function(c) {
    try {
      var a = this.keySelector(c);
    } catch (e) {
      this.error(e);
      return;
    }
    this._group(c, a);
  };
  a.prototype._group = function(c, a) {
    var b = this.groups;
    b || (b = this.groups = new Map);
    var d = b.get(a);
    if (this.elementSelector) {
      try {
        var g = this.elementSelector(c);
      } catch (h) {
        this.error(h);
      }
    } else {
      g = c;
    }
    if (!d && (d = this.subjectSelector ? this.subjectSelector() : new module$output_core.Subject, b.set(a, d), c = new module$output_core.GroupedObservable(a, d, this), this.destination.next(c), this.durationSelector)) {
      c = void 0;
      try {
        c = this.durationSelector(new module$output_core.GroupedObservable(a, d));
      } catch (h) {
        this.error(h);
        return;
      }
      this.add(c.subscribe(new GroupDurationSubscriber$$module$output_core(a, d, this)));
    }
    d.closed || d.next(g);
  };
  a.prototype._error = function(c) {
    var a = this.groups;
    a && (a.forEach(function(a, b) {
      a.error(c);
    }), a.clear());
    this.destination.error(c);
  };
  a.prototype._complete = function() {
    var c = this.groups;
    c && (c.forEach(function(c, a) {
      c.complete();
    }), c.clear());
    this.destination.complete();
  };
  a.prototype.removeGroup = function(c) {
    this.groups.delete(c);
  };
  a.prototype.unsubscribe = function() {
    this.closed || (this.attemptedToUnsubscribe = !0, 0 === this.count && b.prototype.unsubscribe.call(this));
  };
  return a;
}(module$output_core.Subscriber), GroupDurationSubscriber$$module$output_core = function(b) {
  function a(c, a, e) {
    var d = b.call(this, a) || this;
    d.key = c;
    d.group = a;
    d.parent = e;
    return d;
  }
  __extends$$module$output_core(a, b);
  a.prototype._next = function(c) {
    this.complete();
  };
  a.prototype._unsubscribe = function() {
    var c = this.parent, a = this.key;
    this.key = this.parent = null;
    c && c.removeGroup(a);
  };
  return a;
}(module$output_core.Subscriber);
module$output_core.GroupedObservable = function(b) {
  function a(c, a, e) {
    var d = b.call(this) || this;
    d.key = c;
    d.groupSubject = a;
    d.refCountSubscription = e;
    return d;
  }
  __extends$$module$output_core(a, b);
  a.prototype._subscribe = function(c) {
    var a = new module$output_core.Subscription, b = this.refCountSubscription, f = this.groupSubject;
    b && !b.closed && a.add(new InnerRefCountSubscription$$module$output_core(b));
    a.add(f.subscribe(c));
    return a;
  };
  return a;
}(module$output_core.Observable);
var InnerRefCountSubscription$$module$output_core = function(b) {
  function a(c) {
    var a = b.call(this) || this;
    a.parent = c;
    c.count++;
    return a;
  }
  __extends$$module$output_core(a, b);
  a.prototype.unsubscribe = function() {
    var c = this.parent;
    c.closed || this.closed || (b.prototype.unsubscribe.call(this), --c.count, 0 === c.count && c.attemptedToUnsubscribe && c.unsubscribe());
  };
  return a;
}(module$output_core.Subscription);
module$output_core.BehaviorSubject = function(b) {
  function a(c) {
    var a = b.call(this) || this;
    a._value = c;
    return a;
  }
  __extends$$module$output_core(a, b);
  Object.defineProperty(a.prototype, "value", {get:function() {
    return this.getValue();
  }, enumerable:!0, configurable:!0});
  a.prototype._subscribe = function(c) {
    var a = b.prototype._subscribe.call(this, c);
    a && !a.closed && c.next(this._value);
    return a;
  };
  a.prototype.getValue = function() {
    if (this.hasError) {
      throw this.thrownError;
    }
    if (this.closed) {
      throw new module$output_core.ObjectUnsubscribedError;
    }
    return this._value;
  };
  a.prototype.next = function(c) {
    b.prototype.next.call(this, this._value = c);
  };
  return a;
}(module$output_core.Subject);
var Action$$module$output_core = function(b) {
  function a(c, a) {
    return b.call(this) || this;
  }
  __extends$$module$output_core(a, b);
  a.prototype.schedule = function(c, a) {
    return this;
  };
  return a;
}(module$output_core.Subscription), AsyncAction$$module$output_core = function(b) {
  function a(c, a) {
    var d = b.call(this, c, a) || this;
    d.scheduler = c;
    d.work = a;
    d.pending = !1;
    return d;
  }
  __extends$$module$output_core(a, b);
  a.prototype.schedule = function(c, a) {
    void 0 === a && (a = 0);
    if (this.closed) {
      return this;
    }
    this.state = c;
    c = this.id;
    var b = this.scheduler;
    null != c && (this.id = this.recycleAsyncId(b, c, a));
    this.pending = !0;
    this.delay = a;
    this.id = this.id || this.requestAsyncId(b, this.id, a);
    return this;
  };
  a.prototype.requestAsyncId = function(c, a, b) {
    void 0 === b && (b = 0);
    return setInterval(c.flush.bind(c, this), b);
  };
  a.prototype.recycleAsyncId = function(c, a, b) {
    void 0 === b && (b = 0);
    if (null !== b && this.delay === b && !1 === this.pending) {
      return a;
    }
    clearInterval(a);
  };
  a.prototype.execute = function(c, a) {
    if (this.closed) {
      return Error("executing a cancelled action");
    }
    this.pending = !1;
    if (c = this._execute(c, a)) {
      return c;
    }
    !1 === this.pending && null != this.id && (this.id = this.recycleAsyncId(this.scheduler, this.id, null));
  };
  a.prototype._execute = function(c, a) {
    a = !1;
    var b = void 0;
    try {
      this.work(c);
    } catch (f) {
      a = !0, b = !!f && f || Error(f);
    }
    if (a) {
      return this.unsubscribe(), b;
    }
  };
  a.prototype._unsubscribe = function() {
    var c = this.id, a = this.scheduler, b = a.actions, f = b.indexOf(this);
    this.state = this.work = null;
    this.pending = !1;
    this.scheduler = null;
    -1 !== f && b.splice(f, 1);
    null != c && (this.id = this.recycleAsyncId(a, c, null));
    this.delay = null;
  };
  return a;
}(Action$$module$output_core), QueueAction$$module$output_core = function(b) {
  function a(c, a) {
    var d = b.call(this, c, a) || this;
    d.scheduler = c;
    d.work = a;
    return d;
  }
  __extends$$module$output_core(a, b);
  a.prototype.schedule = function(c, a) {
    void 0 === a && (a = 0);
    if (0 < a) {
      return b.prototype.schedule.call(this, c, a);
    }
    this.delay = a;
    this.state = c;
    this.scheduler.flush(this);
    return this;
  };
  a.prototype.execute = function(c, a) {
    return 0 < a || this.closed ? b.prototype.execute.call(this, c, a) : this._execute(c, a);
  };
  a.prototype.requestAsyncId = function(a, d, e) {
    void 0 === e && (e = 0);
    return null !== e && 0 < e || null === e && 0 < this.delay ? b.prototype.requestAsyncId.call(this, a, d, e) : a.flush(this);
  };
  return a;
}(AsyncAction$$module$output_core);
module$output_core.Scheduler = function() {
  function b(a, c) {
    void 0 === c && (c = b.now);
    this.SchedulerAction = a;
    this.now = c;
  }
  b.prototype.schedule = function(a, c, b) {
    void 0 === c && (c = 0);
    return (new this.SchedulerAction(this, a)).schedule(b, c);
  };
  b.now = function() {
    return Date.now();
  };
  return b;
}();
var AsyncScheduler$$module$output_core = function(b) {
  function a(c, d) {
    void 0 === d && (d = module$output_core.Scheduler.now);
    var e = b.call(this, c, function() {
      return a.delegate && a.delegate !== e ? a.delegate.now() : d();
    }) || this;
    e.actions = [];
    e.active = !1;
    e.scheduled = void 0;
    return e;
  }
  __extends$$module$output_core(a, b);
  a.prototype.schedule = function(c, d, e) {
    void 0 === d && (d = 0);
    return a.delegate && a.delegate !== this ? a.delegate.schedule(c, d, e) : b.prototype.schedule.call(this, c, d, e);
  };
  a.prototype.flush = function(a) {
    var c = this.actions;
    if (this.active) {
      c.push(a);
    } else {
      var b;
      this.active = !0;
      do {
        if (b = a.execute(a.state, a.delay)) {
          break;
        }
      } while (a = c.shift());
      this.active = !1;
      if (b) {
        for (; a = c.shift();) {
          a.unsubscribe();
        }
        throw b;
      }
    }
  };
  return a;
}(module$output_core.Scheduler), QueueScheduler$$module$output_core = function(b) {
  function a() {
    return null !== b && b.apply(this, arguments) || this;
  }
  __extends$$module$output_core(a, b);
  return a;
}(AsyncScheduler$$module$output_core);
module$output_core.queueScheduler = new QueueScheduler$$module$output_core(QueueAction$$module$output_core);
module$output_core.EMPTY = new module$output_core.Observable(function(b) {
  return b.complete();
});
function emptyScheduled$$module$output_core(b) {
  return new module$output_core.Observable(function(a) {
    return b.schedule(function() {
      return a.complete();
    });
  });
}
function isScheduler$$module$output_core(b) {
  return b && "function" === typeof b.schedule;
}
var subscribeToArray$$module$output_core = function(b) {
  return function(a) {
    for (var c = 0, d = b.length; c < d && !a.closed; c++) {
      a.next(b[c]);
    }
    a.closed || a.complete();
  };
};
function fromArray$$module$output_core(b, a) {
  return a ? new module$output_core.Observable(function(c) {
    var d = new module$output_core.Subscription, e = 0;
    d.add(a.schedule(function() {
      e === b.length ? c.complete() : (c.next(b[e++]), c.closed || d.add(this.schedule()));
    }));
    return d;
  }) : new module$output_core.Observable(subscribeToArray$$module$output_core(b));
}
function scalar$$module$output_core(b) {
  var a = new module$output_core.Observable(function(a) {
    a.next(b);
    a.complete();
  });
  a._isScalar = !0;
  a.value = b;
  return a;
}
function dispatch$$module$output_core(b) {
  b.subscriber.error(b.error);
}
module$output_core.Notification = function() {
  function b(a, c, b) {
    this.kind = a;
    this.value = c;
    this.error = b;
    this.hasValue = "N" === a;
  }
  b.prototype.observe = function(a) {
    switch(this.kind) {
      case "N":
        return a.next && a.next(this.value);
      case "E":
        return a.error && a.error(this.error);
      case "C":
        return a.complete && a.complete();
    }
  };
  b.prototype.do = function(a, c, b) {
    switch(this.kind) {
      case "N":
        return a && a(this.value);
      case "E":
        return c && c(this.error);
      case "C":
        return b && b();
    }
  };
  b.prototype.accept = function(a, c, b) {
    return a && "function" === typeof a.next ? this.observe(a) : this.do(a, c, b);
  };
  b.prototype.toObservable = function() {
    switch(this.kind) {
      case "N":
        return (0,module$output_core.of)(this.value);
      case "E":
        return (0,module$output_core.throwError)(this.error);
      case "C":
        return (0,module$output_core.empty)();
    }
    throw Error("unexpected notification kind value");
  };
  b.createNext = function(a) {
    return "undefined" !== typeof a ? new b("N", a) : b.undefinedValueNotification;
  };
  b.createError = function(a) {
    return new b("E", void 0, a);
  };
  b.createComplete = function() {
    return b.completeNotification;
  };
  b.completeNotification = new b("C");
  b.undefinedValueNotification = new b("N", void 0);
  return b;
}();
var ObserveOnOperator$$module$output_core = function() {
  function b(a, c) {
    void 0 === c && (c = 0);
    this.scheduler = a;
    this.delay = c;
  }
  b.prototype.call = function(a, c) {
    return c.subscribe(new ObserveOnSubscriber$$module$output_core(a, this.scheduler, this.delay));
  };
  return b;
}(), ObserveOnSubscriber$$module$output_core = function(b) {
  function a(a, d, e) {
    void 0 === e && (e = 0);
    a = b.call(this, a) || this;
    a.scheduler = d;
    a.delay = e;
    return a;
  }
  __extends$$module$output_core(a, b);
  a.dispatch = function(a) {
    a.notification.observe(a.destination);
    this.unsubscribe();
  };
  a.prototype.scheduleMessage = function(c) {
    this.destination.add(this.scheduler.schedule(a.dispatch, this.delay, new ObserveOnMessage$$module$output_core(c, this.destination)));
  };
  a.prototype._next = function(a) {
    this.scheduleMessage(module$output_core.Notification.createNext(a));
  };
  a.prototype._error = function(a) {
    this.scheduleMessage(module$output_core.Notification.createError(a));
    this.unsubscribe();
  };
  a.prototype._complete = function() {
    this.scheduleMessage(module$output_core.Notification.createComplete());
    this.unsubscribe();
  };
  return a;
}(module$output_core.Subscriber), ObserveOnMessage$$module$output_core = function() {
  return function(b, a) {
    this.notification = b;
    this.destination = a;
  };
}();
module$output_core.ReplaySubject = function(b) {
  function a(a, d, e) {
    void 0 === a && (a = Number.POSITIVE_INFINITY);
    void 0 === d && (d = Number.POSITIVE_INFINITY);
    var c = b.call(this) || this;
    c.scheduler = e;
    c._events = [];
    c._infiniteTimeWindow = !1;
    c._bufferSize = 1 > a ? 1 : a;
    c._windowTime = 1 > d ? 1 : d;
    d === Number.POSITIVE_INFINITY ? (c._infiniteTimeWindow = !0, c.next = c.nextInfiniteTimeWindow) : c.next = c.nextTimeWindow;
    return c;
  }
  __extends$$module$output_core(a, b);
  a.prototype.nextInfiniteTimeWindow = function(a) {
    var c = this._events;
    c.push(a);
    c.length > this._bufferSize && c.shift();
    b.prototype.next.call(this, a);
  };
  a.prototype.nextTimeWindow = function(a) {
    this._events.push(new ReplayEvent$$module$output_core(this._getNow(), a));
    this._trimBufferThenGetEvents();
    b.prototype.next.call(this, a);
  };
  a.prototype._subscribe = function(a) {
    var c = this._infiniteTimeWindow, b = c ? this._events : this._trimBufferThenGetEvents(), f = this.scheduler, g = b.length;
    if (this.closed) {
      throw new module$output_core.ObjectUnsubscribedError;
    }
    if (this.isStopped || this.hasError) {
      var h = module$output_core.Subscription.EMPTY;
    } else {
      this.observers.push(a), h = new SubjectSubscription$$module$output_core(this, a);
    }
    f && a.add(a = new ObserveOnSubscriber$$module$output_core(a, f));
    if (c) {
      for (c = 0; c < g && !a.closed; c++) {
        a.next(b[c]);
      }
    } else {
      for (c = 0; c < g && !a.closed; c++) {
        a.next(b[c].value);
      }
    }
    this.hasError ? a.error(this.thrownError) : this.isStopped && a.complete();
    return h;
  };
  a.prototype._getNow = function() {
    return (this.scheduler || module$output_core.queueScheduler).now();
  };
  a.prototype._trimBufferThenGetEvents = function() {
    for (var a = this._getNow(), b = this._bufferSize, e = this._windowTime, f = this._events, g = f.length, h = 0; h < g && !(a - f[h].time < e);) {
      h++;
    }
    g > b && (h = Math.max(h, g - b));
    0 < h && f.splice(0, h);
    return f;
  };
  return a;
}(module$output_core.Subject);
var ReplayEvent$$module$output_core = function() {
  return function(b, a) {
    this.time = b;
    this.value = a;
  };
}();
module$output_core.AsyncSubject = function(b) {
  function a() {
    var a = null !== b && b.apply(this, arguments) || this;
    a.value = null;
    a.hasNext = !1;
    a.hasCompleted = !1;
    return a;
  }
  __extends$$module$output_core(a, b);
  a.prototype._subscribe = function(a) {
    return this.hasError ? (a.error(this.thrownError), module$output_core.Subscription.EMPTY) : this.hasCompleted && this.hasNext ? (a.next(this.value), a.complete(), module$output_core.Subscription.EMPTY) : b.prototype._subscribe.call(this, a);
  };
  a.prototype.next = function(a) {
    this.hasCompleted || (this.value = a, this.hasNext = !0);
  };
  a.prototype.error = function(a) {
    this.hasCompleted || b.prototype.error.call(this, a);
  };
  a.prototype.complete = function() {
    this.hasCompleted = !0;
    this.hasNext && b.prototype.next.call(this, this.value);
    b.prototype.complete.call(this);
  };
  return a;
}(module$output_core.Subject);
var nextHandle$$module$output_core = 1, tasksByHandle$$module$output_core = {};
function runIfPresent$$module$output_core(b) {
  (b = tasksByHandle$$module$output_core[b]) && b();
}
var Immediate$$module$output_core = {setImmediate:function(b) {
  var a = nextHandle$$module$output_core++;
  tasksByHandle$$module$output_core[a] = b;
  Promise.resolve().then(function() {
    return runIfPresent$$module$output_core(a);
  });
  return a;
}, clearImmediate:function(b) {
  delete tasksByHandle$$module$output_core[b];
}}, AsapAction$$module$output_core = function(b) {
  function a(a, d) {
    var c = b.call(this, a, d) || this;
    c.scheduler = a;
    c.work = d;
    return c;
  }
  __extends$$module$output_core(a, b);
  a.prototype.requestAsyncId = function(a, d, e) {
    void 0 === e && (e = 0);
    if (null !== e && 0 < e) {
      return b.prototype.requestAsyncId.call(this, a, d, e);
    }
    a.actions.push(this);
    return a.scheduled || (a.scheduled = Immediate$$module$output_core.setImmediate(a.flush.bind(a, null)));
  };
  a.prototype.recycleAsyncId = function(a, d, e) {
    void 0 === e && (e = 0);
    if (null !== e && 0 < e || null === e && 0 < this.delay) {
      return b.prototype.recycleAsyncId.call(this, a, d, e);
    }
    0 === a.actions.length && (Immediate$$module$output_core.clearImmediate(d), a.scheduled = void 0);
  };
  return a;
}(AsyncAction$$module$output_core), AsapScheduler$$module$output_core = function(b) {
  function a() {
    return null !== b && b.apply(this, arguments) || this;
  }
  __extends$$module$output_core(a, b);
  a.prototype.flush = function(a) {
    this.active = !0;
    this.scheduled = void 0;
    var c = this.actions, b, f = -1, g = c.length;
    a = a || c.shift();
    do {
      if (b = a.execute(a.state, a.delay)) {
        break;
      }
    } while (++f < g && (a = c.shift()));
    this.active = !1;
    if (b) {
      for (; ++f < g && (a = c.shift());) {
        a.unsubscribe();
      }
      throw b;
    }
  };
  return a;
}(AsyncScheduler$$module$output_core);
module$output_core.asapScheduler = new AsapScheduler$$module$output_core(AsapAction$$module$output_core);
module$output_core.asyncScheduler = new AsyncScheduler$$module$output_core(AsyncAction$$module$output_core);
var AnimationFrameAction$$module$output_core = function(b) {
  function a(a, d) {
    var c = b.call(this, a, d) || this;
    c.scheduler = a;
    c.work = d;
    return c;
  }
  __extends$$module$output_core(a, b);
  a.prototype.requestAsyncId = function(a, d, e) {
    void 0 === e && (e = 0);
    if (null !== e && 0 < e) {
      return b.prototype.requestAsyncId.call(this, a, d, e);
    }
    a.actions.push(this);
    return a.scheduled || (a.scheduled = requestAnimationFrame(function() {
      return a.flush(null);
    }));
  };
  a.prototype.recycleAsyncId = function(a, d, e) {
    void 0 === e && (e = 0);
    if (null !== e && 0 < e || null === e && 0 < this.delay) {
      return b.prototype.recycleAsyncId.call(this, a, d, e);
    }
    0 === a.actions.length && (cancelAnimationFrame(d), a.scheduled = void 0);
  };
  return a;
}(AsyncAction$$module$output_core), AnimationFrameScheduler$$module$output_core = function(b) {
  function a() {
    return null !== b && b.apply(this, arguments) || this;
  }
  __extends$$module$output_core(a, b);
  a.prototype.flush = function(a) {
    this.active = !0;
    this.scheduled = void 0;
    var c = this.actions, b, f = -1, g = c.length;
    a = a || c.shift();
    do {
      if (b = a.execute(a.state, a.delay)) {
        break;
      }
    } while (++f < g && (a = c.shift()));
    this.active = !1;
    if (b) {
      for (; ++f < g && (a = c.shift());) {
        a.unsubscribe();
      }
      throw b;
    }
  };
  return a;
}(AsyncScheduler$$module$output_core);
module$output_core.animationFrameScheduler = new AnimationFrameScheduler$$module$output_core(AnimationFrameAction$$module$output_core);
module$output_core.VirtualTimeScheduler = function(b) {
  function a(a, d) {
    void 0 === a && (a = module$output_core.VirtualAction);
    void 0 === d && (d = Number.POSITIVE_INFINITY);
    var c = b.call(this, a, function() {
      return c.frame;
    }) || this;
    c.maxFrames = d;
    c.frame = 0;
    c.index = -1;
    return c;
  }
  __extends$$module$output_core(a, b);
  a.prototype.flush = function() {
    for (var a = this.actions, b = this.maxFrames, e, f; (f = a.shift()) && (this.frame = f.delay) <= b && !(e = f.execute(f.state, f.delay));) {
    }
    if (e) {
      for (; f = a.shift();) {
        f.unsubscribe();
      }
      throw e;
    }
  };
  a.frameTimeFactor = 10;
  return a;
}(AsyncScheduler$$module$output_core);
module$output_core.VirtualAction = function(b) {
  function a(a, d, e) {
    void 0 === e && (e = a.index += 1);
    var c = b.call(this, a, d) || this;
    c.scheduler = a;
    c.work = d;
    c.index = e;
    c.active = !0;
    c.index = a.index = e;
    return c;
  }
  __extends$$module$output_core(a, b);
  a.prototype.schedule = function(c, d) {
    void 0 === d && (d = 0);
    if (!this.id) {
      return b.prototype.schedule.call(this, c, d);
    }
    this.active = !1;
    var e = new a(this.scheduler, this.work);
    this.add(e);
    return e.schedule(c, d);
  };
  a.prototype.requestAsyncId = function(c, b, e) {
    void 0 === e && (e = 0);
    this.delay = c.frame + e;
    c = c.actions;
    c.push(this);
    c.sort(a.sortActions);
    return !0;
  };
  a.prototype.recycleAsyncId = function(a, b, e) {
  };
  a.prototype._execute = function(a, d) {
    if (!0 === this.active) {
      return b.prototype._execute.call(this, a, d);
    }
  };
  a.sortActions = function(a, b) {
    return a.delay === b.delay ? a.index === b.index ? 0 : a.index > b.index ? 1 : -1 : a.delay > b.delay ? 1 : -1;
  };
  return a;
}(AsyncAction$$module$output_core);
function ArgumentOutOfRangeErrorImpl$$module$output_core() {
  Error.call(this);
  this.message = "argument out of range";
  this.name = "ArgumentOutOfRangeError";
  return this;
}
ArgumentOutOfRangeErrorImpl$$module$output_core.prototype = Object.create(Error.prototype);
module$output_core.ArgumentOutOfRangeError = ArgumentOutOfRangeErrorImpl$$module$output_core;
function EmptyErrorImpl$$module$output_core() {
  Error.call(this);
  this.message = "no elements in sequence";
  this.name = "EmptyError";
  return this;
}
EmptyErrorImpl$$module$output_core.prototype = Object.create(Error.prototype);
module$output_core.EmptyError = EmptyErrorImpl$$module$output_core;
function TimeoutErrorImpl$$module$output_core() {
  Error.call(this);
  this.message = "Timeout has occurred";
  this.name = "TimeoutError";
  return this;
}
TimeoutErrorImpl$$module$output_core.prototype = Object.create(Error.prototype);
module$output_core.TimeoutError = TimeoutErrorImpl$$module$output_core;
function map$$module$output_core(b, a) {
  return function(c) {
    if ("function" !== typeof b) {
      throw new TypeError("argument is not a function. Are you looking for `mapTo()`?");
    }
    return c.lift(new MapOperator$$module$output_core(b, a));
  };
}
var MapOperator$$module$output_core = function() {
  function b(a, c) {
    this.project = a;
    this.thisArg = c;
  }
  b.prototype.call = function(a, c) {
    return c.subscribe(new MapSubscriber$$module$output_core(a, this.project, this.thisArg));
  };
  return b;
}(), MapSubscriber$$module$output_core = function(b) {
  function a(a, d, e) {
    a = b.call(this, a) || this;
    a.project = d;
    a.count = 0;
    a.thisArg = e || a;
    return a;
  }
  __extends$$module$output_core(a, b);
  a.prototype._next = function(a) {
    try {
      var c = this.project.call(this.thisArg, a, this.count++);
    } catch (e) {
      this.destination.error(e);
      return;
    }
    this.destination.next(c);
  };
  return a;
}(module$output_core.Subscriber);
function dispatch$1$$module$output_core(b) {
  var a = this, c = b.args, d = b.subscriber, e = b.params;
  b = e.callbackFunc;
  var f = e.context, g = e.scheduler, h = e.subject;
  if (!h) {
    h = e.subject = new module$output_core.AsyncSubject;
    e = function() {
      for (var c = [], b = 0; b < arguments.length; b++) {
        c[b] = arguments[b];
      }
      a.add(g.schedule(dispatchNext$$module$output_core, 0, {value:1 >= c.length ? c[0] : c, subject:h}));
    };
    try {
      b.apply(f, c.concat([e]));
    } catch (k) {
      h.error(k);
    }
  }
  this.add(h.subscribe(d));
}
function dispatchNext$$module$output_core(b) {
  var a = b.subject;
  a.next(b.value);
  a.complete();
}
function dispatch$2$$module$output_core(b) {
  var a = this, c = b.params, d = b.subscriber;
  b = b.context;
  var e = c.callbackFunc, f = c.args, g = c.scheduler, h = c.subject;
  if (!h) {
    h = c.subject = new module$output_core.AsyncSubject;
    c = function() {
      for (var c = [], b = 0; b < arguments.length; b++) {
        c[b] = arguments[b];
      }
      (b = c.shift()) ? a.add(g.schedule(dispatchError$1$$module$output_core, 0, {err:b, subject:h})) : a.add(g.schedule(dispatchNext$1$$module$output_core, 0, {value:1 >= c.length ? c[0] : c, subject:h}));
    };
    try {
      e.apply(b, f.concat([c]));
    } catch (k) {
      this.add(g.schedule(dispatchError$1$$module$output_core, 0, {err:k, subject:h}));
    }
  }
  this.add(h.subscribe(d));
}
function dispatchNext$1$$module$output_core(b) {
  var a = b.subject;
  a.next(b.value);
  a.complete();
}
function dispatchError$1$$module$output_core(b) {
  b.subject.error(b.err);
}
var OuterSubscriber$$module$output_core = function(b) {
  function a() {
    return null !== b && b.apply(this, arguments) || this;
  }
  __extends$$module$output_core(a, b);
  a.prototype.notifyNext = function(a, b, e, f, g) {
    this.destination.next(b);
  };
  a.prototype.notifyError = function(a, b) {
    this.destination.error(a);
  };
  a.prototype.notifyComplete = function(a) {
    this.destination.complete();
  };
  return a;
}(module$output_core.Subscriber), InnerSubscriber$$module$output_core = function(b) {
  function a(a, d, e) {
    var c = b.call(this) || this;
    c.parent = a;
    c.outerValue = d;
    c.outerIndex = e;
    c.index = 0;
    return c;
  }
  __extends$$module$output_core(a, b);
  a.prototype._next = function(a) {
    this.parent.notifyNext(this.outerValue, a, this.outerIndex, this.index++, this);
  };
  a.prototype._error = function(a) {
    this.parent.notifyError(a, this);
    this.unsubscribe();
  };
  a.prototype._complete = function() {
    this.parent.notifyComplete(this);
    this.unsubscribe();
  };
  return a;
}(module$output_core.Subscriber), subscribeToPromise$$module$output_core = function(b) {
  return function(a) {
    b.then(function(c) {
      a.closed || (a.next(c), a.complete());
    }, function(c) {
      return a.error(c);
    }).then(null, hostReportError$$module$output_core);
    return a;
  };
};
function getSymbolIterator$$module$output_core() {
  return "function" === typeof Symbol && Symbol.iterator ? Symbol.iterator : "@@iterator";
}
var iterator$$module$output_core = getSymbolIterator$$module$output_core(), subscribeToIterable$$module$output_core = function(b) {
  return function(a) {
    var c = b[iterator$$module$output_core]();
    do {
      var d = c.next();
      if (d.done) {
        a.complete();
        break;
      }
      a.next(d.value);
      if (a.closed) {
        break;
      }
    } while (1);
    "function" === typeof c.return && a.add(function() {
      c.return && c.return();
    });
    return a;
  };
}, subscribeToObservable$$module$output_core = function(b) {
  return function(a) {
    var c = b[module$output_core.observable]();
    if ("function" !== typeof c.subscribe) {
      throw new TypeError("Provided object does not correctly implement Symbol.observable");
    }
    return c.subscribe(a);
  };
}, isArrayLike$$module$output_core = function(b) {
  return b && "number" === typeof b.length && "function" !== typeof b;
};
function isPromise$$module$output_core(b) {
  return b && "function" !== typeof b.subscribe && "function" === typeof b.then;
}
var subscribeTo$$module$output_core = function(b) {
  if (b instanceof module$output_core.Observable) {
    return function(a) {
      if (b._isScalar) {
        a.next(b.value), a.complete();
      } else {
        return b.subscribe(a);
      }
    };
  }
  if (b && "function" === typeof b[module$output_core.observable]) {
    return subscribeToObservable$$module$output_core(b);
  }
  if (isArrayLike$$module$output_core(b)) {
    return subscribeToArray$$module$output_core(b);
  }
  if (isPromise$$module$output_core(b)) {
    return subscribeToPromise$$module$output_core(b);
  }
  if (b && "function" === typeof b[iterator$$module$output_core]) {
    return subscribeToIterable$$module$output_core(b);
  }
  var a = isObject$$module$output_core(b) ? "an invalid object" : "'" + b + "'";
  throw new TypeError("You provided " + a + " where a stream was expected. You can provide an Observable, Promise, Array, or Iterable.");
};
function subscribeToResult$$module$output_core(b, a, c, d, e) {
  void 0 === e && (e = new InnerSubscriber$$module$output_core(b, c, d));
  if (!e.closed) {
    return subscribeTo$$module$output_core(a)(e);
  }
}
var NONE$$module$output_core = {}, CombineLatestOperator$$module$output_core = function() {
  function b(a) {
    this.resultSelector = a;
  }
  b.prototype.call = function(a, c) {
    return c.subscribe(new CombineLatestSubscriber$$module$output_core(a, this.resultSelector));
  };
  return b;
}(), CombineLatestSubscriber$$module$output_core = function(b) {
  function a(a, d) {
    a = b.call(this, a) || this;
    a.resultSelector = d;
    a.active = 0;
    a.values = [];
    a.observables = [];
    return a;
  }
  __extends$$module$output_core(a, b);
  a.prototype._next = function(a) {
    this.values.push(NONE$$module$output_core);
    this.observables.push(a);
  };
  a.prototype._complete = function() {
    var a = this.observables, b = a.length;
    if (0 === b) {
      this.destination.complete();
    } else {
      this.toRespond = this.active = b;
      for (var e = 0; e < b; e++) {
        var f = a[e];
        this.add(subscribeToResult$$module$output_core(this, f, f, e));
      }
    }
  };
  a.prototype.notifyComplete = function(a) {
    0 === --this.active && this.destination.complete();
  };
  a.prototype.notifyNext = function(a, b, e, f, g) {
    a = this.values;
    f = a[e];
    f = this.toRespond ? f === NONE$$module$output_core ? --this.toRespond : this.toRespond : 0;
    a[e] = b;
    0 === f && (this.resultSelector ? this._tryResultSelector(a) : this.destination.next(a.slice()));
  };
  a.prototype._tryResultSelector = function(a) {
    try {
      var b = this.resultSelector.apply(this, a);
    } catch (e) {
      this.destination.error(e);
      return;
    }
    this.destination.next(b);
  };
  return a;
}(OuterSubscriber$$module$output_core);
function isInteropObservable$$module$output_core(b) {
  return b && "function" === typeof b[module$output_core.observable];
}
function isIterable$$module$output_core(b) {
  return b && "function" === typeof b[iterator$$module$output_core];
}
function fromPromise$$module$output_core(b, a) {
  return a ? new module$output_core.Observable(function(c) {
    var d = new module$output_core.Subscription;
    d.add(a.schedule(function() {
      return b.then(function(b) {
        d.add(a.schedule(function() {
          c.next(b);
          d.add(a.schedule(function() {
            return c.complete();
          }));
        }));
      }, function(b) {
        d.add(a.schedule(function() {
          return c.error(b);
        }));
      });
    }));
    return d;
  }) : new module$output_core.Observable(subscribeToPromise$$module$output_core(b));
}
function fromIterable$$module$output_core(b, a) {
  if (!b) {
    throw Error("Iterable cannot be null");
  }
  return a ? new module$output_core.Observable(function(c) {
    var d = new module$output_core.Subscription, e;
    d.add(function() {
      e && "function" === typeof e.return && e.return();
    });
    d.add(a.schedule(function() {
      e = b[iterator$$module$output_core]();
      d.add(a.schedule(function() {
        if (!c.closed) {
          try {
            var a = e.next();
            var b = a.value;
            var d = a.done;
          } catch (k) {
            c.error(k);
            return;
          }
          d ? c.complete() : (c.next(b), this.schedule());
        }
      }));
    }));
    return d;
  }) : new module$output_core.Observable(subscribeToIterable$$module$output_core(b));
}
function fromObservable$$module$output_core(b, a) {
  return a ? new module$output_core.Observable(function(c) {
    var d = new module$output_core.Subscription;
    d.add(a.schedule(function() {
      var e = b[module$output_core.observable]();
      d.add(e.subscribe({next:function(b) {
        d.add(a.schedule(function() {
          return c.next(b);
        }));
      }, error:function(b) {
        d.add(a.schedule(function() {
          return c.error(b);
        }));
      }, complete:function() {
        d.add(a.schedule(function() {
          return c.complete();
        }));
      }}));
    }));
    return d;
  }) : new module$output_core.Observable(subscribeToObservable$$module$output_core(b));
}
function mergeMap$$module$output_core(b, a, c) {
  void 0 === c && (c = Number.POSITIVE_INFINITY);
  if ("function" === typeof a) {
    return function(d) {
      return d.pipe(mergeMap$$module$output_core(function(c, d) {
        return (0,module$output_core.from)(b(c, d)).pipe(map$$module$output_core(function(b, e) {
          return a(c, b, d, e);
        }));
      }, c));
    };
  }
  "number" === typeof a && (c = a);
  return function(a) {
    return a.lift(new MergeMapOperator$$module$output_core(b, c));
  };
}
var MergeMapOperator$$module$output_core = function() {
  function b(a, b) {
    void 0 === b && (b = Number.POSITIVE_INFINITY);
    this.project = a;
    this.concurrent = b;
  }
  b.prototype.call = function(a, b) {
    return b.subscribe(new MergeMapSubscriber$$module$output_core(a, this.project, this.concurrent));
  };
  return b;
}(), MergeMapSubscriber$$module$output_core = function(b) {
  function a(a, d, e) {
    void 0 === e && (e = Number.POSITIVE_INFINITY);
    a = b.call(this, a) || this;
    a.project = d;
    a.concurrent = e;
    a.hasCompleted = !1;
    a.buffer = [];
    a.active = 0;
    a.index = 0;
    return a;
  }
  __extends$$module$output_core(a, b);
  a.prototype._next = function(a) {
    this.active < this.concurrent ? this._tryNext(a) : this.buffer.push(a);
  };
  a.prototype._tryNext = function(a) {
    var b = this.index++;
    try {
      var c = this.project(a, b);
    } catch (f) {
      this.destination.error(f);
      return;
    }
    this.active++;
    this._innerSub(c, a, b);
  };
  a.prototype._innerSub = function(a, b, e) {
    var c = new InnerSubscriber$$module$output_core(this, void 0, void 0);
    this.destination.add(c);
    subscribeToResult$$module$output_core(this, a, b, e, c);
  };
  a.prototype._complete = function() {
    this.hasCompleted = !0;
    0 === this.active && 0 === this.buffer.length && this.destination.complete();
    this.unsubscribe();
  };
  a.prototype.notifyNext = function(a, b, e, f, g) {
    this.destination.next(b);
  };
  a.prototype.notifyComplete = function(a) {
    var b = this.buffer;
    this.remove(a);
    this.active--;
    0 < b.length ? this._next(b.shift()) : 0 === this.active && this.hasCompleted && this.destination.complete();
  };
  return a;
}(OuterSubscriber$$module$output_core);
function mergeAll$$module$output_core(b) {
  void 0 === b && (b = Number.POSITIVE_INFINITY);
  return mergeMap$$module$output_core(module$output_core.identity, b);
}
function concatAll$$module$output_core() {
  return mergeAll$$module$output_core(1);
}
var ForkJoinSubscriber$$module$output_core = function(b) {
  function a(a, d) {
    a = b.call(this, a) || this;
    a.sources = d;
    a.completed = 0;
    a.haveValues = 0;
    var c = d.length;
    a.values = Array(c);
    for (var f = 0; f < c; f++) {
      var g = subscribeToResult$$module$output_core(a, d[f], null, f);
      g && a.add(g);
    }
    return a;
  }
  __extends$$module$output_core(a, b);
  a.prototype.notifyNext = function(a, b, e, f, g) {
    this.values[e] = b;
    g._hasValue || (g._hasValue = !0, this.haveValues++);
  };
  a.prototype.notifyComplete = function(a) {
    var b = this.destination, c = this.haveValues, f = this.values, g = f.length;
    a._hasValue ? (this.completed++, this.completed === g && (c === g && b.next(f), b.complete())) : b.complete();
  };
  return a;
}(OuterSubscriber$$module$output_core);
function setupSubscription$$module$output_core(b, a, c, d, e) {
  if (isEventTarget$$module$output_core(b)) {
    b.addEventListener(a, c, e);
    var f = function() {
      return b.removeEventListener(a, c, e);
    };
  } else {
    if (isJQueryStyleEventEmitter$$module$output_core(b)) {
      b.on(a, c), f = function() {
        return b.off(a, c);
      };
    } else {
      if (isNodeStyleEventEmitter$$module$output_core(b)) {
        b.addListener(a, c), f = function() {
          return b.removeListener(a, c);
        };
      } else {
        if (b && b.length) {
          for (var g = 0, h = b.length; g < h; g++) {
            setupSubscription$$module$output_core(b[g], a, c, d, e);
          }
        } else {
          throw new TypeError("Invalid event target");
        }
      }
    }
  }
  d.add(f);
}
function isNodeStyleEventEmitter$$module$output_core(b) {
  return b && "function" === typeof b.addListener && "function" === typeof b.removeListener;
}
function isJQueryStyleEventEmitter$$module$output_core(b) {
  return b && "function" === typeof b.on && "function" === typeof b.off;
}
function isEventTarget$$module$output_core(b) {
  return b && "function" === typeof b.addEventListener && "function" === typeof b.removeEventListener;
}
function dispatch$3$$module$output_core(b) {
  var a = b.subscriber, c = b.condition;
  if (!a.closed) {
    if (b.needIterate) {
      try {
        b.state = b.iterate(b.state);
      } catch (f) {
        a.error(f);
        return;
      }
    } else {
      b.needIterate = !0;
    }
    if (c) {
      var d = void 0;
      try {
        d = c(b.state);
      } catch (f) {
        a.error(f);
        return;
      }
      if (!d) {
        a.complete();
        return;
      }
      if (a.closed) {
        return;
      }
    }
    try {
      var e = b.resultSelector(b.state);
    } catch (f) {
      a.error(f);
      return;
    }
    if (!a.closed && (a.next(e), !a.closed)) {
      return this.schedule(b);
    }
  }
}
function isNumeric$$module$output_core(b) {
  return !isArray$$module$output_core(b) && 0 <= b - parseFloat(b) + 1;
}
function dispatch$4$$module$output_core(b) {
  var a = b.subscriber, c = b.counter;
  b = b.period;
  a.next(c);
  this.schedule({subscriber:a, counter:c + 1, period:b}, b);
}
module$output_core.NEVER = new module$output_core.Observable(module$output_core.noop);
function dispatch$5$$module$output_core(b) {
  var a = b.keys, c = b.index, d = b.subscriber, e = b.subscription;
  b = b.obj;
  if (!d.closed) {
    if (c < a.length) {
      var f = a[c];
      d.next([f, b[f]]);
      e.add(this.schedule({keys:a, index:c + 1, subscriber:d, subscription:e, obj:b}));
    } else {
      d.complete();
    }
  }
}
var RaceOperator$$module$output_core = function() {
  function b() {
  }
  b.prototype.call = function(a, b) {
    return b.subscribe(new RaceSubscriber$$module$output_core(a));
  };
  return b;
}(), RaceSubscriber$$module$output_core = function(b) {
  function a(a) {
    a = b.call(this, a) || this;
    a.hasFirst = !1;
    a.observables = [];
    a.subscriptions = [];
    return a;
  }
  __extends$$module$output_core(a, b);
  a.prototype._next = function(a) {
    this.observables.push(a);
  };
  a.prototype._complete = function() {
    var a = this.observables, b = a.length;
    if (0 === b) {
      this.destination.complete();
    } else {
      for (var e = 0; e < b && !this.hasFirst; e++) {
        var f = a[e];
        f = subscribeToResult$$module$output_core(this, f, f, e);
        this.subscriptions && this.subscriptions.push(f);
        this.add(f);
      }
      this.observables = null;
    }
  };
  a.prototype.notifyNext = function(a, b, e, f, g) {
    if (!this.hasFirst) {
      this.hasFirst = !0;
      for (a = 0; a < this.subscriptions.length; a++) {
        a !== e && (f = this.subscriptions[a], f.unsubscribe(), this.remove(f));
      }
      this.subscriptions = null;
    }
    this.destination.next(b);
  };
  return a;
}(OuterSubscriber$$module$output_core);
function dispatch$6$$module$output_core(b) {
  var a = b.start, c = b.index, d = b.subscriber;
  c >= b.count ? d.complete() : (d.next(a), d.closed || (b.index = c + 1, b.start = a + 1, this.schedule(b)));
}
function dispatch$7$$module$output_core(b) {
  var a = b.index, c = b.period, d = b.subscriber;
  d.next(a);
  if (!d.closed) {
    if (-1 === c) {
      return d.complete();
    }
    b.index = a + 1;
    this.schedule(b, c);
  }
}
var ZipOperator$$module$output_core = function() {
  function b(a) {
    this.resultSelector = a;
  }
  b.prototype.call = function(a, b) {
    return b.subscribe(new ZipSubscriber$$module$output_core(a, this.resultSelector));
  };
  return b;
}(), ZipSubscriber$$module$output_core = function(b) {
  function a(a, d, e) {
    void 0 === e && (e = Object.create(null));
    a = b.call(this, a) || this;
    a.iterators = [];
    a.active = 0;
    a.resultSelector = "function" === typeof d ? d : null;
    a.values = e;
    return a;
  }
  __extends$$module$output_core(a, b);
  a.prototype._next = function(a) {
    var b = this.iterators;
    isArray$$module$output_core(a) ? b.push(new StaticArrayIterator$$module$output_core(a)) : "function" === typeof a[iterator$$module$output_core] ? b.push(new StaticIterator$$module$output_core(a[iterator$$module$output_core]())) : b.push(new ZipBufferIterator$$module$output_core(this.destination, this, a));
  };
  a.prototype._complete = function() {
    var a = this.iterators, b = a.length;
    this.unsubscribe();
    if (0 === b) {
      this.destination.complete();
    } else {
      this.active = b;
      for (var e = 0; e < b; e++) {
        var f = a[e];
        f.stillUnsubscribed ? this.destination.add(f.subscribe(f, e)) : this.active--;
      }
    }
  };
  a.prototype.notifyInactive = function() {
    this.active--;
    0 === this.active && this.destination.complete();
  };
  a.prototype.checkIterators = function() {
    for (var a = this.iterators, b = a.length, e = this.destination, f = 0; f < b; f++) {
      var g = a[f];
      if ("function" === typeof g.hasValue && !g.hasValue()) {
        return;
      }
    }
    var h = !1, k = [];
    for (f = 0; f < b; f++) {
      g = a[f];
      var m = g.next();
      g.hasCompleted() && (h = !0);
      if (m.done) {
        e.complete();
        return;
      }
      k.push(m.value);
    }
    this.resultSelector ? this._tryresultSelector(k) : e.next(k);
    h && e.complete();
  };
  a.prototype._tryresultSelector = function(a) {
    try {
      var b = this.resultSelector.apply(this, a);
    } catch (e) {
      this.destination.error(e);
      return;
    }
    this.destination.next(b);
  };
  return a;
}(module$output_core.Subscriber), StaticIterator$$module$output_core = function() {
  function b(a) {
    this.iterator = a;
    this.nextResult = a.next();
  }
  b.prototype.hasValue = function() {
    return !0;
  };
  b.prototype.next = function() {
    var a = this.nextResult;
    this.nextResult = this.iterator.next();
    return a;
  };
  b.prototype.hasCompleted = function() {
    var a = this.nextResult;
    return a && a.done;
  };
  return b;
}(), StaticArrayIterator$$module$output_core = function() {
  function b(a) {
    this.array = a;
    this.length = this.index = 0;
    this.length = a.length;
  }
  b.prototype[iterator$$module$output_core] = function() {
    return this;
  };
  b.prototype.next = function(a) {
    a = this.index++;
    var b = this.array;
    return a < this.length ? {value:b[a], done:!1} : {value:null, done:!0};
  };
  b.prototype.hasValue = function() {
    return this.array.length > this.index;
  };
  b.prototype.hasCompleted = function() {
    return this.array.length === this.index;
  };
  return b;
}(), ZipBufferIterator$$module$output_core = function(b) {
  function a(a, d, e) {
    a = b.call(this, a) || this;
    a.parent = d;
    a.observable = e;
    a.stillUnsubscribed = !0;
    a.buffer = [];
    a.isComplete = !1;
    return a;
  }
  __extends$$module$output_core(a, b);
  a.prototype[iterator$$module$output_core] = function() {
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
  a.prototype.notifyNext = function(a, b, e, f, g) {
    this.buffer.push(b);
    this.parent.checkIterators();
  };
  a.prototype.subscribe = function(a, b) {
    return subscribeToResult$$module$output_core(this, this.observable, this, b);
  };
  return a;
}(OuterSubscriber$$module$output_core);

