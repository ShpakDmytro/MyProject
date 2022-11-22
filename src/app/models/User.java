package app.models;

import java.util.HashMap;
import java.util.UUID;
import app.exception.*;

public class User {
    private String id;
    private String firstName;
    private String lastName;
    double amount;
    private String login;
    private String password;
    private String accessToken;
    private String status;
    private String confirmationCode;

    private String passwordResetCode;

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
        this.confirmationCode = UUID.randomUUID().toString().substring(0,5);
        this.passwordResetCode = null;
    }

    //reconstruction User from bd
    public User(String id, String firstName, String lastName, double amount,
                String login, String password, String accessToken, String status, String confirmationCode,
                String passwordResetCode) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.amount = amount;
        this.login = login;
        this.password = password;
        this.accessToken = accessToken;
        this.status = status;
        this.confirmationCode = confirmationCode;
        this.passwordResetCode = passwordResetCode;
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

    public String getPasswordResetCode(){
        return passwordResetCode;
    }

    public void buyProduct(Product product) throws Exception {
        if (amount - product.getPrice() > 0) {
            this.amount = amount - product.getPrice();
        } else throw new NotEnoughMoneyException("You haven`t enough money");
    }

    public boolean compareConfirmationCode(String code) {
        if (confirmationCode == null) {
            return false;
        }
        return confirmationCode.equals(code);
    }

    public void setConfirmationCode (String confirmationCode){
        this.confirmationCode = confirmationCode;
    }

    public void setPasswordResetCode(String passwordResetCode){
        this.passwordResetCode = passwordResetCode;
    }

    public boolean isConfirmed() {
        return status.equals("confirmed");
    }

    public void setFirstName(String firstName){
        this.firstName = firstName;
    }

    public void setLastName(String lastName){
        this.lastName = lastName;
    }

    public void setAmount(Double amount){
        this.amount = amount;
    }

    public void setLogin(String login){
        this.login = login;
    }

    public void setPassword(String password){
        this.password = password;
    }
    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public void setStatusConfirmed() {
        confirmationCode = null;
        status = "confirmed";
    }

   public void changePasswordFromResetCode(String code, String newPassword) throws BadPasswordResetCodeException, PasswordResetNotRequestedCodeException {
        if (this.passwordResetCode == null){
         throw new PasswordResetNotRequestedCodeException("Reset code not requested");
        }
        if (this.passwordResetCode.equals(code)){
            this.password = newPassword;
            this.passwordResetCode = null;
        } else throw new BadPasswordResetCodeException("Wrong reset code");
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
        user.put("passwordResetCode", this.passwordResetCode);

        return user;
    }
}
