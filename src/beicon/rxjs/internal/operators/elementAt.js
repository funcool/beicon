import { ArgumentOutOfRangeError } from '../util/ArgumentOutOfRangeError.js';
import { filter } from './filter.js';
import { throwIfEmpty } from './throwIfEmpty.js';
import { defaultIfEmpty } from './defaultIfEmpty.js';
import { take } from './take.js';
export function elementAt(index, defaultValue) {
    if (index < 0) {
        throw new ArgumentOutOfRangeError();
    }
    const hasDefaultValue = arguments.length >= 2;
    return (source) => source.pipe(filter((v, i) => i === index), take(1), hasDefaultValue ? defaultIfEmpty(defaultValue) : throwIfEmpty(() => new ArgumentOutOfRangeError()));
}
