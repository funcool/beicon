import { from } from '../Observable.js';
export function rx(source, ...fns) {
    return fns.reduce(pipeReducer, from(source));
}
function pipeReducer(prev, fn) {
    return fn(prev);
}
