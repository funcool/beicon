import { EMPTY } from '../observable/empty.js';
import { Observable, operate } from '../Observable.js';
export function take(count) {
    return count <= 0
        ?
            () => EMPTY
        : (source) => new Observable((destination) => {
            let seen = 0;
            const operatorSubscriber = operate({
                destination,
                next: (value) => {
                    if (++seen < count) {
                        destination.next(value);
                    }
                    else {
                        operatorSubscriber.unsubscribe();
                        destination.next(value);
                        destination.complete();
                    }
                },
            });
            source.subscribe(operatorSubscriber);
        });
}
