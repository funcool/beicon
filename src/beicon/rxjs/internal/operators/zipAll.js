import { zip } from '../observable/zip.js';
import { joinAllInternals } from './joinAllInternals.js';
export function zipAll(project) {
    return joinAllInternals(zip, project);
}
