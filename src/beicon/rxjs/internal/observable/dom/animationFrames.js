import { Observable } from '../../Observable.js';
import { performanceTimestampProvider } from '../../scheduler/performanceTimestampProvider.js';
import { animationFrameProvider } from '../../scheduler/animationFrameProvider.js';
export function animationFrames(timestampProvider) {
    return timestampProvider ? animationFramesFactory(timestampProvider) : DEFAULT_ANIMATION_FRAMES;
}
function animationFramesFactory(timestampProvider) {
    return new Observable((subscriber) => {
        const provider = timestampProvider || performanceTimestampProvider;
        const start = provider.now();
        let id = 0;
        const run = () => {
            if (!subscriber.closed) {
                id = animationFrameProvider.requestAnimationFrame((timestamp) => {
                    id = 0;
                    const now = provider.now();
                    subscriber.next({
                        timestamp: timestampProvider ? now : timestamp,
                        elapsed: now - start,
                    });
                    run();
                });
            }
        };
        run();
        return () => {
            if (id) {
                animationFrameProvider.cancelAnimationFrame(id);
            }
        };
    });
}
const DEFAULT_ANIMATION_FRAMES = animationFramesFactory();
