import { Observable, from, operate } from '../Observable.js';
import { noop } from '../util/noop.js';
export function sample(notifier) {
    return (source) => new Observable((destination) => {
        let hasValue = false;
        let lastValue = null;
        source.subscribe(operate({
            destination,
            next: (value) => {
                hasValue = true;
                lastValue = value;
            },
        }));
        from(notifier).subscribe(operate({
            destination,
            next: () => {
                if (hasValue) {
                    hasValue = false;
                    const value = lastValue;
                    lastValue = null;
                    destination.next(value);
                }
            },
            complete: noop,
        }));
    });
}
