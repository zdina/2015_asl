import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Queue;

class RequestWorker implements Runnable {

	private Socket client;
	private int number;
	private Server rp;

	RequestWorker(Socket client, int number, Server rp) {
		this.client = client;
		this.number = number;
		this.rp = rp;
	}

	public void run() {
		try {
			Thread.sleep(10000);
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		String line;
		BufferedReader in = null;
		PrintWriter out = null;
		try {
			in = new BufferedReader(new InputStreamReader(
					client.getInputStream()));
			line = in.readLine();
			System.out.println("thread " + number + ": " + line);
			rp.addToRequestQueue(new ClientRequest(client, line)); 
			for (ClientRequest cr : rp.getRequestQueue()) {
				System.out.println(cr.getRequest());
			}

			out = new PrintWriter(client.getOutputStream(), true);
			out.println("request received");
		} catch (IOException e) {
			System.out.println("in or out failed");
			System.exit(-1);
		}
		// try {
		// while ((line = in.readLine()) != null) {
		// if (line.equals("r")) {
		// String stm = "INSERT INTO messages (content) VALUES(?)";
		// PreparedStatement pst = con.prepareStatement(stm,
		// Statement.RETURN_GENERATED_KEYS);
		// pst.setString(1, "asdf");
		// pst.executeUpdate();
		// ResultSet generatedId = pst.getGeneratedKeys();
		// generatedId.next();
		// out.println(generatedId.getLong(1));

		// con.close();
		// }
		// }
		// } catch (Exception e) {
		// e.printStackTrace();
		// }
	}
}