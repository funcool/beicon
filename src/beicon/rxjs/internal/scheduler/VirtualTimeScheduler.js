import { AsyncAction } from './AsyncAction.js';
import { Subscription } from '../Observable.js';
import { AsyncScheduler } from './AsyncScheduler.js';
export class VirtualTimeScheduler extends AsyncScheduler {
    maxFrames;
    static frameTimeFactor = 10;
    frame = 0;
    index = -1;
    constructor(schedulerActionCtor = VirtualAction, maxFrames = Infinity) {
        super(schedulerActionCtor, () => this.frame);
        this.maxFrames = maxFrames;
    }
    flush() {
        const { actions, maxFrames } = this;
        let error;
        let action;
        while ((action = actions[0]) && action.delay <= maxFrames) {
            actions.shift();
            this.frame = action.delay;
            if ((error = action.execute(action.state, action.delay))) {
                break;
            }
        }
        if (error) {
            while ((action = actions.shift())) {
                action.unsubscribe();
            }
            throw error;
        }
    }
}
export class VirtualAction extends AsyncAction {
    scheduler;
    work;
    index;
    active = true;
    constructor(scheduler, work, index = (scheduler.index += 1)) {
        super(scheduler, work);
        this.scheduler = scheduler;
        this.work = work;
        this.index = index;
        this.index = scheduler.index = index;
    }
    schedule(state, delay = 0) {
        if (Number.isFinite(delay)) {
            if (!this.id) {
                return super.schedule(state, delay);
            }
            this.active = false;
            const action = new VirtualAction(this.scheduler, this.work);
            this.add(action);
            return action.schedule(state, delay);
        }
        else {
            return Subscription.EMPTY;
        }
    }
    requestAsyncId(scheduler, id, delay = 0) {
        this.delay = scheduler.frame + delay;
        const { actions } = scheduler;
        actions.push(this);
        actions.sort(VirtualAction.sortActions);
        return 1;
    }
    recycleAsyncId(scheduler, id, delay = 0) {
        return undefined;
    }
    _execute(state, delay) {
        if (this.active === true) {
            return super._execute(state, delay);
        }
    }
    static sortActions(a, b) {
        if (a.delay === b.delay) {
            if (a.index === b.index) {
                return 0;
            }
            else if (a.index > b.index) {
                return 1;
            }
            else {
                return -1;
            }
        }
        else if (a.delay > b.delay) {
            return 1;
        }
        else {
            return -1;
        }
    }
}
