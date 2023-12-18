export const timeoutProvider = {
    setTimeout(handler, timeout, ...args) {
        const { delegate } = timeoutProvider;
        if (delegate?.setTimeout) {
            return delegate.setTimeout(handler, timeout, ...args);
        }
        return setTimeout(handler, timeout, ...args);
    },
    clearTimeout(handle) {
        const { delegate } = timeoutProvider;
        return (delegate?.clearTimeout || clearTimeout)(handle);
    },
    delegate: undefined,
};
