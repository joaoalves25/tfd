import java.util.Map;

public class StartView extends Message {

	private static final long serialVersionUID = 5283424956703623855L;
	private int viewNumber;
	private Map<Integer,Request> log;
	private int opNumber;
	private int commitNumber;
	
	
	public StartView(TypeMessage typemsg, int viewNumber, Map<Integer,Request> log, int opNumber, int commitNumber) {
		super(typemsg);
		this.viewNumber = viewNumber;
		this.log = log;
		this.opNumber = opNumber;
		this.commitNumber = commitNumber;
	}

	public int getView() {
		return viewNumber;
	}

	public Map<Integer, Request> getLog() {
		return log;
	}

	public int getOp() {
		return opNumber;
	}

	@Override
	public String toString() {
		return "StartView [viewNumber=" + viewNumber + ", log=" + log + ", opNumber=" + opNumber + ", commitNumber="
				+ commitNumber + "]";
	}
}