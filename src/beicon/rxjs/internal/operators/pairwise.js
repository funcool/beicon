import { Observable, operate } from '../Observable.js';
export function pairwise() {
    return (source) => new Observable((destination) => {
        let prev;
        let hasPrev = false;
        source.subscribe(operate({
            destination,
            next: (value) => {
                const p = prev;
                prev = value;
                hasPrev && destination.next([p, value]);
                hasPrev = true;
            },
        }));
    });
}
