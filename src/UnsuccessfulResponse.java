public class UnsuccessfulResponse implements Response {
    String status;
    String bodyResponse;

    public UnsuccessfulResponse(String bodyResponse) {
        this.status = "HTTP/1.0 400 Bad Request";
        this.bodyResponse = "{\"error\":\"" + bodyResponse +"\"}";
    }

    public String serialize() {
        return this.status + "\r\n" + "Content-length: " + this.bodyResponse.length() + "\r\n" +
                "\r\n" + this.bodyResponse ;
    }
}
