import { operate, from, Observable } from '../Observable.js';
export function exhaustMap(project) {
    return (source) => new Observable((destination) => {
        let index = 0;
        let innerSub = null;
        let isComplete = false;
        source.subscribe(operate({
            destination,
            next: (outerValue) => {
                if (!innerSub) {
                    innerSub = operate({
                        destination,
                        complete: () => {
                            innerSub = null;
                            isComplete && destination.complete();
                        },
                    });
                    from(project(outerValue, index++)).subscribe(innerSub);
                }
            },
            complete: () => {
                isComplete = true;
                !innerSub && destination.complete();
            },
        }));
    });
}
