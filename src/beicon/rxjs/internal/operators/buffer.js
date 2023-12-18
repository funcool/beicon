import { Observable, operate, from } from '../Observable.js';
import { noop } from '../util/noop.js';
export function buffer(closingNotifier) {
    return (source) => new Observable((destination) => {
        let currentBuffer = [];
        from(closingNotifier).subscribe(operate({
            destination,
            next: () => {
                const b = currentBuffer;
                currentBuffer = [];
                destination.next(b);
            },
            complete: noop,
        }));
        source.subscribe(operate({
            destination,
            next: (value) => currentBuffer.push(value),
            complete: () => {
                destination.next(currentBuffer);
                destination.complete();
            },
        }));
        return () => {
            currentBuffer = null;
        };
    });
}
