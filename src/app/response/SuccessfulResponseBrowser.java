package app.response;

import app.Response;

public class SuccessfulResponseBrowser implements Response {

    private String status;
    private String bodyResponse;

    public SuccessfulResponseBrowser(String status, String bodyResponse) {
        this.status = "HTTP/1.0 " + status;
        this.bodyResponse = bodyResponse;
    }

    public String serialize() {
        return this.status + "\r\n" + "Content-length: " + this.bodyResponse.length() + "\r\n" +
                "Content-type:  text/html" + "\r\n" + "\r\n" + this.bodyResponse;
    }

}
