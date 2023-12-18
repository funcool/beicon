import { Observable, from } from '../Observable.js';
import { concatAll } from '../operators/concatAll.js';
export function concatWith(...otherSources) {
    return (source) => new Observable((subscriber) => {
        concatAll()(from([source, ...otherSources])).subscribe(subscriber);
    });
}
