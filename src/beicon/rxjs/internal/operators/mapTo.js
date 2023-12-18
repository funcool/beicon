import { map } from './map.js';
export function mapTo(value) {
    return map(() => value);
}
