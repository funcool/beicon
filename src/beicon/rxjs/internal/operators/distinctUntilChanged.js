import { identity } from '../util/identity.js';
import { Observable, operate } from '../Observable.js';
export function distinctUntilChanged(comparator, keySelector = identity) {
    comparator = comparator ?? defaultCompare;
    return (source) => new Observable((destination) => {
        let previousKey;
        let first = true;
        source.subscribe(operate({
            destination,
            next: (value) => {
                const currentKey = keySelector(value);
                if (first || !comparator(previousKey, currentKey)) {
                    first = false;
                    previousKey = currentKey;
                    destination.next(value);
                }
            },
        }));
    });
}
function defaultCompare(a, b) {
    return a === b;
}
