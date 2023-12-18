import { Observable, operate } from '../../Observable.js';
const DEFAULT_WEBSOCKET_CONFIG = {
    deserializer: (e) => JSON.parse(e.data),
    serializer: (value) => JSON.stringify(value),
};
const WEBSOCKETSUBJECT_INVALID_ERROR_OBJECT = 'WebSocketSubject.error must be called with an object with an error code, and an optional reason: { code: number, reason: string }';
export class WebSocketSubject extends Observable {
    _config;
    _socket = null;
    _inputBuffer = [];
    _hasError = false;
    _error;
    _isComplete = false;
    _subscriberCounter = 0;
    _subscribers = new Map();
    get observed() {
        return this._subscribers.size > 0;
    }
    constructor(urlConfigOrSource) {
        super();
        const userConfig = typeof urlConfigOrSource === 'string' ? { url: urlConfigOrSource } : urlConfigOrSource;
        this._config = {
            ...DEFAULT_WEBSOCKET_CONFIG,
            WebSocketCtor: WebSocket,
            ...userConfig,
        };
        if (!this._config.WebSocketCtor) {
            throw new Error('no WebSocket constructor can be found');
        }
    }
    _resetState() {
        this._socket = null;
        this._subscriberCounter = 0;
        this._subscribers.clear();
        this._inputBuffer = [];
        this._hasError = false;
        this._isComplete = false;
        this._error = null;
    }
    multiplex(subMsg, unsubMsg, messageFilter) {
        return new Observable((destination) => {
            this.next(subMsg());
            destination.add(() => {
                this.next(unsubMsg());
            });
            this.subscribe(operate({
                destination,
                next: (x) => {
                    if (messageFilter(x)) {
                        destination.next(x);
                    }
                },
            }));
        });
    }
    #outputNext(value) {
        for (const subscriber of Array.from(this._subscribers.values())) {
            subscriber.next(value);
        }
    }
    #outputError(err) {
        const subscribers = Array.from(this._subscribers.values());
        for (const subscriber of subscribers) {
            subscriber.error(err);
        }
    }
    #outputComplete() {
        const subscribers = Array.from(this._subscribers.values());
        for (const subscriber of subscribers) {
            subscriber.complete();
        }
    }
    _connectSocket() {
        const { WebSocketCtor, protocol, url, binaryType, deserializer, openObserver, closeObserver } = this._config;
        let socket = null;
        try {
            socket = protocol ? new WebSocketCtor(url, protocol) : new WebSocketCtor(url);
            this._socket = socket;
            if (binaryType) {
                this._socket.binaryType = binaryType;
            }
        }
        catch (err) {
            this.#outputError(err);
            return;
        }
        socket.onopen = (evt) => {
            if (socket !== this._socket) {
                socket?.close();
                return;
            }
            openObserver?.next(evt);
            while (this._inputBuffer.length > 0) {
                this.next(this._inputBuffer.shift());
            }
            if (this._hasError) {
                this.error(this._error);
            }
            else if (this._isComplete) {
                this.complete();
            }
        };
        socket.onerror = (e) => {
            if (socket !== this._socket) {
                return;
            }
            this.#outputError(e);
        };
        socket.onclose = (e) => {
            if (socket !== this._socket) {
                return;
            }
            closeObserver?.next(e);
            if (e.wasClean) {
                this.#outputComplete();
            }
            else {
                this.#outputError(e);
            }
        };
        socket.onmessage = (e) => {
            try {
                this.#outputNext(deserializer(e));
            }
            catch (err) {
                this.#outputError(err);
            }
        };
    }
    next(value) {
        if (this._socket?.readyState !== 1) {
            this._inputBuffer.push(value);
        }
        else {
            try {
                this._socket.send(this._config.serializer(value));
            }
            catch (err) {
                this.error(err);
            }
        }
    }
    error(err) {
        if (this._socket?.readyState === 1) {
            this._config.closingObserver?.next(undefined);
            if (err?.code) {
                this._socket?.close(err.code, err.reason);
            }
            else {
                this.#outputError(new TypeError(WEBSOCKETSUBJECT_INVALID_ERROR_OBJECT));
            }
            this._resetState();
        }
        else {
            this._hasError = true;
            this._error = err;
        }
    }
    complete() {
        if (this._socket?.readyState === 1) {
            this.#closeSocket();
        }
        else {
            this._isComplete = true;
        }
    }
    #closeSocket() {
        const { _socket } = this;
        this._config.closingObserver?.next(undefined);
        if (_socket && _socket.readyState <= 1) {
            _socket.close();
        }
        this._resetState();
    }
    _subscribe(subscriber) {
        if (!this._socket) {
            this._connectSocket();
        }
        const subscriberId = this._subscriberCounter++;
        this._subscribers.set(subscriberId, subscriber);
        subscriber.add(() => {
            this._subscribers.delete(subscriberId);
            if (!this.observed) {
                this.#closeSocket();
            }
        });
        return subscriber;
    }
    unsubscribe() {
        const subscribers = Array.from(this._subscribers.values());
        this._subscribers.clear();
        for (const subscriber of subscribers) {
            subscriber.unsubscribe();
        }
        this._resetState();
    }
}
