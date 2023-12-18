import { Observable } from '../Observable.js';
import { scanInternals } from './scanInternals.js';
export function scan(accumulator, seed) {
    const hasSeed = arguments.length >= 2;
    return (source) => new Observable((subscriber) => scanInternals(accumulator, seed, hasSeed, true, false, source, subscriber));
}
