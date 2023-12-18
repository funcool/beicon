import { Observable, operate, from } from '../Observable.js';
import { identity } from '../util/identity.js';
import { noop } from '../util/noop.js';
import { popResultSelector } from '../util/args.js';
export function withLatestFrom(...inputs) {
    const project = popResultSelector(inputs);
    return (source) => new Observable((destination) => {
        const len = inputs.length;
        const otherValues = new Array(len);
        let hasValue = inputs.map(() => false);
        let ready = false;
        for (let i = 0; i < len; i++) {
            from(inputs[i]).subscribe(operate({
                destination,
                next: (value) => {
                    otherValues[i] = value;
                    if (!ready && !hasValue[i]) {
                        hasValue[i] = true;
                        (ready = hasValue.every(identity)) && (hasValue = null);
                    }
                },
                complete: noop,
            }));
        }
        source.subscribe(operate({
            destination,
            next: (value) => {
                if (ready) {
                    const values = [value, ...otherValues];
                    destination.next(project ? project(...values) : values);
                }
            },
        }));
    });
}
