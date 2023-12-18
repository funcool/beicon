import { isFunction } from '../Observable.js';
import { isScheduler } from './isScheduler.js';
function last(arr) {
    return arr[arr.length - 1];
}
export function popResultSelector(args) {
    return isFunction(last(args)) ? args.pop() : undefined;
}
export function popScheduler(args) {
    return isScheduler(last(args)) ? args.pop() : undefined;
}
export function popNumber(args, defaultValue) {
    return typeof last(args) === 'number' ? args.pop() : defaultValue;
}
