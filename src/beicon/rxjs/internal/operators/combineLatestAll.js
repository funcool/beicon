import { combineLatest } from '../observable/combineLatest.js';
import { joinAllInternals } from './joinAllInternals.js';
export function combineLatestAll(project) {
    return joinAllInternals(combineLatest, project);
}
