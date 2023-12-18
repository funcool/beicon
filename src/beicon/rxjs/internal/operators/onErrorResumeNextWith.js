import { argsOrArgArray } from '../util/argsOrArgArray.js';
import { onErrorResumeNext as oERNCreate } from '../observable/onErrorResumeNext.js';
export function onErrorResumeNextWith(...sources) {
    const nextSources = argsOrArgArray(sources);
    return (source) => oERNCreate(source, ...nextSources);
}
