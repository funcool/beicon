/**
 * @const
 */
var Rx = function() {};

/**
 * @constructor
 */
Rx.Observable = function() {};

/**
 * @constructor
 * @extends {Rx.Observable}
 */
Rx.Subject = function() {};

/**
 * @constructor
 * @extends {Rx.Subject}
 */
Rx.BehaviorSubject = function() {};

/**
 * @constructor
 */
Rx.Subscriber = function() {};

/**
 * @constructor
 */
Rx.Scheduler = function() {};

/**
 * @constructor
 */
Rx.Subscription = function() {};

/**
 * @const
 */
Rx.Scheduler.asap;

/**
 * @const
 */
Rx.Scheduler.queue;

/**
 * @const
 */
Rx.Scheduler.async;

/**
 * @this {null}
 * @return {Rx.Observable}
 */
Rx.Observable.from = function() {};

/**
 * @this {null}
 * @return {Rx.Observable}
 */
Rx.Observable.fromArray = function() {};

/**
 * @this {null}
 * @return {Rx.Observable}
 */
Rx.Observable.of = function() {};

/**
 * @this {null}
 * @return {Rx.Observable}
 */
Rx.Observable.fromPromise = function() {};

/**
 * @this {null}
 * @return {Rx.Observable}
 */
Rx.Observable.throw = function() {};

/**
 * @this {null}
 * @return {Rx.Observable}
 */
Rx.Observable.timer = function() {};

/**
 * @this {null}
 * @return {Rx.Observable}
 */
Rx.Observable.interval = function() {};

/**
 * @this {null}
 * @return {Rx.Observable}
 */
Rx.Observable.range = function() {};

/**
 * @this {null}
 * @return {Rx.Observable}
 */
Rx.Observable.forkJoin = function() {};


/**
 * @this {Rx.Observable}
 * @return {Rx.Subscription}
 */
Rx.Observable.prototype.subscribe = function() {};

/**
 * @this {Rx.Observable}
 * @return {Rx.Observable}
 */
Rx.Observable.prototype.race = function() {};

/**
 * @this {Rx.Observable}
 * @return {Rx.Observable}
 */
Rx.Observable.prototype.zip = function() {};

/**
 * @this {Rx.Observable}
 * @return {Rx.Observable}
 */
Rx.Observable.prototype.zipAll = function() {};

/**
 * @this {Rx.Observable}
 * @return {Rx.Observable}
 */
Rx.Observable.prototype.publish = function() {};

/**
 * @this {Rx.Observable}
 * @return {Rx.Observable}
 */
Rx.Observable.prototype.concat = function() {};

/**
 * @this {Rx.Observable}
 * @return {Rx.Observable}
 */
Rx.Observable.prototype.concatAll = function() {};

/**
 * @this {Rx.Observable}
 * @return {Rx.Observable}
 */
Rx.Observable.prototype.concatMap = function() {};

/**
 * @this {Rx.Observable}
 * @return {Rx.Observable}
 */
Rx.Observable.prototype.concatMapTo = function() {};

/**
 * @this {Rx.Observable}
 * @return {Rx.Observable}
 */
Rx.Observable.prototype.scan = function() {};

/**
 * @this {Rx.Observable}
 * @return {Rx.Observable}
 */
Rx.Observable.prototype.merge = function() {};

/**
 * @this {Rx.Observable}
 * @return {Rx.Observable}
 */
Rx.Observable.prototype.mergeAll = function() {};

/**
 * @this {Rx.Observable}
 * @return {Rx.Observable}
 */
Rx.Observable.prototype.mergeMap = function() {};

/**
 * @this {Rx.Observable}
 * @return {Rx.Observable}
 */
Rx.Observable.prototype.mergeMapTo = function() {};

/**
 * @this {Rx.Observable}
 * @return {Rx.Observable}
 */
Rx.Observable.prototype.filter = function() {};

/**
 * @this {Rx.Observable}
 * @return {Rx.Observable}
 */
Rx.Observable.prototype.map = function() {};

/**
 * @this {Rx.Observable}
 * @return {Rx.Observable}
 */
Rx.Observable.prototype.mapTo = function() {};

/**
 * @this {Rx.Observable}
 * @return {Rx.Observable}
 */
Rx.Observable.prototype.flatMap = function() {};

/**
 * @this {Rx.Observable}
 * @return {Rx.Observable}
 */
Rx.Observable.prototype.skip = function() {};

/**
 * @this {Rx.Observable}
 * @return {Rx.Observable}
 */
Rx.Observable.prototype.skipWhile = function() {};

/**
 * @this {Rx.Observable}
 * @return {Rx.Observable}
 */
Rx.Observable.prototype.skipUntil = function() {};

/**
 * @this {Rx.Observable}
 * @return {Rx.Observable}
 */
Rx.Observable.prototype.take = function() {};

/**
 * @this {Rx.Observable}
 * @return {Rx.Observable}
 */
Rx.Observable.prototype.takeWhile = function() {};

