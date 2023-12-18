export class UnsubscriptionError extends Error {
    errors;
    constructor(errors) {
        super(errors
            ? `${errors.length} errors occurred during unsubscription:
${errors.map((err, i) => `${i + 1}) ${err.toString()}`).join('\n  ')}`
            : '');
        this.errors = errors;
        this.name = 'UnsubscriptionError';
    }
}
export class Subscription {
    initialTeardown;
    static EMPTY = (() => {
        const empty = new Subscription();
        empty.closed = true;
        return empty;
    })();
    closed = false;
    _finalizers = null;
    constructor(initialTeardown) {
        this.initialTeardown = initialTeardown;
    }
    unsubscribe() {
        let errors;
        if (!this.closed) {
            this.closed = true;
            const { initialTeardown: initialFinalizer } = this;
            if (isFunction(initialFinalizer)) {
                try {
                    initialFinalizer();
                }
                catch (e) {
                    errors = e instanceof UnsubscriptionError ? e.errors : [e];
                }
            }
            const { _finalizers } = this;
            if (_finalizers) {
                this._finalizers = null;
                for (const finalizer of _finalizers) {
                    try {
                        execFinalizer(finalizer);
                    }
                    catch (err) {
                        errors = errors ?? [];
                        if (err instanceof UnsubscriptionError) {
                            errors.push(...err.errors);
                        }
                        else {
                            errors.push(err);
                        }
                    }
                }
            }
            if (errors) {
                throw new UnsubscriptionError(errors);
            }
        }
    }
    add(teardown) {
        if (teardown && teardown !== this) {
            if (this.closed) {
                execFinalizer(teardown);
            }
            else {
                if (teardown && 'add' in teardown) {
                    teardown.add(() => {
                        this.remove(teardown);
                    });
                }
                this._finalizers ??= new Set();
                this._finalizers.add(teardown);
            }
        }
    }
    remove(teardown) {
        this._finalizers?.delete(teardown);
    }
}
if (typeof Symbol.dispose === 'symbol') {
    Subscription.prototype[Symbol.dispose] = Subscription.prototype.unsubscribe;
}
function execFinalizer(finalizer) {
    if (isFunction(finalizer)) {
        finalizer();
    }
    else {
        finalizer.unsubscribe();
    }
}
export class Subscriber extends Subscription {
    isStopped = false;
    destination;
    _nextOverride = null;
    _errorOverride = null;
    _completeOverride = null;
    _onFinalize = null;
    constructor(destination, overrides) {
        super();
        this.destination = destination instanceof Subscriber ? destination : createSafeObserver(destination);
        this._nextOverride = overrides?.next ?? null;
        this._errorOverride = overrides?.error ?? null;
        this._completeOverride = overrides?.complete ?? null;
        this._onFinalize = overrides?.finalize ?? null;
        this._next = this._nextOverride ? overrideNext : this._next;
        this._error = this._errorOverride ? overrideError : this._error;
        this._complete = this._completeOverride ? overrideComplete : this._complete;
        if (hasAddAndUnsubscribe(destination)) {
            destination.add(this);
        }
    }
    next(value) {
        if (this.isStopped) {
            handleStoppedNotification(nextNotification(value), this);
        }
        else {
            this._next(value);
        }
    }
    error(err) {
        if (this.isStopped) {
            handleStoppedNotification(errorNotification(err), this);
        }
        else {
            this.isStopped = true;
            this._error(err);
        }
    }
    complete() {
        if (this.isStopped) {
            handleStoppedNotification(COMPLETE_NOTIFICATION, this);
        }
        else {
            this.isStopped = true;
            this._complete();
        }
    }
    unsubscribe() {
        if (!this.closed) {
            this.isStopped = true;
            super.unsubscribe();
            this._onFinalize?.();
        }
    }
    _next(value) {
        this.destination.next(value);
    }
    _error(err) {
        try {
            this.destination.error(err);
        }
        finally {
            this.unsubscribe();
        }
    }
    _complete() {
        try {
            this.destination.complete();
        }
        finally {
            this.unsubscribe();
        }
    }
}
export const config = {
    onUnhandledError: null,
    onStoppedNotification: null,
};
function overrideNext(value) {
    try {
        this._nextOverride(value);
    }
    catch (error) {
        this.destination.error(error);
    }
}
function overrideError(err) {
    try {
        this._errorOverride(err);
    }
    catch (error) {
        this.destination.error(error);
    }
    finally {
        this.unsubscribe();
    }
}
function overrideComplete() {
    try {
        this._completeOverride();
    }
    catch (error) {
        this.destination.error(error);
    }
    finally {
        this.unsubscribe();
    }
}
class ConsumerObserver {
    partialObserver;
    constructor(partialObserver) {
        this.partialObserver = partialObserver;
    }
    next(value) {
        const { partialObserver } = this;
        if (partialObserver.next) {
            try {
                partialObserver.next(value);
            }
            catch (error) {
                reportUnhandledError(error);
            }
        }
    }
    error(err) {
        const { partialObserver } = this;
        if (partialObserver.error) {
            try {
                partialObserver.error(err);
            }
            catch (error) {
                reportUnhandledError(error);
            }
        }
        else {
            reportUnhandledError(err);
        }
    }
    complete() {
        const { partialObserver } = this;
        if (partialObserver.complete) {
            try {
                partialObserver.complete();
            }
            catch (error) {
                reportUnhandledError(error);
            }
        }
    }
}
function createSafeObserver(observerOrNext) {
    return new ConsumerObserver(!observerOrNext || isFunction(observerOrNext) ? { next: observerOrNext ?? undefined } : observerOrNext);
}
function handleStoppedNotification(notification, subscriber) {
    const { onStoppedNotification } = config;
    onStoppedNotification && setTimeout(() => onStoppedNotification(notification, subscriber));
}
function hasAddAndUnsubscribe(value) {
    return value && isFunction(value.unsubscribe) && isFunction(value.add);
}
export function operate({ destination, ...subscriberOverrides }) {
    return new Subscriber(destination, subscriberOverrides);
}
export class Observable {
    constructor(subscribe) {
        if (subscribe) {
            this._subscribe = subscribe;
        }
    }
    subscribe(observerOrNext) {
        const subscriber = observerOrNext instanceof Subscriber ? observerOrNext : new Subscriber(observerOrNext);
        subscriber.add(this._trySubscribe(subscriber));
        return subscriber;
    }
    _trySubscribe(sink) {
        try {
            return this._subscribe(sink);
        }
        catch (err) {
            sink.error(err);
        }
    }
    forEach(next) {
        return new Promise((resolve, reject) => {
            const subscriber = new Subscriber({
                next: (value) => {
                    try {
                        next(value);
                    }
                    catch (err) {
                        reject(err);
                        subscriber.unsubscribe();
                    }
                },
                error: reject,
                complete: resolve,
            });
            this.subscribe(subscriber);
        });
    }
    _subscribe(_subscriber) {
        return;
    }
    [Symbol.observable ?? '@@observable']() {
        return this;
    }
    pipe(...operations) {
        return operations.reduce(pipeReducer, this);
    }
    [Symbol.asyncIterator]() {
        let subscription;
        let hasError = false;
        let error;
        let completed = false;
        const values = [];
        const deferreds = [];
        const handleError = (err) => {
            hasError = true;
            error = err;
            while (deferreds.length) {
                const [_, reject] = deferreds.shift();
                reject(err);
            }
        };
        const handleComplete = () => {
            completed = true;
            while (deferreds.length) {
                const [resolve] = deferreds.shift();
                resolve({ value: undefined, done: true });
            }
        };
        return {
            next: () => {
                if (!subscription) {
                    subscription = this.subscribe({
                        next: (value) => {
                            if (deferreds.length) {
                                const [resolve] = deferreds.shift();
                                resolve({ value, done: false });
                            }
                            else {
                                values.push(value);
                            }
                        },
                        error: handleError,
                        complete: handleComplete,
                    });
                }
                if (values.length) {
                    return Promise.resolve({ value: values.shift(), done: false });
                }
                if (completed) {
                    return Promise.resolve({ value: undefined, done: true });
                }
                if (hasError) {
                    return Promise.reject(error);
                }
                return new Promise((resolve, reject) => {
                    deferreds.push([resolve, reject]);
                });
            },
            throw: (err) => {
                subscription?.unsubscribe();
                handleError(err);
                return Promise.reject(err);
            },
            return: () => {
                subscription?.unsubscribe();
                handleComplete();
                return Promise.resolve({ value: undefined, done: true });
            },
            [Symbol.asyncIterator]() {
                return this;
            },
        };
    }
}
function pipeReducer(prev, fn) {
    return fn(prev);
}
export function reportUnhandledError(err) {
    setTimeout(() => {
        const { onUnhandledError } = config;
        if (onUnhandledError) {
            onUnhandledError(err);
        }
        else {
            throw err;
        }
    });
}
export function from(input) {
    const type = getObservableInputType(input);
    switch (type) {
        case ObservableInputType.Own:
            return input;
        case ObservableInputType.InteropObservable:
            return fromInteropObservable(input);
        case ObservableInputType.ArrayLike:
            return fromArrayLike(input);
        case ObservableInputType.Promise:
            return fromPromise(input);
        case ObservableInputType.AsyncIterable:
            return fromAsyncIterable(input);
        case ObservableInputType.Iterable:
            return fromIterable(input);
        case ObservableInputType.ReadableStreamLike:
            return fromReadableStreamLike(input);
    }
}
function fromInteropObservable(obj) {
    return new Observable((subscriber) => {
        const obs = obj[Symbol.observable ?? '@@observable']();
        if (isFunction(obs.subscribe)) {
            return obs.subscribe(subscriber);
        }
        throw new TypeError('Provided object does not correctly implement Symbol.observable');
    });
}
export function fromArrayLike(array) {
    return new Observable((subscriber) => {
        subscribeToArray(array, subscriber);
    });
}
export function fromPromise(promise) {
    return new Observable((subscriber) => {
        promise
            .then((value) => {
            if (!subscriber.closed) {
                subscriber.next(value);
                subscriber.complete();
            }
        }, (err) => subscriber.error(err))
            .then(null, reportUnhandledError);
    });
}
function fromIterable(iterable) {
    return new Observable((subscriber) => {
        for (const value of iterable) {
            subscriber.next(value);
            if (subscriber.closed) {
                return;
            }
        }
        subscriber.complete();
    });
}
function fromAsyncIterable(asyncIterable) {
    return new Observable((subscriber) => {
        process(asyncIterable, subscriber).catch((err) => subscriber.error(err));
    });
}
function fromReadableStreamLike(readableStream) {
    return fromAsyncIterable(readableStreamLikeToAsyncGenerator(readableStream));
}
async function process(asyncIterable, subscriber) {
    for await (const value of asyncIterable) {
        subscriber.next(value);
        if (subscriber.closed) {
            return;
        }
    }
    subscriber.complete();
}
export function subscribeToArray(array, subscriber) {
    const length = array.length;
    for (let i = 0; i < length; i++) {
        if (subscriber.closed) {
            return;
        }
        subscriber.next(array[i]);
    }
    subscriber.complete();
}
export var ObservableInputType;
(function (ObservableInputType) {
    ObservableInputType[ObservableInputType["Own"] = 0] = "Own";
    ObservableInputType[ObservableInputType["InteropObservable"] = 1] = "InteropObservable";
    ObservableInputType[ObservableInputType["ArrayLike"] = 2] = "ArrayLike";
    ObservableInputType[ObservableInputType["Promise"] = 3] = "Promise";
    ObservableInputType[ObservableInputType["AsyncIterable"] = 4] = "AsyncIterable";
    ObservableInputType[ObservableInputType["Iterable"] = 5] = "Iterable";
    ObservableInputType[ObservableInputType["ReadableStreamLike"] = 6] = "ReadableStreamLike";
})(ObservableInputType || (ObservableInputType = {}));
export function getObservableInputType(input) {
    if (input instanceof Observable) {
        return ObservableInputType.Own;
    }
    if (isInteropObservable(input)) {
        return ObservableInputType.InteropObservable;
    }
    if (isArrayLike(input)) {
        return ObservableInputType.ArrayLike;
    }
    if (isPromise(input)) {
        return ObservableInputType.Promise;
    }
    if (isAsyncIterable(input)) {
        return ObservableInputType.AsyncIterable;
    }
    if (isIterable(input)) {
        return ObservableInputType.Iterable;
    }
    if (isReadableStreamLike(input)) {
        return ObservableInputType.ReadableStreamLike;
    }
    throw new TypeError(`You provided ${input !== null && typeof input === 'object' ? 'an invalid object' : `'${input}'`} where a stream was expected. You can provide an Observable, Promise, ReadableStream, Array, AsyncIterable, or Iterable.`);
}
export function isFunction(value) {
    return typeof value === 'function';
}
function isAsyncIterable(obj) {
    return Symbol.asyncIterator && isFunction(obj?.[Symbol.asyncIterator]);
}
export async function* readableStreamLikeToAsyncGenerator(readableStream) {
    const reader = readableStream.getReader();
    try {
        while (true) {
            const { value, done } = await reader.read();
            if (done) {
                return;
            }
            yield value;
        }
    }
    finally {
        reader.releaseLock();
    }
}
function isReadableStreamLike(obj) {
    return isFunction(obj?.getReader);
}
export function isPromise(value) {
    return isFunction(value?.then);
}
function isInteropObservable(input) {
    return isFunction(input[Symbol.observable ?? '@@observable']);
}
function isIterable(input) {
    return isFunction(input?.[Symbol.iterator]);
}
export function isArrayLike(x) {
    return x && typeof x.length === 'number' && !isFunction(x);
}
export function isObservable(obj) {
    return !!obj && (obj instanceof Observable || (isFunction(obj.lift) && isFunction(obj.subscribe)));
}
export const COMPLETE_NOTIFICATION = (() => createNotification('C', undefined, undefined))();
export function errorNotification(error) {
    return createNotification('E', undefined, error);
}
export function nextNotification(value) {
    return createNotification('N', value, undefined);
}
export function createNotification(kind, value, error) {
    return {
        kind,
        value,
        error,
    };
}
