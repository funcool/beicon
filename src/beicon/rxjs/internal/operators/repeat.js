import { Observable, operate, from } from '../Observable.js';
import { EMPTY } from '../observable/empty.js';
import { timer } from '../observable/timer.js';
export function repeat(countOrConfig) {
    let count = Infinity;
    let delay;
    if (countOrConfig != null) {
        if (typeof countOrConfig === 'object') {
            ({ count = Infinity, delay } = countOrConfig);
        }
        else {
            count = countOrConfig;
        }
    }
    return count <= 0
        ? () => EMPTY
        : (source) => new Observable((destination) => {
            let soFar = 0;
            let sourceSub;
            const resubscribe = () => {
                sourceSub?.unsubscribe();
                sourceSub = null;
                if (delay != null) {
                    const notifier = typeof delay === 'number' ? timer(delay) : from(delay(soFar));
                    const notifierSubscriber = operate({
                        destination,
                        next: () => {
                            notifierSubscriber.unsubscribe();
                            subscribeToSource();
                        },
                    });
                    notifier.subscribe(notifierSubscriber);
                }
                else {
                    subscribeToSource();
                }
            };
            const subscribeToSource = () => {
                let syncUnsub = false;
                sourceSub = source.subscribe(operate({
                    destination,
                    complete: () => {
                        if (++soFar < count) {
                            if (sourceSub) {
                                resubscribe();
                            }
                            else {
                                syncUnsub = true;
                            }
                        }
                        else {
                            destination.complete();
                        }
                    },
                }));
                if (syncUnsub) {
                    resubscribe();
                }
            };
            subscribeToSource();
        });
}
