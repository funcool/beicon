import { Observable } from '../Observable.js';
import { createFind } from './find.js';
export function findIndex(predicate) {
    return (source) => new Observable((subscriber) => createFind(predicate, 'index', source, subscriber));
}
