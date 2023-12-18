import { Observable, Subscription, from, operate } from '../Observable.js';
import { Subject } from '../Subject.js';
import { noop } from '../util/noop.js';
import { arrRemove } from '../util/arrRemove.js';
export function windowToggle(openings, closingSelector) {
    return (source) => new Observable((destination) => {
        const windows = [];
        const handleError = (err) => {
            while (0 < windows.length) {
                windows.shift().error(err);
            }
            destination.error(err);
        };
        from(openings).subscribe(operate({
            destination,
            next: (openValue) => {
                const window = new Subject();
                windows.push(window);
                const closingSubscription = new Subscription();
                const closeWindow = () => {
                    arrRemove(windows, window);
                    window.complete();
                    closingSubscription.unsubscribe();
                };
                let closingNotifier;
                try {
                    closingNotifier = from(closingSelector(openValue));
                }
                catch (err) {
                    handleError(err);
                    return;
                }
                destination.next(window);
                closingSubscription.add(closingNotifier.subscribe(operate({
                    destination,
                    next: closeWindow,
                    error: handleError,
                    complete: noop,
                })));
            },
            complete: noop,
        }));
        source.subscribe(operate({
            destination,
            next: (value) => {
                const windowsCopy = windows.slice();
                for (const window of windowsCopy) {
                    window.next(value);
                }
            },
            error: handleError,
            complete: () => {
                while (0 < windows.length) {
                    windows.shift().complete();
                }
                destination.complete();
            },
            finalize: () => {
                while (0 < windows.length) {
                    windows.shift().unsubscribe();
                }
            },
        }));
    });
}
