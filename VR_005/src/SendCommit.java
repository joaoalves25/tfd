import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class SendCommit extends TimerTask{

	private Commit commit;
	private SendAndReceive sr;
	private int myPort;
	private Timer timer;
	private final int CLIENT_TIMETOUT;

	private String print;

	public SendCommit(Commit commit, SendAndReceive sr, int myPort, int timeout,String print) {
		this.commit = commit;
		this.sr = sr;
		this.myPort = myPort;

		this.CLIENT_TIMETOUT = timeout;

		this.print = print;
		this.timer = new Timer();
	}

	
	public void run() {
		
		
		if(print.equals("VAI COMÇAR A DANÇA DO CREU")){
			timer.cancel();
			timer.purge();
		}
		
	
		System.out.println(print);
		
		Configuration configuration = new Configuration();
		List<String> replicasList = configuration.getReplicas();
		List<Integer> replicasPort = configuration.getReplicasPort();
		for (int i = 0; i < replicasList.size(); i++)
			if (myPort != replicasPort.get(i)) {
				sr.send(commit, replicasList.get(i), replicasPort.get(i));
			}

		timer = new Timer();
		timer.schedule(new SendCommit(new Commit(TypeMessage.COMMIT, commit.getViewNumber(), commit.getCommitNumber()),
				sr, myPort, CLIENT_TIMETOUT, "COMMIT Message sent to all replicas. Vim do SendCommit"), CLIENT_TIMETOUT);
		
		
		
	}
}