public class UnsuccessfulResponse implements Response {
    String status;
    String bodyResponse;

    public UnsuccessfulResponse(String status, String bodyResponse) {
        this.status = "HTTP/1.0 " + status;
        this.bodyResponse = "{\"error\":\"" + bodyResponse + "\"}";
    }

    public String serialize() {
        return this.status + "\r\n" + "Content-length: " + this.bodyResponse.length() + "\r\n" +
                "\r\n" + this.bodyResponse;
    }
}
