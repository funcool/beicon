import { operate, Observable, from } from '../Observable.js';
import { Subject } from '../Subject.js';
import { noop } from '../util/noop.js';
export function windowWhen(closingSelector) {
    return (source) => new Observable((destination) => {
        let window;
        let closingSubscriber;
        const handleError = (err) => {
            window.error(err);
            destination.error(err);
        };
        const openWindow = () => {
            closingSubscriber?.unsubscribe();
            window?.complete();
            window = new Subject();
            destination.next(window.asObservable());
            let closingNotifier;
            try {
                closingNotifier = from(closingSelector());
            }
            catch (err) {
                handleError(err);
                return;
            }
            closingNotifier.subscribe((closingSubscriber = operate({
                destination,
                next: openWindow,
                error: handleError,
                complete: noop,
            })));
        };
        openWindow();
        source.subscribe(operate({
            destination,
            next: (value) => window.next(value),
            error: handleError,
            complete: () => {
                window.complete();
                destination.complete();
            },
            finalize: () => {
                closingSubscriber?.unsubscribe();
                window = null;
            },
        }));
    });
}
