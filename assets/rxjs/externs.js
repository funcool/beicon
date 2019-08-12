/**
 * @const
 */
var rxjs = function() {};

/**
 * @constructor
 */
rxjs.Observable = function() {};

/**
 * @constructor
 * @extends {rxjs.Observable}
 */
rxjs.Subject = function() {};

/**
 * @constructor
 * @extends {rxjs.Subject}
 */
rxjs.BehaviorSubject = function() {};
rxjs.BehaviorSubject.prototype.getValue = function() {};

/**
 * @constructor
 */
rxjs.Subscriber = function() {};

/**
 * @constructor
 */
rxjs.Scheduler = function() {};

/**
 * @constructor
 */
rxjs.Subscription = function() {};

/**
 * @const
 */
rxjs.asapScheduler;

/**
 * @const
 */
rxjs.asyncScheduler;

/**
 * @const
 */
rxjs.queueScheduler;

/**
 * @const
 */
rxjs.animationFrameScheduler;

/**
 * @this {null}
 * @return {rxjs.Observable}
 */
rxjs.Observable.from = function() {};

/**
 * @this {null}
 * @return {rxjs.Observable}
 */
rxjs.Observable.fromArray = function() {};

/**
 * @this {null}
 * @return {rxjs.Observable}
 */
rxjs.of = function() {};

/**
 * @this {null}
 * @return {rxjs.Observable}
 */
rxjs.fromPromise = function() {};

/**
 * @this {null}
 * @return {rxjs.Observable}
 */
rxjs.fromEvent = function() {};

/**
 * @this {null}
 * @return {rxjs.Observable}
 */
rxjs.throwError = function() {};

/**
 * @this {null}
 * @return {rxjs.Observable}
 */
rxjs.Observable.timer = function() {};

/**
 * @this {null}
 * @return {rxjs.Observable}
 */
rxjs.interval = function() {};

/**
 * @this {null}
 * @return {rxjs.Observable}
 */
rxjs.range = function() {};

/**
 * @this {null}
 * @return {rxjs.Observable}
 */
rxjs.forkJoin = function() {};

/**
 * @this {rxjs.Observable}
 * @return {rxjs.Subscription}
 */
rxjs.Observable.prototype.subscribe = function() {};

/**
 * @this {rxjs.Observable}
 * @return {rxjs.Observable}
 */
rxjs.Observable.prototype.pipe = function() {};

/**
 * @this {rxjs.Observable}
 * @return {rxjs.Observable}
 */
rxjs.Observable.prototype.toPromise = function() {};

/**
 * @this {null}
 * @return {rxjs.Observable}
 */
rxjs.operators.race = function() {};

/**
 * @this {null}
 * @return {rxjs.Observable}
 */
rxjs.operators.zip = function() {};

/**
 * @this {null}
 * @return {rxjs.Observable}
 */
rxjs.operators.zipAll = function() {};

/**
 * @this {null}
 * @return {rxjs.Observable}
 */
rxjs.operators.publish = function() {};

/**
 * @this {null}
 * @return {rxjs.Observable}
 */
rxjs.operators.concat = function() {};

/**
 * @this {null}
 * @return {rxjs.Observable}
 */
rxjs.operators.concatAll = function() {};

/**
 * @this {null}
 * @return {rxjs.Observable}
 */
rxjs.operators.concatMap = function() {};

/**
 * @this {null}
 * @return {rxjs.Observable}
 */
rxjs.operators.concatMapTo = function() {};

/**
 * @this {null}
 * @return {rxjs.Observable}
 */
rxjs.operators.scan = function() {};

/**
 * @this {null}
 * @return {rxjs.Observable}
 */
rxjs.operators.merge = function() {};

/**
 * @this {null}
 * @return {rxjs.Observable}
 */
rxjs.operators.mergeAll = function() {};

/**
 * @this {null}
 * @return {rxjs.Observable}
 */
rxjs.operators.mergeMap = function() {};

/**
 * @this {null}
 * @return {rxjs.Observable}
 */
rxjs.operators.mergeMapTo = function() {};

/**
 * @this {null}
 * @return {rxjs.Observable}
 */
rxjs.operators.filter = function() {};

/**
 * @this {null}
 * @return {rxjs.Observable}
 */
rxjs.operators.map = function() {};

