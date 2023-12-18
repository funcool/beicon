import { distinctUntilChanged } from './distinctUntilChanged.js';
export function distinctUntilKeyChanged(key, compare) {
    return distinctUntilChanged((x, y) => (compare ? compare(x[key], y[key]) : x[key] === y[key]));
}
