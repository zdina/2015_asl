import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class Client {

	public static void main(String[] args) throws Exception {
		Socket socket = new Socket("Dinas-MacBook-Air.local", 4321);
		PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
		BufferedReader in = new BufferedReader(new InputStreamReader(
				socket.getInputStream()));
		while (true) {
			out.println("client 2");
			String line = in.readLine();
			System.out.println(line);
			Thread.sleep(1000);
		}
	}
}
