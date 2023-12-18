import { Subscription } from '../Observable.js';
export const animationFrameProvider = {
    schedule(callback) {
        let request = requestAnimationFrame;
        let cancel = cancelAnimationFrame;
        const { delegate } = animationFrameProvider;
        if (delegate) {
            request = delegate.requestAnimationFrame;
            cancel = delegate.cancelAnimationFrame;
        }
        const handle = request((timestamp) => {
            cancel = undefined;
            callback(timestamp);
        });
        return new Subscription(() => cancel?.(handle));
    },
    requestAnimationFrame(...args) {
        const { delegate } = animationFrameProvider;
        return (delegate?.requestAnimationFrame || requestAnimationFrame)(...args);
    },
    cancelAnimationFrame(...args) {
        const { delegate } = animationFrameProvider;
        return (delegate?.cancelAnimationFrame || cancelAnimationFrame)(...args);
    },
    delegate: undefined,
};
