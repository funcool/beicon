import { fromArrayLike } from '../Observable.js';
export function of(...values) {
    return fromArrayLike(values);
}
