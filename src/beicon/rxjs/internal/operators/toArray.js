import { reduce } from './reduce.js';
import { Observable } from '../Observable.js';
const arrReducer = (arr, value) => (arr.push(value), arr);
export function toArray() {
    return (source) => new Observable((subscriber) => {
        reduce(arrReducer, [])(source).subscribe(subscriber);
    });
}
