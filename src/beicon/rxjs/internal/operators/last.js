import { EmptyError } from '../util/EmptyError.js';
import { filter } from './filter.js';
import { takeLast } from './takeLast.js';
import { throwIfEmpty } from './throwIfEmpty.js';
import { defaultIfEmpty } from './defaultIfEmpty.js';
import { identity } from '../util/identity.js';
export function last(predicate, defaultValue) {
    const hasDefaultValue = arguments.length >= 2;
    return (source) => source.pipe(predicate ? filter((v, i) => predicate(v, i, source)) : identity, takeLast(1), hasDefaultValue ? defaultIfEmpty(defaultValue) : throwIfEmpty(() => new EmptyError()));
}
