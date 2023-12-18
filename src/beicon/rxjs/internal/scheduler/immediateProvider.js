import { Immediate } from '../util/Immediate.js';
const { setImmediate, clearImmediate } = Immediate;
export const immediateProvider = {
    setImmediate(...args) {
        const { delegate } = immediateProvider;
        return (delegate?.setImmediate || setImmediate)(...args);
    },
    clearImmediate(handle) {
        const { delegate } = immediateProvider;
        return (delegate?.clearImmediate || clearImmediate)(handle);
    },
    delegate: undefined,
};
