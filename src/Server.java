import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.*;
import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

public class Server {
    static final int port = 8080;

    private Database database;

    public Server() {
        this.database = new Database();
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
        HashMap <String,String> queryStringAsHashMap = new HashMap<>();

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

        String method = requestAsString.split("\n")[0].split(" ")[0];
        String command = requestAsString.split("\n")[0].split(" ")[1].split("\\?")[0];

        String body = "";

        try {
            body = requestAsString.split("\n\n")[1];
        } catch (ArrayIndexOutOfBoundsException ignored) {}

        if (requestAsString.split("\n")[0].contains("?")){
            String queryString = requestAsString.split("\n")[0].split("\\?")[1].split(" ")[0];
            String [] querys = queryString.split("&");
            for (String query : querys){
                queryStringAsHashMap.put(query.split("=")[0],query.split("=")[1]);
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
                response = cmdSignUp(objRequest);

            } else if (objRequest.getEndpoint().equals("POST /finish-sign-up")) {
                response = cmdFinishSignUp(objRequest);

            } else if (objRequest.getEndpoint().equals("POST /sign-in")) {
                response = cmdSignIn(objRequest);

            } else if (objRequest.getEndpoint().equals("POST /sign-out")) {
                response = cmdSignOut(objRequest);

            } else if (objRequest.getEndpoint().equals("POST /product")) {
                response = cmdNewProduct(objRequest);

            } else if (objRequest.getEndpoint().equals("GET /users")) {
                response = cmdListUsers();

            } else if (objRequest.getEndpoint().equals("GET /products")) {
                response = cmdListProducts();

            } else if (objRequest.getEndpoint().equals("POST /bought-product")) {
                response = cmdBuyProduct(objRequest);

            } else if (objRequest.getEndpoint().equals("POST /user-products")) {
                response = cmdListUsersProduct(objRequest);

            } else if (objRequest.getEndpoint().equals("POST /product-users")) {
                response = cmdListProductUsers(objRequest);

            } else {
                response = new UnsuccessfulResponse("404 Not Found", "Unknown command");
            }
        } catch (Throwable throwable) {
            response = new UnsuccessfulResponse("500 Internal Server Error", "Server mistake");
        }
        pout.print(response.serialize());
        pout.close();

    }

    public Response cmdSignUp(Request objRequest) {
        ObjectMapper mapper = new ObjectMapper();

        try {
            HashMap requestBody = mapper.readValue(objRequest.getBody(), HashMap.class);
            if ((Double) requestBody.get("amount") <= 0) {
                return new UnsuccessfulResponse("400 Bad Request", "Wrong amount value");
            }

            if (database.existsUserByLogin((String) requestBody.get("login"))) {
                return new UnsuccessfulResponse("400 Bad Request", "This login already exists");
            }

            User user = new User(UUID.randomUUID().toString(), (String) requestBody.get("firstName"),
                    (String) requestBody.get("lastName"), (Double) requestBody.get("amount"),
                    (String) requestBody.get("login"), (String) requestBody.get("password"));

            database.insertUser(user);

            SMSSender smSsender = new SMSSender();
            smSsender.sendSms(user.getLogin(), user.getConfirmationCode());

        } catch (JsonProcessingException e) {
            return new UnsuccessfulResponse("400 Bad Request", "Wrong request format");
        }

        return new SuccessfulResponseMessage("200 OK", "Successful add new user");
    }

    private Response cmdFinishSignUp(Request objRequest) {
        ObjectMapper mapper = new ObjectMapper();

        try {
            HashMap requestBody = mapper.readValue(objRequest.getBody(), HashMap.class);
            User user = database.findUserById((String) requestBody.get("userId"));

            if (user != null) {
                if (user.isConfirmed()) {
                    return new UnsuccessfulResponse("400 Bad Request", "User already confirmed");
                }
                if (user.compareConfirmationCode((String) requestBody.get("confirmationCode"))) {
                    user.setStatusConfirmed();
                    database.updateUser(user);
                    return new SuccessfulResponseMessage("200 OK", "Successful confirmed user");
                } else {
                    return new UnsuccessfulResponse("400 Bad Request", "Wrong confirmed code");
                }
            }

        } catch (JsonProcessingException e) {
            return new UnsuccessfulResponse("400 Bad Request", "Wrong request format");
        }
        return new UnsuccessfulResponse("404 Not Found", "User not found");
    }

    private Response cmdSignIn(Request objRequest) {
        ObjectMapper mapper = new ObjectMapper();

        try {
            HashMap requestBody = mapper.readValue(objRequest.getBody(), HashMap.class);
            String login = (String) requestBody.get("login");
            String password = (String) requestBody.get("password");

            User user = database.findUserByLoginAndPassword(login, password);
            if (user != null) {
                user.setAccessToken(UUID.randomUUID().toString());
                database.updateUser(user);
                return new SuccessfulResponseSignIn(user.getAccessToken());
            }

        } catch (JsonProcessingException e) {
            return new UnsuccessfulResponse("400 Bad Request", "Wrong request format");
        }

        return new UnsuccessfulResponse("400 Bad Request", "No user found");
    }

