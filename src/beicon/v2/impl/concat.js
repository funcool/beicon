import { isSome } from './util.js';
import op from 'rxjs';

export function concat(...args) {
  args = args.filter(isSome);
  return op.concat(...args);
}
