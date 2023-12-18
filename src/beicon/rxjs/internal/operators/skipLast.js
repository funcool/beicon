import { identity } from '../util/identity.js';
import { Observable, operate } from '../Observable.js';
export function skipLast(skipCount) {
    return skipCount <= 0
        ?
            identity
        : (source) => new Observable((destination) => {
            let ring = new Array(skipCount);
            let seen = 0;
            source.subscribe(operate({
                destination,
                next: (value) => {
                    const valueIndex = seen++;
                    if (valueIndex < skipCount) {
                        ring[valueIndex] = value;
                    }
                    else {
                        const index = valueIndex % skipCount;
                        const oldValue = ring[index];
                        ring[index] = value;
                        destination.next(oldValue);
                    }
                },
            }));
            return () => {
                ring = null;
            };
        });
}
