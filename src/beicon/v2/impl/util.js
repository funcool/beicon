function first(arr) {
  return arr[0];
}

export function isFunction(value) {
  return typeof value === 'function';
}

export function shiftResultSelector(args) {
    return isFunction(first(args)) ? args.shift() : undefined;
}

export function isSome(v) {
  return v !== undefined && v !== null;
}
