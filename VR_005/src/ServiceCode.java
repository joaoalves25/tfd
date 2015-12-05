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
	private String primary;
	private int replicasPort;
	private SendAndReceive sr;
	private int totalReplicas;
	private int toleratedF;

	public ServiceCode() throws UnknownHostException, SocketException {
		configuration = new Configuration().getReplicas();
		replicaNumber = -1;
		viewNumber = 0;
		status = Status.NORMAL;
		opNumber = 0;
		log = new HashMap<>();
		commitNumber = opNumber;
		client_table = new HashMap<>();
		toleratedF = (configuration.size() -1) / 2;
		fPlusOne = (configuration.size() / 2) + 1;
		myIP = "";
		primary = new Configuration().getReplicas().get(0);
		replicasPort = new Configuration().getReplicasPort();
		sr = new SendAndReceive(new DatagramSocket(replicasPort));

		if (configuration.get(0).equals("127.0.0.1")) {
			myIP = "127.0.0.1";
			StringBuilder sb = new StringBuilder();

			try {
				BufferedReader file = new BufferedReader(new FileReader(
						"Configuration.txt"));
				String line;
				totalReplicas = 0;
				while ((line = file.readLine()) != null) {
					sb.append(line + "\n");
					totalReplicas++;
					if (line.equals("SERVERS_PORT")) {
						while ((line = file.readLine()) != null) {
							replicaNumber++;
							sb.append(line + "\n");
						}
					}
				}
				file.close();

				if (replicaNumber < (totalReplicas-3))
					sb.append(replicasPort + 1);
				else
					sb.setLength(sb.length() - 11);

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
				Enumeration<NetworkInterface> interfaces = NetworkInterface
						.getNetworkInterfaces();
				while (interfaces.hasMoreElements()) {
					NetworkInterface iface = interfaces.nextElement();
					// filtra interfaces inactivas, assim como o endereco
					// 127.0.0.1
					if (iface.isLoopback() || !iface.isUp())
						continue;

					Enumeration<InetAddress> addresses = iface
							.getInetAddresses();
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

		}
		if (replicaNumber == -1) {
			System.out.println("PERMISSION DENIED: TRY HARDER");
			System.exit(1);
		}
		System.out.println("Viewstamp Replication System has started!");
	}

	private String executeOperation(String request) {
		return request.toUpperCase();
	}


	private Map<Integer,Request> selectLog(ArrayList<DoViewChange> replicasLog){
		Map<Integer,Request> newLog = log;
		for(int i=0; i < replicasLog.size(); i++)
			if (replicasLog.get(i).getMyV() > viewNumber)
				if (replicasLog.get(i).getOp() > opNumber)
					newLog = replicasLog.get(i).getLog();

		return newLog;
	}

	
	
	public void leaderSend(Request request) {
		opNumber++;
		log.put(opNumber, request);
		client_table.put(request.getClientId(), new Pair<>(request.getNumber(),
				""));
		// enviar para as replicas
		for (int i = 1; i < configuration.size(); i++)
			if(configuration.get(0).equals("127.0.0.1"))
			sr.send(new Prepare(TypeMessage.PREPARE, viewNumber, request,
					opNumber, commitNumber), configuration.get(i), replicasPort+i);
			else
				sr.send(new Prepare(TypeMessage.PREPARE, viewNumber, request,
						opNumber, commitNumber), configuration.get(i), replicasPort);
		
		List<PrepareOK> replicasOK = new ArrayList<>();
		int counter = 0;
		while (counter < fPlusOne) {
			replicasOK.add((PrepareOK) sr.receive());
			System.out.println("Replica "
					+ replicasOK.get(replicasOK.size() - 1).replicaNumber()
					+ " has answered");
			counter++;
		}

		if (counter >= fPlusOne) {
			commitNumber++;
			client_table.put(
					request.getClientId(),
					new Pair<>(request.getNumber(), executeOperation(request
							.getOperation())));
			
			System.out.println("CLIENT DESTINATION = "+ request.getClientId().split(":")[0]);
			System.out.println("CLIENT PORT = "+ request.getClientId().split(":")[1]);
			
			request.setExecuted();
			log.put(opNumber, request);
			
			sr.send(new Reply(TypeMessage.REPLY, viewNumber, request
					.getNumber(), client_table.get(request.getClientId())
					.getSecond()), 
					request.getClientId().split(":")[0], 
					Integer.parseInt(request.getClientId().split(":")[1]));
		}

		System.out.println("Client Table = " + client_table.toString());
		System.out.println("Log = "+ log.toString());
	}

	public void run() {
		
		System.out.println("I TOLERATE F: "+ toleratedF);
		Timer timer = null;
		if (replicaNumber == 0)
			System.out
					.println("I'm the primary server. All replicas must obey to my commands!");
		else
			System.out
					.println("Greetings! I'm replica number " + replicaNumber);

		System.out.println("My IP address is: " + myIP);

		while (true) {
			if (replicaNumber == 0) {
				timer = new Timer();
				System.out.println("REPLICA NUMBER: "+replicaNumber);
				timer.schedule(new SendCommit(new Commit(TypeMessage.COMMIT,
						viewNumber, commitNumber), sr), 10000);
			}
			Message message = sr.receive();
			if (replicaNumber == 0) { // PRIMARY 
				switch (message.getTypeMessage()) {
				case REQUEST:
					timer.cancel();
					// sou o primeiro e recebi uma mensagem.
					Request request = (Request) message;
					System.out
							.println("New request received. Operation to be executed: '"
									+ request.getOperation() + "'.");

					// primeira vez do cliente (nao existe na client_table)
					if (!client_table.containsKey(request.getClientId())) {
						leaderSend(request);

					} else { // Mais de um pedido para o mesmo cliente

						if (request.getNumber() > client_table.get(
								request.getClientId()).getFirst()) { // request_number
																		// eh mais
																		// recente
							leaderSend(request);

						} else if (request.getNumber() == client_table.get(
								request.getClientId()).getFirst()
								&& !client_table.get(request.getClientId())
										.getSecond().isEmpty()) {
							System.out.println("Resending last reply...");
							sr.send(new Reply(TypeMessage.REPLY, viewNumber,
									request.getNumber(), client_table.get(
											request.getClientId()).getSecond()),
									request.getClientId().split(":")[0], Integer
											.parseInt(request.getClientId().split(
													":")[1]));
						} else {
							System.out.println("Duplicated message received!");
							// message dropped
						}
					}
					break;
				default:
					System.err.println("ERROR: DON'T RECOGNIZE THIS TYPE OF MESSAGE!");
					break;
				}
				
			} else { // CODIGO DAS REPLICAS
				if (status.equals(Status.NORMAL)){
					switch(message.getTypeMessage()){
					case COMMIT:
						Commit commit = (Commit) message;
						if (opNumber < commit.getCommitNumber()) {
							// wait for the request

						}
						if (commitNumber < commit.getCommitNumber()) {
							commitNumber++;
							client_table.put(log.get(commitNumber).getClientId(),
											new Pair<>(log.get(commitNumber).getNumber(),
													executeOperation(log.get(commitNumber).getOperation())));

							log.get(commitNumber).setExecuted();
							log.put(commitNumber, log.get(commitNumber));
						}
						System.out.println("Client Table = "
								+ client_table.toString());
						System.out.println("Log = "+log.toString());
						break;
					case PREPARE:

						Prepare prepare = (Prepare) message;

						// verifica se opnumber eh o seguinte
						if (prepare.getOpNumber() == log.keySet().size() + 1) {
							opNumber++;
							log.put(opNumber, prepare.getRequest());

							System.out.println("OPERATION NUMBER: " + opNumber);

							if (opNumber > 1) {
								if (commitNumber + 1 == prepare
										.getCommitNumber()) {
									commitNumber++;
									client_table
											.put(log.get(commitNumber)
													.getClientId(),
													new Pair<>(
															log.get(commitNumber)
																	.getNumber(),
															executeOperation(log
																	.get(commitNumber)
																	.getOperation())));
									
									
									log.get(opNumber-1).setExecuted();
									log.put(opNumber-1, log.get(opNumber-1));
									
								} else {
									// problema de commits!
								}
							}
							client_table.put(
									prepare.getRequest().getClientId(),
									new Pair<>(
											prepare.getRequest().getNumber(),
											""));

							System.out
									.println("Message received! Sending PrepareOK to primary.");
							System.out.println("Client Table = "
									+ client_table.toString());
							System.out.println("Log = " + log.toString());
							
							if(configuration.get(0).equals("127.0.0.1"))
							sr.send(new PrepareOK(TypeMessage.PREPAREOK,
									viewNumber, opNumber, replicaNumber),
									primary, replicasPort-replicaNumber);
							else
								sr.send(new PrepareOK(TypeMessage.PREPAREOK,
										viewNumber, opNumber, replicaNumber),
										primary, replicasPort);
						} else {
							// esta replica nao tem todos os pedidos!!!
							// esperar ate ter todos!!!! recuperacao!!!
						}
						break;
					case STARTVIEWCHANGE:

						break;
					default:
						System.err.println("ERROR: DON'T RECOGNIZE THIS TYPE OF MESSAGE!");
						break;
					}
					
				}else{ // STATUS != NORMAL
					
				}
			}
		}
	}

	public void close() {
		sr.close();
	}
}