import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.HashMap;

public class Product {
    int id;
    String name;
    double price;
    ArrayList<User> userBuy;

    public Product(int id, String name, double price) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.userBuy = new ArrayList<>();
    }

    public void addUser(User user) {
        userBuy.add(user);
    }

    public int howManyUsers() {
        return userBuy.size();
    }

    public User getUserAtIndex(int index) throws Exception {
        if (index > userBuy.size() || index < 0) {
            throw new Exception("Index wrong");
        }
        return userBuy.get(index);
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Double getPrice() {
        return price;
    }
    public HashMap toHashMapProduct() {

        HashMap <String,Object> product = new HashMap<>();
        product.put("id",this.id);
        product.put("name",this.name);
        product.put("price",this.price);
        ArrayList <HashMap> userBuyList = new ArrayList<>();
        for (User info : this.userBuy) {
            HashMap<String, Object> user = new HashMap<>();
            user.put("id", info.getId());
            user.put("firstName", info.getFirstName());
            user.put("lastName", info.getLastName());
            userBuyList.add(user);
        }
        return product;
    }
}
