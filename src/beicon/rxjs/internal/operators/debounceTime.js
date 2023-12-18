import { asyncScheduler } from '../scheduler/async.js';
import { Observable, operate } from '../Observable.js';
export function debounceTime(dueTime, scheduler = asyncScheduler) {
    return (source) => new Observable((destination) => {
        let activeTask = null;
        let lastValue = null;
        let lastTime = null;
        let scheduling = false;
        const emit = () => {
            if (scheduling || activeTask) {
                if (activeTask) {
                    activeTask.unsubscribe();
                    activeTask = null;
                }
                const value = lastValue;
                lastValue = null;
                destination.next(value);
            }
        };
        function emitWhenIdle() {
            const targetTime = lastTime + dueTime;
            const now = scheduler.now();
            if (now < targetTime) {
                activeTask = this.schedule(undefined, targetTime - now);
                destination.add(activeTask);
                return;
            }
            emit();
        }
        source.subscribe(operate({
            destination,
            next: (value) => {
                lastValue = value;
                lastTime = scheduler.now();
                if (!activeTask) {
                    scheduling = true;
                    activeTask = scheduler.schedule(emitWhenIdle, dueTime);
                    scheduling = false;
                    destination.add(activeTask);
                }
            },
            complete: () => {
                emit();
                destination.complete();
            },
            finalize: () => {
                lastValue = activeTask = null;
            },
        }));
    });
}
