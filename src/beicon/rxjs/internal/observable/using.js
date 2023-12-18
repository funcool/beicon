import { Observable, from } from '../Observable.js';
import { EMPTY } from './empty.js';
export function using(resourceFactory, observableFactory) {
    return new Observable((subscriber) => {
        const resource = resourceFactory();
        const result = observableFactory(resource);
        const source = result ? from(result) : EMPTY;
        source.subscribe(subscriber);
        return () => {
            if (resource) {
                resource.unsubscribe();
            }
        };
    });
}
