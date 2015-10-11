import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;


public class DatabaseWorker implements Runnable {
	
	private DatabaseProcessor dp;
	private Connection con;
	private ClientProxy cr;
	private Server middleware;

	public DatabaseWorker(DatabaseProcessor dp, Server middleware, ClientProxy cr) {
		this.dp = dp;
		this.cr = cr;
		this.middleware = middleware;
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
			long id =  generatedId.getLong(1);
			System.out.println("Inserted id for '" + cr.getRequest() + "': " + id);
			cr.setResponse("100 " + id);
			middleware.addToResponseQueue(cr);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		dp.returnConnectionToPool(con);
	}
	
}
