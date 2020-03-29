/**
 * @const
 */
var rxjsMain = function() {};
var rxjsOperators = function() {};

/**
 * @constructor
 */
rxjsMain.Observable = function() {};

/**
 * @constructor
 * @extends {rxjsMain.Observable}
 */
rxjsMain.Subject = function() {};

/**
 * @return {rxjsMain.Observable}
 */
rxjsMain.Subject.prototype.asObservable = function() {};

/**
 * @constructor
 * @extends {rxjsMain.Subject}
 */
rxjsMain.BehaviorSubject = function() {};
rxjsMain.BehaviorSubject.prototype.getValue = function() {};

/**
 * @constructor
 */
rxjsMain.Subscriber = function() {};

/**
 * @constructor
 */
rxjsMain.Scheduler = function() {};

/**
 * @constructor
 */
rxjsMain.Subscription = function() {};

/**
 * @const
 */
rxjsMain.asapScheduler;

/**
 * @const
 */
rxjsMain.asyncScheduler;

/**
 * @const
 */
rxjsMain.queueScheduler;

/**
 * @const
 */
rxjsMain.animationFrameScheduler;

/**
 * @this {null}
 * @return {rxjsMain.Observable}
 */
rxjsMain.Observable.from = function() {};

/**
 * @this {null}
 * @return {rxjsMain.Observable}
 */
rxjsMain.Observable.fromArray = function() {};

/**
 * @this {null}
 * @return {rxjsMain.Observable}
 */
rxjsMain.of = function() {};

/**
 * @this {null}
 * @return {rxjsMain.Observable}
 */
rxjsMain.fromPromise = function() {};

/**
 * @this {null}
 * @return {rxjsMain.Observable}
 */
rxjsMain.fromEvent = function() {};

/**
 * @this {null}
 * @return {rxjsMain.Observable}
 */
rxjsMain.throwError = function() {};

/**
 * @this {null}
 * @return {rxjsMain.Observable}
 */
rxjsMain.Observable.timer = function() {};

/**
 * @this {null}
 * @return {rxjsMain.Observable}
 */
rxjsMain.interval = function() {};

/**
 * @this {null}
 * @return {rxjsMain.Observable}
 */
rxjsMain.range = function() {};

/**
 * @this {null}
 * @return {rxjsMain.Observable}
 */
rxjsMain.forkJoin = function() {};

/**
 * @this {rxjsMain.Observable}
 * @return {rxjsMain.Subscription}
 */
rxjsMain.Observable.prototype.subscribe = function() {};

/**
 * @this {rxjsMain.Observable}
 * @return {rxjsMain.Observable}
 */
rxjsMain.Observable.prototype.pipe = function() {};

/**
 * @this {rxjsMain.Observable}
 * @return {rxjsMain.Observable}
 */
rxjsMain.Observable.prototype.toPromise = function() {};

rxjsOperators.race = function() {};
rxjsOperators.zip = function() {};
rxjsOperators.zipAll = function() {};
rxjsOperators.publish = function() {};
rxjsOperators.concat = function() {};
rxjsOperators.concatAll = function() {};
rxjsOperators.concatMap = function() {};
rxjsOperators.concatMapTo = function() {};
rxjsOperators.scan = function() {};
rxjsOperators.merge = function() {};
rxjsOperators.mergeAll = function() {};
rxjsOperators.mergeMap = function() {};
rxjsOperators.mergeMapTo = function() {};
rxjsOperators.first = function() {};
rxjsOperators.last = function() {};
rxjsOperators.filter = function() {};
rxjsOperators.map = function() {};
rxjsOperators.mapTo = function() {};
rxjsOperators.flatMap = function() {};
rxjsOperators.skip = function() {};
rxjsOperators.skipWhile = function() {};
rxjsOperators.skipUntil = function() {};
rxjsOperators.take = function() {};
rxjsOperators.takeWhile = function() {};
rxjsOperators.takeUntil = function() {};
rxjsOperators.reduce = function() {};
rxjsOperators.tap = function() {};
rxjsOperators.throttle = function() {};
rxjsOperators.throttleTime = function() {};
rxjsOperators.debounce = function() {};
rxjsOperators.debounceTime = function() {};
rxjsOperators.sample = function() {};
rxjsOperators.sampleTime = function() {};
rxjsOperators.delay = function() {};
rxjsOperators.timeout = function() {};
rxjsOperators.timeoutWith = function() {};
rxjsOperators.distinctUntilChanged = function() {};
rxjsOperators.bufferCount = function() {};
rxjsOperators.buffer = function() {};
rxjsOperators.bufferTime = function() {};
rxjsOperators.bufferWhen = function() {};
rxjsOperators.buffer = function() {};
rxjsOperators.retry = function() {};
rxjsOperators.retryWhen = function() {};
rxjsOperators.withLatestFrom = function() {};
rxjsOperators.combineLatest = function() {};
rxjsOperators.catchError = function() {};
rxjsOperators.partition = function() {};
rxjsOperators.repeat = function() {};
rxjsOperators.share = function() {};
rxjsOperators.ignoreElements = function() {};
rxjsOperators.subscribeOn = function() {};
rxjsOperators.observeOn = function() {};
