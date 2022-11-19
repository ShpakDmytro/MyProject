package models;

public class Purchase {
    private String id;
    private String userId;
    private String productId;

    public Purchase(String id, String userId, String productId) {
        this.id = id;
        this.userId = userId;
        this.productId = productId;
    }

    public String getId () {
        return id;
    }

    public String getProductId() {
        return productId;
    }

    public String getUserId() {
        return userId;
    }
}
