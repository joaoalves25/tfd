import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;

public class ServiceCode {

	private List<String> replicasList;
	private int replicaNumber;
	private int viewNumber;
	private Status status;
	private Integer opNumber;
	private Map<Integer, Request> log;
	private int commitNumber;
	private Map<String, Pair<Integer, String>> client_table;
	private int fPlusOne;
	private String myIP;
	private List<Integer> replicasPort;
	private int myPort;
	private SendAndReceive sr;
	private boolean primary;
	private int totalReplicas;
	private int f;
	private final int CLIENT_TIMETOUT = 5000; // 20 sec
	private final int PRIMARY_TIMEOUT = CLIENT_TIMETOUT * 2;
	private boolean firstTime;
	private boolean local;
	private String primaryAddress;
	private int primaryPort;

	public ServiceCode(String argsPort) throws UnknownHostException, SocketException {
		Configuration configuration = new Configuration();
		viewNumber = 0;
		status = Status.NORMAL;
		opNumber = 0;
		log = new HashMap<>();
		commitNumber = opNumber;
		client_table = new HashMap<>();
		firstTime = false;
		replicasList = configuration.getReplicas();
		replicasPort = configuration.getReplicasPort();
		totalReplicas = replicasList.size();
		local = replicasList.get(0).equals("127.0.0.1");

		if (local) {
			myIP = "127.0.0.1";
			replicaNumber = myReplicaNumber(Integer.parseInt(argsPort));
		} else {
			myIP = myIPAddress();
			replicaNumber = myReplicaNumber(Integer.parseInt(argsPort));
		}

		if (replicaNumber == -1 || myIP == null) {
			System.out.println("NOT A VALID REPLICA.");
			System.exit(1);
		}

		myPort = replicasPort.get(replicaNumber);

		if (primary = replicasPort.get(0) == myPort) {
			primaryAddress = myIP;
			primaryPort = myPort;
		} else {
			primaryAddress = replicasList.get(0);
			primaryPort = replicasPort.get(0);
		}

		f = (totalReplicas - 1) / 2;
		fPlusOne = f + 1;
		sr = new SendAndReceive(new DatagramSocket(myPort));
		System.out.println("Viewstamp Replication System has started in port " + myPort + ".");
	}

	private int myReplicaNumber(int givenPort) {
		for (int i = 0; i < totalReplicas; i++)
			if (replicasPort.get(i) == givenPort)
				return i;

		return -1;
	}

