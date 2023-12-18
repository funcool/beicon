import { Observable, isArrayLike, isFunction } from '../Observable.js';
import { mapOneOrManyArgs } from '../util/mapOneOrManyArgs.js';
const nodeEventEmitterMethods = ['addListener', 'removeListener'];
const eventTargetMethods = ['addEventListener', 'removeEventListener'];
const jqueryMethods = ['on', 'off'];
export function fromEvent(target, eventName, options, resultSelector) {
    if (isFunction(options)) {
        resultSelector = options;
        options = undefined;
    }
    if (resultSelector) {
        return fromEvent(target, eventName, options).pipe(mapOneOrManyArgs(resultSelector));
    }
    const isValidTarget = isNodeStyleEventEmitter(target) || isJQueryStyleEventEmitter(target) || isEventTarget(target);
    if (!isValidTarget && !isArrayLike(target)) {
        throw new TypeError('Invalid event target');
    }
    return new Observable((subscriber) => {
        const handler = (...args) => subscriber.next(1 < args.length ? args : args[0]);
        if (isValidTarget) {
            doSubscribe(handler, subscriber, target, eventName, options);
        }
        else {
            for (let i = 0; i < target.length && !subscriber.closed; i++) {
                const subTarget = target[i];
                doSubscribe(handler, subscriber, subTarget, eventName, options);
            }
        }
    });
}
function doSubscribe(handler, subscriber, subTarget, eventName, options) {
    const [addMethod, removeMethod] = getRegistryMethodNames(subTarget);
    if (!addMethod || !removeMethod) {
        throw new TypeError('Invalid event target');
    }
    subTarget[addMethod](eventName, handler, options);
    subscriber.add(() => subTarget[removeMethod](eventName, handler, options));
}
function getRegistryMethodNames(target) {
    return isEventTarget(target)
        ? eventTargetMethods
        :
            isNodeStyleEventEmitter(target)
                ? nodeEventEmitterMethods
                : isJQueryStyleEventEmitter(target)
                    ? jqueryMethods
                    : [];
}
function isNodeStyleEventEmitter(target) {
    return isFunction(target.addListener) && isFunction(target.removeListener);
}
function isJQueryStyleEventEmitter(target) {
    return isFunction(target.on) && isFunction(target.off);
}
function isEventTarget(target) {
    return isFunction(target.addEventListener) && isFunction(target.removeEventListener);
}
