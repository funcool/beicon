export function observeNotification(notification, observer) {
    const { kind, value, error } = notification;
    if (typeof kind !== 'string') {
        throw new TypeError('Invalid notification, missing "kind"');
    }
    kind === 'N' ? observer.next?.(value) : kind === 'E' ? observer.error?.(error) : observer.complete?.();
}
