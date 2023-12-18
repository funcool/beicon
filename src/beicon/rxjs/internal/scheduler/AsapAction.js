import { AsyncAction } from './AsyncAction.js';
import { immediateProvider } from './immediateProvider.js';
export class AsapAction extends AsyncAction {
    scheduler;
    work;
    constructor(scheduler, work) {
        super(scheduler, work);
        this.scheduler = scheduler;
        this.work = work;
    }
    requestAsyncId(scheduler, id, delay = 0) {
        if (delay !== null && delay > 0) {
            return super.requestAsyncId(scheduler, id, delay);
        }
        scheduler.actions.push(this);
        return scheduler._scheduled || (scheduler._scheduled = immediateProvider.setImmediate(scheduler.flush.bind(scheduler, undefined)));
    }
    recycleAsyncId(scheduler, id, delay = 0) {
        if (delay != null ? delay > 0 : this.delay > 0) {
            return super.recycleAsyncId(scheduler, id, delay);
        }
        const { actions } = scheduler;
        if (id != null && actions[actions.length - 1]?.id !== id) {
            immediateProvider.clearImmediate(id);
            if (scheduler._scheduled === id) {
                scheduler._scheduled = undefined;
            }
        }
        return undefined;
    }
}
