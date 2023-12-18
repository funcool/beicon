import { Subscription, Observable, operate, from } from '../Observable.js';
import { noop } from '../util/noop.js';
import { arrRemove } from '../util/arrRemove.js';
export function bufferToggle(openings, closingSelector) {
    return (source) => new Observable((destination) => {
        const buffers = [];
        from(openings).subscribe(operate({
            destination,
            next: (openValue) => {
                const buffer = [];
                buffers.push(buffer);
                const closingSubscription = new Subscription();
                const emitBuffer = () => {
                    arrRemove(buffers, buffer);
                    destination.next(buffer);
                    closingSubscription.unsubscribe();
                };
                closingSubscription.add(from(closingSelector(openValue)).subscribe(operate({ destination, next: emitBuffer, complete: noop })));
            },
            complete: noop,
        }));
        source.subscribe(operate({
            destination,
            next: (value) => {
                for (const buffer of buffers) {
                    buffer.push(value);
                }
            },
            complete: () => {
                while (buffers.length > 0) {
                    destination.next(buffers.shift());
                }
                destination.complete();
            },
        }));
    });
}
