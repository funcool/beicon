import { Observable } from '../Observable.js';
import { mergeInternals } from './mergeInternals.js';
export function expand(project, concurrent = Infinity, scheduler) {
    concurrent = (concurrent || 0) < 1 ? Infinity : concurrent;
    return (source) => new Observable((subscriber) => mergeInternals(source, subscriber, project, concurrent, undefined, true, scheduler));
}
