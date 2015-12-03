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
	private int  replicaNumber;
	private int viewNumber;
	private Status status;
	private Integer opNumber;
	private Map<Integer,Request> log;
	private int commitNumber;
	private Map<String,Pair<Integer,String>> client_table;
	private int fPlusOne;
	private String myIP;
	private String primary;
	private int replicasPort;
	private SendAndReceive sr;
	
	public ServiceCode() throws UnknownHostException, SocketException {
		configuration = new Configuration().getReplicas();
		replicaNumber = -1;
		viewNumber = 0;
		status = Status.NORMAL;
		opNumber = 0;
		log = new HashMap<>();
		commitNumber = opNumber;
		client_table = new HashMap<>();
		fPlusOne = (configuration.size()/ 2) + 1;
		myIP = "";
		primary = new Configuration().getReplicas().get(0);
		replicasPort = new Configuration().getReplicasPort();
		sr = new SendAndReceive(new DatagramSocket(replicasPort));
		
		    try {
		        Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
		        while (interfaces.hasMoreElements()) {
		            NetworkInterface iface = interfaces.nextElement();
		            // filtra interfaces inactivas, assim como o endereco 127.0.0.1
		            if (iface.isLoopback() || !iface.isUp())
		                continue;

		            Enumeration<InetAddress> addresses = iface.getInetAddresses();
		            while(addresses.hasMoreElements()) {
		                InetAddress addr = addresses.nextElement();
		                myIP = addr.getHostAddress();
		            }
		        }
		    } catch (SocketException e) {
		        throw new RuntimeException(e);
		    }
		for (int i = 0; i < configuration.size(); i++) {
			if (myIP.equals(configuration.get(i)))
				replicaNumber = i;
		}
		if (replicaNumber == -1){
			System.out.println("PERMISSION DENIED: TRY HARDER");
			System.exit(1);
		}
		System.out.println("Viewstamp Replication System has started!");
	}
	
	private String executeOperation(String request){
		return request.toUpperCase();
	}

	public void leaderSend(Request request){
		opNumber++;
		log.put(opNumber,request);
		client_table.put(request.getClientId(), new Pair<>(request.getNumber(), ""));
		//enviar para as replicas
		for (int i = 1; i < configuration.size(); i++)
			sr.send(new Prepare(TypeMessage.PREPARE, viewNumber, request, opNumber, commitNumber), configuration.get(i), replicasPort);

		List<PrepareOK> replicasOK = new ArrayList<>();
		int counter = 0;
		while(counter < fPlusOne){
			replicasOK.add((PrepareOK) sr.receive());
			System.out.println("Replica "+replicasOK.get(replicasOK.size()-1).replicaNumber()+" has answered");
			counter++;
		}
		
		if(counter >= fPlusOne){
			commitNumber++;
			client_table.put(request.getClientId(), new Pair<>(request.getNumber(), executeOperation(request.getOperation())));
			sr.send(new Reply(TypeMessage.REPLY, viewNumber, request.getNumber(), client_table.get(request.getClientId()).getSecond()), request.getClientId().split(":")[0],Integer.parseInt(request.getClientId().split(":")[1]));
		}
	
		System.out.println("Client Table = "+client_table.toString());
	}
	
	public void run(){
		Timer timer = null;
		if(replicaNumber==0)
			System.out.println("I'm the primary server. All replicas must obey to my commands!");
		else
			System.out.println("Greetings! I'm replica number "+replicaNumber);
		
		System.out.println("My IP address is: "+myIP);
		
		while (true) {
			if (replicaNumber == 0){
			timer = new Timer();
			timer.schedule(new SendCommit(new Commit(TypeMessage.COMMIT, viewNumber, commitNumber),sr), 10000);
			}
			Message message = sr.receive();
			if (replicaNumber == 0){
				timer.cancel();
				//sou o primeiro e recebi uma mensagem.
				Request request = (Request) message;
				System.out.println("New request received. Operation to be executed: '"+request.getOperation()+"'.");
				
				//primeira vez do cliente (nao existe na client_table)
				if(!client_table.containsKey(request.getClientId())){
					leaderSend(request);
					
				}else{ // Mais de um pedido para o mesmo cliente
					
					if(request.getNumber() > client_table.get(request.getClientId()).getFirst()){ //request_number eh mais recente
						leaderSend(request);
						
					}else if (request.getNumber() == client_table.get(request.getClientId()).getFirst() && !client_table.get(request.getClientId()).getSecond().isEmpty()){
						System.out.println("Resending last reply...");
						sr.send(new Reply(TypeMessage.REPLY, viewNumber, request.getNumber(), client_table.get(request.getClientId()).getSecond()), request.getClientId().split(":")[0],Integer.parseInt(request.getClientId().split(":")[1]));
						}else{
							System.out.println("Duplicated message received!");
							// message dropped
						}
					}
					
			}else{ // CODIGO DAS REPLICAS
				if(!message.getTypeMessage().equals(TypeMessage.REQUEST) && status.equals(Status.NORMAL)){
					if (message.getTypeMessage().equals(TypeMessage.COMMIT)){
						Commit commit = (Commit) message;
						if (opNumber < commit.getCommitNumber()){
							//wait for the request
							
						}
						if (commitNumber < commit.getCommitNumber()){
						commitNumber++;
						client_table.put(log.get(commitNumber).getClientId(),new Pair<>(log.get(commitNumber).getNumber(),executeOperation(log.get(commitNumber).getOperation())));
						
						}
						System.out.println("Client Table = "+client_table.toString());
						
					}else{
						
						Prepare prepare = (Prepare) message;
						
						//verifica se opnumber eh o seguinte
						if(prepare.getOpNumber() == log.keySet().size()+1){
							opNumber++;
							log.put(opNumber, prepare.getRequest());
							
							System.out.println("OPERATION NUMBER: "+opNumber);
							
							if (opNumber > 1){
								if(commitNumber+1 == prepare.getCommitNumber()){
									commitNumber++;
									client_table.put(log.get(commitNumber).getClientId(),new Pair<>(log.get(commitNumber).getNumber(),executeOperation(log.get(commitNumber).getOperation())));
								}else{
									//problema de commits!
								}
							}
								client_table.put(prepare.getRequest().getClientId(), new Pair<>(prepare.getRequest().getNumber(), ""));
								System.out.println("Message received! Sending PrepareOK to primary.");
								System.out.println("Client Table = "+client_table.toString());
								sr.send(new PrepareOK(TypeMessage.PREPAREOK, viewNumber, opNumber, replicaNumber), primary, replicasPort);		
						}else{ 
							//esta replica nao tem todos os pedidos!!!
							//esperar ate ter todos!!!! recuperacao!!!
						}
					}
				}
			}
		}
	}
	public void close(){
		sr.close();
	}
}