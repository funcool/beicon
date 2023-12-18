import { operate } from '../Observable.js';
export function scanInternals(accumulator, seed, hasSeed, emitOnNext, emitBeforeComplete, source, destination) {
    let hasState = hasSeed;
    let state = seed;
    let index = 0;
    source.subscribe(operate({
        destination,
        next: (value) => {
            const i = index++;
            state = hasState
                ?
                    accumulator(state, value, i)
                :
                    ((hasState = true), value);
            emitOnNext && destination.next(state);
        },
        complete: emitBeforeComplete
            ? () => {
                hasState && destination.next(state);
                destination.complete();
            }
            : undefined,
    }));
}
