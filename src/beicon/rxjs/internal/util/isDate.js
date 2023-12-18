export function isValidDate(value) {
    return value instanceof Date && !isNaN(value);
}
