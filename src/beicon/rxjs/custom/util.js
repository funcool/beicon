import { isFunction } from '../internal/Observable.js';

function first(arr) {
  return arr[0];
}

export function shiftResultSelector(args) {
    return isFunction(first(args)) ? args.shift() : undefined;
}

export function isSome(v) {
  return v !== undefined && v !== null;
}

