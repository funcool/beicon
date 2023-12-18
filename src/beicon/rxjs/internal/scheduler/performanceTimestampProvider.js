export const performanceTimestampProvider = {
    now() {
        return (performanceTimestampProvider.delegate || performance).now();
    },
    delegate: undefined,
};
