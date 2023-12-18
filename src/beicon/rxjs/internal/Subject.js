import { Observable, Subscription } from './Observable.js';
export class Subject extends Observable {
    _closed = false;
    get closed() {
        return this._closed;
    }
    _observerCounter = 0;
    currentObservers = new Map();
    observerSnapshot;
    get observers() {
        return (this.observerSnapshot ??= Array.from(this.currentObservers.values()));
    }
    hasError = false;
    thrownError = null;
    constructor() {
        super();
    }
    _clearObservers() {
        this.currentObservers.clear();
        this.observerSnapshot = undefined;
    }
    next(value) {
        if (!this._closed) {
            const { observers } = this;
            const len = observers.length;
            for (let i = 0; i < len; i++) {
                observers[i].next(value);
            }
        }
    }
    error(err) {
        if (!this._closed) {
            this.hasError = this._closed = true;
            this.thrownError = err;
            const { observers } = this;
            const len = observers.length;
            for (let i = 0; i < len; i++) {
                observers[i].error(err);
            }
            this._clearObservers();
        }
    }
    complete() {
        if (!this._closed) {
            this._closed = true;
            const { observers } = this;
            const len = observers.length;
            for (let i = 0; i < len; i++) {
                observers[i].complete();
            }
            this._clearObservers();
        }
    }
    unsubscribe() {
        this._closed = true;
        this._clearObservers();
    }
    get observed() {
        return this.currentObservers.size > 0;
    }
    _subscribe(subscriber) {
        this._checkFinalizedStatuses(subscriber);
        return this._innerSubscribe(subscriber);
    }
    _innerSubscribe(subscriber) {
        if (this.hasError || this._closed) {
            return Subscription.EMPTY;
        }
        const { currentObservers } = this;
        const observerId = this._observerCounter++;
        currentObservers.set(observerId, subscriber);
        this.observerSnapshot = undefined;
        subscriber.add(() => {
            currentObservers.delete(observerId);
            this.observerSnapshot = undefined;
        });
        return subscriber;
    }
    _checkFinalizedStatuses(subscriber) {
        const { hasError, thrownError, _closed } = this;
        if (hasError) {
            subscriber.error(thrownError);
        }
        else if (_closed) {
            subscriber.complete();
        }
    }
    asObservable() {
        return new Observable((subscriber) => this.subscribe(subscriber));
    }
}
