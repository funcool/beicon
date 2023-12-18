import { shiftResultSelector } from './util.js';
import * as fj from '../internal/observable/forkJoin.js';

export function forkJoin(...args) {
  const resultSelector = shiftResultSelector(args);
  if (resultSelector === undefined) {
    return fj.forkJoin(...args);
  } else {
    return fj.forkJoin(...args, resultSelector);
  }
}
