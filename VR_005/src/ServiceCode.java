import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
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

	private List<String> configuration;
	private int replicaNumber;
	private int viewNumber;
	private Status status;
	private Integer opNumber;
	private Map<Integer, Request> log;
	private int commitNumber;
	private Map<String, Pair<Integer, String>> client_table;
	private int fPlusOne;
	private String myIP;
	private String primaryAddress;
	private List<Integer> replicasPort;
	private int myPort;
	private SendAndReceive sr;
	private boolean primary;
	private int totalReplicas;
	private int f;
	private final int CLIENT_TIMETOUT = 10000;
	private final int PRIMARY_TIMEOUT = CLIENT_TIMETOUT * 2;
	private boolean firstTime;

	public ServiceCode() throws UnknownHostException, SocketException {
		configuration = new Configuration().getReplicas();
		totalReplicas = configuration.size();
		replicaNumber = -1;
		viewNumber = 0;
		status = Status.NORMAL;
		opNumber = 0;
		log = new HashMap<>();
		commitNumber = opNumber;
		client_table = new HashMap<>();
		f = (totalReplicas - 1) / 2;
		fPlusOne = f + 1;
		myIP = "";

		firstTime = false;
		
		primaryAddress = configuration.get(0);
		replicasPort = new Configuration().getReplicasPort();
		

		if (configuration.get(0).equals("127.0.0.1")) {
			myIP = "127.0.0.1";
			StringBuilder sb = new StringBuilder();

			try {
				BufferedReader file = new BufferedReader(new FileReader("Configuration.txt"));
				String line;
				while ((line = file.readLine()) != null) {
					sb.append(line + "\n");
					if (line.equals("SERVERS_PORT")) {
						while ((line = file.readLine()) != null) {
							replicaNumber++;
							sb.append(line + "\n");
						}
					}
				}
				file.close();
				myPort = replicasPort.get(replicaNumber);
				
				if (replicaNumber < totalReplicas-1)
					sb.append(myPort+1);

				FileOutputStream fileOut;
				fileOut = new FileOutputStream("Configuration.txt");
				fileOut.write(sb.toString().getBytes());
				fileOut.close();

			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}

		} else {

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
						myIP = addr.getHostAddress();
					}
				}
			} catch (SocketException e) {
				throw new RuntimeException(e);
			}

			for (int i = 0; i < configuration.size(); i++)
				if (myIP.equals(configuration.get(i)))
					replicaNumber = i;
			
			myPort = replicasPort.get(0);

		}

		switch (replicaNumber) {
		case (0):
			primary = true;
			break;
		case (-1):
			System.out.println("NOT A VALID REPLICA.");
			System.exit(1);
			break;
		default:
			primary = false;
			break;
		}
		sr = new SendAndReceive(new DatagramSocket(myPort));
		System.out.println("Viewstamp Replication System has started!");
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

	public void leaderSend(Request request) {
		opNumber++;
		log.put(opNumber, request);
		client_table.put(request.getClientId(), new Pair<>(request.getNumber(), ""));
		// enviar para as replicas
		for (int i = 1; i < configuration.size(); i++)
			if (configuration.get(0).equals("127.0.0.1"))
				sr.send(new Prepare(TypeMessage.PREPARE, viewNumber, request, opNumber, commitNumber),
						configuration.get(i), myPort + i);
			else
				sr.send(new Prepare(TypeMessage.PREPARE, viewNumber, request, opNumber, commitNumber),
						configuration.get(i), replicasPort.get(0));

		List<PrepareOK> replicasOK = new ArrayList<>();
		int counter = 1
				;
		System.out.println("FPLUSONE: "+(fPlusOne));
		while (counter < fPlusOne) {
			replicasOK.add((PrepareOK) sr.receive());
			System.out.println("Replica " + replicasOK.get(replicasOK.size() - 1).replicaNumber() + " has answered");
			counter++;
		}
		
		System.out.println("COUNTER: "+ counter);
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
		else{
			timer = new Timer();
			System.out.println("Greetings traveler! I'm replica number " + replicaNumber);
			timer.schedule(new SendViewChange(new StartViewChange(TypeMessage.STARTVIEWCHANGE, viewNumber, replicaNumber), sr), PRIMARY_TIMEOUT);
		}
		System.out.println("My IP address is: " + myIP);

		while (true) {
			Message message = sr.receive();
			if (primary) {
				switch (message.getTypeMessage()) {
				case REQUEST:
					System.out.println("RECEBI DO CLIENTE");
					if(firstTime){
						System.out.println("VOU CANCERLAR");
						timer.cancel();
						timer.purge();
					}
					
					
					// sou o primeiro e recebi uma mensagem.
					Request request = (Request) message;
					System.out.println("New request received. Operation to be executed: '" + request.getOperation()
							+ "'.");

					// primeira vez do cliente (nao existe na client_table)
					if (!client_table.containsKey(request.getClientId())) {
						leaderSend(request);

					} else { // Mais de um pedido para o mesmo cliente

						// se o requestNumber for o mais recente
						if (request.getNumber() > client_table.get(request.getClientId()).getFirst()) {
							leaderSend(request);

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
					timer = new Timer();
					timer.schedule(new SendCommit(new Commit(TypeMessage.COMMIT, viewNumber, commitNumber), sr, myPort),
							CLIENT_TIMETOUT);
					firstTime = true;
					break;
				case STARTVIEWCHANGE:
					System.out.println("RECEBI UM STARTVIEWCAHNGE");
					
					break;
				case DOVIEWCHANGE:
					break;
				case COMMIT:
					break;
				case PREPAREOK:
					System.out.println("Thank you but i dont need you!");
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
						System.out.println("MY COMMIT NUMBER: "+ commitNumber);
						System.out.println("COMMIT NUMBER FROM MESSAGE: "+ commit.getCommitNumber());
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
						timer.schedule(new SendViewChange(new StartViewChange(TypeMessage.STARTVIEWCHANGE, viewNumber, replicaNumber), sr), PRIMARY_TIMEOUT);
						break;
					case PREPARE:
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

							if (configuration.get(0).equals("127.0.0.1"))
								sr.send(new PrepareOK(TypeMessage.PREPAREOK, viewNumber, opNumber, replicaNumber),
										primaryAddress, replicasPort.get(0));
							else
								sr.send(new PrepareOK(TypeMessage.PREPAREOK, viewNumber, opNumber, replicaNumber),
										primaryAddress, replicasPort.get(0));
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