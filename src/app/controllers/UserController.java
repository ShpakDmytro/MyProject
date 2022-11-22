package app.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;
import app.models.*;
import app.exception.*;
import app.response.*;
import app.*;
@Controller
public class UserController {

    private Database database;
    private Logger logger;

    public UserController (){
        this.database = new Database();
        this.logger = new Logger();
    }

    @EndpointHandler(endpoint = "POST /sign-up")
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
    @EndpointHandler(endpoint = "POST /finish-sign-up")
    public Response cmdFinishSignUp(Request objRequest) {
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
    @EndpointHandler(endpoint = "POST /sign-in")
    public Response cmdSignIn(Request objRequest) {
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
    @EndpointHandler(endpoint = "POST /sign-out")
    public Response cmdSignOut(Request objRequest) {
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
    @EndpointHandler(endpoint = "GET /users")
    public Response cmdFindUsers(Request objRequest) {

        ArrayList<User> allUsers = database.findUsers(objRequest.getQueryString());
        ArrayList<HashMap> allUsersForResponse = new ArrayList<>();
        for (User user : allUsers) {
            allUsersForResponse.add(user.toHashMapUser());
        }
        return new SuccessfulResponseArray("200 OK", allUsersForResponse);

    }
    @EndpointHandler(endpoint = "PATCH /user")
    public Response cmdPatchUser(Request objRequest) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            HashMap updateProduct = mapper.readValue(objRequest.getBody(), HashMap.class);
            if (updateProduct.containsKey("amount")) {
                if ((Double) updateProduct.get("amount") <= 0) {
                    return new UnsuccessfulResponse("400 Bad Request", "Wrong amount value");
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
    @EndpointHandler(endpoint = "POST /forgot-password")
    public Response cmdForgotPassword(Request objRequest) {
        ObjectMapper mapper = new ObjectMapper();

        try {
            HashMap request = mapper.readValue(objRequest.getBody(), HashMap.class);

            if (!request.containsKey("login")) {
                return new UnsuccessfulResponse("400 Bad Request", "Login required");
            }
            User user = database.findUserByLogin((String) request.get("login"));
            if (user == null) {
                return new UnsuccessfulResponse("400 Bad Request", "Wrong login");
            }
            SMSSender smSsender = new SMSSender();
            user.setPasswordResetCode(UUID.randomUUID().toString().substring(0, 5));
            database.updateUser(user);
            smSsender.sendSms(user.getLogin(), user.getPasswordResetCode());

            return new SuccessfulResponseMessage("200 OK", "Send password reset code");

        } catch (JsonProcessingException e) {
            return new UnsuccessfulResponse("400 Bad Request", "Invalid JSON format");
        }
    }
    @EndpointHandler(endpoint = "POST /reset-password/finish")
    public Response cmdResetPasswordFinish(Request objRequest) {
        ObjectMapper mapper = new ObjectMapper();

        try {
            HashMap request = mapper.readValue(objRequest.getBody(), HashMap.class);
            if (!request.containsKey("login") || !request.containsKey("password") || !request.containsKey("passwordResetCode")){
                return new UnsuccessfulResponse("400 Bad Request", "There are no mandatory parameters");
            }
            User user = database.findUserByLogin((String) request.get("login"));
            if (user == null){
                return new UnsuccessfulResponse("400 Bad Request", "Wrong login");
            }
            try {
                user.changePasswordFromResetCode((String) request.get("passwordResetCode"),(String) request.get("password"));
            } catch (BadPasswordResetCodeException e) {
                return new UnsuccessfulResponse("400 Bad Request", "Wrong reset code");
            } catch (PasswordResetNotRequestedCodeException e) {
                return new UnsuccessfulResponse("400 Bad Request", "Reset code not requested");
            }
            database.updateUser(user);
            return new SuccessfulResponseMessage("200 OK", "Password successful changed");

        } catch (JsonProcessingException e) {
            return new UnsuccessfulResponse("400 Bad Request", "Invalid JSON format");
        }
    }
    @EndpointHandler(endpoint = "DELETE /user")
    public Response cmdDeleteUser(Request objRequest) {
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
            logger.log(e.getMessage(),"ERROR");
            return new UnsuccessfulResponse("500 Internal Server Error", "Something wrong");
        }
    }
}
