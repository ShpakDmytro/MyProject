

public class SuccessfulResponseSignUp implements Response {

    String status;
    String bodyResponse;
    String accessToken;

    public SuccessfulResponseSignUp(String status, String accessToken) {
        this.status = "HTTP/1.0 " + status;
        this.accessToken = accessToken;
        this.bodyResponse = "{\"accessToken\":\"" + this.accessToken + "\"}";
    }

    public String serialize() {
        return this.status + "\r\n" + "Content-length: " + this.bodyResponse.length() + "\r\n" +
                "Content-type: application/json" + "\r\n" + "\r\n" + this.bodyResponse;
    }

}
