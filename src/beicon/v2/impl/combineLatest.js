import { shiftResultSelector } from './util.js';
import op from 'rxjs';

export function combineLatest(...sources) {
  const projectFunction = shiftResultSelector(sources);
  return op.combineLatest(sources, projectFunction);
}
