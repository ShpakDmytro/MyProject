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
public class ProductController {

    private Database database;
    private Logger logger;

    public ProductController(){
        this.database = new Database();
        this.logger = new Logger();
    }

    @EndpointHandler(endpoint = "POST /product")
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

    @EndpointHandler(endpoint = "GET /products")
    public Response cmdFindProducts(Request objRequest) {

        ArrayList<Product> allProduct = database.findProducts(objRequest.getQueryString());
        ArrayList<HashMap> allProductForResponse = new ArrayList<>();
        for (Product product : allProduct) {
            allProductForResponse.add(product.toHashMapProduct());
        }

        return new SuccessfulResponseArray("200 OK", allProductForResponse);
    }

    @EndpointHandler(endpoint = "POST /bought-product")
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
            logger.log(e.getMessage(),"ERROR");
            return new UnsuccessfulResponse("500 Internal Server Error", "Something wrong");
        }
    }

    @EndpointHandler(endpoint = "PATCH /product")
    public Response cmdPatchProduct(Request objRequest) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            HashMap updateProduct = mapper.readValue(objRequest.getBody(), HashMap.class);
            if (updateProduct.containsKey("price")) {
                if ((Double) updateProduct.get("price") <= 0) {
                    return new UnsuccessfulResponse("400 Bad Request", "Wrong amount value");
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

    @EndpointHandler(endpoint = "DELETE /product")
    public Response cmdDeleteProduct(Request objRequest) {
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
            logger.log(e.getMessage(),"ERROR");
            return new UnsuccessfulResponse("500 Internal Server Error", "Something wrong");
        }
    }
}
