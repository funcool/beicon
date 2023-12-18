import { Observable } from '../Observable.js';
export function fromSubscribable(subscribable) {
    return new Observable((subscriber) => subscribable.subscribe(subscriber));
}
