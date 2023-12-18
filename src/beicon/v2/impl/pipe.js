export function pipeWith(...fns) {
  const input = fns.pop();
  return fns.reduce(pipeReducer, input);
}

export function pipeComp(...fns) {
  return (source) => fns.reduce(pipeReducer, source);
}

function pipeReducer(prev, fn) {
  return fn(prev);
}
