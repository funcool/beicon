import { switchMap } from './switchMap.js';
export function switchMapTo(innerObservable) {
    return switchMap(() => innerObservable);
}
