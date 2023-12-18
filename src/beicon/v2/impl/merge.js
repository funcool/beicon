import { EMPTY, from, mergeAll } from "rxjs";
import { isSome } from './util.js';

export function merge(...args) {
  const sources = args.filter(isSome);
  return !sources.length
    ? EMPTY
    : sources.length === 1
    ? from(sources[0])
    : mergeAll(Infinity)(from(sources));
}

