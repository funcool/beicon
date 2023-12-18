import { Observable, Subscription } from '../Observable.js';
import { observeNotification } from '../Notification.js';
import { logSubscribedFrame, logUnsubscribedFrame } from './subscription-logging.js';
export class ColdObservable extends Observable {
    messages;
    scheduler;
    subscriptions = [];
    logSubscribedFrame = logSubscribedFrame;
    logUnsubscribedFrame = logUnsubscribedFrame;
    _subscribe(subscriber) {
        const index = this.logSubscribedFrame();
        const subscription = new Subscription();
        subscription.add(new Subscription(() => {
            this.logUnsubscribedFrame(index);
        }));
        this.scheduleMessages(subscriber);
        return subscription;
    }
    constructor(messages, scheduler) {
        super();
        this.messages = messages;
        this.scheduler = scheduler;
    }
    scheduleMessages(subscriber) {
        const messagesLength = this.messages.length;
        for (let i = 0; i < messagesLength; i++) {
            const message = this.messages[i];
            subscriber.add(this.scheduler.schedule((state) => {
                const { message: { notification }, subscriber: destination, } = state;
                observeNotification(notification, destination);
            }, message.frame, { message, subscriber }));
        }
    }
}
