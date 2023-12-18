import { Observable, subscribeToArray, operate } from '../Observable.js';
export function startWith(...values) {
    return (source) => new Observable((destination) => {
        subscribeToArray(values, operate({ destination, complete: () => source.subscribe(destination) }));
    });
}
