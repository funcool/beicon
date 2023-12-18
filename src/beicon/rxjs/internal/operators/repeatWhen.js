import { Observable, from, operate } from '../Observable.js';
import { Subject } from '../Subject.js';
export function repeatWhen(notifier) {
    return (source) => new Observable((destination) => {
        let innerSub;
        let syncResub = false;
        let completions$;
        let isNotifierComplete = false;
        let isMainComplete = false;
        const checkComplete = () => isMainComplete && isNotifierComplete && (destination.complete(), true);
        const getCompletionSubject = () => {
            if (!completions$) {
                completions$ = new Subject();
                from(notifier(completions$)).subscribe(operate({
                    destination,
                    next: () => {
                        if (innerSub) {
                            subscribeForRepeatWhen();
                        }
                        else {
                            syncResub = true;
                        }
                    },
                    complete: () => {
                        isNotifierComplete = true;
                        checkComplete();
                    },
                }));
            }
            return completions$;
        };
        const subscribeForRepeatWhen = () => {
            isMainComplete = false;
            innerSub = source.subscribe(operate({
                destination,
                complete: () => {
                    isMainComplete = true;
                    !checkComplete() && getCompletionSubject().next();
                },
            }));
            if (syncResub) {
                innerSub.unsubscribe();
                innerSub = null;
                syncResub = false;
                subscribeForRepeatWhen();
            }
        };
        subscribeForRepeatWhen();
    });
}
