import { Observable } from '../Observable.js';
import { zip } from '../observable/zip.js';
export function zipWith(...otherInputs) {
    return (source) => new Observable((subscriber) => {
        zip(source, ...otherInputs).subscribe(subscriber);
    });
}
