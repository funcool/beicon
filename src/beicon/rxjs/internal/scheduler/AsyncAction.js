import { Action } from './Action.js';
import { intervalProvider } from './intervalProvider.js';
import { arrRemove } from '../util/arrRemove.js';
export class AsyncAction extends Action {
    scheduler;
    work;
    id;
    state;
    delay;
    pending = false;
    constructor(scheduler, work) {
        super(scheduler, work);
        this.scheduler = scheduler;
        this.work = work;
    }
    schedule(state, delay = 0) {
        if (this.closed) {
            return this;
        }
        this.state = state;
        const id = this.id;
        const scheduler = this.scheduler;
        if (id != null) {
            this.id = this.recycleAsyncId(scheduler, id, delay);
        }
        this.pending = true;
        this.delay = delay;
        this.id = this.id ?? this.requestAsyncId(scheduler, this.id, delay);
        return this;
    }
    requestAsyncId(scheduler, _id, delay = 0) {
        return intervalProvider.setInterval(scheduler.flush.bind(scheduler, this), delay);
    }
    recycleAsyncId(_scheduler, id, delay = 0) {
        if (delay != null && this.delay === delay && this.pending === false) {
            return id;
        }
        if (id != null) {
            intervalProvider.clearInterval(id);
        }
        return undefined;
    }
    execute(state, delay) {
        if (this.closed) {
            return new Error('executing a cancelled action');
        }
        this.pending = false;
        const error = this._execute(state, delay);
        if (error) {
            return error;
        }
        else if (this.pending === false && this.id != null) {
            this.id = this.recycleAsyncId(this.scheduler, this.id, null);
        }
    }
    _execute(state, _delay) {
        let errored = false;
        let errorValue;
        try {
            this.work(state);
        }
        catch (e) {
            errored = true;
            errorValue = e ? e : new Error('Scheduled action threw falsy error');
        }
        if (errored) {
            this.unsubscribe();
            return errorValue;
        }
    }
    unsubscribe() {
        if (!this.closed) {
            const { id, scheduler } = this;
            const { actions } = scheduler;
            this.work = this.state = this.scheduler = null;
            this.pending = false;
            arrRemove(actions, this);
            if (id != null) {
                this.id = this.recycleAsyncId(scheduler, id, null);
            }
            this.delay = null;
            super.unsubscribe();
        }
    }
}
