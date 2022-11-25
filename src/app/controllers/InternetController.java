package app.controllers;

import app.*;
import app.response.SuccessfulResponseBrowser;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;


@Controller
public class InternetController {

    Logger logger;

    public InternetController(){
        this.logger = new Logger();
    }

    @EndpointHandler(endpoint = "GET /hello")
    public Response cmdHello (Request objRequest){
        StringBuilder response = new StringBuilder();
        try {
            BufferedReader reader = new BufferedReader(
                    new FileReader("D:\\projects\\Myprodject\\html\\hello.html"));

            while (true){
                 String line = reader.readLine();
                if (line == null)
                    break;
                response.append(line).append("\n");
            }

            reader.close();

            return new SuccessfulResponseBrowser("200 OK", response.toString());
        } catch (IOException e) {
            logger.log(e.getMessage(),"ERROR");
            throw new RuntimeException(e);
        }
    }
}
