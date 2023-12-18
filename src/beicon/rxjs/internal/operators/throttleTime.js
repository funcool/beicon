import { asyncScheduler } from '../scheduler/async.js';
import { throttle } from './throttle.js';
import { timer } from '../observable/timer.js';
export function throttleTime(duration, scheduler = asyncScheduler, config) {
    const duration$ = timer(duration, scheduler);
    return throttle(() => duration$, config);
}
