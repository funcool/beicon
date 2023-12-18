import { operate, Observable, from } from '../Observable.js';
export function audit(durationSelector) {
    return (source) => new Observable((destination) => {
        let hasValue = false;
        let lastValue = null;
        let durationSubscriber = null;
        let isComplete = false;
        const endDuration = () => {
            durationSubscriber?.unsubscribe();
            durationSubscriber = null;
            if (hasValue) {
                hasValue = false;
                const value = lastValue;
                lastValue = null;
                destination.next(value);
            }
            isComplete && destination.complete();
        };
        const cleanupDuration = () => {
            durationSubscriber = null;
            isComplete && destination.complete();
        };
        source.subscribe(operate({
            destination,
            next: (value) => {
                hasValue = true;
                lastValue = value;
                if (!durationSubscriber) {
                    from(durationSelector(value)).subscribe((durationSubscriber = operate({
                        destination,
                        next: endDuration,
                        complete: cleanupDuration,
                    })));
                }
            },
            complete: () => {
                isComplete = true;
                (!hasValue || !durationSubscriber || durationSubscriber.closed) && destination.complete();
            },
        }));
    });
}
