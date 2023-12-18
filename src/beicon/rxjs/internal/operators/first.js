import { EmptyError } from '../util/EmptyError.js';
import { filter } from './filter.js';
import { take } from './take.js';
import { defaultIfEmpty } from './defaultIfEmpty.js';
import { throwIfEmpty } from './throwIfEmpty.js';
import { identity } from '../util/identity.js';
export function first(predicate, defaultValue) {
    const hasDefaultValue = arguments.length >= 2;
    return (source) => source.pipe(predicate ? filter((v, i) => predicate(v, i, source)) : identity, take(1), hasDefaultValue ? defaultIfEmpty(defaultValue) : throwIfEmpty(() => new EmptyError()));
}
