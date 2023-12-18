export const intervalProvider = {
    setInterval(handler, timeout, ...args) {
        const { delegate } = intervalProvider;
        if (delegate?.setInterval) {
            return delegate.setInterval(handler, timeout, ...args);
        }
        return setInterval(handler, timeout, ...args);
    },
    clearInterval(handle) {
        const { delegate } = intervalProvider;
        return (delegate?.clearInterval || clearInterval)(handle);
    },
    delegate: undefined,
};
