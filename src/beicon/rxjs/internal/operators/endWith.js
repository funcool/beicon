import { Observable, operate, subscribeToArray } from '../Observable.js';
export function endWith(...values) {
    return (source) => new Observable((destination) => {
        source.subscribe(operate({
            destination,
            complete: () => {
                subscribeToArray(values, destination);
            },
        }));
    });
}
