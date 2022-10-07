public class Response {
    String status;
    String bodyResponse;

    public Response(String status, String bodyResponse) {
        this.status = status;
        this.bodyResponse = bodyResponse;
    }

    public String serialize() {
        return this.status + "\r\n" + "Content-length: " + this.bodyResponse.length() + "\r\n" +
                "\r\n" + this.bodyResponse;
    }
}
