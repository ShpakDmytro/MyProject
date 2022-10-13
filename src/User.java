import java.util.ArrayList;
import java.util.HashMap;

public class User {
    int id;
    String firstName;
    String lastName;
    double amount;
    ArrayList<Product> boughtList;

    public User(int id, String firstName, String lastName, double amount) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.amount = amount;
        this.boughtList = new ArrayList<>();
    }

    public User(Object id, Object firstName, Object lastName) {
        this.id = (int) id;
        this.firstName = (String) firstName;
        this.lastName = (String) lastName;
    }

    public int getId() {
        return id;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void buyProduct(Product product) throws Exception {
        if (amount - product.getPrice() > 0) {
            this.amount = amount - product.getPrice();
            boughtList.add(product);
        } else throw new Exception("You haven`t enough money");
    }

    public ArrayList<Product> getBoughtList() {
        return boughtList;
    }

    public HashMap toHashMapUser() {

        HashMap<String, Object> user = new HashMap<>();
        user.put("id", this.id);
        user.put("firstName", this.firstName);
        user.put("lastName", this.lastName);
        user.put("amount", this.amount);
        ArrayList<HashMap> boughtUserList = new ArrayList<>();
        for (int i = 0; i < this.boughtList.size(); i++) {
            Product info = this.boughtList.get(i);
            HashMap<String, Object> product = new HashMap<>();
            product.put("id", info.getId());
            product.put("name", info.getName());
            product.put("price", info.getPrice());
            boughtUserList.add(product);
        }
        user.put("boughtlist", boughtUserList);

        return user;
    }
}
