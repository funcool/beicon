import { Observable, operate } from '../Observable.js';
import { EmptyError } from '../util/EmptyError.js';
import { SequenceError } from '../util/SequenceError.js';
import { NotFoundError } from '../util/NotFoundError.js';
export function single(predicate) {
    return (source) => new Observable((destination) => {
        let hasValue = false;
        let singleValue;
        let seenValue = false;
        let index = 0;
        source.subscribe(operate({
            destination,
            next: (value) => {
                seenValue = true;
                if (!predicate || predicate(value, index++, source)) {
                    hasValue && destination.error(new SequenceError('Too many matching values'));
                    hasValue = true;
                    singleValue = value;
                }
            },
            complete: () => {
                if (hasValue) {
                    destination.next(singleValue);
                    destination.complete();
                }
                else {
                    destination.error(seenValue ? new NotFoundError('No matching values') : new EmptyError());
                }
            },
        }));
    });
}
