import { Observable } from '../Observable.js';
import { mergeInternals } from './mergeInternals.js';
export function mergeMap(project, concurrent = Infinity) {
    return (source) => new Observable((subscriber) => mergeInternals(source, subscriber, project, concurrent));
}
