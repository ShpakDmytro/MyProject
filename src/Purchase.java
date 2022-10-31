public class Purchase {
    String id;
    String userId;
    String productId;

    public Purchase(String id, String userId, String productId) {
        this.id = id;
        this.userId = userId;
        this.productId = productId;
    }

    public String getProductId() {
        return productId;
    }

    public String getUserId() {
        return userId;
    }
}
