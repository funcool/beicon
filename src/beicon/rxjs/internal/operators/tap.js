import { Observable, operate, isFunction } from '../Observable.js';
import { identity } from '../util/identity.js';
export function tap(observerOrNext) {
    const tapObserver = isFunction(observerOrNext) ? { next: observerOrNext } : observerOrNext;
    return tapObserver
        ? (source) => new Observable((destination) => {
            tapObserver.subscribe?.();
            let isUnsub = true;
            source.subscribe(operate({
                destination,
                next: (value) => {
                    tapObserver.next?.(value);
                    destination.next(value);
                },
                error: (err) => {
                    isUnsub = false;
                    tapObserver.error?.(err);
                    destination.error(err);
                },
                complete: () => {
                    isUnsub = false;
                    tapObserver.complete?.();
                    destination.complete();
                },
                finalize: () => {
                    if (isUnsub) {
                        tapObserver.unsubscribe?.();
                    }
                    tapObserver.finalize?.();
                },
            }));
        })
        :
            identity;
}
