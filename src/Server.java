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

    public static void main(String[] args) throws Exception {
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

    private void programLogic(Request objRequest, PrintStream pout) throws Exception {
        Response response = null;

        if (objRequest.getEndpoint().equals("POST /user")) {
            response = cmdNewUser(objRequest);
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
            response = new Response("HTTP/1.0 404 Not Found", "Unknown command");
        }
        pout.print(response.serialize());
        pout.close();
    }

    public void cmdLoad() {
        try {
            BufferedReader readerForId = new BufferedReader(
                    new FileReader("D:\\projects\\Myprodject\\db\\id"));
            String idAsString = readerForId.readLine();
            id = Integer.parseInt(idAsString);
            readerForId.close();
        } catch (IOException e) {
            System.out.println("Error ID load");
        }

        File actual = new File("D:\\projects\\Myprodject\\db\\users");
        for (File f : actual.listFiles()) {
            try {
                ObjectMapper mapper = new ObjectMapper();
                BufferedReader br = new BufferedReader(new FileReader(f.getAbsolutePath()));
                HashMap user = mapper.readValue(br.readLine(),HashMap.class);
                User newUser = new User((Integer) user.get("id"), (String) user.get("firstName"),
                               (String) user.get("lastName"), (Double) user.get("amount"));
                users.add(newUser);
                br.close();
            } catch (IOException ignored) {}
        }

        File productsDir = new File("D:\\projects\\Myprodject\\db\\products");
        for (int i = 0; i < productsDir.listFiles().length; i++) {
            String path = productsDir.listFiles()[i].getAbsolutePath();
            try {
                ObjectMapper mapper = new ObjectMapper();
                BufferedReader readerProduct = new BufferedReader(new FileReader(path));
                HashMap product = mapper.readValue(readerProduct.readLine(), HashMap.class);
                Product newProduct = new Product((Integer) product.get("id"), (String) product.get("name"),
                                     (Double) product.get("price"));
                products.add(newProduct);
                readerProduct.close();
            } catch (IOException e) {
                System.out.println("Error product load");
            }
        }
    }

    public void cmdSave() {

        try {
            PrintWriter saveId = new PrintWriter("D:\\projects\\Myprodject\\db\\id",
                    StandardCharsets.UTF_8);
            saveId.println(id);
            saveId.close();
        } catch (IOException e) {
            System.out.println("Error Save");
        }

        for (User user : users) {
            try {
                PrintWriter writer = new PrintWriter(
                        "D:\\projects\\Myprodject\\db\\users\\" + user.getId(),
                        StandardCharsets.UTF_8);
                ObjectMapper mapper = new ObjectMapper();
                String save = mapper.writeValueAsString(user.toHashMapUser());
                writer.print(save);
                writer.close();
            } catch (IOException e) {
                System.out.println("Error saving file");
            }
        }

        for (Product product : products) {
            try {
                PrintWriter writer = new PrintWriter(
                        "D:\\projects\\Myprodject\\db\\products\\" + product.getId(),
                        StandardCharsets.UTF_8);
                ObjectMapper mapper = new ObjectMapper();
                String save = mapper.writeValueAsString(product.toHashMapProduct());
                writer.print(save);
                writer.close();
            } catch (IOException e) {
                System.out.println("Error saving file");
            }
        }
    }

    public Response cmdNewUser(Request objRequest) {

        String firstName = objRequest.body.split(",")[0];
        String lastName = objRequest.body.split(",")[1];
        String amountAsString = objRequest.body.split(",")[2];

        double amountAsDouble;
        try {
            amountAsDouble = Double.parseDouble(amountAsString);
        } catch (NumberFormatException exception) {
            return new Response("HTTP/1.0 400 Bad Request", "Wrong amount value");
        }

        User user = new User(id, firstName, lastName, amountAsDouble);
        users.add(user);
        id++;
        return new Response("HTTP/1.0 200 OK", "Add user successful");

    }

    public Response cmdNewProduct(Request objRequest) {

        String productName = objRequest.body.split(",")[0];
        String productPriceAsString = objRequest.body.split(",")[1];

        double productPriceASDouble;
        try {
            productPriceASDouble = Double.parseDouble(productPriceAsString);
        } catch (NumberFormatException exception) {
            return new Response("HTTP/1.0 400 Bad Request", "Wrong amount value");
        }

        Product product = new Product(id, productName, productPriceASDouble);
        products.add(product);
        id++;
        return new Response("HTTP/1.0 200 OK", "Add product successful");
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

        return new Response("HTTP/1.0 200 OK", response);
    }

    public Response cmdListProducts() {
        ObjectMapper mapper = new ObjectMapper();
        ArrayList <HashMap> productsForResponse = new ArrayList<>();
        for (Product info : products) {
            productsForResponse.add(info.toHashMapProduct());
        }
        System.out.println(products);
        System.out.println(productsForResponse);
        String response = null;
        try {
            response = mapper.writeValueAsString(productsForResponse);
        } catch (JsonProcessingException ignored) {}
        System.out.println(response);

        return new Response("HTTP/1.0 200 OK", response);
    }

    public Response cmdBuyProduct(Request objRequest) throws Exception {

        String idUserAsString = objRequest.body.split(",")[0];
        int idUserAsInt = Integer.parseInt(idUserAsString);

        String idProductAsString = objRequest.body.split(",")[1];
        int idProductAsInt = Integer.parseInt(idProductAsString);

        boolean userFound = false;
        User user = null;
        for (User check : users) {
            if (check.getId() == idUserAsInt) {
                userFound = true;
                user = check;
            }
        }

        if (!userFound) {
            return new Response("HTTP/1.0 400 Bad Request", "Wrong user id");
        }

        Product product = null;
        boolean productFound = false;
        for (Product check : products) {
            if (check.getId() == idProductAsInt) {
                productFound = true;
                product = check;
            }
        }
        if (!productFound) {
            return new Response("HTTP/1.0 400 Bad Request", "Wrong product id");
        }

        try {
            user.buyProduct(product);
            product.addUser(user);
            return new Response("HTTP/1.0 200 OK", "You did successful buying");
        } catch (Exception e) {
            return new Response("HTTP/1.0 400 Bad Request", "You haven`t enough money");
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
            return new Response("HTTP/1.0 400 Bad Request", "Wrong user id");
        }

        ArrayList<Product> buying = user.getBoughtList();
        if (buying.size() < 1) {
            return new Response("HTTP/1.0 200 OK", "You haven`t buying");
        } else {
            StringBuilder result = new StringBuilder();
            for (int i = 0; i < buying.size(); i++) {
                Product buy = buying.get(i);
                result.append(i).append(": ").append(buy.getName()).append("\n");
            }
            return new Response("HTTP/1.0 200 OK", result.toString());
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
            return new Response("HTTP/1.0 200 OK", result.toString());
        } else {
            return new Response("HTTP/1.0 200 OK", "Product don`t buying");
        }
    }
}