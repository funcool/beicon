import { Observable, operate } from '../Observable.js';
export function skipWhile(predicate) {
    return (source) => new Observable((destination) => {
        let taking = false;
        let index = 0;
        source.subscribe(operate({ destination, next: (value) => (taking || (taking = !predicate(value, index++))) && destination.next(value) }));
    });
}
