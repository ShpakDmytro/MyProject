import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Request {
    private String method;
    private String command;
    private String body;
    private String endpoint;
    private ArrayList <HTTPHeader> headers;

    private HashMap <String,String> queryString;

    public Request(String method, String command, String body,ArrayList <HTTPHeader> headers, HashMap <String,String> queryString ){
        this.method = method;
        this.command = command;
        this.body = body;
        this.endpoint = this.method + " " + this.command;
        this.headers = headers;
        this.queryString = queryString;
    }

    public String getEndpoint(){
        return this.endpoint;
    }

    public String getBody (){
        return body;
    }

}
