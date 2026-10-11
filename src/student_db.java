import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;


public class student_db {

    private static final String URL = "jdbc:mysql://localhost:3306/student_db";


    private static final String USER = "root";
    private static final String PASSWORD = "1122";

    public  static Connection getConnect() throws SQLException{
        return DriverManager.getConnection(URL,USER,PASSWORD);
    }

}
