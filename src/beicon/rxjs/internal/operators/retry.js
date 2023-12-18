import { Observable, operate, from } from '../Observable.js';
import { identity } from '../util/identity.js';
import { timer } from '../observable/timer.js';
export function retry(configOrCount = Infinity) {
    let config;
    if (configOrCount && typeof configOrCount === 'object') {
        config = configOrCount;
    }
    else {
        config = {
            count: configOrCount,
        };
    }
    const { count = Infinity, delay, resetOnSuccess: resetOnSuccess = false } = config;
    return count <= 0
        ? identity
        : (source) => new Observable((destination) => {
            let soFar = 0;
            let innerSub;
            const subscribeForRetry = () => {
                let syncUnsub = false;
                innerSub = source.subscribe(operate({
                    destination,
                    next: (value) => {
                        if (resetOnSuccess) {
                            soFar = 0;
                        }
                        destination.next(value);
                    },
                    error: (err) => {
                        if (soFar++ < count) {
                            const resub = () => {
                                if (innerSub) {
                                    innerSub.unsubscribe();
                                    innerSub = null;
                                    subscribeForRetry();
                                }
                                else {
                                    syncUnsub = true;
                                }
                            };
                            if (delay != null) {
                                const notifier = typeof delay === 'number' ? timer(delay) : from(delay(err, soFar));
                                const notifierSubscriber = operate({
                                    destination,
                                    next: () => {
                                        notifierSubscriber.unsubscribe();
                                        resub();
                                    },
                                    complete: () => {
                                        destination.complete();
                                    },
                                });
                                notifier.subscribe(notifierSubscriber);
                            }
                            else {
                                resub();
                            }
                        }
                        else {
                            destination.error(err);
                        }
                    },
                }));
                if (syncUnsub) {
                    innerSub.unsubscribe();
                    innerSub = null;
                    subscribeForRetry();
                }
            };
            subscribeForRetry();
        });
}
