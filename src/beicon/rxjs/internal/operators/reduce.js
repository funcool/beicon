import { scanInternals } from './scanInternals.js';
import { Observable } from '../Observable.js';
export function reduce(accumulator, seed) {
    const hasSeed = arguments.length >= 2;
    return (source) => new Observable((subscriber) => scanInternals(accumulator, seed, hasSeed, false, true, source, subscriber));
}
