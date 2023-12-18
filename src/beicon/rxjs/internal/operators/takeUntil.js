import { Observable, operate, from } from '../Observable.js';
import { noop } from '../util/noop.js';
export function takeUntil(notifier) {
    return (source) => new Observable((destination) => {
        from(notifier).subscribe(operate({ destination, next: () => destination.complete(), complete: noop }));
        !destination.closed && source.subscribe(destination);
    });
}
