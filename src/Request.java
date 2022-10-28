import java.util.ArrayList;
import java.util.List;

public class Request {
    String method;
    String command;
    String body;
    String endpoint;
    ArrayList <HTTPHeader> headers;

    public Request(String method, String command, String body,ArrayList <HTTPHeader> headers ){
        this.method = method;
        this.command = command;
        this.body = body;
        this.endpoint = this.method + " " + this.command;
        this.headers = headers;
    }

    public String getEndpoint(){
        return this.endpoint;
    }

}
