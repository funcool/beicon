import { switchMap } from './switchMap.js';
import { identity } from '../util/identity.js';
export function switchAll() {
    return switchMap(identity);
}
