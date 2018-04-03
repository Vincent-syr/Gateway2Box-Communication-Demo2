

import java.io.IOException;
import java.net.SocketException;
import java.net.UnknownHostException;

public class Box3 {
	public static void main(String[] args) throws ClassNotFoundException, IOException, InterruptedException {
		Box1 box3 = new Box1("box3",8893);
		box3.work();
	}
}
