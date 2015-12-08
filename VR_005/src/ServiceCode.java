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
import java.util.Set;
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
	private final int CLIENT_TIMETOUT = 1000000 * 200; // 10 sec
	private final int PRIMARY_TIMEOUT = 1000 * 10;
	private boolean firstTime;
	private boolean local;
	private String primaryAddress;
	private int primaryPort;
	private List<DoViewChange> doViewChangeMessages;
	private boolean istartedViewChange;
	
	private int stateTransferTest;

	public ServiceCode(String argsPort) throws UnknownHostException, SocketException {
		Configuration configuration = new Configuration();
		doViewChangeMessages = new ArrayList<>();
		istartedViewChange = false;
		viewNumber = 0;
		status = Status.NORMAL;
		commitNumber = opNumber = 0;
		log = new HashMap<>();
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

		
		if (primary = calculatePrimary(viewNumber) == replicaNumber) {
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

		stateTransferTest = 0;

	}

	private Integer getLogLastKey(){
		Integer last = 0;
		final Set<Map.Entry<Integer, Request>> entries = log.entrySet();

		for (Map.Entry<Integer, Request> entry : entries) {
			last = entry.getKey();
		}
		return last;
	}
	private int calculatePrimary(int viewNumber) {
		return (viewNumber % totalReplicas);
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

	public void primarySendToReplicas(Request request) {
		opNumber++;
		log.put(opNumber, request);
		client_table.put(request.getClientId(), new Pair<>(request.getRequestNumber(), ""));

//		if (stateTransferTest > 3) {
//			// enviar para as replicas
//			for (int i = 0; i < replicasList.size(); i++)
//				if (myPort != replicasPort.get(i)) {
//
//					System.out.println("SENDING TO: " + replicasPort.get(i));
//
//					sr.send(new Prepare(TypeMessage.PREPARE, viewNumber, request, opNumber, commitNumber), replicasList.get(i),
//							replicasPort.get(i));
//
//				}
//		} else {
			for (int i = 0; i < replicasList.size(); i++){
				if (myPort != replicasPort.get(i) ) {
//&& i != 1
					System.out.println("SENDING TO: " + replicasPort.get(i));

					sr.send(new Prepare(TypeMessage.PREPARE, viewNumber, request, opNumber, commitNumber), replicasList.get(i),
							replicasPort.get(i));
				}
			}
//		}

		stateTransferTest++;

		List<PrepareOK> replicasOK = new ArrayList<>();
		int counter = 1;

		while (counter < fPlusOne) {
			replicasOK.add((PrepareOK) sr.receive());
			System.out.println("Replica " + replicasOK.get(replicasOK.size() - 1).replicaNumber() + " has answered");
			counter++;
		}

		if (counter >= fPlusOne) {
			commitNumber++;
			client_table.put(request.getClientId(),
					new Pair<>(request.getRequestNumber(), executeOperation(request.getOperation())));

			System.out.println("CLIENT DESTINATION = " + request.getClientId().split(":")[0]);
			System.out.println("CLIENT PORT = " + request.getClientId().split(":")[1]);

			request.setExecuted();
			log.put(opNumber, request);

			sr.send(new Reply(TypeMessage.REPLY, viewNumber, request.getRequestNumber(), client_table.get(request.getClientId())
					.getSecond()), request.getClientId().split(":")[0], Integer.parseInt(request.getClientId().split(":")[1]));
		}

		System.out.println("Client Table = " + client_table.toString());
		System.out.println("Log = " + log.toString());
	}

	
	public void start() {
		int counter = 0;
		Timer timer = null;
		if (primary)
			System.out.println("I'm the PRIMARY server. All replicas must obey to my commands!");
		else {
			timer = new Timer();
			System.out.println("Greetings traveler! I'm replica number " + replicaNumber);
			timer.schedule(new SendViewChange(new StartViewChange(TypeMessage.STARTVIEWCHANGE, viewNumber+1, calculatePrimary(viewNumber+1)), sr, replicaNumber, istartedViewChange=true),
					PRIMARY_TIMEOUT);
		}
		System.out.println("My IP address is: " + myIP);

		while (true) {
			if (primary){
				System.out.println("");
				System.out.println("I'm the PRIMARY server. All replicas must obey to my commands!");
				System.out.println("O MEU LOG: "+ log.toString());
			}
			Message message = sr.receive();

			if (primary) {
				
				switch (message.getTypeMessage()) {
				case REQUEST:

					if (firstTime) {
						System.out.println("VOU CANCERLAR");
						timer.cancel();
						timer.purge();
					}

					// sou o primeiro e recebi uma mensagem.
					Request request = (Request) message;

					// primeira vez do cliente (nao existe na client_table)
					if (!client_table.containsKey(request.getClientId())) {
						System.out.println("New request received. Operation to be executed: '" + request.getOperation() + "'.");
						primarySendToReplicas(request);

					} else { // Mais de um pedido para o mesmo cliente

						// se o requestNumber for o mais recente
						if (request.getRequestNumber() > client_table.get(request.getClientId()).getFirst()) {
							System.out.println("New request received. Operation to be executed: '" + request.getOperation()
									+ "'.");
							primarySendToReplicas(request);

						} else if (request.getRequestNumber() == client_table.get(request.getClientId()).getFirst()
								&& !client_table.get(request.getClientId()).getSecond().isEmpty()) {
							System.out.println("Resending last reply...");
							sr.send(new Reply(TypeMessage.REPLY, viewNumber, request.getRequestNumber(), client_table.get(
									request.getClientId()).getSecond()), request.getClientId().split(":")[0],
									Integer.parseInt(request.getClientId().split(":")[1]));
						} else {
							System.out.println("Recovering client request number...");
							sr.send(new Reply(TypeMessage.REPLY, viewNumber, client_table.get(request.getClientId()).getFirst(),
									"recover"), request.getClientId().split(":")[0], Integer.parseInt(request.getClientId()
									.split(":")[1]));
						}
					}

					/*
					 * new SendCommit(new Commit(TypeMessage.COMMIT, viewNumber, commitNumber), sr, myPort,
					 * CLIENT_TIMETOUT,"COMMIT Message sent to all replicas. Vim do ServiceCode").run();
					 */
					timer = new Timer();
					timer.schedule(new SendCommit(new Commit(TypeMessage.COMMIT, viewNumber, commitNumber), sr, myPort,
							CLIENT_TIMETOUT, "COMMIT Message sent to all replicas. Vim do ServiceCode"), CLIENT_TIMETOUT);
					firstTime = true;

					break;
				case STARTVIEWCHANGE:
					System.out.println("STARTVIEWCAHNGE Received");
					break;
				case DOVIEWCHANGE:
					break;
				case COMMIT:
						Commit commit = (Commit) message;
						for (int i = commitNumber; i <= commit.getCommitNumber(); i++){
							client_table.put(log.get(i).getClientId(), new Pair<>(log.get(i).getRequestNumber(),executeOperation(log.get(i).getOperation())));
							log.get(i).setExecuted();
							log.put(i, log.get(i));
						}
						commitNumber = commit.getCommitNumber();
					break;
				case PREPAREOK:
					// System.out.println("PREPAREOK Received");
					break;
				default:
					System.err.println("ERROR: THIS TYPE OF MESSAGE IS NOT RECOGNIZED!");
					break;
				}
			} else { // CODIGO DAS REPLICAS

				switch (message.getTypeMessage()) {
				case COMMIT:
					timer.cancel();
					timer.purge();
					Commit commit = (Commit) message;
					if (opNumber < commit.getCommitNumber()) {
						// wait for the request
					}
					if (commitNumber < commit.getCommitNumber()) {
						System.out.println("ENTRREI NO COMMITNUMBER!");
						commitNumber++;
						client_table
								.put(log.get(commitNumber).getClientId(), new Pair<>(log.get(commitNumber).getRequestNumber(),
										executeOperation(log.get(commitNumber).getOperation())));
						log.get(commitNumber).setExecuted();
						log.put(commitNumber, log.get(commitNumber));

					}
					System.out.println("Client Table = " + client_table.toString());
					System.out.println("Log = " + log.toString());
					timer = new Timer();
					timer.schedule(new SendViewChange(
							new StartViewChange(TypeMessage.STARTVIEWCHANGE, viewNumber+1, calculatePrimary(viewNumber+1)), sr, replicaNumber, istartedViewChange=true), PRIMARY_TIMEOUT);
					break;
				case PREPARE:
					if (status.equals(Status.NORMAL)) {
						timer.cancel();
						timer.purge();
						Prepare prepare = (Prepare) message;
						// verifica se opnumber eh o seguinte
						if (prepare.getOpNumber() == log.keySet().size() + 1) {
							opNumber++;
							log.put(opNumber, prepare.getRequest());

							if (opNumber > 1) {
								if (commitNumber + 1 == prepare.getCommitNumber()) {
									commitNumber++;
									client_table.put(log.get(commitNumber).getClientId(), new Pair<>(log.get(commitNumber)
											.getRequestNumber(), executeOperation(log.get(commitNumber).getOperation())));

									log.get(opNumber - 1).setExecuted();
									log.put(opNumber - 1, log.get(opNumber - 1));

								} else {
									// problema de commits!
								}
							}
							client_table.put(prepare.getRequest().getClientId(), new Pair<>(prepare.getRequest()
									.getRequestNumber(), ""));

							System.out.println("Message received! Sending PrepareOK to primary.");
							System.out.println("Client Table = " + client_table.toString());
							System.out.println("Log = " + log.toString());

							System.out.println("PRIMARY ADDRESS = " + primaryAddress + "\nPRIMARY PORT = " + primaryPort);
							System.out.println(primary);

							sr.send(new PrepareOK(TypeMessage.PREPAREOK, viewNumber, opNumber, replicaNumber), primaryAddress,
									primaryPort);
							
							timer = new Timer();
							timer.schedule(new SendViewChange(
									new StartViewChange(TypeMessage.STARTVIEWCHANGE, viewNumber+1, calculatePrimary(viewNumber+1)), sr, replicaNumber, istartedViewChange=true), PRIMARY_TIMEOUT);
							
						} else {
							// esta replica nao tem todos os pedidos!!!

							System.out.println("START STATE TRANSFER");
							for (int i = 0; i < totalReplicas; i++) {
								if (myPort != replicasPort.get(i) && replicasPort.get(i) != primaryPort) {
									System.out.println("Asking help to " + replicasList.get(i) + ":" + replicasPort.get(i));
									sr.send(new GetState(TypeMessage.GETSTATE, viewNumber, opNumber, replicaNumber),
											replicasList.get(i), replicasPort.get(i));
									break;
								}
							}
						}
					}
					break;
				case STARTVIEWCHANGE:
					StartViewChange svc = (StartViewChange) message;
					if (istartedViewChange){
						System.out.println("Enviei para mim para enviar aos outros!");
						status = Status.VIEW_CHANGE;
						for (int i = 0; i < totalReplicas; i++){
							if (local){
								if (replicasPort.get(i) != myPort){
									sr.send(svc, replicasList.get(i), replicasPort.get(i));
								}
							}else{
								if (!replicasList.get(i).equals(myIP) && replicasPort.get(i) != myPort)
									sr.send(svc, replicasList.get(i), replicasPort.get(i));
							}
							
						}
						istartedViewChange = false;
						System.out.println("Ja enviei ao outros");
					}else{
						counter++;
						System.out
								.println("RECEBI UM STARTVIEWCAHNGE E O MEU counter  PASSOU PARA: "
										+ counter);
						if (counter == f) { // faz doViewChange para quem iniciou o
											// StartViewChange
							DoViewChange dvc = new DoViewChange(TypeMessage.DOVIEWCHANGE, svc.getViewNumber(), log, viewNumber, opNumber, commitNumber,
									svc.getReplicaNumber());
							System.out.println("REPLCIA TO SEND:" + svc.getReplicaNumber());
							sr.send(dvc, replicasList.get(svc.getReplicaNumber()), replicasPort.get(svc.getReplicaNumber()));
							counter = 0;
						} else if (counter == 1){
								status = Status.VIEW_CHANGE;
								for (int i = 0; i < totalReplicas; i++)
									if (!replicasList.get(i).equals(myIP) && replicasPort.get(i) != myPort)
										sr.send(svc, replicasList.get(i), replicasPort.get(i));
						}
					}
					break;
				case DOVIEWCHANGE:
					DoViewChange aux = (DoViewChange) message;
					doViewChangeMessages.add(aux);
					System.out.println("Im thew new Primary and i receive a DOVIEWCHANGE and now my counter is: "+doViewChangeMessages.size());
					if (status.equals(Status.VIEW_CHANGE)){
					
						if (doViewChangeMessages.size() == fPlusOne){
							
							for (DoViewChange changeMessage : doViewChangeMessages){
							
								if (changeMessage.getViewNumberNormal() > viewNumber){
									log = changeMessage.getLog();
									opNumber = changeMessage.getOpNumber();
								}else if (changeMessage.getViewNumberNormal() == viewNumber){
									if (changeMessage.getOpNumber() > log.size()){
										log = changeMessage.getLog();
										opNumber = changeMessage.getOpNumber();
									}
								}
								if (changeMessage.getCommitNumber() > commitNumber)
									commitNumber = changeMessage.getCommitNumber();
							}
							viewNumber = doViewChangeMessages.get(0).getViewNumber();
							status = Status.NORMAL;
							StartView sv = new StartView(TypeMessage.STARTVIEW, viewNumber, log, opNumber, commitNumber);
					
							for (int i = 0; i < totalReplicas; i++)
								if (local){
									if (replicasPort.get(i) != myPort){
										sr.send(sv, replicasList.get(i), replicasPort.get(i));
									}
								}else{
									if (!replicasList.get(i).equals(myIP) && replicasPort.get(i) != myPort)
										sr.send(sv, replicasList.get(i), replicasPort.get(i));
								}
									
							primary = true;
						}
					}
					break;
				case STARTVIEW:
					StartView startView = (StartView) message;
					
					log = startView.getLog();
					opNumber = getLogLastKey();
					viewNumber = startView.getView();
					status = Status.NORMAL;
					final Set<Map.Entry<Integer, Request>> entries = log.entrySet();
					int primaryNumber = calculatePrimary(viewNumber);
					primaryPort = replicasPort.get(primaryNumber);
					primaryAddress = replicasList.get(primaryNumber);
					for (Map.Entry<Integer, Request> entry : entries) {
						if (!entry.getValue().isExecuted()){
							sr.send(new PrepareOK(TypeMessage.PREPAREOK, viewNumber, opNumber, replicaNumber), primaryAddress,
									primaryPort);
							Request rAux = entry.getValue();
							rAux.setExecuted();
							log.put(entry.getKey(), rAux);
							commitNumber++;
							client_table.put(log.get(commitNumber).getClientId(), new Pair<>(log.get(commitNumber)
									.getRequestNumber(), executeOperation(log.get(commitNumber).getOperation())));
						}
					}
					
					if (!client_table.isEmpty()){
						for (int i = commitNumber; i <= opNumber; i++){
							client_table.put(log.get(i).getClientId(), new Pair<>(log.get(i).getRequestNumber(),executeOperation(log.get(i).getOperation())));
							log.get(i).setExecuted();
							log.put(i, log.get(i));
						}
					}
					commitNumber = opNumber;
					break;
				case GETSTATE:
					GetState getstate = (GetState) message;
					if (getstate.getViewNumber() == viewNumber && status.equals(Status.NORMAL)) {

						Map<Integer, Request> newLog = new HashMap<>();

						final Set<Map.Entry<Integer, Request>> entriesState = log.entrySet();

						for (Map.Entry<Integer, Request> entry : entriesState) {
							if (entry.getKey() > getstate.getOpNumber())
								newLog.put(entry.getKey(), entry.getValue());
						}
						sr.send(new NewState(TypeMessage.NEWSTATE, viewNumber, newLog, opNumber, commitNumber),
								replicasList.get(getstate.getReplicaNumber()), replicasPort.get(getstate.getReplicaNumber()));
					}
					break;

				case NEWSTATE:
					NewState newstate = (NewState) message;
					log.putAll(newstate.getLog());
					viewNumber = newstate.getViewNumber();
					opNumber = newstate.getOpNumber();
					commitNumber = newstate.getCommitNumber();
					System.out.println("Log has been sucessfully updated to:\n" + log.toString());
					break;
				default:
					System.err.println("ERROR: THIS TYPE OF MESSAGE IS NOT RECOGNIZED!");
					break;
				}
			}
		}
	}

	public void close() {
		sr.close();
	}
}