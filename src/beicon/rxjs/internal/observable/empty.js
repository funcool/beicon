import { Observable } from '../Observable.js';
export const EMPTY = new Observable((subscriber) => subscriber.complete());
