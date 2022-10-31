import java.util.HashMap;

public class User {
    String id;
    String firstName;
    String lastName;
    double amount;
    String login;
    String password;
    String accessToken;
    String status;
    String confirmationCode;

    //create User
    public User(String id, String firstName, String lastName, double amount,
                String login, String password) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.amount = amount;
        this.login = login;
        this.password = password;
        this.accessToken = null;
        this.status = "unconfirmed";
        this.confirmationCode = new ConfirmationCodeGenerator().generateConfirmationCode();
    }

    //reconstraction User from bd
    public User(String id, String firstName, String lastName, double amount,
                String login, String password, String accessToken, String status, String confirmationCode) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.amount = amount;
        this.login = login;
        this.password = password;
        this.accessToken = accessToken;
        this.status = status;
        this.confirmationCode = confirmationCode;
    }

    public String getId() {
        return id;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public Double getAmount() {return amount;}

    public String getStatus(){return status;}
    public String getLogin() {
        return login;
    }

    public String getPassword() {
        return password;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public String getConfirmationCode() {
        return confirmationCode;
    }

    public void buyProduct(Product product) throws Exception {
        if (amount - product.getPrice() > 0) {
            this.amount = amount - product.getPrice();
        } else throw new Exception("You haven`t enough money");
    }

    boolean compareConfirmationCode(String code) {
        if (confirmationCode == null) {
            return false;
        }
        return confirmationCode.equals(code);
    }

    boolean isConfirmed() {
        return status.equals("confirmed");
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public void setStatusConfirmed() {
        confirmationCode = null;
        status = "confirmed";
    }

    public HashMap toHashMapUser() {

        HashMap<String, Object> user = new HashMap<>();
        user.put("id", this.id);
        user.put("firstName", this.firstName);
        user.put("lastName", this.lastName);
        user.put("amount", this.amount);
        user.put("login", this.login);
        user.put("password", this.password);
        user.put("accessToken", this.accessToken);
        user.put("status", this.status);
        user.put("confirmationCode", this.confirmationCode);

        return user;
    }
}
