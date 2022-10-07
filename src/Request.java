public class Request {
    String method;
    String command;
    String body;
    String endpoint;
    public Request(String method, String command, String body){
        this.method = method;
        this.command = command;
        this.body = body;
        this.endpoint = this.method + " " + this.command;
    }

    public String getEndpoint(){
        return this.endpoint;
    }

}
