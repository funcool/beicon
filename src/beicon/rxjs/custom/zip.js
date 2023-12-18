import { shiftResultSelector } from './util.js';
import { isFunction } from '../internal/Observable.js';
import * as op from '../internal/observable/zip.js';

export function zip(...sources) {
  const projectFunction = shiftResultSelector(sources);

  if (isFunction(projectFunction)) {
    return op.zip(...sources, projectFunction);
  } else {
    return op.zip(...sources);
  }
}
