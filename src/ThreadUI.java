

import java.net.UnknownHostException;
import java.util.Scanner;

// ����ģ�ⳤ�ɵ�UI �߳�������
public class ThreadUI extends Thread{
	public void run() {
		while(true) {
			// Input command
			System.out.println("ThreadUI.run***please input command: ..");
			Scanner scan = new Scanner(System.in);
			String command = scan.nextLine();
			// start a new tempThread
			ThreadFromUI tempThread = new ThreadFromUI(command);
			tempThread.start();
			
			//
			try {
				// �ȴ�tempThread�����󣬲ſ���ִ����һ��UI command����
				tempThread.join();
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
//		new ThreadFromUI().start();s
		
	}
}
