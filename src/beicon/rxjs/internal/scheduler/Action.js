import { Subscription } from '../Observable.js';
export class Action extends Subscription {
    constructor(scheduler, work) {
        super();
    }
    schedule(state, delay = 0) {
        return this;
    }
}
