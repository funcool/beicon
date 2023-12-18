import { Observable, from } from '../Observable.js';
import { Subject } from '../Subject.js';
import { fromSubscribable } from '../observable/fromSubscribable.js';
const DEFAULT_CONFIG = {
    connector: () => new Subject(),
};
export function connect(selector, config = DEFAULT_CONFIG) {
    const { connector } = config;
    return (source) => new Observable((subscriber) => {
        const subject = connector();
        from(selector(fromSubscribable(subject))).subscribe(subscriber);
        subscriber.add(source.subscribe(subject));
    });
}
