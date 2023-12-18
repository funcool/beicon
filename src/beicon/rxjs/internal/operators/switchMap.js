import { Observable, from, operate } from '../Observable.js';
export function switchMap(project) {
    return (source) => new Observable((destination) => {
        let innerSubscriber = null;
        let index = 0;
        let isComplete = false;
        const checkComplete = () => isComplete && !innerSubscriber && destination.complete();
        source.subscribe(operate({
            destination,
            next: (value) => {
                innerSubscriber?.unsubscribe();
                const outerIndex = index++;
                from(project(value, outerIndex)).subscribe((innerSubscriber = operate({
                    destination,
                    complete: () => {
                        innerSubscriber = null;
                        checkComplete();
                    },
                })));
            },
            complete: () => {
                isComplete = true;
                checkComplete();
            },
        }));
    });
}
