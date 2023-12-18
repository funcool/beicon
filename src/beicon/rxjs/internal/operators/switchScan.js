import { switchMap } from './switchMap.js';
import { Observable, operate } from '../Observable.js';
export function switchScan(accumulator, seed) {
    return (source) => new Observable((destination) => {
        let state = seed;
        switchMap((value, index) => accumulator(state, value, index))(source).subscribe(operate({
            destination,
            next: (innerValue) => {
                state = innerValue;
                destination.next(innerValue);
            },
        }));
        return () => {
            state = null;
        };
    });
}
