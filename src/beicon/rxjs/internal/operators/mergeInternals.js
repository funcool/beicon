import { from, operate } from '../Observable.js';
import { executeSchedule } from '../util/executeSchedule.js';
export function mergeInternals(source, destination, project, concurrent, onBeforeNext, expand, innerSubScheduler, additionalFinalizer) {
    const buffer = [];
    let active = 0;
    let index = 0;
    let isComplete = false;
    const checkComplete = () => {
        if (isComplete && !buffer.length && !active) {
            destination.complete();
        }
    };
    const outerNext = (value) => (active < concurrent ? doInnerSub(value) : buffer.push(value));
    const doInnerSub = (value) => {
        expand && destination.next(value);
        active++;
        let innerComplete = false;
        from(project(value, index++)).subscribe(operate({
            destination,
            next: (innerValue) => {
                onBeforeNext?.(innerValue);
                if (expand) {
                    outerNext(innerValue);
                }
                else {
                    destination.next(innerValue);
                }
            },
            complete: () => {
                innerComplete = true;
            },
            finalize: () => {
                if (innerComplete) {
                    try {
                        active--;
                        while (buffer.length && active < concurrent) {
                            const bufferedValue = buffer.shift();
                            if (innerSubScheduler) {
                                executeSchedule(destination, innerSubScheduler, () => doInnerSub(bufferedValue));
                            }
                            else {
                                doInnerSub(bufferedValue);
                            }
                        }
                        checkComplete();
                    }
                    catch (err) {
                        destination.error(err);
                    }
                }
            },
        }));
    };
    source.subscribe(operate({
        destination,
        next: outerNext,
        complete: () => {
            isComplete = true;
            checkComplete();
        },
    }));
    return () => {
        additionalFinalizer?.();
    };
}
