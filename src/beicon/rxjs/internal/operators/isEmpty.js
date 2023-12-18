import { Observable, operate } from '../Observable.js';
export function isEmpty() {
    return (source) => new Observable((destination) => {
        source.subscribe(operate({
            destination,
            next: () => {
                destination.next(false);
                destination.complete();
            },
            complete: () => {
                destination.next(true);
                destination.complete();
            },
        }));
    });
}
