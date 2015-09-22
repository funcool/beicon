var Rx = {}

Rx.Notification = function() {};

Rx.Observable = function() {};
Rx.Observable.create = function() {};
Rx.Observable.fromArray = function() {};
Rx.Observable.fromCallback = function() {};
Rx.Observable.prototype.subscribe = function() {};
Rx.Observable.prototype.subscribeOnError = function() {};
Rx.Observable.prototype.subscribeOnNext = function() {};
Rx.Observable.prototype.subscribeOnComplete = function() {};
Rx.Observable.prototype.amb = function() {};
Rx.Observable.prototype.merge = function() {};
Rx.Observable.prototype.skip = function() {};
Rx.Observable.prototype.take = function() {};
Rx.Observable.prototype.takeWhile = function() {};
Rx.Observable.prototype.concat = function() {};
Rx.Observable.prototype.map = function() {};
Rx.Observable.prototype.flatMap = function() {};
Rx.Observable.prototype.zip = function() {};
Rx.Observable.prototype.filter = function() {};
Rx.Observable.prototype.reduce = function() {};

Rx.Observer = function() {};
Rx.Observer.prototype.onNext = function() {};
Rx.Observer.prototype.onError = function() {};
Rx.Observer.prototype.onComplete = function() {};

Rx.Subject = function() {};
Rx.Subject.prototype.subscribe = function() {};
Rx.Subject.prototype.onNext = function() {};
Rx.Subject.prototype.onError = function() {};
Rx.Subject.prototype.onComplete = function() {};
Rx.Subject.prototype.dispose = function() {};

