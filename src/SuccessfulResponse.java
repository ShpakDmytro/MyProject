public class SuccessfulResponse implements Response {
    String status;
    String bodyResponse;

    public SuccessfulResponse(String status, String bodyResponse) {
        this.status = "HTTP/1.0" + status;
        this.bodyResponse = "{\"message\":\"" + bodyResponse +"\"}";
    }

    public String serialize() {
        return this.status + "\r\n" + "Content-length: " + this.bodyResponse.length() + "\r\n" +
                "\r\n" + this.bodyResponse;
    }
}
