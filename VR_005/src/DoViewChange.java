import java.util.Map;

/**
 * @author group 005
 * DoviewChange
 */
public class DoViewChange extends Message {


	private static final long serialVersionUID = 7708642366712950582L;
	private int v;
	private Map<Integer,Request> log;
	private int myV;
	private int op;
	private int commitNumber;
	private int replicaNumber;
	
	/**
	 * @requires TypeMesage typemsg == DoviewChange
	 * @param v - new view number
	 * @param log - replica's log 
	 * @param myV - replica's view number
	 * @param commitNumber - last replica's commit number
	 * @param i - replica's number
	 * @param typemsg - the message type, in this case is DoviewChange
	 */
	public DoViewChange(int v, Map<Integer,Request> log, int myV, int op, int commitNumber, int i, TypeMessage typemsg) {
		super(typemsg);
		this.v = v;
		this.log = log;
		this.myV = myV;
		this.commitNumber = commitNumber;
		this.replicaNumber = i;
	}

	/**
	 * 
	 */
	public int getReplicaNumber() {
		return replicaNumber;
	}

	/**
	 * 
	 */
	public int getV() {
		return v;
	}

	/**
	 * 
	 */
	public Map<Integer, Request> getLog() {
		return log;
	}

	/**
	 * 
	 */
	public int getMyV() {
		return myV;
	}

	/**
	 * 
	 */
	public int getOp() {
		return op;
	}

	/**
	 * 
	 */
	public int getCommitNumber() {
		return commitNumber;
	}

}
