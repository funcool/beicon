import { AsyncAction } from './AsyncAction.js';
import { animationFrameProvider } from './animationFrameProvider.js';
export class AnimationFrameAction extends AsyncAction {
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
        return scheduler._scheduled || (scheduler._scheduled = animationFrameProvider.requestAnimationFrame(() => scheduler.flush(undefined)));
    }
    recycleAsyncId(scheduler, id, delay = 0) {
        if (delay != null ? delay > 0 : this.delay > 0) {
            return super.recycleAsyncId(scheduler, id, delay);
        }
        const { actions } = scheduler;
        if (id != null && actions[actions.length - 1]?.id !== id) {
            animationFrameProvider.cancelAnimationFrame(id);
            scheduler._scheduled = undefined;
        }
        return undefined;
    }
}
