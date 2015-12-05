import java.util.Map;

public class DoViewChange extends Message {

	private static final long serialVersionUID = 7708642366712950582L;
	private int newViewNumber;
	private Map<Integer, Request> log;
	private int replicaViewNumber;
	private int opNumber;
	private int commitNumber;
	private int replicaNumber;

	public DoViewChange(int newViewNumber, Map<Integer, Request> log, int replicaViewNumber, int opNumber,
			int commitNumber, int replicaNumber, TypeMessage typemsg) {
		super(typemsg);
		this.newViewNumber = newViewNumber;
		this.log = log;
		this.replicaViewNumber = replicaViewNumber;
		this.opNumber = opNumber;
		this.commitNumber = commitNumber;
		this.replicaNumber = replicaNumber;
	}

	public int getReplicaNumber() {
		return replicaNumber;
	}

	public int getNewViewNumber() {
		return newViewNumber;
	}

	public Map<Integer, Request> getLog() {
		return log;
	}

	public int getReplicaViewNumber() {
		return replicaViewNumber;
	}

	public int getOpNumber() {
		return opNumber;
	}

	public int getCommitNumber() {
		return commitNumber;
	}
}