import java.util.List;
import java.util.TimerTask;

public class SendCommit extends TimerTask {

	private Commit commit;
	private SendAndReceive sr;
	private int myPort;

	public SendCommit(Commit commit, SendAndReceive sr, int myPort) {
		this.commit = commit;
		this.sr = sr;
		this.myPort = myPort;
	}

	@Override
	public void run() {
		System.out
				.println("Client timed out! Sending COMMIT message to all backups...");
		Configuration conf = new Configuration();
		List<String> replicasList = conf.getReplicas();
		for (int i = 0; i < replicasList.size(); i++)
			if (myPort != conf.getReplicasPort().get(i)){
				if (replicasList.get(0).equals("127.0.0.1"))
					sr.send(commit, "127.0.0.1",
							new Configuration().getReplicasPort().get(i));
				else
					sr.send(commit, replicasList.get(i),
							new Configuration().getReplicasPort().get(0));
			}
		
	}

}