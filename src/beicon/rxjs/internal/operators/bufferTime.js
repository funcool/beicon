import { Subscription, Observable, operate } from '../Observable.js';
import { arrRemove } from '../util/arrRemove.js';
import { asyncScheduler } from '../scheduler/async.js';
import { popScheduler } from '../util/args.js';
import { executeSchedule } from '../util/executeSchedule.js';
export function bufferTime(bufferTimeSpan, ...otherArgs) {
    const scheduler = popScheduler(otherArgs) ?? asyncScheduler;
    const bufferCreationInterval = otherArgs[0] ?? null;
    const maxBufferSize = otherArgs[1] || Infinity;
    return (source) => new Observable((destination) => {
        let bufferRecords = [];
        let restartOnEmit = false;
        const emit = (record) => {
            const { buffer, subs } = record;
            subs.unsubscribe();
            arrRemove(bufferRecords, record);
            destination.next(buffer);
            restartOnEmit && startBuffer();
        };
        const startBuffer = () => {
            if (bufferRecords) {
                const subs = new Subscription();
                destination.add(subs);
                const buffer = [];
                const record = {
                    buffer,
                    subs,
                };
                bufferRecords.push(record);
                executeSchedule(subs, scheduler, () => emit(record), bufferTimeSpan);
            }
        };
        if (bufferCreationInterval !== null && bufferCreationInterval >= 0) {
            executeSchedule(destination, scheduler, startBuffer, bufferCreationInterval, true);
        }
        else {
            restartOnEmit = true;
        }
        startBuffer();
        const bufferTimeSubscriber = operate({
            destination,
            next: (value) => {
                const recordsCopy = bufferRecords.slice();
                for (const record of recordsCopy) {
                    const { buffer } = record;
                    buffer.push(value);
                    maxBufferSize <= buffer.length && emit(record);
                }
            },
            complete: () => {
                while (bufferRecords?.length) {
                    destination.next(bufferRecords.shift().buffer);
                }
                bufferTimeSubscriber?.unsubscribe();
                destination.complete();
                destination.unsubscribe();
            },
            finalize: () => (bufferRecords = null),
        });
        source.subscribe(bufferTimeSubscriber);
    });
}
