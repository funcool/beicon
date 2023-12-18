export const dateTimestampProvider = {
    now() {
        return (dateTimestampProvider.delegate || Date).now();
    },
    delegate: undefined,
};
