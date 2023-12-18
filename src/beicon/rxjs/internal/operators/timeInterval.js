import { asyncScheduler } from '../scheduler/async.js';
import { Observable, operate } from '../Observable.js';
export function timeInterval(scheduler = asyncScheduler) {
    return (source) => new Observable((destination) => {
        let last = scheduler.now();
        source.subscribe(operate({
            destination,
            next: (value) => {
                const now = scheduler.now();
                const interval = now - last;
                last = now;
                destination.next(new TimeInterval(value, interval));
            },
        }));
    });
}
export class TimeInterval {
    value;
    interval;
    constructor(value, interval) {
        this.value = value;
        this.interval = interval;
    }
}
