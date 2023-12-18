import { raceInit } from '../observable/race.js';
import { Observable } from '../Observable.js';
import { identity } from '../util/identity.js';
export function raceWith(...otherSources) {
    return !otherSources.length
        ? identity
        : (source) => new Observable((subscriber) => {
            raceInit([source, ...otherSources])(subscriber);
        });
}
