public class Purchases {
    String id;
    String idUser;
    String idProduct;

    public Purchases(String id, String idUser, String idProduct) {
        this.id = id;
        this.idUser = idUser;
        this.idProduct = idProduct;
    }

    public String getIdProduct() {return idProduct;}

    public String getIdUser() {return idUser;}
}
