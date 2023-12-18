import { Observable, operate } from '../Observable.js';
export function takeWhile(predicate, inclusive = false) {
    return (source) => new Observable((destination) => {
        let index = 0;
        const operatorSubscriber = operate({
            destination,
            next: (value) => {
                if (predicate(value, index++)) {
                    destination.next(value);
                }
                else {
                    operatorSubscriber.unsubscribe();
                    if (inclusive) {
                        destination.next(value);
                    }
                    destination.complete();
                }
            },
        });
        source.subscribe(operatorSubscriber);
    });
}
