import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;

public class Database {

    private Connection transactionConnection;

    public Database() {
    }

    private Connection createConnection() {

        try {
            return DriverManager.getConnection(
                    "jdbc:mysql://db-mysql-lon1-60836-do-user-2065621-0.b.db.ondigitalocean.com:25060/defaultdb",
                    "dima", "AVNS_7XEtwNq4TW_QQ5PqQIU");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    void insertUser(User user) {

        Connection connection = createConnection();

        try {
            PreparedStatement stmt = connection.prepareStatement("INSERT INTO users (id, firstName, lastName, amount, login, password, accessToken, status, confirmationCode) VALUES (?,?,?,?,?,?,?,?,?)");
            stmt.setString(1, user.getId());
            stmt.setString(2, user.getFirstName());
            stmt.setString(3, user.getLastName());
            stmt.setDouble(4, user.getAmount());
            stmt.setString(5, user.getLogin());
            stmt.setString(6, user.getPassword());
            stmt.setString(7, user.getAccessToken());
            stmt.setString(8, user.getStatus());
            stmt.setString(9, user.getConfirmationCode());
            stmt.execute();
        } catch (SQLException e) {
            System.err.println("Something wrong with user add");
            throw new RuntimeException(e);
        } finally {
            try {
                connection.close();
            } catch (SQLException ignored) {
            }
        }
    }

    void insertProduct(Product product) {

        Connection connection = createConnection();
        try {
            PreparedStatement stmt = connection.prepareStatement("INSERT INTO products (id, name, price) VALUES (?,?,?)");
            stmt.setString(1, product.getId());
            stmt.setString(2, product.getName());
            stmt.setDouble(3, product.getPrice());
            stmt.execute();

        } catch (SQLException e) {
            System.err.println("Something wrong with product add");
            throw new RuntimeException(e);
        } finally {
            try {
                connection.close();
            } catch (SQLException ignored) {
            }
        }
    }

    public void updateProduct(Product product) {

        Connection connection = createConnection();
        try {
            PreparedStatement stmt = connection.prepareStatement("UPDATE products SET id = ?, name = ?, price = ? WHERE id = ?");
            stmt.setString(1, product.getId());
            stmt.setString(2, product.getName());
            stmt.setDouble(3, product.getPrice());
            stmt.setString(4, product.getId());
            stmt.execute();

        } catch (SQLException e) {
            System.err.println("Something wrong with product add");
            throw new RuntimeException(e);
        } finally {
            try {
                connection.close();
            } catch (SQLException ignored) {
            }
        }
    }

    public void insertPurchase(Purchase purchase) {

        Connection connection;
        if (this.transactionConnection == null) {
            connection = createConnection();
        } else {
            connection = transactionConnection;
        }

        try {
            PreparedStatement stmt = connection.prepareStatement("INSERT INTO purchases (id, userId, productId) VALUES (?,?,?)");
            stmt.setString(1, purchase.getId());
            stmt.setString(2, purchase.getUserId());
            stmt.setString(3, purchase.getProductId());
            stmt.execute();

        } catch (SQLException e) {
            System.err.println("Something wrong with purchases add");
            throw new RuntimeException(e);
        } finally {
            try {
                if (transactionConnection == null) {
                    connection.close();
                }
            } catch (SQLException ignored) {
            }
        }
    }

    void updateUser(User user) {
        Connection connection;
        if (this.transactionConnection == null) {
            connection = createConnection();
        } else {
            connection = transactionConnection;
        }

        try {
            PreparedStatement stmt = connection.prepareStatement("UPDATE users SET id = ?, firstName = ?, lastName = ?, " +
                    "amount = ?, login = ?, password = ?, accessToken = ?,  status = ?, confirmationCode = ? WHERE id = ?");
            stmt.setString(1, user.getId());
            stmt.setString(2, user.getFirstName());
            stmt.setString(3, user.getLastName());
            stmt.setDouble(4, user.getAmount());
            stmt.setString(5, user.getLogin());
            stmt.setString(6, user.getPassword());
            stmt.setString(7, user.getAccessToken());
            stmt.setString(8, user.getStatus());
            stmt.setString(9, user.getConfirmationCode());
            stmt.setString(10, user.getId());
            stmt.execute();

        } catch (SQLException e) {
            System.err.println("Something wrong with user update");
            throw new RuntimeException(e);
        } finally {
            try {
                if (transactionConnection == null) {
                    connection.close();
                }
            } catch (SQLException ignored) {
            }
        }
    }

    boolean existsUserByLogin(String loginFromUser) {

        Connection connection = createConnection();
        try {
            PreparedStatement statement = connection.prepareStatement("SELECT * FROM users WHERE login = ?");
            statement.setString(1, loginFromUser);
            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                return true;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            try {
                connection.close();
            } catch (SQLException ignored) {
            }
        }

        return false;

    }

    User findUserByLoginAndPassword(String login, String password) {

        Connection connection = createConnection();
        try {
            PreparedStatement statement = connection.prepareStatement("SELECT * FROM users WHERE login = ? AND password = ?");
            statement.setString(1, login);
            statement.setString(2, password);
            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                return new User(resultSet.getString("id"), resultSet.getString("firstName"),
                        resultSet.getString("lastName"), resultSet.getDouble("amount"),
                        resultSet.getString("login"), resultSet.getString("password"),
                        resultSet.getString("accessToken"), resultSet.getString("status"),
                        resultSet.getString("confirmationCode"));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            try {
                connection.close();
            } catch (SQLException ignored) {
            }
        }

        return null;
    }

    User findUserByAccessToken(String accessToken) {
        Connection connection = createConnection();
        try {
            PreparedStatement statement = connection.prepareStatement("SELECT * FROM users WHERE accessToken = ?");
            statement.setString(1, accessToken);
            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                return new User(resultSet.getString("id"), resultSet.getString("firstName"),
                        resultSet.getString("lastName"), resultSet.getDouble("amount"),
                        resultSet.getString("login"), resultSet.getString("password"),
                        resultSet.getString("accessToken"), resultSet.getString("status"),
                        resultSet.getString("confirmationCode"));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            try {
                connection.close();
            } catch (SQLException ignored) {
            }
        }

        return null;
    }

    User findUserById(String id) {
        Connection connection = createConnection();
        try {
            PreparedStatement statement = connection.prepareStatement("SELECT * FROM users WHERE id = ?");
            statement.setString(1, id);
            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                return new User(resultSet.getString("id"), resultSet.getString("firstName"),
                        resultSet.getString("lastName"), resultSet.getDouble("amount"),
                        resultSet.getString("login"), resultSet.getString("password"),
                        resultSet.getString("accessToken"), resultSet.getString("status"),
                        resultSet.getString("confirmationCode"));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            try {
                connection.close();
            } catch (SQLException ignored) {
            }
        }

        return null;
    }

    Product findProductById(String id) {
        Connection connection = createConnection();
        try {
            PreparedStatement statement = connection.prepareStatement("SELECT * FROM products WHERE id = ?");
            statement.setString(1, id);
            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                return new Product(resultSet.getString("id"), resultSet.getString("name"),
                        resultSet.getDouble("price"));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            try {
                connection.close();
            } catch (SQLException ignored) {
            }
        }

        return null;
    }

    ArrayList<User> findUsers(HashMap<String, String> criteria) {
        ArrayList<User> usersForResponse = new ArrayList<>();
        ArrayList<String> preparedStatementValues = new ArrayList<>();

        Connection connection = createConnection();
        try {
            String sqlQuery = "SELECT * FROM users WHERE ";
            if (criteria.isEmpty()) {
                sqlQuery = "SELECT * FROM users";

            }
            if (criteria.containsKey("productId")) {
                sqlQuery = "SELECT users.id, users.firstName, users.lastName, users.amount, users.login, users.password, " +
                        "users.accessToken, users.status, users.confirmationCode FROM users " +
                        "JOIN purchases ON users.id = purchases.userId WHERE purchases.productId = ?";
                preparedStatementValues.add(criteria.get("productId"));
            }
            if (criteria.containsKey("id")) {
                sqlQuery += sqlQuery.contains("=") ? " AND users.id = ?" : " id = ?";
                preparedStatementValues.add(criteria.get("id"));

            }
            if (criteria.containsKey("firstName")) {
                sqlQuery += sqlQuery.contains("=") ? " AND users.firstName = ?" : " firstName = ?";
                preparedStatementValues.add(criteria.get("firstName"));

            }
            if (criteria.containsKey("lastName")) {
                sqlQuery += sqlQuery.contains("=") ? " AND users.lastName = ?" : " lastName = ?";
                preparedStatementValues.add(criteria.get("lastName"));

            }
            if (criteria.containsKey("amount")) {
                sqlQuery += sqlQuery.contains("=") ? " AND users.amount = ?" : " amount = ?";
                preparedStatementValues.add(criteria.get("amount"));

            }
            if (criteria.containsKey("login")) {
                sqlQuery += sqlQuery.contains("=") ? " AND users.login = ?" : " login = ?";
                preparedStatementValues.add(criteria.get("login"));

            }
            if (criteria.containsKey("password")) {
                sqlQuery += sqlQuery.contains("=") ? " AND users.password = ?" : " password = ?";
                preparedStatementValues.add(criteria.get("password"));

            }
            if (criteria.containsKey("accessToken")) {

                if (criteria.get("accessToken").equals("null")) {
                    sqlQuery += sqlQuery.contains("=") ? " AND users.accessToken IS NULL" : "accessToken IS NULL";

                } else {
                    sqlQuery += sqlQuery.contains("=") ? " AND users.accessToken = ?" : " accessToken = ?";
                    preparedStatementValues.add(criteria.get("accessToken"));
                }

            }
            if (criteria.containsKey("status")) {
                sqlQuery += sqlQuery.contains("=") ? " AND users.status = ?" : " status = ?";
                preparedStatementValues.add(criteria.get("status"));

            }
            if (criteria.containsKey("confirmationCode")) {
                sqlQuery += sqlQuery.contains("=") ? " AND users.confirmationCode = ?" : " confirmationCode = ?";
                preparedStatementValues.add(criteria.get("confirmationCode"));

            }

            PreparedStatement statement = connection.prepareStatement(sqlQuery);

            for (int i = 0; i < preparedStatementValues.size(); i++) {
                statement.setString(i + 1, preparedStatementValues.get(i));
            }

            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                User user = new User(resultSet.getString("id"), resultSet.getString("firstName"),
                        resultSet.getString("lastName"), resultSet.getDouble("amount"),
                        resultSet.getString("login"), resultSet.getString("password"),
                        resultSet.getString("accessToken"), resultSet.getString("status"),
                        resultSet.getString("confirmationCode"));
                usersForResponse.add(user);
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            try {
                connection.close();
            } catch (SQLException ignored) {
            }
        }
        return usersForResponse;
    }

    ArrayList<Product> findProducts(HashMap<String, String> criteria) {
        ArrayList<Product> productsForResponse = new ArrayList<>();
        ArrayList<String> preparedStatementValues = new ArrayList<>();

        Connection connection = createConnection();
        try {
            String sqlQuery = "SELECT * FROM products WHERE ";
            if (criteria.isEmpty()) {
                sqlQuery = "SELECT * FROM products";

            }
            if (criteria.containsKey("userId")) {
                sqlQuery = "SELECT products.id, products.name, products.price FROM products JOIN purchases ON " +
                        "products.id = purchases.productId WHERE userId = ?";
                preparedStatementValues.add(criteria.get("userId"));

            }
            if (criteria.containsKey("id")) {
                sqlQuery += sqlQuery.contains("=") ? " AND products.id = ?" : " id = ?";
                preparedStatementValues.add(criteria.get("id"));
            }
            if (criteria.containsKey("name")) {
                sqlQuery += sqlQuery.contains("=") ? " AND products.name = ?" : " name = ?";
                preparedStatementValues.add(criteria.get("name"));

            }
            if (criteria.containsKey("price")) {
                sqlQuery += sqlQuery.contains("=") ? " AND products.price = ?" : " price = ?";
                preparedStatementValues.add(criteria.get("price"));

            }

            PreparedStatement statement = connection.prepareStatement(sqlQuery);

            for (int i = 0; i < preparedStatementValues.size(); i++) {
                statement.setString(i + 1, preparedStatementValues.get(i));
            }

            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                Product product = new Product(resultSet.getString("id"), resultSet.getString("name"),
                        resultSet.getDouble("price"));
                productsForResponse.add(product);
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            try {
                connection.close();
            } catch (SQLException ignored) {
            }
        }
        return productsForResponse;
    }

    ArrayList <Purchase> findPurchasesByProductId(String id){
        ArrayList <Purchase> purchasesForResponse = new ArrayList<>();

        Connection connection = createConnection();
        try {
            PreparedStatement statement = connection.prepareStatement("SELECT FROM purchases WHERE productId = ?");
            statement.setString(1,id);
            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                Purchase purchase = new Purchase(resultSet.getString("id"), resultSet.getString("userId"),
                        resultSet.getString("productId"));
                purchasesForResponse.add(purchase);
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            try {
                connection.close();
            } catch (SQLException ignored) {
            }
        }

        return purchasesForResponse;
    }

    public void startTransaction() {
        this.transactionConnection = createConnection();
        try {
            PreparedStatement statement = transactionConnection.prepareStatement("START TRANSACTION;");
            statement.execute();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void closeTransaction() {
        try {
            PreparedStatement statement = transactionConnection.prepareStatement("COMMIT");
            statement.execute();
            this.transactionConnection.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void rollback() {
        try {
            PreparedStatement statement = transactionConnection.prepareStatement("ROLLBACK");
            statement.execute();
            this.transactionConnection.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void deleteProduct(String id) {
        Connection connection;
        if (this.transactionConnection == null) {
            connection = createConnection();
        } else {
            connection = transactionConnection;
        }
        try {
            PreparedStatement statement = connection.prepareStatement("DELETE FROM products WHERE id = ?");
            statement.setString(1, id);
            statement.execute();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            try {
                if (transactionConnection == null) {
                    connection.close();
                }
            } catch (SQLException ignored) {
            }
        }
    }

    public void deletePurchases(String id) {
        Connection connection;
        if (this.transactionConnection == null) {
            connection = createConnection();
        } else {
            connection = transactionConnection;
        }
        try {
            PreparedStatement statement = connection.prepareStatement("DELETE FROM purchases WHERE id = ?");
            statement.setString(1, id);
            statement.execute();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            try {
                if (transactionConnection == null) {
                    connection.close();
                }
            } catch (SQLException ignored) {
            }
        }
    }
}