/**
 * @this {null}
 * @return {rxjs.Observable}
 */
rxjs.operators.mapTo = function() {};

/**
 * @this {null}
 * @return {rxjs.Observable}
 */
rxjs.operators.flatMap = function() {};

/**
 * @this {null}
 * @return {rxjs.Observable}
 */
rxjs.operators.skip = function() {};

/**
 * @this {null}
 * @return {rxjs.Observable}
 */
rxjs.operators.skipWhile = function() {};

/**
 * @this {null}
 * @return {rxjs.Observable}
 */
rxjs.operators.skipUntil = function() {};

/**
 * @this {null}
 * @return {rxjs.Observable}
 */
rxjs.operators.take = function() {};

/**
 * @this {null}
 * @return {rxjs.Observable}
 */
rxjs.operators.takeWhile = function() {};

/**
 * @this {null}
 * @return {rxjs.Observable}
 */
rxjs.operators.takeUntil = function() {};

/**
 * @this {null}
 * @return {rxjs.Observable}
 */
rxjs.operators.reduce = function() {};

/**
 * @this {null}
 * @return {rxjs.Observable}
 */
rxjs.operators.tap = function() {};

/**
 * @this {null}
 * @return {rxjs.Observable}
 */
rxjs.operators.throttle = function() {};

/**
 * @this {null}
 * @return {rxjs.Observable}
 */
rxjs.operators.throttleTime = function() {};

/**
 * @this {null}
 * @return {rxjs.Observable}
 */
rxjs.operators.debounce = function() {};

/**
 * @this {null}
 * @return {rxjs.Observable}
 */
rxjs.operators.debounceTime = function() {};

/**
 * @this {null}
 * @return {rxjs.Observable}
 */
rxjs.operators.sample = function() {};

/**
 * @this {null}
 * @return {rxjs.Observable}
 */
rxjs.operators.sampleTime = function() {};

/**
 * @this {null}
 * @return {rxjs.Observable}
 */
rxjs.operators.delay = function() {};

/**
 * @this {null}
 * @return {rxjs.Observable}
 */
rxjs.operators.delayWhen = function() {};

/**
 * @this {null}
 * @return {rxjs.Observable}
 */
rxjs.operators.timeout = function() {};

/**
 * @this {null}
 * @return {rxjs.Observable}
 */
rxjs.operators.timeoutWith = function() {};

/**
 * @this {null}
 * @return {rxjs.Observable}
 */
rxjs.operators.distinctUntilChanged = function() {};

/**
 * @this {null}
 * @return {rxjs.Observable}
 */
rxjs.operators.bufferCount = function() {};

/**
 * @this {null}
 * @return {rxjs.Observable}
 */
rxjs.operators.bufferTime = function() {};

/**
 * @this {null}
 * @return {rxjs.Observable}
 */
rxjs.operators.bufferWhen = function() {};

/**
 * @this {null}
 * @return {rxjs.Observable}
 */
rxjs.operators.buffer = function() {};

/**
 * @this {null}
 * @return {rxjs.Observable}
 */
rxjs.operators.retry = function() {};

/**
 * @this {null}
 * @return {rxjs.Observable}
 */
rxjs.operators.retryWhen = function() {};

/**
 * @this {null}
 * @return {rxjs.Observable}
 */
rxjs.operators.withLatestFrom = function() {};

/**
 * @this {null}
 * @return {rxjs.Observable}
 */
rxjs.operators.combineLatest = function() {};

/**
 * @this {null}
 * @return {rxjs.Observable}
 */
rxjs.operators.catchError = function() {};

/**
 * @this {null}
 * @return {rxjs.Observable}
 */
rxjs.operators.partition = function() {};

/**
 * @this {null}
 * @return {rxjs.Observable}
 */
rxjs.operators.repeat = function() {};

/**
 * @this {null}
 * @return {rxjs.Observable}
 */
rxjs.operators.share = function() {};

/**
 * @this {null}
 * @return {rxjs.Observable}
 */
rxjs.operators.ignoreElements = function() {};

/**
 * @this {null}
 * @return {rxjs.Observable}
 */
rxjs.operators.subscribeOn = function() {};

/**
 * @this {null}
 * @return {rxjs.Observable}
 */
rxjs.operators.observeOn = function() {};
