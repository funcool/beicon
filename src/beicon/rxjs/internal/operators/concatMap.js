import { mergeMap } from './mergeMap.js';
export function concatMap(project) {
    return mergeMap(project, 1);
}
