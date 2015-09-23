import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;



public class Database {

	public static void main(String[] args) throws Exception {
		Class.forName("org.postgresql.Driver");
		Connection connection = null;
		connection = DriverManager.getConnection(
		   "jdbc:postgresql://localhost:5432/test","dinazverinski", "");
		
		String stm = "INSERT INTO messages (content) VALUES(?)";
        PreparedStatement pst = connection.prepareStatement(stm);
        pst.setString(1, "asdf");                    
        pst.executeUpdate();
        
		connection.close();
		
	}
}
