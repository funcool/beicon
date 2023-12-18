export class AjaxError extends Error {
    xhr;
    request;
    status;
    responseType;
    response;
    constructor(message, xhr, request) {
        super(message);
        this.name = 'AjaxError';
        this.xhr = xhr;
        this.request = request;
        this.status = xhr.status;
        this.responseType = xhr.responseType;
        this.response = xhr.response;
    }
}
export class AjaxTimeoutError extends AjaxError {
    constructor(xhr, request) {
        super('ajax timeout', xhr, request);
        this.name = 'AjaxTimeoutError';
    }
}
