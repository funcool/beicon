import { from } from '../Observable.js';
import { concatAll } from '../operators/concatAll.js';
import { popScheduler } from '../util/args.js';
import { scheduled } from '../scheduled/scheduled.js';
export function concat(...args) {
    const scheduler = popScheduler(args);
    return concatAll()(scheduler ? scheduled(args, scheduler) : from(args));
}
