import { Observable, from, operate } from '../Observable.js';
import { Subject } from '../Subject.js';
export function groupBy(keySelector, elementOrOptions, duration, connector) {
    return (source) => new Observable((destination) => {
        let element;
        if (!elementOrOptions || typeof elementOrOptions === 'function') {
            element = elementOrOptions;
        }
        else {
            ({ duration, element, connector } = elementOrOptions);
        }
        const groups = new Map();
        const notify = (cb) => {
            groups.forEach(cb);
            cb(destination);
        };
        const handleError = (err) => notify((consumer) => consumer.error(err));
        const groupBySourceSubscriber = operate({
            destination,
            next: (value) => {
                try {
                    const key = keySelector(value);
                    let group = groups.get(key);
                    if (!group) {
                        groups.set(key, (group = connector ? connector() : new Subject()));
                        const grouped = createGroupedObservable(key, group);
                        destination.next(grouped);
                        if (duration) {
                            const durationSubscriber = operate({
                                destination: group,
                                next: () => {
                                    group.complete();
                                    durationSubscriber?.unsubscribe();
                                },
                                finalize: () => groups.delete(key),
                            });
                            groupBySourceSubscriber.add(from(duration(grouped)).subscribe(durationSubscriber));
                        }
                    }
                    group.next(element ? element(value) : value);
                }
                catch (err) {
                    handleError(err);
                }
            },
            error: handleError,
            complete: () => notify((consumer) => consumer.complete()),
            finalize: () => groups.clear(),
        });
        source.subscribe(groupBySourceSubscriber);
        function createGroupedObservable(key, groupSubject) {
            const result = new Observable((groupSubscriber) => groupSubject.subscribe(groupSubscriber));
            result.key = key;
            return result;
        }
    });
}
