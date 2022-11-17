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
                response = cmdFindUsers(objRequest);

            } else if (objRequest.getEndpoint().equals("GET /products")) {
                response = cmdFindProducts(objRequest);

            } else if (objRequest.getEndpoint().equals("POST /bought-product")) {
                response = cmdBuyProduct(objRequest);

            } else if (objRequest.getEndpoint().equals("PATCH /product")) {
                response = cmdPatchProduct(objRequest);

            } else if (objRequest.getEndpoint().equals("PATCH /user")) {
                response = cmdPatchUser(objRequest);

            } else if (objRequest.getEndpoint().equals("POST /forgot-password")) {
                response = cmdForgotPassword(objRequest);

            } else if (objRequest.getEndpoint().equals("POST /forgot-password-finish")) {
                response = cmdForgotPasswordFinish(objRequest);

            } else if (objRequest.getEndpoint().equals("DELETE /product")) {
                response = cmdDeleteProduct(objRequest);

            } else if (objRequest.getEndpoint().equals("DELETE /user")) {
                response = cmdDeleteUser(objRequest);

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

    public Response cmdFindUsers(Request objRequest) {

        ArrayList<User> allUsers = database.findUsers(objRequest.getQueryString());
        ArrayList<HashMap> allUsersForResponse = new ArrayList<>();
        for (User user : allUsers) {
            allUsersForResponse.add(user.toHashMapUser());
        }
        return new SuccessfulResponseArray("200 OK", allUsersForResponse);

    }

    public Response cmdFindProducts(Request objRequest) {

        ArrayList<Product> allProduct = database.findProducts(objRequest.getQueryString());
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
            database.startTransaction();
            database.updateUser(user);
            database.insertPurchase(new Purchase(UUID.randomUUID().toString(), user.getId(), product.getId()));
            database.closeTransaction();
            return new SuccessfulResponseMessage("200 OK", "You did successful buying");
        } catch (NotEnoughMoneyException exception) {
            return new UnsuccessfulResponse("400 Bad Request", "User don`t have enough money");
        } catch (Exception e) {
            database.rollback();
            return new UnsuccessfulResponse("500 Internal Server Error", "Something wrong");
        }
    }

    private Response cmdPatchProduct(Request objRequest) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            HashMap updateProduct = mapper.readValue(objRequest.getBody(), HashMap.class);
            if (updateProduct.containsKey("price")) {
                if ((Double) updateProduct.get("price") <= 0) {
                    return new UnsuccessfulResponse("400", "Wrong amount value");
                }
            }
            Product product = database.findProductById((String) objRequest.getQueryString().get("id"));
            if (updateProduct.containsKey("name")) {
                product.setName((String) updateProduct.get("name"));
            }
            if (updateProduct.containsKey("price")) {
                product.setPrice((Double) updateProduct.get("price"));
            }
            database.updateProduct(product);

        } catch (JsonProcessingException e) {
            return new UnsuccessfulResponse("400 Bad Request", "Wrong request format");
        }

        return new SuccessfulResponseMessage("200 OK", "Product update successful");
    }

    private Response cmdPatchUser(Request objRequest) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            HashMap updateProduct = mapper.readValue(objRequest.getBody(), HashMap.class);
            if (updateProduct.containsKey("amount")) {
                if ((Double) updateProduct.get("amount") <= 0) {
                    return new UnsuccessfulResponse("400", "Wrong amount value");
                }
            }
            User user = database.findUserById((String) objRequest.getQueryString().get("id"));
            if (updateProduct.containsKey("firstName")) {
                user.setFirstName((String) updateProduct.get("firstName"));
            }
            if (updateProduct.containsKey("lastName")) {
                user.setLastName((String) updateProduct.get("lastName"));
            }
            if (updateProduct.containsKey("amount")) {
                user.setAmount((Double) updateProduct.get("amount"));
            }
            if (updateProduct.containsKey("login")) {
                user.setLogin((String) updateProduct.get("login"));
            }
            if (updateProduct.containsKey("password")) {
                user.setPassword((String) updateProduct.get("password"));
            }

            database.updateUser(user);

        } catch (JsonProcessingException e) {
            return new UnsuccessfulResponse("400 Bad Request", "Wrong request format");
        }

        return new SuccessfulResponseMessage("200 OK", "User update successful");
    }

    private Response cmdDeleteProduct(Request objRequest) {
        Product product = database.findProductById((String) objRequest.getQueryString().get("id"));
        if (product == null) {
            return new UnsuccessfulResponse("400 Bad Request", "Wrong product id");
        }
        try {
            ArrayList<Purchase> purchases = database.findPurchasesByProductId((String) objRequest.getQueryString().get("id"));
            database.startTransaction();
            database.deleteProduct(product);
            for (Purchase purchase : purchases) {
                database.deletePurchases(purchase);
            }
            database.closeTransaction();
            return new SuccessfulResponseMessage("200 OK", "Product successful delete");
        } catch (Exception e) {
            database.rollback();
            return new UnsuccessfulResponse("500 Internal Server Error", "Something wrong");
        }
    }

    private Response cmdDeleteUser(Request objRequest) {
        User user = database.findUserById((String) objRequest.getQueryString().get("id"));
        if (user == null) {
            return new UnsuccessfulResponse("400 Bad Request", "Wrong user id");
        }

        try {
            ArrayList<Purchase> purchases = database.findPurchasesByUserId((String) objRequest.getQueryString().get("id"));
            database.startTransaction();
            database.deleteUser(user);
            for (Purchase purchase : purchases) {
                database.deletePurchases(purchase);
            }
            database.closeTransaction();
            return new SuccessfulResponseMessage("200 OK", "User successful delete");
        } catch (Exception e) {
            database.rollback();
            return new UnsuccessfulResponse("500 Internal Server Error", "Something wrong");
        }
    }

    private Response cmdForgotPassword(Request objRequest) {
        ObjectMapper mapper = new ObjectMapper();

        try {
            HashMap request = mapper.readValue(objRequest.getBody(), HashMap.class);
            if (request.containsKey("login")) {

                User user = database.findUserByLogin((String) request.get("login"));
                SMSSender smSsender = new SMSSender();
                user.setRestoreCode(UUID.randomUUID().toString().substring(0,5));
                database.updateUser(user);
                smSsender.sendSms(user.getLogin(), user.getRestoreCode());

                return new SuccessfulResponseMessage("200 OK", "Send confirmation code");
            }
            return new UnsuccessfulResponse("400 Bad Request", "Wrong login");
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    private Response cmdForgotPasswordFinish(Request objRequest) {
        ObjectMapper mapper = new ObjectMapper();

        try {
            HashMap request = mapper.readValue(objRequest.getBody(), HashMap.class);
            if (request.containsKey("login")) {
                User user = database.findUserByLogin((String) request.get("login"));
                if (user.getRestoreCode().equals(request.get("restoreCode"))){
                    user.setPassword((String) request.get("password"));
                    user.setRestoreCode("null");
                    database.updateUser(user);
                }
                return new SuccessfulResponseMessage("200 OK", "Password successful changed");
            }
            return new UnsuccessfulResponse("400 Bad Request", "Wrong confirmation code");
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
