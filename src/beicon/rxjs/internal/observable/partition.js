import { not } from '../util/not.js';
import { filter } from '../operators/filter.js';
import { from } from '../Observable.js';
export function partition(source, predicate, thisArg) {
    return [filter(predicate, thisArg)(from(source)), filter(not(predicate, thisArg))(from(source))];
}
