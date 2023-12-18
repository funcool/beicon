import { dateTimestampProvider } from '../scheduler/dateTimestampProvider.js';
import { map } from './map.js';
export function timestamp(timestampProvider = dateTimestampProvider) {
    return map((value) => ({ value, timestamp: timestampProvider.now() }));
}
