import { Observable, Subscriber, from } from '../Observable.js';
import { Subject } from '../Subject.js';
export function share(options = {}) {
    const { connector = () => new Subject(), resetOnError = true, resetOnComplete = true, resetOnRefCountZero = true } = options;
    return (source) => {
        let connection;
        let resetConnection;
        let subject;
        let refCount = 0;
        let hasCompleted = false;
        let hasErrored = false;
        const cancelReset = () => {
            resetConnection?.unsubscribe();
            resetConnection = undefined;
        };
        const reset = () => {
            cancelReset();
            connection = subject = undefined;
            hasCompleted = hasErrored = false;
        };
        const resetAndUnsubscribe = () => {
            const conn = connection;
            reset();
            conn?.unsubscribe();
        };
        return new Observable((subscriber) => {
            refCount++;
            if (!hasErrored && !hasCompleted) {
                cancelReset();
            }
            const dest = (subject = subject ?? connector());
            subscriber.add(() => {
                refCount--;
                if (refCount === 0 && !hasErrored && !hasCompleted) {
                    resetConnection = handleReset(resetAndUnsubscribe, resetOnRefCountZero);
                }
            });
            dest.subscribe(subscriber);
            if (!connection &&
                refCount > 0) {
                connection = new Subscriber({
                    next: (value) => dest.next(value),
                    error: (err) => {
                        hasErrored = true;
                        cancelReset();
                        resetConnection = handleReset(reset, resetOnError, err);
                        dest.error(err);
                    },
                    complete: () => {
                        hasCompleted = true;
                        cancelReset();
                        resetConnection = handleReset(reset, resetOnComplete);
                        dest.complete();
                    },
                });
                from(source).subscribe(connection);
            }
        });
    };
}
function handleReset(reset, on, ...args) {
    if (on === true) {
        reset();
        return;
    }
    if (on === false) {
        return;
    }
    const onSubscriber = new Subscriber({
        next: () => {
            onSubscriber.unsubscribe();
            reset();
        },
    });
    return from(on(...args)).subscribe(onSubscriber);
}
