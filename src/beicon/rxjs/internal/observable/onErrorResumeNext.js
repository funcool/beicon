import { Observable, operate, from } from '../Observable.js';
import { argsOrArgArray } from '../util/argsOrArgArray.js';
import { noop } from '../util/noop.js';
export function onErrorResumeNext(...sources) {
    const nextSources = argsOrArgArray(sources);
    return new Observable((destination) => {
        let sourceIndex = 0;
        const subscribeNext = () => {
            if (sourceIndex < nextSources.length) {
                let nextSource;
                try {
                    nextSource = from(nextSources[sourceIndex++]);
                }
                catch (err) {
                    subscribeNext();
                    return;
                }
                const innerSubscriber = operate({ destination, error: noop, complete: noop });
                nextSource.subscribe(innerSubscriber);
                innerSubscriber.add(subscribeNext);
            }
            else {
                destination.complete();
            }
        };
        subscribeNext();
    });
}
