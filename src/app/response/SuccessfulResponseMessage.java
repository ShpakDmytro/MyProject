package app.response;

import app.Response;

public class SuccessfulResponseMessage implements Response {
    private String status;
    private String bodyResponse;

    public SuccessfulResponseMessage(String status, String bodyResponse) {
        this.status = "HTTP/1.0 " + status;
        this.bodyResponse = "{\"message\":\"" + bodyResponse +"\"}";
    }

    public String serialize() {
        return this.status + "\r\n" + "Content-length: " + this.bodyResponse.length() + "\r\n" +
                "Content-type: application/json" + "\r\n" + "\r\n" + this.bodyResponse;
    }
}
