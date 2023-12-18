import { asyncScheduler } from '../scheduler/async.js';
import { sample } from './sample.js';
import { interval } from '../observable/interval.js';
export function sampleTime(period, scheduler = asyncScheduler) {
    return sample(interval(period, scheduler));
}
