import { Observable, from } from '../Observable.js';
import { mergeAll } from '../operators/mergeAll.js';
export function mergeWith(...otherSources) {
    return (source) => new Observable((subscriber) => {
        mergeAll()(from([source, ...otherSources])).subscribe(subscriber);
    });
}
