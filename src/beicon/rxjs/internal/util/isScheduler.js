import { isFunction } from '../Observable.js';
export function isScheduler(value) {
    return value && isFunction(value.schedule);
}
