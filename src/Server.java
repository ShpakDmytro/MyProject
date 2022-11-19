
import java.net.*;
import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;


public class Server {
    static final int port = 8080;

    public Server() {

    }

    public static void main(String[] args) {
        new Server().run();
    }

    public void run() {

        try {
            ServerSocket socket = new ServerSocket(port);

            while (true) {
                Socket connection = socket.accept();

                try {
                    BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    PrintStream pout = new PrintStream(new BufferedOutputStream(connection.getOutputStream()));

                    programLogic(readRequest(in), pout);

                } catch (Throwable tri) {
                    System.err.println("Error handling request: " + tri);
                }
                connection.close();
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

        while (true) {
            String line = in.readLine();

            if (line == null || line.length() == 0) break;
            else requestInSb.append(line).append("\n");

            if (line.split(":")[0].equals("Content-Length")) {
                contentLength = Integer.parseInt(line.split(":")[1].trim());
            }

            if (line.contains(":")) {
                headers.add(line);
            }
        }
        requestInSb.append("\n");

        int read = 0;
        while (read < contentLength) {
            requestInSb.append((char) in.read());
            read++;
        }

        String requestAsString = requestInSb.toString();
        System.out.println(requestAsString);
        String method = requestAsString.split("\n")[0].split(" ")[0];
        String command = requestAsString.split("\n")[0].split(" ")[1].split("\\?")[0];
        System.out.println(method);
        System.out.println(command);

        String body = "";

        try {
            body = requestAsString.split("\n\n")[1];
        } catch (ArrayIndexOutOfBoundsException ignored) {
        }
        System.out.println(body);
        if (requestAsString.split("\n")[0].contains("?")) {
            String queryString = requestAsString.split("\n")[0].split("\\?")[1].split(" ")[0];
            String[] querys = queryString.split("&");
            for (String query : querys) {
                queryStringAsHashMap.put(query.split("=")[0], query.split("=")[1]);
            }
        }

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
            if (objRequest.getEndpoint().equals("POST /sign-up")) {
                response = new UserController().cmdSignUp(objRequest);

            } else if (objRequest.getEndpoint().equals("POST /finish-sign-up")) {
                response = new UserController().cmdFinishSignUp(objRequest);

            } else if (objRequest.getEndpoint().equals("POST /sign-in")) {
                response = new UserController().cmdSignIn(objRequest);

            } else if (objRequest.getEndpoint().equals("POST /sign-out")) {
                response = new UserController().cmdSignOut(objRequest);

            } else if (objRequest.getEndpoint().equals("POST /product")) {
                response = new ProductController().cmdNewProduct(objRequest);

            } else if (objRequest.getEndpoint().equals("GET /users")) {
                response = new UserController().cmdFindUsers(objRequest);

            } else if (objRequest.getEndpoint().equals("GET /products")) {
                response = new ProductController().cmdFindProducts(objRequest);

            } else if (objRequest.getEndpoint().equals("POST /bought-product")) {
                response = new ProductController().cmdBuyProduct(objRequest);

            } else if (objRequest.getEndpoint().equals("PATCH /product")) {
                response = new ProductController().cmdPatchProduct(objRequest);

            } else if (objRequest.getEndpoint().equals("PATCH /user")) {
                response = new UserController().cmdPatchUser(objRequest);

            } else if (objRequest.getEndpoint().equals("POST /forgot-password")) {
                response = new UserController().cmdForgotPassword(objRequest);

            } else if (objRequest.getEndpoint().equals("POST /reset-password/finish")) {
                response = new UserController().cmdResetPasswordFinish(objRequest);

            } else if (objRequest.getEndpoint().equals("DELETE /product")) {
                response = new ProductController().cmdDeleteProduct(objRequest);

            } else if (objRequest.getEndpoint().equals("DELETE /user")) {
                response = new UserController().cmdDeleteUser(objRequest);

            } else {
                response = new UnsuccessfulResponse("404 Not Found", "Unknown command");
            }
        } catch (Throwable throwable) {
            response = new UnsuccessfulResponse("500 Internal Server Error", "Server mistake");
        }
        pout.print(response.serialize());
        pout.close();

    }
}
