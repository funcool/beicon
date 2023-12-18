import { EMPTY } from '../observable/empty.js';
import { Observable, operate } from '../Observable.js';
export function takeLast(count) {
    return count <= 0
        ? () => EMPTY
        : (source) => new Observable((destination) => {
            let ring = new Array(count);
            let counter = 0;
            source.subscribe(operate({
                destination,
                next: (value) => {
                    ring[counter++ % count] = value;
                },
                complete: () => {
                    const start = count <= counter ? counter : 0;
                    const total = Math.min(count, counter);
                    for (let n = 0; n < total; n++) {
                        destination.next(ring[(start + n) % count]);
                    }
                    destination.complete();
                },
                finalize: () => {
                    ring = null;
                },
            }));
        });
}
