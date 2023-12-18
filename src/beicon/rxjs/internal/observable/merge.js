import { from } from '../Observable.js';
import { mergeAll } from '../operators/mergeAll.js';
import { EMPTY } from './empty.js';
import { popNumber, popScheduler } from '../util/args.js';
import { scheduled } from '../scheduled/scheduled.js';
export function merge(...args) {
    const scheduler = popScheduler(args);
    const concurrent = popNumber(args, Infinity);
    const sources = args;
    return !sources.length
        ?
            EMPTY
        : sources.length === 1
            ?
                from(sources[0])
            :
                mergeAll(concurrent)(scheduler ? scheduled(sources, scheduler) : from(sources));
}
