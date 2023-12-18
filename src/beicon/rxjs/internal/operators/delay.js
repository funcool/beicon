import { asyncScheduler } from '../scheduler/async.js';
import { delayWhen } from './delayWhen.js';
import { timer } from '../observable/timer.js';
export function delay(due, scheduler = asyncScheduler) {
    const duration = timer(due, scheduler);
    return delayWhen(() => duration);
}
