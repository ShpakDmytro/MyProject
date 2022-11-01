

public class SuccessfulResponseSignIn implements Response {

    private String status;
    private String bodyResponse;
    private String accessToken;

    public SuccessfulResponseSignIn(String accessToken) {
        this.status = "HTTP/1.0 200 OK";
        this.accessToken = accessToken;
        this.bodyResponse = "{\"accessToken\":\"" + this.accessToken + "\"}";
    }

    public String serialize() {
        return this.status + "\r\n" + "Content-length: " + this.bodyResponse.length() + "\r\n" +
                "Content-type: application/json" + "\r\n" + "\r\n" + this.bodyResponse;
    }

}
