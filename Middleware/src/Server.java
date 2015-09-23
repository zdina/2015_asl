import java.net.ServerSocket;

public class Server {


	public static void main(String[] args) throws Exception {
		ServerSocket server = new ServerSocket(4321);

		while (true) {
			ClientWorker w = new ClientWorker(server.accept());
			Thread t = new Thread(w);
			t.start();
		}
		//to clean up resources
		//server.close();
	}

}
