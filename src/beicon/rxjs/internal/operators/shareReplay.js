import { ReplaySubject } from '../ReplaySubject.js';
import { share } from './share.js';
export function shareReplay(configOrBufferSize, windowTime, scheduler) {
    let bufferSize;
    let refCount = false;
    if (configOrBufferSize && typeof configOrBufferSize === 'object') {
        ({ bufferSize = Infinity, windowTime = Infinity, refCount = false, scheduler } = configOrBufferSize);
    }
    else {
        bufferSize = (configOrBufferSize ?? Infinity);
    }
    return share({
        connector: () => new ReplaySubject(bufferSize, windowTime, scheduler),
        resetOnError: true,
        resetOnComplete: false,
        resetOnRefCountZero: refCount,
    });
}
