import { Observable } from '../Observable.js';
import { noop } from '../util/noop.js';
export const NEVER = new Observable(noop);
