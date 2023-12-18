import { operate, Observable, from } from '../Observable.js';
import { noop } from '../util/noop.js';
export function debounce(durationSelector) {
    return (source) => new Observable((destination) => {
        let hasValue = false;
        let lastValue = null;
        let durationSubscriber = null;
        const emit = () => {
            durationSubscriber?.unsubscribe();
            durationSubscriber = null;
            if (hasValue) {
                hasValue = false;
                const value = lastValue;
                lastValue = null;
                destination.next(value);
            }
        };
        source.subscribe(operate({
            destination,
            next: (value) => {
                durationSubscriber?.unsubscribe();
                hasValue = true;
                lastValue = value;
                durationSubscriber = operate({ destination, next: emit, complete: noop });
                from(durationSelector(value)).subscribe(durationSubscriber);
            },
            complete: () => {
                emit();
                destination.complete();
            },
            finalize: () => {
                lastValue = durationSubscriber = null;
            },
        }));
    });
}