/**
 * @this {Rx.Observable}
 * @return {Rx.Observable}
 */
Rx.Observable.prototype.takeUntil = function() {};

/**
 * @this {Rx.Observable}
 * @return {Rx.Observable}
 */
Rx.Observable.prototype.reduce = function() {};

/**
 * @this {Rx.Observable}
 * @return {Rx.Observable}
 */
Rx.Observable.prototype.do = function() {};

/**
 * @this {Rx.Observable}
 * @return {Rx.Observable}
 */
Rx.Observable.prototype.throttle = function() {};

/**
 * @this {Rx.Observable}
 * @return {Rx.Observable}
 */
Rx.Observable.prototype.throttleTime = function() {};

/**
 * @this {Rx.Observable}
 * @return {Rx.Observable}
 */
Rx.Observable.prototype.debounce = function() {};

/**
 * @this {Rx.Observable}
 * @return {Rx.Observable}
 */
Rx.Observable.prototype.debounceTime = function() {};

/**
 * @this {Rx.Observable}
 * @return {Rx.Observable}
 */
Rx.Observable.prototype.sample = function() {};

/**
 * @this {Rx.Observable}
 * @return {Rx.Observable}
 */
Rx.Observable.prototype.sampleTime = function() {};

/**
 * @this {Rx.Observable}
 * @return {Rx.Observable}
 */
Rx.Observable.prototype.delay = function() {};

/**
 * @this {Rx.Observable}
 * @return {Rx.Observable}
 */
Rx.Observable.prototype.delayWhen = function() {};

/**
 * @this {Rx.Observable}
 * @return {Rx.Observable}
 */
Rx.Observable.prototype.timeout = function() {};

/**
 * @this {Rx.Observable}
 * @return {Rx.Observable}
 */
Rx.Observable.prototype.timeoutWith = function() {};

/**
 * @this {Rx.Observable}
 * @return {Rx.Observable}
 */
Rx.Observable.prototype.distinctUntilChanged = function() {};

/**
 * @this {Rx.Observable}
 * @return {Rx.Observable}
 */
Rx.Observable.prototype.bufferCount = function() {};

/**
 * @this {Rx.Observable}
 * @return {Rx.Observable}
 */
Rx.Observable.prototype.bufferTime = function() {};

/**
 * @this {Rx.Observable}
 * @return {Rx.Observable}
 */
Rx.Observable.prototype.bufferWhen = function() {};

/**
 * @this {Rx.Observable}
 * @return {Rx.Observable}
 */
Rx.Observable.prototype.buffer = function() {};

/**
 * @this {Rx.Observable}
 * @return {Rx.Observable}
 */
Rx.Observable.prototype.retry = function() {};

/**
 * @this {Rx.Observable}
 * @return {Rx.Observable}
 */
Rx.Observable.prototype.retryWhen = function() {};

/**
 * @this {Rx.Observable}
 * @return {Rx.Observable}
 */
Rx.Observable.prototype.withLatestFrom = function() {};

/**
 * @this {Rx.Observable}
 * @return {Rx.Observable}
 */
Rx.Observable.prototype.combineLatest = function() {};

/**
 * @this {Rx.Observable}
 * @return {Rx.Observable}
 */
Rx.Observable.prototype.catch = function() {};

/**
 * @this {Rx.Observable}
 * @return {Rx.Observable}
 */
Rx.Observable.prototype.partition = function() {};

/**
 * @this {Rx.Observable}
 * @return {Rx.Observable}
 */
Rx.Observable.prototype.repeat = function() {};

/**
 * @this {Rx.Observable}
 * @return {Rx.Observable}
 */
Rx.Observable.prototype.share = function() {};

/**
 * @this {Rx.Observable}
 * @return {Rx.Observable}
 */
Rx.Observable.prototype.ignoreElements = function() {};

/**
 * @this {Rx.Observable}
 * @return {Rx.Observable}
 */
Rx.Observable.prototype.subscribeOn = function() {};

/**
 * @this {Rx.Observable}
 * @return {Rx.Observable}
 */
Rx.Observable.prototype.observeOn = function() {};

/**
 * @this {null}
 * @return {Rx.Subscriber}
 */
Rx.Subscriber.create = function() {};

/**
 * @this {Rx.Subscriber}
 * @return {null}
 */
Rx.Subscriber.prototype.next = function() {};

/**
 * @this {Rx.Subscriber}
 * @return {null}
 */
Rx.Subscriber.prototype.error = function() {};

/**
 * @this {Rx.Subscriber}
 * @return {null}
 */
Rx.Subscriber.prototype.complete = function() {};


/**
 * @this {Rx.Subscription}
 * @return {null}
 */
Rx.Subscription.prototype.unsubscribe = function() {};

/**
 * @this {Rx.Subscription}
 * @return {null}
 */
Rx.Subscription.prototype.add = function() {};

/**
 * @this {Rx.Subscription}
 * @return {null}
 */
Rx.Subscription.prototype.remove = function() {};
