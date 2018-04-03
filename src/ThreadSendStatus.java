import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetSocketAddress;

import javax.swing.Box;

public class ThreadSendStatus extends Thread{
// Attrubute
	public Box1 box1;
//	public Content content = null;
	public ProtocolPacket statusProtocol = null;
	
// Method	
	public ThreadSendStatus(Box1 box1) {
		super();
		this.box1 = box1;
		// set statusProtocol
		statusProtocol = new ProtocolPacket();
		statusProtocol.role = Role.BOX2;
		statusProtocol.fromMac = box1.localMac;
		statusProtocol.type = Type.BOX;
		statusProtocol.fromName = box1.name;
		
		statusProtocol.function = Function.MSG2;
		statusProtocol.cmd = Cmd.STATUS11;		
	}
	
	public void setContent() {
		// content set
		statusProtocol.content = new Content();
		statusProtocol.content.temperature = (short) ((24.5 + 50)*10);
		statusProtocol.content.humidity = (short) (52.3*10);
		statusProtocol.content.pm25 = (short) (32.5*10);
		statusProtocol.content.pm10 = (short) (301.3*10);
		statusProtocol.content.co2 = 479;
		statusProtocol.content.formaldehyde = (short) (0.082*1000);
		statusProtocol.content.tvoc = (short) (0.275*1000);
		statusProtocol.content.time = System.currentTimeMillis();
	}
	
	public void run() {
		int i = 1;
		System.out.println("ThreadSendStatus.run*** ThreadSendStatus running");
		System.out.println("ThreadSendStatus.run*** localMac is: " + box1.localMac);
		while(true) {
			// gatewayBound≈–∂œ
			if (! Box1.gatewayBound.isEmpty()) {
				// set protocolPacket
				setContent();
				statusProtocol.toMac = Box1.gatewayBound;
				// set packetOut
				InetSocketAddress remoteIP = (InetSocketAddress) Box1.gateway_mac_ip_map.get(Box1.gatewayBound);
				DatagramPacket packetOut = new DatagramPacket(BoxManager.BUF, BoxManager.BUF.length, remoteIP);
				byte[] byteOut = statusProtocol.packet2Byte();
				packetOut.setData(byteOut);
				// send packetOut
				try {
					box1.socketBox.send(packetOut);
					System.out.println("ThreadSendStatus.run*** box1 have sent status to gateway,  " + i + "th");
					System.out.println("ThreadSendStatus.run*** the status content is: \n" + statusProtocol.content.toString());
					i++;
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			// sleep 30*1000
			try {
				Thread.sleep(BoxManager.STATUS_TIME_INTERVAL);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}