	private String myIPAddress() {
		String ipAddress = "";
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
					ipAddress = addr.getHostAddress();
				}
			}
		} catch (SocketException e) {
			throw new RuntimeException(e);
		}
		if (replicasList.contains(ipAddress))
			return ipAddress;

		return null;
	}

	private String executeOperation(String request) {
		return request.toUpperCase();
	}

	private Map<Integer, Request> selectLog(ArrayList<DoViewChange> replicasLog) {
		Map<Integer, Request> newLog = log;
		for (int i = 0; i < replicasLog.size(); i++)
			if (replicasLog.get(i).getReplicaViewNumber() > viewNumber)
				if (replicasLog.get(i).getOpNumber() > opNumber)
					newLog = replicasLog.get(i).getLog();

		return newLog;
	}

	public void primarySendToReplicas(Request request) {
		opNumber++;
		log.put(opNumber, request);
		client_table.put(request.getClientId(), new Pair<>(request.getNumber(), ""));
		// enviar para as replicas
		for (int i = 0; i < replicasList.size(); i++)
			if(myPort != replicasPort.get(i))
			sr.send(new Prepare(TypeMessage.PREPARE, viewNumber, request, opNumber, commitNumber), replicasList.get(i),
					replicasPort.get(i));

		List<PrepareOK> replicasOK = new ArrayList<>();
		int counter = 1;

		while (counter < fPlusOne) {
			replicasOK.add((PrepareOK) sr.receive());
			System.out.println("Replica " + replicasOK.get(replicasOK.size() - 1).replicaNumber() + " has answered");
			counter++;
		}

		System.out.println("COUNTER: " + counter);
		if (counter >= fPlusOne) {
			commitNumber++;
			client_table.put(request.getClientId(),
					new Pair<>(request.getNumber(), executeOperation(request.getOperation())));

			System.out.println("CLIENT DESTINATION = " + request.getClientId().split(":")[0]);
			System.out.println("CLIENT PORT = " + request.getClientId().split(":")[1]);

			request.setExecuted();
			log.put(opNumber, request);

			sr.send(new Reply(TypeMessage.REPLY, viewNumber, request.getNumber(), client_table.get(
					request.getClientId()).getSecond()), request.getClientId().split(":")[0],
					Integer.parseInt(request.getClientId().split(":")[1]));

		}

		System.out.println("Client Table = " + client_table.toString());
		System.out.println("Log = " + log.toString());
	}

	public void run() {
		Timer timer = null;
		if (primary)
			System.out.println("I'm the PRIMARY server. All replicas must obey to my commands!");
		else {
			timer = new Timer();
			System.out.println("Greetings traveler! I'm replica number " + replicaNumber);
			timer.schedule(new SendViewChange(new StartViewChange(TypeMessage.STARTVIEWCHANGE, viewNumber,
					replicaNumber), sr), PRIMARY_TIMEOUT);
		}
		System.out.println("My IP address is: " + myIP);

		while (true) {

			Message message = sr.receive();
			
			
			
			
			
			if (primary) {

				switch (message.getTypeMessage()) {
				case REQUEST:
					System.out.println("RECEBI DO CLIENTE");
			/*		if (firstTime) {
						System.out.println("VOU CANCERLAR");
						timer.cancel();
						timer.purge();
					}*/

					// sou o primeiro e recebi uma mensagem.
					Request request = (Request) message;
					System.out.println("New request received. Operation to be executed: '" + request.getOperation()
							+ "'.");

					// primeira vez do cliente (nao existe na client_table)
					if (!client_table.containsKey(request.getClientId())) {
						primarySendToReplicas(request);

					} else { // Mais de um pedido para o mesmo cliente

						// se o requestNumber for o mais recente
						if (request.getNumber() > client_table.get(request.getClientId()).getFirst()) {
							primarySendToReplicas(request);

						} else if (request.getNumber() == client_table.get(request.getClientId()).getFirst()
								&& !client_table.get(request.getClientId()).getSecond().isEmpty()) {
							System.out.println("Resending last reply...");
							sr.send(new Reply(TypeMessage.REPLY, viewNumber, request.getNumber(), client_table.get(
									request.getClientId()).getSecond()), request.getClientId().split(":")[0],
									Integer.parseInt(request.getClientId().split(":")[1]));
						} else {
							System.out.println("Duplicated message received!");
							// message dropped
						}
					}
					
					
					new SendCommit(new Commit(TypeMessage.COMMIT, viewNumber, commitNumber), sr, myPort,
							CLIENT_TIMETOUT,"COMMIT Message sent to all replicas. Vim do ServiceCode").run();
					
					
			/*		timer = new Timer();
					timer.schedule(new SendCommit(new Commit(TypeMessage.COMMIT, viewNumber, commitNumber), sr, myPort,
							CLIENT_TIMETOUT), CLIENT_TIMETOUT);
					firstTime = true;*/
					
					break;
				case STARTVIEWCHANGE:
					System.out.println("STARTVIEWCAHNGE Received");

					break;
				case DOVIEWCHANGE:
					break;
				case COMMIT:
					break;
				case PREPAREOK:
					System.out.println("PREPAREOK Received");
					break;
				default:
					System.err.println("ERROR: THIS TYPE OF MESSAGE IS NOT RECOGNIZED!");
					break;
				}
			} else { // CODIGO DAS REPLICAS

				if (status.equals(Status.NORMAL)) {
					switch (message.getTypeMessage()) {
					case COMMIT:
						timer.cancel();
						timer.purge();
						Commit commit = (Commit) message;
						System.out.println("MY COMMIT NUMBER: " + commitNumber);
						System.out.println("COMMIT NUMBER FROM MESSAGE: " + commit.getCommitNumber());
						if (opNumber < commit.getCommitNumber()) {
							// wait for the request
						}
						if (commitNumber < commit.getCommitNumber()) {
							System.out.println("ENTRREI NO COMMITNUMBER!");
							commitNumber++;
							client_table.put(log.get(commitNumber).getClientId(), new Pair<>(log.get(commitNumber)
									.getNumber(), executeOperation(log.get(commitNumber).getOperation())));
							log.get(commitNumber).setExecuted();
							log.put(commitNumber, log.get(commitNumber));

						}
						System.out.println("Client Table = " + client_table.toString());
						System.out.println("Log = " + log.toString());
						timer = new Timer();
						timer.schedule(new SendViewChange(new StartViewChange(TypeMessage.STARTVIEWCHANGE, viewNumber,
								replicaNumber), sr), PRIMARY_TIMEOUT);
						break;
					case PREPARE:
						timer.cancel();
						timer.purge();
						Prepare prepare = (Prepare) message;
						// verifica se opnumber eh o seguinte
						if (prepare.getOpNumber() == log.keySet().size() + 1) {
							opNumber++;
							log.put(opNumber, prepare.getRequest());

							System.out.println("OPERATION NUMBER: " + opNumber);

							if (opNumber > 1) {
								if (commitNumber + 1 == prepare.getCommitNumber()) {
									commitNumber++;
									client_table.put(
											log.get(commitNumber).getClientId(),
											new Pair<>(log.get(commitNumber).getNumber(), executeOperation(log.get(
													commitNumber).getOperation())));

									log.get(opNumber - 1).setExecuted();
									log.put(opNumber - 1, log.get(opNumber - 1));

								} else {
									// problema de commits!
								}
							}
							client_table.put(prepare.getRequest().getClientId(), new Pair<>(prepare.getRequest()
									.getNumber(), ""));

							System.out.println("Message received! Sending PrepareOK to primary.");
							System.out.println("Client Table = " + client_table.toString());
							System.out.println("Log = " + log.toString());

							System.out.println("PRIMARY ADDRESS = " + primaryAddress + "\nPRIMARY PORT = "
									+ primaryPort);
							System.out.println(primary);

							sr.send(new PrepareOK(TypeMessage.PREPAREOK, viewNumber, opNumber, replicaNumber),
									primaryAddress, primaryPort);
						} else {
							// esta replica nao tem todos os pedidos!!!
							// esperar ate ter todos!!!! recuperacao!!!
						}
					case STARTVIEWCHANGE:
						System.out.println("RECEBI");
						break;
					case DOVIEWCHANGE:
						break;
					default:
						System.err.println("ERROR: THIS TYPE OF MESSAGE IS NOT RECOGNIZED!");
						break;
					}
				} else {
					// TO-DO
				}
			}
		}
	}

	public void close() {
		sr.close();
	}
}