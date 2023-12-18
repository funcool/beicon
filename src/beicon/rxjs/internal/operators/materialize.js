import { Observable, operate, COMPLETE_NOTIFICATION, errorNotification, nextNotification } from '../Observable.js';
export function materialize() {
    return (source) => new Observable((destination) => {
        source.subscribe(operate({
            destination,
            next: (value) => {
                destination.next(nextNotification(value));
            },
            error: (error) => {
                destination.next(errorNotification(error));
                destination.complete();
            },
            complete: () => {
                destination.next(COMPLETE_NOTIFICATION);
                destination.complete();
            },
        }));
    });
}
