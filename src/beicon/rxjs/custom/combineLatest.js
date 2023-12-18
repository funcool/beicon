import { shiftResultSelector } from './util.js';
import * as op from '../internal/observable/combineLatest.js';

export function combineLatest(...sources) {
  const projectFunction = shiftResultSelector(sources);
  return op.combineLatest(sources, projectFunction);
}
