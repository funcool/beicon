import { Observable, operate } from '../Observable.js';
export function find(predicate) {
    return (source) => new Observable((subscriber) => createFind(predicate, 'value', source, subscriber));
}
export function createFind(predicate, emit, source, destination) {
    const findIndex = emit === 'index';
    let index = 0;
    source.subscribe(operate({
        destination,
        next: (value) => {
            const i = index++;
            if (predicate(value, i, source)) {
                destination.next(findIndex ? i : value);
                destination.complete();
            }
        },
        complete: () => {
            destination.next(findIndex ? -1 : undefined);
            destination.complete();
        },
    }));
}
