import java.util.Scanner;

public class ClientMain {

	public static void main(String[] args) {
		System.out.println("Welcome to the Viewstamp Replication System!");
		UserCode c = new UserCode(Integer.parseInt(args[0]));
		Scanner sc = new Scanner(System.in);
		System.out
				.println("Press 'Q' to exit. Press any key to issue the 'teste' operation");
		String tecla = sc.nextLine().toLowerCase();
		while (tecla.isEmpty() || !tecla.equals("q")) {
			try {
				c.run();
				System.out
						.println("\nPress 'Q' and enter to exit. Press any key to issue the 'teste' operation.");
				tecla = sc.nextLine();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		sc.close();
		c.close();
		System.out.println("Goodbye ;_;");
	}
}