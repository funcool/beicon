import { Observable, operate, from } from '../Observable.js';
import { arrayOrObject } from '../util/argsArgArrayOrObject.js';
import { identity } from '../util/identity.js';
import { createObject } from '../util/createObject.js';
import { EMPTY } from './empty.js';
export function combineLatest(sources, resultSelector) {
    const parts = arrayOrObject(sources);
    if (!parts) {
        throw new TypeError('sources must be an array or object');
    }
    const { args: observables, keys } = parts;
    if (observables.length === 0) {
        return EMPTY;
    }
    return new Observable(combineLatestInit(observables, keys
        ? (values) => createObject(keys, values)
        : resultSelector
            ? (values) => resultSelector(...values)
            : identity));
}
export function combineLatestInit(observables, valueTransform = identity) {
    return (destination) => {
        const { length } = observables;
        const values = new Array(length);
        let active = length;
        let remainingFirstValues = length;
        for (let i = 0; i < length; i++) {
            const source = from(observables[i]);
            let hasFirstValue = false;
            source.subscribe(operate({
                destination,
                next: (value) => {
                    values[i] = value;
                    if (!hasFirstValue) {
                        hasFirstValue = true;
                        remainingFirstValues--;
                    }
                    if (!remainingFirstValues) {
                        destination.next(valueTransform(Array.from(values)));
                    }
                },
                complete: () => {
                    if (!--active) {
                        destination.complete();
                    }
                },
            }));
        }
    };
}
