import { dateTimestampProvider } from './scheduler/dateTimestampProvider.js';
export class Scheduler {
    schedulerActionCtor;
    static now = dateTimestampProvider.now;
    constructor(schedulerActionCtor, now = Scheduler.now) {
        this.schedulerActionCtor = schedulerActionCtor;
        this.now = now;
    }
    now;
    schedule(work, delay = 0, state) {
        return new this.schedulerActionCtor(this, work).schedule(state, delay);
    }
}
