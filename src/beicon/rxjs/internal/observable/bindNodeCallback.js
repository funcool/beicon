import { bindCallbackInternals } from './bindCallbackInternals.js';
export function bindNodeCallback(callbackFunc, resultSelector, scheduler) {
    return bindCallbackInternals(true, callbackFunc, resultSelector, scheduler);
}
