import { operate, Observable, from } from '../Observable.js';
import { noop } from '../util/noop.js';
export function bufferWhen(closingSelector) {
    return (source) => new Observable((subscriber) => {
        let buffer = null;
        let closingSubscriber = null;
        const openBuffer = () => {
            closingSubscriber?.unsubscribe();
            const b = buffer;
            buffer = [];
            b && subscriber.next(b);
            from(closingSelector()).subscribe((closingSubscriber = operate({
                destination: subscriber,
                next: openBuffer,
                complete: noop,
            })));
        };
        openBuffer();
        source.subscribe(operate({
            destination: subscriber,
            next: (value) => buffer?.push(value),
            complete: () => {
                buffer && subscriber.next(buffer);
                subscriber.complete();
            },
            finalize: () => (buffer = closingSubscriber = null),
        }));
    });
}
