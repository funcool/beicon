const { isArray } = Array;
const { getPrototypeOf, prototype: objectProto, keys: getKeys } = Object;
export function argsArgArrayOrObject(args) {
    if (args.length === 1) {
        const first = args[0];
        const result = arrayOrObject(first);
        if (result) {
            return result;
        }
    }
    return { args: args, keys: null };
}
export function arrayOrObject(first) {
    if (isArray(first)) {
        return { args: first, keys: null };
    }
    if (isPOJO(first)) {
        const keys = getKeys(first);
        return {
            args: keys.map((key) => first[key]),
            keys,
        };
    }
    return null;
}
function isPOJO(obj) {
    return obj && typeof obj === 'object' && getPrototypeOf(obj) === objectProto;
}
