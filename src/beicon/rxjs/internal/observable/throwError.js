import { Observable } from '../Observable.js';
export function throwError(errorFactory) {
    return new Observable((subscriber) => {
        subscriber.error(errorFactory());
    });
}
