import java.util.HashMap;

public class Product {
    private String id;
    private String name;
    private double price;

    public Product(String id, String name, double price) {
        this.id = id;
        this.name = name;
        this.price = price;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Double getPrice() {
        return price;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPrice (Double price){
        this.price = price;
    }

    public HashMap toHashMapProduct() {

        HashMap <String,Object> product = new HashMap<>();
        product.put("id",this.id);
        product.put("name",this.name);
        product.put("price",this.price);

        return product;
    }
}
