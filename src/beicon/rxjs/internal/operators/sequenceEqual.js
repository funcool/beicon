import { Observable, operate, from } from '../Observable.js';
export function sequenceEqual(compareTo, comparator = (a, b) => a === b) {
    return (source) => new Observable((destination) => {
        const aState = createState();
        const bState = createState();
        const emit = (isEqual) => {
            destination.next(isEqual);
            destination.complete();
        };
        const createSubscriber = (selfState, otherState) => {
            const sequenceEqualSubscriber = operate({
                destination,
                next: (a) => {
                    const { buffer, complete } = otherState;
                    if (buffer.length === 0) {
                        complete ? emit(false) : selfState.buffer.push(a);
                    }
                    else {
                        !comparator(a, buffer.shift()) && emit(false);
                    }
                },
                complete: () => {
                    selfState.complete = true;
                    const { complete, buffer } = otherState;
                    complete && emit(buffer.length === 0);
                    sequenceEqualSubscriber?.unsubscribe();
                },
            });
            return sequenceEqualSubscriber;
        };
        source.subscribe(createSubscriber(aState, bState));
        from(compareTo).subscribe(createSubscriber(bState, aState));
    });
}
function createState() {
    return {
        buffer: [],
        complete: false,
    };
}
