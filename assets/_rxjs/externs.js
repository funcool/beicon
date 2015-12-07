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
 * @extends {Rx.Observable}
 */
Rx.ConnectableObservable = function() {};

/**
 * @this {Rx.Subject}
 * @return {undefined}
 */
Rx.Subject.prototype.onNext = function() {};

/**
 * @this {Rx.Subject}
 * @return {undefined}
 */
Rx.Subject.prototype.onError = function() {};

/**
 * @this {Rx.Subject}
 * @return {undefined}
 */
Rx.Subject.prototype.onCompleted = function() {};

/**
 * @constructor
 */
Rx.Disposable = function() {};

/**
 * @this {Rx.Disposable}
 * @return {undefined}
 */
Rx.Disposable.prototype.dispose = function() {};

/**
 * @this {null}
 * @return {Rx.Observable}
 */
Rx.Observable.create = function() {};

/**
 * @this {null}
 * @return {Rx.Observable}
 */
Rx.Observable.repeat = function() {};

/**
 * @this {null}
 * @return {Rx.Observable}
 */
Rx.Observable.fromArray = function() {};

/**
 * @this {null}
 * @return {Rx.Observable}
 */
Rx.Observable.fromPromise = function() {};

/**
 * @this {null}
 * @return {Rx.Observable}
 */
Rx.Observable.just = function() {};

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
 * @this {Rx.Observable}
 * @return {Rx.Disposable}
 */
Rx.Observable.prototype.subscribe = function() {};

/**
 * @this {Rx.Observable}
 * @return {Rx.Disposable}
 */
Rx.Observable.prototype.subscribeOnNext = function() {};

/**
 * @this {Rx.Observable}
 * @return {Rx.Disposable}
 */
Rx.Observable.prototype.subscribeOnError = function() {};

/**
 * @this {Rx.Observable}
 * @return {Rx.Disposable}
 */
Rx.Observable.prototype.subscribeOnCompleted = function() {};

/**
 * @this {Rx.Observable}
 * @return {Rx.Observable}
 */
Rx.Observable.prototype.amb = function() {};

/**
 * @this {Rx.Observable}
 * @return {Rx.Observable}
 */
Rx.Observable.prototype.zip = function() {};

/**
 * @this {Rx.Observable}
 * @return {Rx.Observable}
 */
Rx.Observable.prototype.publish = function() {};

/**
 * @this {Rx.Observable}
 * @return {Rx.Observable}
 */
Rx.Observable.prototype.connect = function() {};

/**
 * @this {Rx.Observable}
 * @return {Rx.Observable}
 */
Rx.Observable.prototype.concat = function() {};

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
Rx.Observable.prototype.slice = function() {};

/**
 * @this {Rx.Observable}
 * @return {Rx.Observable}
 */
Rx.Observable.prototype.reduce = function() {};

/**
 * @this {Rx.Observable}
 * @return {Rx.Observable}
 */
Rx.Observable.prototype.tap = function() {};

/**
 * @this {Rx.Observable}
 * @return {Rx.Observable}
 */
Rx.Observable.prototype.throttle = function() {};

/**
 * @this {Rx.Observable}
 * @return {Rx.Observable}
 */
Rx.Observable.prototype.pausableBuffered = function() {};

/**
 * @this {Rx.Observable}
 * @return {Rx.Observable}
 */
Rx.Observable.prototype.pausable = function() {};

/**
 * @this {Rx.Observable}
 * @return {Rx.Observable}
 */
Rx.Observable.prototype.distinctUntilChanged = function() {};

/**
 * @this {Rx.Observable}
 * @return {Rx.Observable}
 */
Rx.Observable.prototype.distinct = function() {};

/**
 * @this {Rx.Observable}
 * @return {Rx.Observable}
 */
Rx.Observable.prototype.bufferWithCount = function() {};

/**
 * @this {Rx.Observable}
 * @return {Rx.Observable}
 */
Rx.Observable.prototype.asObservable = function() {};

/**
 * @this {Rx.Observable}
 * @return {Rx.Observable}
 */
Rx.Observable.prototype.distinct = function() {};

/**
 * @this {Rx.Observable}
 * @return {Rx.Observable}
 */
Rx.Observable.prototype.retry = function() {};

/**
 * @this {Rx.Observable}
 * @return {Rx.Observable}
 */
Rx.Observable.prototype.withLatestFrom = function() {};

/**
 * @this {Rx.Observable}
 * @return {Rx.Observable}
 */
Rx.Observable.prototype.catch = function() {};

/**
 * @this {Rx.Observable}
 * @return {Rx.Observable}
 */
Rx.Observable.prototype.empty = function() {};

/**
 * @this {Rx.Observable}
 * @return {Rx.Observable}
 */
Rx.Observable.prototype.share = function() {};

