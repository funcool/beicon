import { Observable, operate } from '../Observable.js';
import { Subject } from '../Subject.js';
export function windowCount(windowSize, startWindowEvery = 0) {
    const startEvery = startWindowEvery > 0 ? startWindowEvery : windowSize;
    return (source) => new Observable((destination) => {
        let windows = [new Subject()];
        let starts = [];
        let count = 0;
        destination.next(windows[0].asObservable());
        source.subscribe(operate({
            destination,
            next: (value) => {
                for (const window of windows) {
                    window.next(value);
                }
                const c = count - windowSize + 1;
                if (c >= 0 && c % startEvery === 0) {
                    windows.shift().complete();
                }
                if (++count % startEvery === 0) {
                    const window = new Subject();
                    windows.push(window);
                    destination.next(window.asObservable());
                }
            },
            error: (err) => {
                while (windows.length > 0) {
                    windows.shift().error(err);
                }
                destination.error(err);
            },
            complete: () => {
                while (windows.length > 0) {
                    windows.shift().complete();
                }
                destination.complete();
            },
            finalize: () => {
                starts = null;
                windows = null;
            },
        }));
    });
}
