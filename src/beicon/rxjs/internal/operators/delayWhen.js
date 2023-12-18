import { from } from '../Observable.js';
import { concat } from '../observable/concat.js';
import { take } from './take.js';
import { ignoreElements } from './ignoreElements.js';
import { mapTo } from './mapTo.js';
import { mergeMap } from './mergeMap.js';
export function delayWhen(delayDurationSelector, subscriptionDelay) {
    if (subscriptionDelay) {
        return (source) => concat(subscriptionDelay.pipe(take(1), ignoreElements()), source.pipe(delayWhen(delayDurationSelector)));
    }
    return mergeMap((value, index) => from(delayDurationSelector(value, index)).pipe(take(1), mapTo(value)));
}
