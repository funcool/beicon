import { Observable, from, operate } from '../Observable.js';
import { argsArgArrayOrObject } from '../util/argsArgArrayOrObject.js';
import { popResultSelector } from '../util/args.js';
import { mapOneOrManyArgs } from '../util/mapOneOrManyArgs.js';
import { createObject } from '../util/createObject.js';
import { EmptyError } from '../util/EmptyError.js';
export function forkJoin(...args) {
    const resultSelector = popResultSelector(args);
    const { args: sources, keys } = argsArgArrayOrObject(args);
    const result = new Observable((destination) => {
        const { length } = sources;
        if (!length) {
            destination.complete();
            return;
        }
        const values = new Array(length);
        let remainingCompletions = length;
        let remainingEmissions = length;
        for (let sourceIndex = 0; sourceIndex < length; sourceIndex++) {
            let hasValue = false;
            from(sources[sourceIndex]).subscribe(operate({
                destination,
                next: (value) => {
                    if (!hasValue) {
                        hasValue = true;
                        remainingEmissions--;
                    }
                    values[sourceIndex] = value;
                },
                complete: () => remainingCompletions--,
                finalize: () => {
                    if (!remainingCompletions || !hasValue) {
                        if (remainingEmissions === 0) {
                            destination.next(keys ? createObject(keys, values) : values);
                            destination.complete();
                        }
                        else {
                            destination.error(new EmptyError());
                        }
                    }
                },
            }));
        }
    });
    return resultSelector ? result.pipe(mapOneOrManyArgs(resultSelector)) : result;
}
