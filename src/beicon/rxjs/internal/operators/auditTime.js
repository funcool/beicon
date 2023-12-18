import { asyncScheduler } from '../scheduler/async.js';
import { audit } from './audit.js';
import { timer } from '../observable/timer.js';
export function auditTime(duration, scheduler = asyncScheduler) {
    return audit(() => timer(duration, scheduler));
}
