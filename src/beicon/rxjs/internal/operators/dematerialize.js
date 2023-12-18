import { observeNotification } from '../Notification.js';
import { Observable, operate } from '../Observable.js';
export function dematerialize() {
    return (source) => new Observable((destination) => {
        source.subscribe(operate({ destination, next: (notification) => observeNotification(notification, destination) }));
    });
}
