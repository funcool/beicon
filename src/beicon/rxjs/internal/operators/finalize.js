import { Observable } from '../Observable.js';
export function finalize(callback) {
    return (source) => new Observable((subscriber) => {
        source.subscribe(subscriber);
        subscriber.add(callback);
    });
}
