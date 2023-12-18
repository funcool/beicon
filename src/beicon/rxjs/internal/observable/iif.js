import { defer } from './defer.js';
export function iif(condition, trueResult, falseResult) {
    return defer(() => (condition() ? trueResult : falseResult));
}
