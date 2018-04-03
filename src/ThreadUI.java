

import java.net.UnknownHostException;
import java.util.Scanner;

// 用于模拟长松的UI 线程做测试
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
				// 等待tempThread结束后，才可以执行下一次UI command输入
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
