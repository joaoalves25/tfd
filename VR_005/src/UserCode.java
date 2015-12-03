import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

public class UserCode {

	private String id;
	private int requestNumber;
	private int clientPort;
	private SendAndReceive sr;

	public UserCode() {
		requestNumber = 1;
//		clientPort = new Configuration().getClientsPort();
		try {
			sr = new SendAndReceive(new DatagramSocket());
			clientPort = sr.getPort();
			System.out.println("MY PORT IS: "+clientPort);
			Enumeration<NetworkInterface> interfaces = NetworkInterface
					.getNetworkInterfaces();
			while (interfaces.hasMoreElements()) {
				NetworkInterface iface = interfaces.nextElement();
				// filtra interfaces inactivas, assim como o endereco 127.0.0.1
				if (iface.isLoopback() || !iface.isUp())
					continue;

				Enumeration<InetAddress> addresses = iface.getInetAddresses();
				while (addresses.hasMoreElements()) {
					InetAddress addr = addresses.nextElement();
					id = addr.getHostAddress() + ":" + clientPort;
				}
			}
		} catch (SocketException e) {
			throw new RuntimeException(e);
		}
	}

	public void run() {
		String primary = new Configuration().getReplicas().get(0);
		int primaryPort = new Configuration().getReplicasPort();
		Request request = new Request("teste", id, requestNumber);
		sr.send(request, primary, primaryPort);
		System.out
				.println("Request successfully sent! Waiting for the upercase string conversion...");
		Reply reply = (Reply) sr.receive();
		System.out.println("The operation '" + request.getOperation()
				+ "' was successfully converted to '" + reply.getOperation()
				+ "'.");
		requestNumber++;
	}

	public void close() {
		sr.close();
	}
}