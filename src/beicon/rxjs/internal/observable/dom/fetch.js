import { operate, Observable, from } from '../../Observable.js';
export function fromFetch(input, initWithSelector = {}) {
    const { selector, ...init } = initWithSelector;
    return new Observable((destination) => {
        const controller = new AbortController();
        const { signal } = controller;
        let abortable = true;
        const { signal: outerSignal } = init;
        if (outerSignal) {
            if (outerSignal.aborted) {
                controller.abort();
            }
            else {
                const outerSignalHandler = () => {
                    if (!signal.aborted) {
                        controller.abort();
                    }
                };
                outerSignal.addEventListener('abort', outerSignalHandler);
                destination.add(() => outerSignal.removeEventListener('abort', outerSignalHandler));
            }
        }
        const perSubscriberInit = { ...init, signal };
        const handleError = (err) => {
            abortable = false;
            destination.error(err);
        };
        fetch(input, perSubscriberInit)
            .then((response) => {
            if (selector) {
                from(selector(response)).subscribe(operate({
                    destination,
                    complete: () => {
                        abortable = false;
                        destination.complete();
                    },
                    error: handleError,
                }));
            }
            else {
                abortable = false;
                destination.next(response);
                destination.complete();
            }
        })
            .catch(handleError);
        return () => {
            if (abortable) {
                controller.abort();
            }
        };
    });
}
