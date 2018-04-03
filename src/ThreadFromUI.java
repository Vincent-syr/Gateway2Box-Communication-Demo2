

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;



public class ThreadFromUI extends Thread{
	public ThreadFromUI(String command) {
		super(command);
	}
	
	public void run() {
		//scan
		if (this.getName().equals("scan")) {
			System.out.println("ThreadFromUI.run***ThreadFromUI.run***start running scan!!");
			try {
				Map<Object, Object> tempMap =  BoxManager.scan();
				System.out.println("ThreadFromUI.run***  the value map is: " + tempMap.get("value"));

			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		//bind
		if(this.getName().equals("bind")) {
			System.out.println("ThreadFromUI.run***please Input which box to bind: ");
			Scanner scan = new Scanner(System.in);
			String bindName = scan.nextLine();
			System.out.println("ThreadFromUI.run***ThreadFromUI.run***start running bind!!");
			try {
				System.out.println("ThreadFromUI.run*** the returnMap of bind: " + BoxManager.bind(bindName));
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		//unbind
		if(this.getName().equals("unbind")) {
			try {
				System.out.println("ThreadFromUI.run*** the returnMap of unbind: " + BoxManager.unbind());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		// find
		if(this.getName().equals("find")) {
			try {
				System.out.println("ThreadFromUI.run*** the returnMap of find: " + BoxManager.find());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
		
		// status
		if(this.getName().equals("status")) {
			System.out.println("ThreadFromUI.run*** the returnMap of status: " + BoxManager.status());
		}
		
		// statusList, only for display in test
		if(this.getName().equals("statusList")) {
			System.out.println("ThreadFromUI.run*** the returnMap of statusList: " + BoxManager.statusList());
		}

	}
}
