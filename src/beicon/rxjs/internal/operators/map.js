import { Observable, operate } from '../Observable.js';
export function map(project) {
    return (source) => new Observable((destination) => {
        let index = 0;
        source.subscribe(operate({
            destination,
            next: (value) => {
                destination.next(project(value, index++));
            },
        }));
    });
}
