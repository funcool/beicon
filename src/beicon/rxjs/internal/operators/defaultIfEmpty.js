import { Observable, operate } from '../Observable.js';
export function defaultIfEmpty(defaultValue) {
    return (source) => new Observable((destination) => {
        let hasValue = false;
        source.subscribe(operate({
            destination,
            next: (value) => {
                hasValue = true;
                destination.next(value);
            },
            complete: () => {
                if (!hasValue) {
                    destination.next(defaultValue);
                }
                destination.complete();
            },
        }));
    });
}
