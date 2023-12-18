import { Observable } from '../Observable.js';
export function subscribeOn(scheduler, delay = 0) {
    return (source) => new Observable((subscriber) => {
        subscriber.add(scheduler.schedule(() => source.subscribe(subscriber), delay));
    });
}
