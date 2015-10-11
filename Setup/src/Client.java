import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class Client {

	public static void main(String[] args) throws Exception {
//		System.out.println("Client started");
		Socket socket = new Socket("Dinas-MacBook-Air.local", 4321);
//		System.out.println(socket.toString());
		PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
		BufferedReader in = new BufferedReader(new InputStreamReader(
				socket.getInputStream()));
		int count = 0;
		while (true) {
			out.println("client 2: # " + count);
			Thread.sleep(1000);
//			String line = in.readLine();
//			System.out.println(line);
			count++;
		}
//		socket.close();
	}
}
