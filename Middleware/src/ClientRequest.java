import java.net.InetAddress;

public class ClientRequest {

	private String request;
	private InetAddress address;
	private int port;

	public ClientRequest(InetAddress adress, int port, String request) {
		this.address = adress;
		this.port = port;
		this.request = request;
	}

	public String getRequest() {
		return request;
	}

	public InetAddress getAddress() {
		return address;
	}

	public int getPort() {
		return port;
	}
}