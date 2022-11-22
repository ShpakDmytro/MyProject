package app;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.*;
import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import app.response.*;
import org.reflections.Reflections;


public class Server {
    static final int port = 8080;
    private Logger logger;

    public Server() {
        this.logger = new Logger();
    }

    public static void main(String[] args) {
        new Server().run();
    }

    public void run() {

        try {
            ServerSocket socket = new ServerSocket(port);

            while (true) {
                Socket connection = socket.accept();
                logger.log("Start connection", "INFO",getClass().toString());

                try {
                    BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    PrintStream pout = new PrintStream(new BufferedOutputStream(connection.getOutputStream()));

                    programLogic(readRequest(in), pout);

                } catch (Throwable tri) {
                    tri.printStackTrace();
                    System.err.println("Error handling request: " + tri);
                }
                connection.close();
                logger.log("Finish connection","INFO",getClass().toString());
            }
        } catch (Throwable tr) {
            System.err.println("Could not start server: " + tr);
        }
    }

    private Request readRequest(BufferedReader in) throws IOException {
        int contentLength = 0;

        ArrayList<String> headers = new ArrayList<>();
        StringBuilder requestInSb = new StringBuilder();
        HashMap<String, String> queryStringAsHashMap = new HashMap<>();
        int count = 0;
        String method = "";
        String command = "";

        while (true) {
            String line = in.readLine();

            if (line == null || line.length() == 0) break;
            else requestInSb.append(line).append("\r\n");

            if (count == 0){
                method = line.split(" ")[0];
                command = line.split(" ")[1].split("\\?")[0];

                if (line.split(" ")[1].contains("?")) {
                    String queryString = line.split(" ")[1].split("\\?")[1];
                    logger.log(queryString,"INFO",getClass().toString());
                    String[] querys = queryString.split("&");
                    for (String query : querys) {
                        queryStringAsHashMap.put(query.split("=")[0], query.split("=")[1]);
                    }
                }
            }

            if (line.split(":")[0].equals("Content-Length")) {
                contentLength = Integer.parseInt(line.split(":")[1].trim());
            }

            if (line.contains(":")) {
                headers.add(line);
            }
            count++;
        }
        requestInSb.append("\r\n");

        int read = 0;
        while (read < contentLength) {
            requestInSb.append((char) in.read());
            read++;
        }

        String requestAsString = requestInSb.toString();

        String body = "";

        try {
            body = requestAsString.split("\r\n\r\n")[1];
        } catch (ArrayIndexOutOfBoundsException e) {
            logger.log(e.getMessage(),"ERROR",getClass().toString());
        }
        logger.log(body,"INFO",getClass().toString());


        ArrayList<HTTPHeader> headersAsObject = new ArrayList<>();
        for (String header : headers) {
            HTTPHeader httpHeader = new HTTPHeader(header.split(":")[0], header.split(":")[1]);
            headersAsObject.add(httpHeader);
        }

        return new Request(method, command, body, headersAsObject, queryStringAsHashMap);

    }

    private void programLogic(Request objRequest, PrintStream pout) {

        Response response = null;
        try {
            response = checkRequestForEndpoint(objRequest);
            if (response == null) {
                response = new UnsuccessfulResponse("404 Not Found", "Unknown command");
            }

        } catch (Throwable e) {
            logger.log(e.getMessage(),"ERROR",getClass().toString());
            response = new UnsuccessfulResponse("500 Internal Server Error", "Server mistake");
        }

        pout.print(response.serialize());
        logger.log(response.serialize(), "INFO",getClass().toString());
        pout.close();

    }

    private Response checkRequestForEndpoint(Request objRequest) {

        Reflections ref = new Reflections("app");
        for (Class<?> cl : ref.getTypesAnnotatedWith(Controller.class)) {

            Method[] methods = cl.getDeclaredMethods();
            for (Method m : methods) {

                Annotation[] annotations = m.getDeclaredAnnotations();
                for (Annotation an : annotations) {

                    if (an instanceof EndpointHandler) {

                        if (((EndpointHandler) an).endpoint().equals(objRequest.getEndpoint())) {
                            try {
                                logger.log(m.getName(),"INFO",getClass().toString());
                                return (Response) m.invoke(cl.getDeclaredConstructor().newInstance(), objRequest);
                            } catch (IllegalAccessException | InvocationTargetException | InstantiationException |
                                     NoSuchMethodException e) {
                                logger.log("Parse annotation exception" + e,"ERROR",getClass().toString());
                                throw new RuntimeException(e);
                            }
                        }
                    }
                }
            }
        }
        return null;
    }
}
