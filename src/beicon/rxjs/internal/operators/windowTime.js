import { Subject } from '../Subject.js';
import { asyncScheduler } from '../scheduler/async.js';
import { Observable, operate, Subscription } from '../Observable.js';
import { arrRemove } from '../util/arrRemove.js';
import { popScheduler } from '../util/args.js';
import { executeSchedule } from '../util/executeSchedule.js';
export function windowTime(windowTimeSpan, ...otherArgs) {
    const scheduler = popScheduler(otherArgs) ?? asyncScheduler;
    const windowCreationInterval = otherArgs[0] ?? null;
    const maxWindowSize = otherArgs[1] || Infinity;
    return (source) => new Observable((destination) => {
        let windowRecords = [];
        let restartOnClose = false;
        const closeWindow = (record) => {
            const { window, subs } = record;
            window.complete();
            subs.unsubscribe();
            arrRemove(windowRecords, record);
            restartOnClose && startWindow();
        };
        const startWindow = () => {
            if (windowRecords) {
                const subs = new Subscription();
                destination.add(subs);
                const window = new Subject();
                const record = {
                    window,
                    subs,
                    seen: 0,
                };
                windowRecords.push(record);
                destination.next(window.asObservable());
                executeSchedule(subs, scheduler, () => closeWindow(record), windowTimeSpan);
            }
        };
        if (windowCreationInterval !== null && windowCreationInterval >= 0) {
            executeSchedule(destination, scheduler, startWindow, windowCreationInterval, true);
        }
        else {
            restartOnClose = true;
        }
        startWindow();
        const loop = (cb) => windowRecords.slice().forEach(cb);
        const terminate = (cb) => {
            loop(({ window }) => cb(window));
            cb(destination);
            destination.unsubscribe();
        };
        source.subscribe(operate({
            destination,
            next: (value) => {
                loop((record) => {
                    record.window.next(value);
                    maxWindowSize <= ++record.seen && closeWindow(record);
                });
            },
            error: (err) => terminate((consumer) => consumer.error(err)),
            complete: () => terminate((consumer) => consumer.complete()),
        }));
        return () => {
            windowRecords = null;
        };
    });
}
