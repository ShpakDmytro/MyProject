import java.sql.*;

public class Test {
    public static void main(String[] args) throws SQLException {
        Connection conn = DriverManager.getConnection(
                "jdbc:mysql://db-mysql-lon1-60836-do-user-2065621-0.b.db.ondigitalocean.com:25060/defaultdb",
                "dima","AVNS_7XEtwNq4TW_QQ5PqQIU");
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT id FROM users");
        while (rs.next()){
            System.out.println(rs.getInt("id"));
        }
    }
}
