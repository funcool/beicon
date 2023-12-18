import { executeSchedule } from '../util/executeSchedule.js';
import { Observable, operate } from '../Observable.js';
export function observeOn(scheduler, delay = 0) {
    return (source) => new Observable((destination) => {
        source.subscribe(operate({
            destination,
            next: (value) => executeSchedule(destination, scheduler, () => destination.next(value), delay),
            error: (err) => executeSchedule(destination, scheduler, () => destination.error(err), delay),
            complete: () => executeSchedule(destination, scheduler, () => destination.complete(), delay),
        }));
    });
}
