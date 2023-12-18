import { asyncScheduler } from '../scheduler/async.js';
import { isValidDate } from '../util/isDate.js';
import { timeout } from './timeout.js';
export function timeoutWith(due, withObservable, scheduler) {
    let first;
    let each;
    let _with;
    scheduler = scheduler ?? asyncScheduler;
    if (isValidDate(due)) {
        first = due;
    }
    else if (typeof due === 'number') {
        each = due;
    }
    if (withObservable) {
        _with = () => withObservable;
    }
    else {
        throw new TypeError('No observable provided to switch to');
    }
    if (first == null && each == null) {
        throw new TypeError('No timeout provided.');
    }
    return timeout({
        first,
        each,
        scheduler,
        with: _with,
    });
}
