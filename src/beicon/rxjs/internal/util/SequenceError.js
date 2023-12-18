export class SequenceError extends Error {
    constructor(message) {
        super(message);
        this.name = 'SequenceError';
    }
}
