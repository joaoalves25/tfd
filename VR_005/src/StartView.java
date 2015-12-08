import java.util.Map;

/**
 * 
 */

/**
 * @author joaoalves
 *
 */
public class StartView extends Message {

	private int v;
	private Map<Integer,Request> log;
	private int n;
	private int k;
	private TypeMessage typemsg;
	
	/**
	 * @param typemsg
	 */
	public StartView(TypeMessage typemsg, int v, Map<Integer,Request> log, int n, int k) {
		super(typemsg);
		this.v = v;
		this.log = log;
		this.n = n;
		this.k = k;
	}

	public int getView() {
		return v;
	}

	public Map<Integer, Request> getLog() {
		return log;
	}

	public int getOp() {
		return n;
	}

	public TypeMessage getTypemsg() {
		return typemsg;
	}
}
