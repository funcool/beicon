import { shiftResultSelector } from './util.js';
import * as op from '../internal/operators/withLatestFrom.js';

export function withLatestFrom(...args) {
  const resultSelector = shiftResultSelector(args);
  if (resultSelector === undefined) {
    return op.withLatestFrom(...args);
  } else {
    return op.withLatestFrom(...args, resultSelector);
  }
}
