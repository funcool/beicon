import { Observable, operate } from '../Observable.js';
import { noop } from '../util/noop.js';
export function ignoreElements() {
    return (source) => new Observable((destination) => {
        source.subscribe(operate({ destination, next: noop }));
    });
}
