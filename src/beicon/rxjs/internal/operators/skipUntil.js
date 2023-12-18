import { Observable, operate, from } from '../Observable.js';
import { noop } from '../util/noop.js';
export function skipUntil(notifier) {
    return (source) => new Observable((destination) => {
        let taking = false;
        const skipSubscriber = operate({
            destination,
            next: () => {
                skipSubscriber?.unsubscribe();
                taking = true;
            },
            complete: noop,
        });
        from(notifier).subscribe(skipSubscriber);
        source.subscribe(operate({ destination, next: (value) => taking && destination.next(value) }));
    });
}
