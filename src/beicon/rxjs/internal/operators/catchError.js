import { Observable, operate, from } from '../Observable.js';
export function catchError(selector) {
    return (source) => new Observable((destination) => {
        let innerSub = null;
        let syncUnsub = false;
        let handledResult;
        innerSub = source.subscribe(operate({
            destination,
            error: (err) => {
                handledResult = from(selector(err, catchError(selector)(source)));
                if (innerSub) {
                    innerSub.unsubscribe();
                    innerSub = null;
                    handledResult.subscribe(destination);
                }
                else {
                    syncUnsub = true;
                }
            },
        }));
        if (syncUnsub) {
            innerSub.unsubscribe();
            innerSub = null;
            handledResult.subscribe(destination);
        }
    });
}
