import { from } from '../Observable.js';
import { observeOn } from '../operators/observeOn.js';
import { subscribeOn } from '../operators/subscribeOn.js';
export function schedulePromise(input, scheduler) {
    return from(input).pipe(subscribeOn(scheduler), observeOn(scheduler));
}
