import * as op from '../internal/observable/concat.js';
import { isSome } from './util.js';

export function concat(...args) {
  args = args.filter(isSome);
  return op.concat(...args);
}
