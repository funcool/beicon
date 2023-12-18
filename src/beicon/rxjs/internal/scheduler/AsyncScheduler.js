import { Scheduler } from '../Scheduler.js';
export class AsyncScheduler extends Scheduler {
    actions = [];
    _active = false;
    _scheduled;
    constructor(SchedulerAction, now = Scheduler.now) {
        super(SchedulerAction, now);
    }
    flush(action) {
        const { actions } = this;
        if (this._active) {
            actions.push(action);
            return;
        }
        let error;
        this._active = true;
        do {
            if ((error = action.execute(action.state, action.delay))) {
                break;
            }
        } while ((action = actions.shift()));
        this._active = false;
        if (error) {
            while ((action = actions.shift())) {
                action.unsubscribe();
            }
            throw error;
        }
    }
}
