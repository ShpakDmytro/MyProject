import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;

public class Database {
    int id;
    ArrayList<User> users;
    ArrayList<Product> products;

    public Database() {
        this.id = 0;
        this.users = new ArrayList<>();
        this.products = new ArrayList<>();
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

    void updateUserAfterFinishSignUp(User user) {

        Connection connection = createConnection();

        try {
            PreparedStatement stmt = connection.prepareStatement("UPDATE users SET confirmationCode = ?, status = ? WHERE id = ?");
            stmt.setString(1, user.getConfirmationCode());
            stmt.setString(2, user.getStatus());
            stmt.setString(3, user.getId());
            stmt.execute();

        } catch (SQLException e) {
            System.err.println("Something wrong with user update");
            throw new RuntimeException(e);
        } finally {
            try {
                connection.close();
            } catch (SQLException ignored) {
            }
        }
    }

    void updateUserAccessToken(User user) {
        Connection connection = createConnection();

        try {
            PreparedStatement stmt = connection.prepareStatement("UPDATE users SET accessToken = ? WHERE id = ?");
            stmt.setString(1, user.getAccessToken());
            stmt.setString(2, user.getId());
            stmt.execute();

        } catch (SQLException e) {
            System.err.println("Something wrong with user update");
            throw new RuntimeException(e);
        } finally {
            try {
                connection.close();
            } catch (SQLException ignored) {}
        }
    }

    void updateUserAmountAfterBuying(User user) {
        Connection connection = createConnection();

        try {
            PreparedStatement stmt = connection.prepareStatement("UPDATE users SET amount = ? WHERE id = ?");
            stmt.setDouble(1, user.getAmount());
            stmt.setString(2, user.getId());
            stmt.execute();

        } catch (SQLException e) {
            System.err.println("Something wrong with user update");
            throw new RuntimeException(e);
        } finally {
            try {
                connection.close();
            } catch (SQLException ignored) {}
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

    ArrayList<User> getAllUser() {
        ArrayList<User> usersForResponse = new ArrayList<>();

        for (User user : users) {
            usersForResponse.add(user);
        }
        return usersForResponse;
    }

    ArrayList<Product> getAllProduct() {
        ArrayList<Product> productsForResponse = new ArrayList<>();
        for (Product product : products) {
            productsForResponse.add(product);
        }
        return productsForResponse;
    }

}
