import { Observable, from } from '../Observable.js';
export function defer(observableFactory) {
    return new Observable((subscriber) => {
        from(observableFactory()).subscribe(subscriber);
    });
}
