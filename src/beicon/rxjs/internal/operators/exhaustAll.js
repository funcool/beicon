import { exhaustMap } from './exhaustMap.js';
import { identity } from '../util/identity.js';
export function exhaustAll() {
    return exhaustMap(identity);
}
