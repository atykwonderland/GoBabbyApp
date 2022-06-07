package GoBabbyApp;
import java.sql.* ;

class JDBC
{
    static int sqlCode=0;      // Variable to hold SQLCODE
    static String sqlState="00000";  // Variable to hold SQLSTATE

    public static void insert(Statement statement, String SQL) {
        // Inserting Data into the table
        try {
            //System.out.println(SQL);
            statement.executeUpdate(SQL);
        } catch (SQLException e) {
            sqlCode = e.getErrorCode(); // Get SQLCODE
            sqlState = e.getSQLState(); // Get SQLSTATE

            // Your code to handle errors comes here;
            //TODO: something more meaningful than a print would be good
            System.out.println("Code: " + sqlCode + "  sqlState: " + sqlState);
            System.out.println(e);
        }
    }

    public static ResultSet query(Statement statement, String SQL) {
        // Querying a table
        java.sql.ResultSet rs;
        try {
            //System.out.println(SQL);
            rs = statement.executeQuery(SQL);
        } catch (SQLException e) {
            sqlCode = e.getErrorCode(); // Get SQLCODE
            sqlState = e.getSQLState(); // Get SQLSTATE

            // Your code to handle errors comes here;
            //TODO: something more meaningful than a print would be good
            System.out.println("Code: " + sqlCode + "  sqlState: " + sqlState);
            System.out.println(e);
            return null;
        }
        return rs;
    }
}