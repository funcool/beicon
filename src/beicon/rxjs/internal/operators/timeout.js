import { asyncScheduler } from '../scheduler/async.js';
import { isValidDate } from '../util/isDate.js';
import { Observable, from, operate } from '../Observable.js';
import { executeSchedule } from '../util/executeSchedule.js';
export class TimeoutError extends Error {
    info;
    constructor(info = null) {
        super('Timeout has occurred');
        this.info = info;
        this.name = 'TimeoutError';
    }
}
export function timeout(config, schedulerArg) {
    const { first, each, with: _with = timeoutErrorFactory, scheduler = schedulerArg ?? asyncScheduler, meta = null, } = (isValidDate(config) ? { first: config } : typeof config === 'number' ? { each: config } : config);
    if (first == null && each == null) {
        throw new TypeError('No timeout provided.');
    }
    return (source) => new Observable((destination) => {
        let originalSourceSubscription;
        let timerSubscription;
        let lastValue = null;
        let seen = 0;
        const startTimer = (delay) => {
            timerSubscription = executeSchedule(destination, scheduler, () => {
                try {
                    originalSourceSubscription.unsubscribe();
                    from(_with({
                        meta,
                        lastValue,
                        seen,
                    })).subscribe(destination);
                }
                catch (err) {
                    destination.error(err);
                }
            }, delay);
        };
        originalSourceSubscription = source.subscribe(operate({
            destination,
            next: (value) => {
                timerSubscription?.unsubscribe();
                seen++;
                destination.next((lastValue = value));
                each > 0 && startTimer(each);
            },
            finalize: () => {
                if (!timerSubscription?.closed) {
                    timerSubscription?.unsubscribe();
                }
                lastValue = null;
            },
        }));
        !seen && startTimer(first != null ? (typeof first === 'number' ? first : +first - scheduler.now()) : each);
    });
}
function timeoutErrorFactory(info) {
    throw new TimeoutError(info);
}
