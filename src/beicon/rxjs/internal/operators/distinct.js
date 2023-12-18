import { Observable, operate, from } from '../Observable.js';
import { noop } from '../util/noop.js';
export function distinct(keySelector, flushes) {
    return (source) => new Observable((destination) => {
        const distinctKeys = new Set();
        source.subscribe(operate({
            destination,
            next: (value) => {
                const key = keySelector ? keySelector(value) : value;
                if (!distinctKeys.has(key)) {
                    distinctKeys.add(key);
                    destination.next(value);
                }
            },
        }));
        flushes && from(flushes).subscribe(operate({ destination, next: () => distinctKeys.clear(), complete: noop }));
    });
}
