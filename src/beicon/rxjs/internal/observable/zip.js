import { Observable, from, operate } from '../Observable.js';
import { argsOrArgArray } from '../util/argsOrArgArray.js';
import { EMPTY } from './empty.js';
import { popResultSelector } from '../util/args.js';
export function zip(...args) {
    const resultSelector = popResultSelector(args);
    const sources = argsOrArgArray(args);
    return sources.length
        ? new Observable((destination) => {
            let buffers = sources.map(() => []);
            let completed = sources.map(() => false);
            destination.add(() => {
                buffers = completed = null;
            });
            for (let sourceIndex = 0; !destination.closed && sourceIndex < sources.length; sourceIndex++) {
                from(sources[sourceIndex]).subscribe(operate({
                    destination,
                    next: (value) => {
                        buffers[sourceIndex].push(value);
                        if (buffers.every((buffer) => buffer.length)) {
                            const result = buffers.map((buffer) => buffer.shift());
                            destination.next(resultSelector ? resultSelector(...result) : result);
                            if (buffers.some((buffer, i) => !buffer.length && completed[i])) {
                                destination.complete();
                            }
                        }
                    },
                    complete: () => {
                        completed[sourceIndex] = true;
                        !buffers[sourceIndex].length && destination.complete();
                    },
                }));
            }
            return () => {
                buffers = completed = null;
            };
        })
        : EMPTY;
}
