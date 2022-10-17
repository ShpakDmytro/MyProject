import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;

public class Server {
    static final int port = 8080;
    int id;
    ArrayList<User> users;
    ArrayList<Product> products;

    public Server() {
        this.id = 0;
        this.users = new ArrayList<>();
        this.products = new ArrayList<>();
    }

    public static void main(String[] args) {
        new Server().run();
    }

    public void run() {
        try {
            cmdLoad();
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
            cmdSave();
        } else if (objRequest.getEndpoint().equals("POST /sign-in")) {
            response = cmdSignIn(objRequest);
            cmdSave();
        } else if (objRequest.getEndpoint().equals("POST /sign-out")) {
            response = cmdSignOut(objRequest);
            cmdSave();
        } else if (objRequest.getEndpoint().equals("POST /product")) {
            response = cmdNewProduct(objRequest);
            cmdSave();
        } else if (objRequest.getEndpoint().equals("GET /users")) {
            response = cmdListUsers();
        } else if (objRequest.getEndpoint().equals("GET /products")) {
            response = cmdListProducts();
        } else if (objRequest.getEndpoint().equals("POST /bought-product")) {
            response = cmdBuyProduct(objRequest);
            cmdSave();
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

    private Response cmdSignOut(Request objRequest) {
        ObjectMapper mapper = new ObjectMapper();

        try {
            HashMap requestBody = mapper.readValue(objRequest.body, HashMap.class);

            for (int i = 0; i < users.size(); i++) {
                User user = users.get(i);
                if (user.checkAccessToken((String) requestBody.get("accessToken"))) {
                    user.setAccessToken(null);
                    return new SuccessfulResponse("200 OK","The exit has been successfully completed");
                }
            }

        } catch (JsonProcessingException e) {
            return new UnsuccessfulResponse("400 Bad Request", "Wrong request format");
        }

        return new UnsuccessfulResponse("400 Bad Request","No user found");
    }

    public void cmdLoad() {
        try {
            BufferedReader loadData = new BufferedReader(
                    new FileReader("D:\\projects\\Myprodject\\db\\data.json"));
            ObjectMapper mapper = new ObjectMapper();
            HashMap data = mapper.readValue(loadData.readLine(), HashMap.class);

            id = (int) data.get("id");

            ArrayList<HashMap> usersLoad = (ArrayList<HashMap>) data.get("users");
            for (int i = 0; i < usersLoad.size(); i++) {
                HashMap<String, Object> userHashMap = usersLoad.get(i);
                User newUser = new User((Integer) userHashMap.get("id"), (String) userHashMap.get("firstName"),
                        (String) userHashMap.get("lastName"), (Double) userHashMap.get("amount"),
                        (String) userHashMap.get("login"), (String) userHashMap.get("password"),
                        (String) userHashMap.get("accessToken"));

                ArrayList<HashMap> boughtList = (ArrayList<HashMap>) userHashMap.get("boughtlist");
                for (int j = 0; j < boughtList.size(); j++) {
                    HashMap<String, Object> productHash = boughtList.get(j);
                    Product product = new Product((Integer) productHash.get("id"), (String) productHash.get("name"),
                            (Double) productHash.get("price"));
                    newUser.boughtList.add(product);
                }

                users.add(newUser);
            }

            ArrayList<HashMap> productsLoad = (ArrayList<HashMap>) data.get("products");
            for (int i = 0; i < productsLoad.size(); i++) {
                HashMap<String, Object> productHashMap = productsLoad.get(i);
                Product newProduct = new Product((Integer) productHashMap.get("id"), (String) productHashMap.get("name"),
                        (Double) productHashMap.get("price"));

                ArrayList<HashMap> userBuy = (ArrayList<HashMap>) productHashMap.get("userBuy");
                for (int j = 0; j < userBuy.size(); j++) {
                    HashMap<String, Object> userHash = userBuy.get(j);
                    User user = new User(userHash.get("id"), userHash.get("firstName"), userHash.get("lastName"));
                    newProduct.userBuy.add(user);
                }
                products.add(newProduct);
            }

            loadData.close();
        } catch (IOException e) {
            System.out.println(e);
            System.out.println("Error load");
        }

    }

    public void cmdSave() {

        try {
            PrintWriter saveData = new PrintWriter("D:\\projects\\Myprodject\\db\\data.json",
                    StandardCharsets.UTF_8);
            ObjectMapper mapper = new ObjectMapper();

            HashMap<String, Object> dataForSave = new HashMap<>();
            ArrayList<HashMap> usersForSave = new ArrayList<>();
            for (int i = 0; i < users.size(); i++) {
                usersForSave.add(users.get(i).toHashMapUser());
            }
            dataForSave.put("users", usersForSave);

            ArrayList<HashMap> productsForSave = new ArrayList<>();
            for (int i = 0; i < products.size(); i++) {
                productsForSave.add(products.get(i).toHashMapProduct());
            }
            dataForSave.put("products", productsForSave);

            dataForSave.put("id", id);

            String save = mapper.writeValueAsString(dataForSave);
            saveData.print(save);
            saveData.close();
        } catch (IOException e) {
            System.out.println(e);
            System.out.println("Error Save");
        }
    }

    public Response cmdSignUp(Request objRequest) {
        ObjectMapper mapper = new ObjectMapper();

        try {
            HashMap requestBody = mapper.readValue(objRequest.body, HashMap.class);
            if ((Double) requestBody.get("amount") <= 0) {
                return new UnsuccessfulResponse("400 Bad Request", "Wrong amount value");
            }

            for (int i = 0; i < users.size(); i++) {
                User user = users.get(i);
                if (user.checkLogin((String) requestBody.get("login"))) {
                    return new UnsuccessfulResponse("400 Bad Request","This login already exists");
                }
            }

            User user = new User(id, (String) requestBody.get("firstName"),
                    (String) requestBody.get("lastName"), (Double) requestBody.get("amount"),
                    (String) requestBody.get("login"), (String) requestBody.get("password"));
            users.add(user);
            id++;

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

            for (int i = 0; i < users.size(); i++) {
                User user = users.get(i);
                if (user.checkLoginPassword(login,password)) {
                    String accessToken = new TokenGenerator().generateToken();
                    user.setAccessToken(accessToken);
                    return new SuccessfulResponseSignIn(accessToken);
                }
            }

        } catch (JsonProcessingException e) {
            return new UnsuccessfulResponse("400 Bad Request", "Wrong request format");
        }

        return new UnsuccessfulResponse("400 Bad Request","No user found");
    }

    public Response cmdNewProduct(Request objRequest) {
        ObjectMapper mapper = new ObjectMapper();

        try {
            HashMap createProduct = mapper.readValue(objRequest.body, HashMap.class);
            if ((Double) createProduct.get("price") <= 0) {
                return new UnsuccessfulResponse("400", "Wrong amount value");
            }
            Product product = new Product(id, (String) createProduct.get("name"), (Double) createProduct.get("price"));

            products.add(product);
            id++;

        } catch (JsonProcessingException e) {
            return new UnsuccessfulResponse("400 Bad Request", "Wrong request format");
        }

        return new SuccessfulResponse("200 OK","Add product successful");
    }

    public Response cmdListUsers() {
        ObjectMapper mapper = new ObjectMapper();

        ArrayList<HashMap> usersForResponse = new ArrayList<>();

        for (User user : users) {
            usersForResponse.add(user.toHashMapUser());
        }

        String response = null;
        try {
            response = mapper.writeValueAsString(usersForResponse);
        } catch (JsonProcessingException ignored) {}

        return new SuccessfulResponse("200 OK",response);
    }

    public Response cmdListProducts() {
        ObjectMapper mapper = new ObjectMapper();
        ArrayList<HashMap> productsForResponse = new ArrayList<>();
        for (Product info : products) {
            productsForResponse.add(info.toHashMapProduct());
        }
        String response = null;
        try {
            response = mapper.writeValueAsString(productsForResponse);
        } catch (JsonProcessingException ignored) {
        }

        return new SuccessfulResponse("200 OK",response);
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
            return new UnsuccessfulResponse("400 Bad Request","Wrong request format");
        }

        boolean userFound = false;
        User user = null;
        for (User check : users) {
            if (check.getId() == userIdForBuying) {
                userFound = true;
                user = check;
            }
        }

        if (!userFound) {
            return new UnsuccessfulResponse("400 Bad Request","Wrong user id");
        }

        Product product = null;
        boolean productFound = false;
        for (Product check : products) {
            if (check.getId() == productIdForBuying) {
                productFound = true;
                product = check;
            }
        }
        if (!productFound) {
            return new UnsuccessfulResponse("400 Bad Request","Wrong product id");
        }

        try {
            user.buyProduct(product);
            product.addUser(user);
            return new SuccessfulResponse("200 OK","You did successful buying");
        } catch (Exception e) {
            return new UnsuccessfulResponse("400 Bad Request","You haven`t enough money");
        }
    }

    public Response cmdListUsersProduct(Request objRequest) {

        int number = Integer.parseInt(objRequest.body.split(",")[0]);

        User user = null;
        boolean rightUser = false;

        for (User check : users) {
            if (number == check.getId()) {
                rightUser = true;
                user = check;
            }
        }
        if (!rightUser) {
            return new UnsuccessfulResponse("400 Bad Request","Wrong user id");
        }

        ArrayList<Product> buying = user.getBoughtList();
        if (buying.size() < 1) {
            return new UnsuccessfulResponse("400 Bad Request","You haven`t buying");
        } else {
            StringBuilder result = new StringBuilder();
            for (int i = 0; i < buying.size(); i++) {
                Product buy = buying.get(i);
                result.append(i).append(": ").append(buy.getName()).append("\n");
            }
            return new SuccessfulResponse("200 OK",result.toString());
        }
    }

    public Response cmdListProductUsers(Request objRequest) {

        String checkProductAsString = objRequest.body.split(",")[0];
        int checkProductAsInt = Integer.parseInt(checkProductAsString);
        boolean rightProduct = false;
        Product product = null;

        for (Product check : products) {
            if (check.getId() == checkProductAsInt) {
                rightProduct = true;
                product = check;
            }
        }
        if (rightProduct) {
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
            return new SuccessfulResponse("200 OK","Product don`t buying");
        }
    }
}
