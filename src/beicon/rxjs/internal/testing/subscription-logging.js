export class SubscriptionLog {
    subscribedFrame;
    unsubscribedFrame;
    constructor(subscribedFrame, unsubscribedFrame = Infinity) {
        this.subscribedFrame = subscribedFrame;
        this.unsubscribedFrame = unsubscribedFrame;
    }
}
export function logUnsubscribedFrame(index) {
    const subscriptionLogs = this.subscriptions;
    const oldSubscriptionLog = subscriptionLogs[index];
    subscriptionLogs[index] = new SubscriptionLog(oldSubscriptionLog.subscribedFrame, this.scheduler.now());
}
export function logSubscribedFrame() {
    this.subscriptions.push(new SubscriptionLog(this.scheduler.now()));
    return this.subscriptions.length - 1;
}
