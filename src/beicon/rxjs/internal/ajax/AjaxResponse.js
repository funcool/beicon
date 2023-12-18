export class AjaxResponse {
    originalEvent;
    xhr;
    request;
    type;
    status;
    response;
    responseType;
    loaded;
    total;
    responseHeaders;
    constructor(originalEvent, xhr, request, type = 'download_load') {
        this.originalEvent = originalEvent;
        this.xhr = xhr;
        this.request = request;
        this.type = type;
        const { status, responseType } = xhr;
        this.status = status ?? 0;
        this.responseType = responseType ?? '';
        const allHeaders = xhr.getAllResponseHeaders();
        this.responseHeaders = allHeaders
            ?
                allHeaders.split('\n').reduce((headers, line) => {
                    const index = line.indexOf(': ');
                    headers[line.slice(0, index)] = line.slice(index + 2);
                    return headers;
                }, {})
            : {};
        this.response = xhr.response;
        const { loaded, total } = originalEvent;
        this.loaded = loaded;
        this.total = total;
    }
}
