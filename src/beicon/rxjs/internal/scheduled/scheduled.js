import { scheduleObservable } from './scheduleObservable.js';
import { schedulePromise } from './schedulePromise.js';
import { scheduleArray } from './scheduleArray.js';
import { scheduleIterable } from './scheduleIterable.js';
import { scheduleAsyncIterable } from './scheduleAsyncIterable.js';
import { ObservableInputType, getObservableInputType } from '../Observable.js';
import { scheduleReadableStreamLike } from './scheduleReadableStreamLike.js';
export function scheduled(input, scheduler) {
    const type = getObservableInputType(input);
    switch (type) {
        case ObservableInputType.Own:
        case ObservableInputType.InteropObservable:
            return scheduleObservable(input, scheduler);
        case ObservableInputType.Promise:
            return schedulePromise(input, scheduler);
        case ObservableInputType.ArrayLike:
            return scheduleArray(input, scheduler);
        case ObservableInputType.Iterable:
            return scheduleIterable(input, scheduler);
        case ObservableInputType.AsyncIterable:
            return scheduleAsyncIterable(input, scheduler);
        case ObservableInputType.ReadableStreamLike:
            return scheduleReadableStreamLike(input, scheduler);
    }
}
