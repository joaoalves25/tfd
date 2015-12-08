import java.net.SocketException;
import java.net.UnknownHostException;

public class ReplicasMain {
	public static void main(String[] args) {
		try {
<<<<<<< HEAD
			ServiceCode r = new ServiceCode(args[0]);
			r.run();	
=======
			ServiceCode r = new ServiceCode(Integer.parseInt(args[0]));
			r.run();
>>>>>>> fc40373_version
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (SocketException e) {
			e.printStackTrace();
		}
	}
}