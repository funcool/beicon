import { Observable, operate } from '../Observable.js';
import { arrRemove } from '../util/arrRemove.js';
export function bufferCount(bufferSize, startBufferEvery = null) {
    startBufferEvery = startBufferEvery ?? bufferSize;
    return (source) => new Observable((destination) => {
        let buffers = [];
        let count = 0;
        source.subscribe(operate({
            destination,
            next: (value) => {
                let toEmit = null;
                if (count++ % startBufferEvery === 0) {
                    buffers.push([]);
                }
                for (const buffer of buffers) {
                    buffer.push(value);
                    if (bufferSize <= buffer.length) {
                        toEmit = toEmit ?? [];
                        toEmit.push(buffer);
                    }
                }
                if (toEmit) {
                    for (const buffer of toEmit) {
                        arrRemove(buffers, buffer);
                        destination.next(buffer);
                    }
                }
            },
            complete: () => {
                for (const buffer of buffers) {
                    destination.next(buffer);
                }
                destination.complete();
            },
            finalize: () => {
                buffers = null;
            },
        }));
    });
}
