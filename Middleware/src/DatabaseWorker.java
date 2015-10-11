import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;


public class DatabaseWorker implements Runnable {
	
	private DatabaseProcessor dp;
	private Connection con;
	private ClientRequest cr;

	public DatabaseWorker(DatabaseProcessor dp, ClientRequest cr) {
		this.dp = dp;
		this.cr = cr;
	}
	
	public void run() {
		con = dp.getConnectionFromPool();
		while (con == null)
			con = dp.getConnectionFromPool();
		
		try {
			RequestTranslator rt = new RequestTranslator(cr.getRequest(), con);
			PreparedStatement ps = rt.getStatement();
			ps.executeUpdate();
			ResultSet generatedId = ps.getGeneratedKeys();
			generatedId.next();
			System.out.println("Inserted id for '" + cr.getRequest() + "': " + generatedId.getLong(1));
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		dp.returnConnectionToPool(con);
	}
	
}
