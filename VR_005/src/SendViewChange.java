import java.util.List;
import java.util.TimerTask;

public class SendViewChange extends TimerTask {

	private StartViewChange startViewChange;
	private SendAndReceive sr;
	private int replicaNumber;
	public SendViewChange(StartViewChange startViewChange, SendAndReceive sr, int replicaNumber, boolean started) {
		this.startViewChange = startViewChange;
		this.sr = sr;
		this.replicaNumber = replicaNumber;
	}

	@Override
	public void run() {
		System.out.println("Primary timed out! Sending SENDVIEWCHANGE message to all backups...");
		Configuration configuration = new Configuration();
		List<String> replicasList = configuration.getReplicas();
		List<Integer> replicasPort = configuration.getReplicasPort();
		sr.send(startViewChange, replicasList.get(replicaNumber), replicasPort.get(replicaNumber));
	}
}