import { Observable } from '../Observable.js';
import { mergeInternals } from './mergeInternals.js';
export function mergeScan(accumulator, seed, concurrent = Infinity) {
    return (source) => new Observable((subscriber) => {
        let state = seed;
        return mergeInternals(source, subscriber, (value, index) => accumulator(state, value, index), concurrent, (value) => {
            state = value;
        }, false, undefined, () => (state = null));
    });
}
