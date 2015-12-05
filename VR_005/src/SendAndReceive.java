import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class SendAndReceive {

	private DatagramSocket socket;

	public SendAndReceive(DatagramSocket socket) {
		this.socket = socket;
	}

	public void send(Message m, String destination, int port) {
		try {
			InetAddress aDestination = InetAddress.getByName(destination);
			ByteArrayOutputStream byteStream = new ByteArrayOutputStream(5000);
			ObjectOutputStream os = new ObjectOutputStream(
					new BufferedOutputStream(byteStream));
			os.flush();
			os.writeObject(m);
			os.flush();
			// retrieves byte array
			byte[] sendBuf = byteStream.toByteArray();
			DatagramPacket sendPacket = new DatagramPacket(sendBuf,
					sendBuf.length, aDestination, port);
			// averiguar int byteCount = sendPacket.getLength();
			socket.send(sendPacket);
			os.close();
		} catch (UnknownHostException e) {
			System.out.println("404: IP not found");
			e.printStackTrace();
		} catch (IOException e) {
			System.out.println("Couldn't send package");
			e.printStackTrace();
		}
	}

	public int getPort() {
		return socket.getLocalPort();
	}

	public Message receive() {
	
		try {
			byte[] recvBuf = new byte[5000];
			DatagramPacket receivePacket = new DatagramPacket(recvBuf,
					recvBuf.length);
			socket.receive(receivePacket);
			// AVERIGUAR!!! int byteCount = receivePacket.getLength();
			ByteArrayInputStream byteStream = new ByteArrayInputStream(recvBuf);
			ObjectInputStream is = new ObjectInputStream(
					new BufferedInputStream(byteStream));
			Message m = (Message) is.readObject();
			is.close();
			return (m);
		} catch (IOException e) {
			System.err.println("Exception:  " + e);
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		return null;
	}

	public void close() {
		socket.close();
	}
}