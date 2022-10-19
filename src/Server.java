import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.*;
import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;

public class Server {
    static final int port = 8080;

    Database database;

    public Server() {
        this.database = new Database();
    }

    public static void main(String[] args) {
        new Server().run();
    }

    public void run() {

        try {
            this.database.loadData();
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
        StringBuilder requestInSb = new StringBuilder();
        while (true) {
            String line = in.readLine();
            if (line == null || line.length() == 0) break;
            else requestInSb.append(line).append("\n");

            if (line.split(":")[0].equals("Content-Length")) {
                contentLength = Integer.parseInt(line.split(":")[1].trim());
            }
        }
        requestInSb.append("\n");

        int read = 0;
        while (read < contentLength) {
            requestInSb.append((char) in.read());
            read++;
        }
        String request = requestInSb.toString();
        String method = request.split("\n")[0].split(" ")[0];
        String command = request.split("\n")[0].split(" ")[1];
        String body = "";
        try {
            body = request.split("\n\n")[1];
        } catch (ArrayIndexOutOfBoundsException ignored) {
        }
        return new Request(method, command, body);
    }

    private void programLogic(Request objRequest, PrintStream pout) {
        Response response = null;

        if (objRequest.getEndpoint().equals("POST /sign-up")) {
            response = cmdSignUp(objRequest);
            database.saveData();
        } else if (objRequest.getEndpoint().equals("POST /sign-in")) {
            response = cmdSignIn(objRequest);
            database.saveData();
        } else if (objRequest.getEndpoint().equals("POST /sign-out")) {
            response = cmdSignOut(objRequest);
            database.saveData();
        } else if (objRequest.getEndpoint().equals("POST /product")) {
            response = cmdNewProduct(objRequest);
            database.saveData();
        } else if (objRequest.getEndpoint().equals("GET /users")) {
            response = cmdListUsers();
        } else if (objRequest.getEndpoint().equals("GET /products")) {
            response = cmdListProducts();
        } else if (objRequest.getEndpoint().equals("POST /bought-product")) {
            response = cmdBuyProduct(objRequest);
            database.saveData();
        } else if (objRequest.getEndpoint().equals("GET /user-products")) {
            response = cmdListUsersProduct(objRequest);
        } else if (objRequest.getEndpoint().equals("GET /product-users")) {
            response = cmdListProductUsers(objRequest);
        } else {
            response = new UnsuccessfulResponse("404 Not Found", "Unknown command");
        }
        pout.print(response.serialize());
        System.out.println(response.serialize());
        pout.close();
    }

    public Response cmdSignUp(Request objRequest) {
        ObjectMapper mapper = new ObjectMapper();

        try {
            HashMap requestBody = mapper.readValue(objRequest.body, HashMap.class);
            if ((Double) requestBody.get("amount") <= 0) {
                return new UnsuccessfulResponse("400 Bad Request", "Wrong amount value");
            }

            if (database.existsUserByLogin((String) requestBody.get("login"))) {
                return new UnsuccessfulResponse("400 Bad Request", "This login already exists");
            }

            User user = new User(database.nextId(), (String) requestBody.get("firstName"),
                    (String) requestBody.get("lastName"), (Double) requestBody.get("amount"),
                    (String) requestBody.get("login"), (String) requestBody.get("password"));
            database.addUser(user);

        } catch (JsonProcessingException e) {
            return new UnsuccessfulResponse("400 Bad Request", "Wrong request format");
        }

        return new SuccessfulResponse("200 OK", "Successful add new user");
    }

    private Response cmdSignIn(Request objRequest) {
        ObjectMapper mapper = new ObjectMapper();

        try {
            HashMap requestBody = mapper.readValue(objRequest.body, HashMap.class);
            String login = (String) requestBody.get("login");
            String password = (String) requestBody.get("password");

            User user = database.findUserByLoginAndPassword(login, password);
            if (user != null) {
                String accessToken = new TokenGenerator().generateToken();
                user.setAccessToken(accessToken);
                return new SuccessfulResponseSignIn(accessToken);
            }

        } catch (JsonProcessingException e) {
            return new UnsuccessfulResponse("400 Bad Request", "Wrong request format");
        }

        return new UnsuccessfulResponse("400 Bad Request", "No user found");
    }

    private Response cmdSignOut(Request objRequest) {
        ObjectMapper mapper = new ObjectMapper();

        try {
            HashMap requestBody = mapper.readValue(objRequest.body, HashMap.class);

            User user = database.findUserByAccessToken((String) requestBody.get("accessToken"));
            System.out.println((String) requestBody.get("accessToken"));
            if (user != null) {
                user.setAccessToken(null);
                return new SuccessfulResponse("200 OK", "The exit has been successfully completed");
            }

        } catch (JsonProcessingException e) {
            return new UnsuccessfulResponse("400 Bad Request", "Wrong request format");
        }

        return new UnsuccessfulResponse("400 Bad Request", "No user found");
    }

    public Response cmdNewProduct(Request objRequest) {
        ObjectMapper mapper = new ObjectMapper();

        try {
            HashMap createProduct = mapper.readValue(objRequest.body, HashMap.class);
            if ((Double) createProduct.get("price") <= 0) {
                return new UnsuccessfulResponse("400", "Wrong amount value");
            }
            Product product = new Product(database.nextId(), (String) createProduct.get("name"), (Double) createProduct.get("price"));

            database.addProduct(product);

        } catch (JsonProcessingException e) {
            return new UnsuccessfulResponse("400 Bad Request", "Wrong request format");
        }

        return new SuccessfulResponse("200 OK", "Add product successful");
    }

    public Response cmdListUsers() {
        ObjectMapper mapper = new ObjectMapper();

        String response = null;
        try {
            response = mapper.writeValueAsString(database.getAllUser());
        } catch (JsonProcessingException ignored) {
        }

        return new SuccessfulResponse("200 OK", response);
    }

    public Response cmdListProducts() {
        ObjectMapper mapper = new ObjectMapper();

        String response = null;
        try {
            response = mapper.writeValueAsString(database.getAllProduct());
        } catch (JsonProcessingException ignored) {
        }

        return new SuccessfulResponse("200 OK", response);
    }

    public Response cmdBuyProduct(Request objRequest) {
        ObjectMapper mapper = new ObjectMapper();
        int userIdForBuying;
        int productIdForBuying;

        try {
            HashMap buyingRequest = mapper.readValue(objRequest.body, HashMap.class);
            userIdForBuying = (int) buyingRequest.get("userId");
            productIdForBuying = (int) buyingRequest.get("productId");

        } catch (JsonProcessingException e) {
            return new UnsuccessfulResponse("400 Bad Request", "Wrong request format");
        }

        User user = database.findUserById(userIdForBuying);
        if (user == null) {
            return new UnsuccessfulResponse("400 Bad Request", "Wrong user id");
        }

        Product product = database.findProductById(productIdForBuying);
        if (product == null) {
            return new UnsuccessfulResponse("400 Bad Request", "Wrong product id");
        }

        try {
            user.buyProduct(product);
            product.addUser(user);
            return new SuccessfulResponse("200 OK", "You did successful buying");
        } catch (Exception e) {
            return new UnsuccessfulResponse("400 Bad Request", "You haven`t enough money");
        }
    }

    public Response cmdListUsersProduct(Request objRequest) {

        int number = Integer.parseInt(objRequest.body.split(",")[0]);

        User user = database.findUserById(number);

        if (user == null) {
            return new UnsuccessfulResponse("400 Bad Request", "Wrong user id");
        }

        ArrayList<Product> buying = user.getBoughtList();
        if (buying.size() < 1) {
            return new UnsuccessfulResponse("400 Bad Request", "You haven`t buying");
        } else {
            StringBuilder result = new StringBuilder();
            for (int i = 0; i < buying.size(); i++) {
                Product buy = buying.get(i);
                result.append(i).append(": ").append(buy.getName()).append("\n");
            }
            return new SuccessfulResponse("200 OK", result.toString());
        }
    }

    public Response cmdListProductUsers(Request objRequest) {

        String checkProductAsString = objRequest.body.split(",")[0];
        int checkProductAsInt = Integer.parseInt(checkProductAsString);

        Product product = database.findProductById(checkProductAsInt);

        if (product != null) {
            StringBuilder result = new StringBuilder();
            for (int j = 0; j < product.howManyUsers(); j++) {
                User user = null;
                try {
                    user = product.getUserAtIndex(j);
                    result.append(j).append(": ");
                    result.append(user.getFirstName()).append(" ").append(user.getLastName());
                    result.append("\n");
                } catch (Exception ignored) {
                }
            }
            return new SuccessfulResponse("200 OK", result.toString());
        } else {
            return new SuccessfulResponse("200 OK", "Product don`t buying");
        }
    }
}
