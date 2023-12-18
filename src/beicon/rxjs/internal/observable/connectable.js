import { Subject } from '../Subject.js';
import { Observable } from '../Observable.js';
import { defer } from './defer.js';
const DEFAULT_CONFIG = {
    connector: () => new Subject(),
    resetOnDisconnect: true,
};
export function connectable(source, config = DEFAULT_CONFIG) {
    let connection = null;
    const { connector, resetOnDisconnect = true } = config;
    let subject = connector();
    const result = new Observable((subscriber) => {
        return subject.subscribe(subscriber);
    });
    result.connect = () => {
        if (!connection || connection.closed) {
            connection = defer(() => source).subscribe(subject);
            if (resetOnDisconnect) {
                connection.add(() => (subject = connector()));
            }
        }
        return connection;
    };
    return result;
}
