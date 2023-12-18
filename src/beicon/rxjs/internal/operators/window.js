import { Observable, from, operate } from '../Observable.js';
import { Subject } from '../Subject.js';
import { noop } from '../util/noop.js';
export function window(windowBoundaries) {
    return (source) => new Observable((destination) => {
        let windowSubject = new Subject();
        destination.next(windowSubject.asObservable());
        const errorHandler = (err) => {
            windowSubject.error(err);
            destination.error(err);
        };
        source.subscribe(operate({
            destination,
            next: (value) => windowSubject?.next(value),
            complete: () => {
                windowSubject.complete();
                destination.complete();
            },
            error: errorHandler,
        }));
        from(windowBoundaries).subscribe(operate({
            destination,
            next: () => {
                windowSubject.complete();
                destination.next((windowSubject = new Subject()));
            },
            complete: noop,
            error: errorHandler,
        }));
        return () => {
            windowSubject?.unsubscribe();
            windowSubject = null;
        };
    });
}
