export function pipe(...fns) {
    return fns.length === 1 ? fns[0] : (input) => fns.reduce(pipeReducer, input);
}
function pipeReducer(prev, fn) {
    return fn(prev);
}
