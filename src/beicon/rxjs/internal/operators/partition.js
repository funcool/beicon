import { not } from '../util/not.js';
import { filter } from './filter.js';
export function partition(predicate, thisArg) {
    return (source) => [filter(predicate, thisArg)(source), filter(not(predicate, thisArg))(source)];
}
