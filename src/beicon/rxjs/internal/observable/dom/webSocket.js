import { WebSocketSubject } from './WebSocketSubject.js';
export function webSocket(urlConfigOrSource) {
    return new WebSocketSubject(urlConfigOrSource);
}
