import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;


public class ResponseAcceptor implements Runnable {

	private ServerSocket acceptor;
	
	public ResponseAcceptor(int port) throws IOException {
		acceptor = new ServerSocket(port);
	}
	
	@Override
	public void run() {
		while (true) {
			try {
				Socket middleware = acceptor.accept();
				BufferedReader in = new BufferedReader(new InputStreamReader(
						middleware.getInputStream()));
				String line = in.readLine();
				System.out.println(line);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
	}

	
}
