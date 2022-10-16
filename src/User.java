import java.util.ArrayList;
import java.util.HashMap;

public class User {
    int id;
    String firstName;
    String lastName;
    double amount;
    String login;
    String password;
    String accessToken;
    ArrayList<Product> boughtList;

    public User(int id, String firstName, String lastName, double amount,
                String login, String password) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.amount = amount;
        this.boughtList = new ArrayList<>();
        this.login = login;
        this.password = password;
        this.accessToken = null;
    }

    public User(int id, String firstName, String lastName, double amount,
                String login, String password, String accessToken) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.amount = amount;
        this.boughtList = new ArrayList<>();
        this.login = login;
        this.password = password;
        this.accessToken = accessToken;
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

    boolean checkLoginPassword(String login, String password, String loginFromUser, String passwordFromUser) {
        if (login.equals(loginFromUser) && password.equals(passwordFromUser)) {
            return true;
        }
        return false;
    }

    public String getLogin() {
        return login;
    }

    public String getPassword() {
        return password;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
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
        user.put("login", this.login);
        user.put("password", this.password);
        user.put("accessToken", this.accessToken);

        return user;
    }
}
