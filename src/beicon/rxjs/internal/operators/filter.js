import { Observable, operate } from '../Observable.js';
export function filter(predicate, thisArg) {
    return (source) => new Observable((destination) => {
        let index = 0;
        source.subscribe(operate({ destination, next: (value) => predicate.call(thisArg, value, index++) && destination.next(value) }));
    });
}
