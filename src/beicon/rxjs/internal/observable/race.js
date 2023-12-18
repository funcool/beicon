import { Observable, from, operate } from '../Observable.js';
import { argsOrArgArray } from '../util/argsOrArgArray.js';
export function race(...sources) {
    sources = argsOrArgArray(sources);
    return sources.length === 1 ? from(sources[0]) : new Observable(raceInit(sources));
}
export function raceInit(sources) {
    return (destination) => {
        let subscriptions = [];
        for (let i = 0; subscriptions && !destination.closed && i < sources.length; i++) {
            subscriptions.push(from(sources[i]).subscribe(operate({
                destination,
                next: (value) => {
                    if (subscriptions) {
                        for (let s = 0; s < subscriptions.length; s++) {
                            s !== i && subscriptions[s].unsubscribe();
                        }
                        subscriptions = null;
                    }
                    destination.next(value);
                },
            })));
        }
    };
}
