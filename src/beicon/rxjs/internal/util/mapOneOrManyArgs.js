import { map } from '../operators/map.js';
const { isArray } = Array;
function callOrApply(fn, args) {
    return isArray(args) ? fn(...args) : fn(args);
}
export function mapOneOrManyArgs(fn) {
    return map((args) => callOrApply(fn, args));
}
