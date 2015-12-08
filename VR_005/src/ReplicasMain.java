import java.net.SocketException;
import java.net.UnknownHostException;

public class ReplicasMain {
	public static void main(String[] args) {
		try {
			ServiceCode r = new ServiceCode(args[0]);
			r.run();	
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (SocketException e) {
			e.printStackTrace();
		}
	}
}