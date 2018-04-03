

import java.io.IOException;
import java.net.SocketException;
import java.net.UnknownHostException;

public class Box2 {
	public static void main(String[] args) throws ClassNotFoundException, IOException, InterruptedException {
		Box1 box2 = new Box1("box2", 8892);
		box2.work();
	}
}
