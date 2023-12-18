import { Observable, operate, from } from '../Observable.js';
export function throttle(durationSelector, config) {
    return (source) => new Observable((destination) => {
        const { leading = true, trailing = false } = config ?? {};
        let hasValue = false;
        let sendValue = null;
        let throttled = null;
        let isComplete = false;
        const endThrottling = () => {
            throttled?.unsubscribe();
            throttled = null;
            if (trailing) {
                send();
                isComplete && destination.complete();
            }
        };
        const cleanupThrottling = () => {
            throttled = null;
            isComplete && destination.complete();
        };
        const startThrottle = (value) => (throttled = from(durationSelector(value)).subscribe(operate({ destination, next: endThrottling, complete: cleanupThrottling })));
        const send = () => {
            if (hasValue) {
                hasValue = false;
                const value = sendValue;
                sendValue = null;
                destination.next(value);
                !isComplete && startThrottle(value);
            }
        };
        source.subscribe(operate({
            destination,
            next: (value) => {
                hasValue = true;
                sendValue = value;
                !(throttled && !throttled.closed) && (leading ? send() : startThrottle(value));
            },
            complete: () => {
                isComplete = true;
                !(trailing && hasValue && throttled && !throttled.closed) && destination.complete();
            },
        }));
    });
}
