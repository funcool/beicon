import { from } from '../internal/Observable.js';
import { mergeAll } from '../internal/operators/mergeAll.js';
import { EMPTY } from '../internal/observable/empty.js';

import { isSome } from './util.js';

export function merge(...args) {
  const sources = args.filter(isSome);
  return !sources.length
    ? EMPTY
    : sources.length === 1
    ? from(sources[0])
    : mergeAll(Infinity)(from(sources));
}

