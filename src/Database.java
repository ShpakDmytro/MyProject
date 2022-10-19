import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;

public class Database {
    int id;
    ArrayList<User> users;
    ArrayList<Product> products;

    public Database() {
        this.id = 0;
        this.users = new ArrayList<>();
        this.products = new ArrayList<>();
    }

    public void loadData() {
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

    public void saveData() {

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

    boolean existsUserByLogin(String loginFromUser){
        for (int i = 0; i < users.size() ; i++) {
            User user = users.get(i);
            if (user.login.equals(loginFromUser)){
                return true;
            }
        }
        return false;
    }

    int nextId() {
        return id++;
    }

    void addUser (User user){
        users.add(user);
    }

    void addProduct(Product product){
        products.add(product);
    }
    User findUserByLoginAndPassword(String login, String password) {
        for (int i = 0; i < users.size(); i++) {
            User user = users.get(i);
            if (user.login.equals(login) && user.password.equals(password)) {
                return user;
            }
        }
        return null;
    }

    User findUserByAccessToken(String AccessTokenFromUser) {
        for (int i = 0; i < users.size(); i++) {
            User user = users.get(i);
            if (user.accessToken == null){continue;}
            if (user.accessToken.equals(AccessTokenFromUser)) {
                System.out.println(user.accessToken);
                return user;
            }
        }
        return null;
    }

    User findUserById(int idFromUser){
        for (int i = 0; i < users.size() ; i++) {
            User user = users.get(i);
            if (user.getId() == idFromUser){
                return user;
            }
        }
        return null;
    }

    Product findProductById(int idFromUser){
        for (int i = 0; i < products.size() ; i++) {
            Product product = products.get(i);
            if (product.getId() == idFromUser){
                return product;
            }
        }
        return null;
    }

    ArrayList<HashMap> getAllUser(){
        ArrayList<HashMap> usersForResponse = new ArrayList<>();

        for (User user : users) {
            usersForResponse.add(user.toHashMapUser());
        }
        return usersForResponse;
    }

    ArrayList <HashMap> getAllProduct(){
        ArrayList<HashMap> productsForResponse = new ArrayList<>();
        for (Product info : products) {
            productsForResponse.add(info.toHashMapProduct());
        }
        return productsForResponse;
    }
}
