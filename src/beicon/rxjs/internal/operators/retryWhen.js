import { Observable, from, operate } from '../Observable.js';
import { Subject } from '../Subject.js';
export function retryWhen(notifier) {
    return (source) => new Observable((destination) => {
        let innerSub;
        let syncResub = false;
        let errors$;
        const subscribeForRetryWhen = () => {
            innerSub = source.subscribe(operate({
                destination,
                error: (err) => {
                    if (!errors$) {
                        errors$ = new Subject();
                        from(notifier(errors$)).subscribe(operate({
                            destination,
                            next: () => innerSub ? subscribeForRetryWhen() : (syncResub = true),
                        }));
                    }
                    if (errors$) {
                        errors$.next(err);
                    }
                },
            }));
            if (syncResub) {
                innerSub.unsubscribe();
                innerSub = null;
                syncResub = false;
                subscribeForRetryWhen();
            }
        };
        subscribeForRetryWhen();
    });
}
