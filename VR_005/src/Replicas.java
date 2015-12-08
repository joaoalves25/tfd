import java.util.HashMap;
import java.util.Map;

/**
 * 
 */

/**
 * @author joaoalves
 *
 */
public class Replicas {
	
	private String iP;
	private int port;
	private int number;
	private int commitNumber;
	private int opNumber;
	private int viewNumber;
	private Status status;
	private Map<Integer,Request> log;
	private Map<String, Pair<Integer, String>> client_table;
	
	public Replicas(int port, Configuration configuration){
		number = findMe(port);
		if (number != -1){
			iP = configuration.getReplicas().get(number);
			this.port = port;
			commitNumber = 0;
			opNumber = 0;
			viewNumber = 0;
			status = Status.NORMAL;
			log = new HashMap<>();
			client_table = new HashMap<>();
		}	
	}
	
	private int findMe(int port){
		return -1;
	}

	public String getIP() {
		return iP;
	}

	public int getPort() {
		return port;
	}


	public int getNumber() {
		return number;
	}
	
	public int getCommitNumber() {
		return commitNumber;
	}

	public void setCommitNumber(int commitNumber) {
		this.commitNumber = commitNumber;
	}
	
	public void commitNumberUpdate(){
		this.commitNumber ++;
	}

	public int getOpNumber() {
		return opNumber;
	}

	public void setOpNumber(int opNumber) {
		this.opNumber = opNumber;
	}
	
	public void opNumberUpdate(){
		this.opNumber++;
	}

	public int getViewNumber() {
		return viewNumber;
	}

	public void setViewNumber(int viewNumber) {
		this.viewNumber = viewNumber;
	}

	public Status getStatus() {
		return status;
	}

	public void setStatus(Status status) {
		this.status = status;
	}

	public Map<Integer, Request> getLog() {
		return log;
	}

	public void setLog(Map<Integer, Request> log) {
		this.log = log;
	}

	public Map<String, Pair<Integer, String>> getClient_table() {
		return client_table;
	}

	public void setClient_table(Map<String, Pair<Integer, String>> client_table) {
		this.client_table = client_table;
	}
	
	
	
}
