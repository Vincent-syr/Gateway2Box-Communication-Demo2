

import java.io.IOException;

public class RunProtocol {
	public static void main(String[] args) throws IOException, InterruptedException {
		ThreadRecv recvThread = new ThreadRecv();
		ThreadUI uiThread = new ThreadUI();
		recvThread.start();
		Thread.sleep(50);  // ��console�����ֵ�textΪ���input command
		uiThread.start();
	}
}
