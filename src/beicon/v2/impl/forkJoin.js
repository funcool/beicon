import { shiftResultSelector } from './util.js';
import op from 'rxjs';

export function forkJoin(...args) {
  const resultSelector = shiftResultSelector(args);
  if (resultSelector === undefined) {
    return op.forkJoin(...args);
  } else {
    return op.forkJoin(...args, resultSelector);
  }
}
