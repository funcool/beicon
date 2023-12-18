import { filter } from './filter.js';
export function skip(count) {
    return filter((_, index) => count <= index);
}