    private Response cmdSignOut(Request objRequest) {
        ObjectMapper mapper = new ObjectMapper();

        try {
            HashMap requestBody = mapper.readValue(objRequest.getBody(), HashMap.class);

            User user = database.findUserByAccessToken((String) requestBody.get("accessToken"));

            if (user != null) {
                user.setAccessToken(null);
                database.updateUser(user);
                return new SuccessfulResponseMessage("200 OK", "The exit has been successfully completed");
            }

        } catch (JsonProcessingException e) {
            return new UnsuccessfulResponse("400 Bad Request", "Wrong request format");
        }

        return new UnsuccessfulResponse("400 Bad Request", "No user found");
    }

    public Response cmdNewProduct(Request objRequest) {
        ObjectMapper mapper = new ObjectMapper();

        try {
            HashMap createProduct = mapper.readValue(objRequest.getBody(), HashMap.class);

            if ((Double) createProduct.get("price") <= 0) {
                return new UnsuccessfulResponse("400", "Wrong amount value");
            }
            Product product = new Product(UUID.randomUUID().toString(), (String) createProduct.get("name"),
                    (Double) createProduct.get("price"));

            database.insertProduct(product);

        } catch (JsonProcessingException e) {
            return new UnsuccessfulResponse("400 Bad Request", "Wrong request format");
        }

        return new SuccessfulResponseMessage("200 OK", "Add product successful");
    }

    public Response cmdListUsers() {

        ArrayList<User> allUsers = database.getAllUser();
        ArrayList<HashMap> allUsersForResponse = new ArrayList<>();
        for (User user : allUsers) {
            allUsersForResponse.add(user.toHashMapUser());
        }

        return new SuccessfulResponseArray("200 OK", allUsersForResponse);
    }

    public Response cmdListProducts() {

        ArrayList<Product> allProduct = database.getAllProduct();
        ArrayList<HashMap> allProductForResponse = new ArrayList<>();
        for (Product product : allProduct) {
            allProductForResponse.add(product.toHashMapProduct());
        }

        return new SuccessfulResponseArray("200 OK", allProductForResponse);
    }

    public Response cmdBuyProduct(Request objRequest) {
        ObjectMapper mapper = new ObjectMapper();
        String userIdForBuying;
        String productIdForBuying;

        try {
            HashMap buyingRequest = mapper.readValue(objRequest.getBody(), HashMap.class);
            userIdForBuying = (String) buyingRequest.get("userId");
            productIdForBuying = (String) buyingRequest.get("productId");

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
            database.updateUser(user);
            database.insertPurchase(new Purchase( UUID.randomUUID().toString(),user.getId(),product.getId()));
            return new SuccessfulResponseMessage("200 OK", "You did successful buying");
        } catch (Exception e) {
            return new UnsuccessfulResponse("400 Bad Request", "You haven`t enough money");
        }
    }

    public Response cmdListUsersProduct(Request objRequest) {

        ObjectMapper mapper = new ObjectMapper();

        try {
            HashMap requestBody = mapper.readValue(objRequest.getBody(), HashMap.class);
            ArrayList <Product> products = database.getUserBoughtProduct((String) requestBody.get("userId"));

            if (products.size() < 1) {
                return new UnsuccessfulResponse("400 Bad Request", "User haven`t buy");
            }

            ArrayList <HashMap> userBoughtList = new ArrayList<>();
            for (Product product : products) {
                HashMap <String,Object> productForResponse = new HashMap<>();
                productForResponse.put("name", product.getName());
                productForResponse.put("price",product.getPrice());
                userBoughtList.add(productForResponse);
            }

            return new SuccessfulResponseArray("200 OK", userBoughtList);
        } catch (JsonProcessingException e) {
            return new UnsuccessfulResponse("400 Bad Request", "Wrong request format");
        }
    }

    public Response cmdListProductUsers(Request objRequest) {
        ObjectMapper mapper = new ObjectMapper();

        try {
            HashMap requestBody = mapper.readValue(objRequest.getBody(), HashMap.class);

            ArrayList <HashMap> userBuy = new ArrayList<>();

            ArrayList <User> users = database.getProductСustomers((String) requestBody.get("productId"));
            if (users.size() < 1) {
                return new UnsuccessfulResponse("400 Bad Request", "Product haven`t user");
            }

            for (User user: users) {
                HashMap <String,String> userAsHashMap = new HashMap<>();
                userAsHashMap.put("firstName",user.getFirstName());
                userAsHashMap.put("lastName",user.getLastName());
                userBuy.add(userAsHashMap);
            }

            return new SuccessfulResponseArray("200 OK", userBuy);
        } catch (JsonProcessingException e) {
            return new UnsuccessfulResponse("400 Bad Request", "Wrong request format");
        }
    }
}
