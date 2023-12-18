import { EmptyError } from '../util/EmptyError.js';
import { Observable, operate } from '../Observable.js';
export function throwIfEmpty(errorFactory = defaultErrorFactory) {
    return (source) => new Observable((destination) => {
        let hasValue = false;
        source.subscribe(operate({
            destination,
            next: (value) => {
                hasValue = true;
                destination.next(value);
            },
            complete: () => (hasValue ? destination.complete() : destination.error(errorFactory())),
        }));
    });
}
function defaultErrorFactory() {
    return new EmptyError();
}
