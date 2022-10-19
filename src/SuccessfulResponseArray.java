import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.HashMap;

public class SuccessfulResponseArray implements Response {
    String status;
    String bodyResponse;

    public SuccessfulResponseArray(String status, ArrayList<HashMap> bodyResponse) {
        ObjectMapper mapper = new ObjectMapper();
        this.status = "HTTP/1.0 " + status;
        try {
            this.bodyResponse = mapper.writeValueAsString(bodyResponse);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public String serialize() {
        return this.status + "\r\n" + "Content-length: " + this.bodyResponse.length() + "\r\n" +
                "Content-type: application/json" + "\r\n" + "\r\n" + this.bodyResponse;
    }
}
