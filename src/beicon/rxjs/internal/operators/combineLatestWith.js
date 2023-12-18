import { combineLatestInit } from '../observable/combineLatest.js';
import { Observable } from '../Observable.js';
export function combineLatestWith(...otherSources) {
    return (source) => new Observable((subscriber) => combineLatestInit([source, ...otherSources])(subscriber));
}
