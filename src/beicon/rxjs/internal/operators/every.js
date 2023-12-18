import { Observable, operate } from '../Observable.js';
export function every(predicate, thisArg) {
    return (source) => new Observable((destination) => {
        let index = 0;
        source.subscribe(operate({
            destination,
            next: (value) => {
                if (!predicate.call(thisArg, value, index++, source)) {
                    destination.next(false);
                    destination.complete();
                }
            },
            complete: () => {
                destination.next(true);
                destination.complete();
            },
        }));
    });
}
