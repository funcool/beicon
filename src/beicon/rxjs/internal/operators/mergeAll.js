import { mergeMap } from './mergeMap.js';
import { identity } from '../util/identity.js';
export function mergeAll(concurrent = Infinity) {
    return mergeMap(identity, concurrent);
}
