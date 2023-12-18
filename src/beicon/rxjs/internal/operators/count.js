import { reduce } from './reduce.js';
export function count(predicate) {
    return reduce((total, value, i) => (!predicate || predicate(value, i) ? total + 1 : total), 0);
}
