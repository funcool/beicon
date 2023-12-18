import { Subscriber } from './Observable.js';
import { EmptyError } from './util/EmptyError.js';
export function firstValueFrom(source, config) {
    const hasConfig = typeof config === 'object';
    return new Promise((resolve, reject) => {
        const subscriber = new Subscriber({
            next: (value) => {
                resolve(value);
                subscriber.unsubscribe();
            },
            error: reject,
            complete: () => {
                if (hasConfig) {
                    resolve(config.defaultValue);
                }
                else {
                    reject(new EmptyError());
                }
            },
        });
        source.subscribe(subscriber);
    });
}
