

import java.io.IOException;
import java.io.ObjectOutputStream.PutField;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class ThreadRecv extends Thread{
	//public static attribution and method
	static int flagBind=1, flagUnbind, flagFind=1, flagStatus=1, flagControl=1;
	public static BoxManager gateway;           // gateway object 
	public static DatagramPacket packetIn = null;
	public static Map<Object, Object> box_mac_ip_map = new HashMap();
	public static Content[] statusContent = null;
	public static int statusTotalTimes = 0;
	public static int statusPointer = 0;

	
	// constructor and instance 
	public ThreadRecv() throws IOException {
		super();
		statusContent = new Content[5];
		ThreadRecv.gateway = new BoxManager();	
		packetIn = new DatagramPacket(BoxManager.BUF, BoxManager.BUF.length);
		System.out.println("TreadRecv.constructor***gateway, broadcastIP, and packetIn[10] has been instanced!");
	}
	

	
	// act2scanack
	public void act2Scanack(ProtocolPacket protocolIn) {
		System.out.println("TreadRecv.act2Scanack*** reaction to Scanack...");
		// 因为BoxManager.scan()已经初始化box_mac_ip_map， 且processPacketIn已经对box_mac_ip_map进行实时update，所以此方法什么也不用做
		// 结束语
		System.out.println("ThreadRecv.act2Scanack*** act2Scanack finished");
	}
	
	//act2bindack
	public void act2Bindack(ProtocolPacket protocolIn) {
		System.out.println("TreadRecv.act2Bindack*** reaction to Bindack...");
		// 因为会发6次包，所以会调用6次act2Bindack。当box_bound不为空就直接返回
		if (!BoxManager.box_bound.isEmpty()) {
			System.out.println("ThreadRecv.act2Bindack*** act_bound had been set before");
			return;
		}
		//normal case
		BoxManager.box_bound = protocolIn.fromMac;
		flagBind = 0;
		System.out.println("ThreadRecv.act2Bindack*** act2Bindack finish");
	}
	
	// act2unbindack
	public void act2Unbindack(ProtocolPacket protocolIn) {
		System.out.println("TreadRecv.act2Bindack*** reaction to Bindack...");
		// 因为会发6次包，所以会调用6次act2unBindack。当box_bound为空就直接返回
		if(BoxManager.box_bound.isEmpty()) {
			System.out.println("ThreadRecv.act2Unbindack*** act_bound had been empty");
			return;
		}
		//normal case
		BoxManager.box_bound = "";
		flagUnbind = 0;
		System.out.println("ThreadRecv.act2Unbindack*** act2Unbindack finish");
	}
	
	// act2findack
	public void act2Findack(ProtocolPacket protocolIn) {
		//... no operation
		ThreadRecv.flagFind = 0;
	}
	
	// statusack
	public void statusack(ProtocolPacket protocolIn) throws IOException {
		// reset statusContent
		ThreadRecv.statusContent[ThreadRecv.statusPointer] = new Content();

		// set statusContent
		ThreadRecv.statusContent[ThreadRecv.statusPointer] = protocolIn.content;
		System.out.println("ThreadRecv.act2Unbindack*** statusContent setting finished,\n the current statusPointer is " + 
				ThreadRecv.statusPointer + "; the current statusContent is below: ");
		System.out.println(statusContent[ThreadRecv.statusPointer].toString());		
		ThreadRecv.statusPointer++;  // 指针+1
		ThreadRecv.statusTotalTimes++;  // 总收到次数
		// 设置指针，使 循环覆盖
		if (ThreadRecv.statusPointer == 5) {
			ThreadRecv.statusPointer = 0;
		}
		
		// set and send protocol
		// initial and set protocolPachet
		ThreadRecv.gateway.initialProtocolGateway();  //重置protocol为gateway构造时的状态
		ThreadRecv.gateway.protocol.cmd = Cmd.STATUSACK12;
		ThreadRecv.gateway.protocol.function = Function.MSG2;
		ThreadRecv.gateway.protocol.toMac = BoxManager.box_bound;
		
		// set target ip
		InetSocketAddress targetBoxIP = (InetSocketAddress) ThreadRecv.box_mac_ip_map.get(BoxManager.box_bound);
		System.out.println("ThreadRecv.statusack*** the target ip is: " + targetBoxIP);
		
		// set byteOut and send packetOut
		DatagramPacket packetOut = new DatagramPacket(BoxManager.BUF, BoxManager.BUF.length, targetBoxIP);
		byte[] byteOut = ThreadRecv.gateway.protocol.packet2Byte();    // byteOut
		packetOut.setData(byteOut);

		ThreadRecv.gateway.msGateway.send(packetOut);
		System.out.println("ThreadRecv.statusack*** total time received status packetIn: " + ThreadRecv.statusTotalTimes);
		System.out.println("ThreadRecv.statusack*** have sent packetOut" );
	}
	
	
	public void processPacketIn(DatagramPacket packetIn) throws ClassNotFoundException, IOException {
		// TO DO: 收到不同的包分别如何处理！！
		// parse packet, store the data from packet
		System.out.println("ThreadRecv.processPacketIn*** begin processing PacketIn...");
		byte[] byteIn = packetIn.getData();
		// 对包的有效性验证
		String validationString = new String(byteIn, 0, 3);
		System.out.println("ThreadRecv.processPacketIn*** the validationString is: " + validationString);
		if(! validationString.equals("BOX")) {
			System.out.println("ThreadRecv.processPacketIn*** wrong validation! the validationString is: " + validationString);  
			return ;
		}
		ProtocolPacket protocolIn = new ProtocolPacket(byteIn);
		// update box_mac_ip_map
		ThreadRecv.box_mac_ip_map.put(protocolIn.fromMac, packetIn.getSocketAddress());
		System.out.println("the box_mac_ip is: " + ThreadRecv.box_mac_ip_map);
		// 可视化protocolIn
		System.out.println(protocolIn.toString());

		// act2scanack
		if (protocolIn.cmd.equals(Cmd.SCANACK2)) {
			act2Scanack(protocolIn);
		}
		// act2bindack
		if (protocolIn.cmd.equals(Cmd.BINDACK4)) {
			act2Bindack(protocolIn);
		}
	
		// act2unbindack
		if (protocolIn.cmd.equals(Cmd.UNBIND7)) {
			act2Unbindack(protocolIn);
		}
		
		// act2findack
		if(protocolIn.cmd.equals(Cmd.FINDACK10)) {
			if (ThreadRecv.flagFind == 1) {
				act2Findack(protocolIn);		
			}	
		}
		
		// statusack
		if(protocolIn.cmd.equals(Cmd.STATUS11)) {
			statusack(protocolIn);
		}
		System.out.println("ThreadRecv.processPacketIn*** packetIn has been processed");
	}
	
	
	
	public void run() {
		while(true) {	
			System.out.println("ThreadRecv.run*** keep listening packetIn");
			// 堵塞住，每收到一个包才继续运行.
			try {
				// receive packetIn
				ThreadRecv.gateway.msGateway.receive(packetIn);
				System.out.println("msGateway remote ip is: " + ThreadRecv.gateway.msGateway.getInetAddress());
				System.out.println("ThreadRecv.run***" + "ThreadRecv successfully received packetIn");
				processPacketIn(packetIn);   //对packetIn做出逻辑处理
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
	}
}
