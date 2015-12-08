import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.List;

public class TrafficGenerator implements Runnable {

	// duracao em segundos que o traffic generator vai executar
	private static final int DURATION = 30 * 1000;
	// numero de pedidos por segundo
	private static final int MAX_REQUESTS = 1000 / 500;
	// numero de clientes
	private static final int NUM_THREADS = 1;

	private String clientId;
	private int requestNumber;
	private SendAndReceive sr;
	private String primary;
	private int primaryPort;
	private List<String> replicasList;
	private List<Integer> replicasPort;

	public TrafficGenerator(int clientPort) {
		Configuration configuration = new Configuration();
		replicasList = configuration.getReplicas();
		replicasPort = configuration.getReplicasPort();
		primary = replicasList.get(0);
		primaryPort = replicasPort.get(0);
		requestNumber = 1;

		try {
			sr = new SendAndReceive(new DatagramSocket(clientPort));
		} catch (SocketException e) {
			System.out.println("DatagramSocket already in use!");
			System.exit(1);
		}

		if (primary.equals("127.0.0.1"))
			clientId = "127.0.0.1" + ":" + clientPort;
		else {
			try {
				Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
				while (interfaces.hasMoreElements()) {
					NetworkInterface iface = interfaces.nextElement();
					// filtra interfaces inactivas, assim como o endereco
					// 127.0.0.1
					if (iface.isLoopback() || !iface.isUp())
						continue;

					Enumeration<InetAddress> addresses = iface.getInetAddresses();
					while (addresses.hasMoreElements()) {
						InetAddress addr = addresses.nextElement();
						clientId = addr.getHostAddress() + ":" + clientPort;
					}
				}
			} catch (SocketException e) {
				throw new RuntimeException(e);
			}
		}
	}

	@Override
	public void run() {
		int currentTime = (int) System.currentTimeMillis();
		int duration = currentTime + DURATION;

		while (currentTime < duration) {

			Request request = new Request("teste", clientId, requestNumber);

			sr.send(request, primary, primaryPort);
			System.out.println("Request successfully sent! Waiting for the upercase string conversion...");

			Reply reply = (Reply) sr.receive();

			System.out.println("\nReply received:\n" + reply.toString() + "\n");


				System.out.println("The operation '" + request.getOperation() + "' was successfully converted to '"
						+ reply.getOperation() + "'.");
				requestNumber = request.getRequestNumber() + 1;


			primary = replicasList.get(reply.getViewNumber() % replicasList.size());
			primaryPort = replicasPort.get(reply.getViewNumber() % replicasList.size());

			try {
				Thread.sleep(MAX_REQUESTS);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			}
		}
	}

	public static void main(String[] args) {

		Runnable trafficGenerator = new TrafficGenerator(Integer.parseInt(args[0]));
		for (int i = 1; i < NUM_THREADS + 1; i++) {
			Thread thread = new Thread(trafficGenerator);
			thread.start();
		}
	}
}