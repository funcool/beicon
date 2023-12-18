import { asyncScheduler } from '../scheduler/async.js';
import { timer } from './timer.js';
export function interval(period = 0, scheduler = asyncScheduler) {
    if (period < 0) {
        period = 0;
    }
    return timer(period, period, scheduler);
}
