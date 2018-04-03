

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MulticastSocket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;



public class BoxManager {
// Attribute	
	// static 变量
	public static final int SCAN_LOOP_TIMES = 6;
	public static final long SCAN_LOOP_SLEEP_TIME = 500;
	public static final int BIND_LOOP_TIMES = 6;
	public static final int BUF_SIZE = 1024*2;
	public static final byte[] BUF = new byte[BoxManager.BUF_SIZE];
	public static final long STATUS_TIME_INTERVAL = 15*60*1000;
	public static InetAddress broadcast = null;
	public static int gatewayPort = 8890;
	public static int box1Port = 8891;
	public static int box2Port = 8892;
	public static int box3Port = 8893;
	public static int remotePort = 20162;
	public static int testPort = 10086;
	public static InetAddress group; // 224.0.0.3
	public static InetAddress boxSpecifiedAddress = null;   // 

	
	// 成员变量
	private String localMac = "005056C00008";
	public String name = "gateway";
	public ProtocolPacket protocol = null;
//	public DatagramSocket socketGateway;
	public MulticastSocket msGateway;
	
	public static Set<Object> scan_set=null;
	public static String box_bound = "";
		
	
//Method	
	// 添加默认的protocol值
	public void initialProtocolGateway() {
		protocol.resetProtocol();
		protocol.type = Type.BOX;
		protocol.role = Role.GATEWAY1;
		protocol.fromMac = "005056C00008";
		protocol.fromName = this.name;
		System.out.println("BoxManager.initialProtocolGateway*** intitial ProtocolGateway");
	}
	// constructed224
	public BoxManager() throws IOException {
		protocol = new ProtocolPacket();
		initialProtocolGateway();  // 初始化protocol
		BoxManager.broadcast = InetAddress.getByName("192.168.52.255");
		BoxManager.group = InetAddress.getByName("224.0.0.3");
		boxSpecifiedAddress = InetAddress.getByName("192.168.7.255");
//		socketGateway = new DatagramSocket(BoxManager.gatewayPort);
		msGateway = new MulticastSocket(8889);
		msGateway.joinGroup(BoxManager.group);
		System.out.println("BoxManager.BoxManager *** BoxManager constructed");
	}
	
	
	// scan command!
	public static Map<Object, Object> scan() throws SocketException,IOException, InterruptedException {
		Map<Object, Object>	returnMap = new HashMap();   // initial returnMap
		BoxManager.scan_set = new HashSet();   // 重置scan_set
		ThreadRecv.box_mac_ip_map = new HashMap();   // 重置scan reset box_mac_ip_map
		// initial and set protocolPachet
		ThreadRecv.gateway.initialProtocolGateway();  //重置protocol为gateway构造时的状态
		ThreadRecv.gateway.protocol.cmd = Cmd.SCAN1;   
		ThreadRecv.gateway.protocol.function = Function.CONFIG1;
		ThreadRecv.gateway.protocol.toMac = "ALL";
		// set MulticastSocket
		
		
		// set packetOut
		DatagramPacket packetOut = new DatagramPacket(BoxManager.BUF, BoxManager.BUF.length, BoxManager.group ,remotePort);
		byte[] byteOut = ThreadRecv.gateway.protocol.packet2Byte();    // byteOut
		packetOut.setData(byteOut);
		// send Packet
		int counter = 0;

		System.out.println("the local msGateway ip is: " + ThreadRecv.gateway.msGateway.getLocalSocketAddress());
//		System.out.println("the local socketGateway ip is: " + ThreadRecv.gateway.socketGateway.getInetAddress());
		System.out.println("the target ip is: " + packetOut.getSocketAddress());
		System.out.println();
		while(counter < BoxManager.SCAN_LOOP_TIMES) {
			ThreadRecv.gateway.msGateway.send(packetOut);  //发送packetOut1
			Thread.sleep(BoxManager.SCAN_LOOP_SLEEP_TIME);  // sleep ,  等box发包和Receive线程处理  
			System.out.println(counter + "th send scan packet");
			counter++;
		}
		Thread.sleep(50);
		// empty scan_set error
		// normal case
		BoxManager.scan_set = ThreadRecv.box_mac_ip_map.keySet();
		// empty scan_set判断
		if (BoxManager.scan_set.isEmpty()) {
			returnMap.put("condition_code", 111);
			return returnMap;
		}
		returnMap.put("condition_code", 0);
		returnMap.put("value", scan_set);
		System.out.println("BoxManager.scan *** box_mac_ip_mpa is: " + ThreadRecv.box_mac_ip_map);
		System.out.println("BoxManager.scan *** scan_set is: " + scan_set);
		return returnMap;
		
	}
	
	public static Map<Object, Object> bind(String box2bindMac) throws IOException, InterruptedException{
		ThreadRecv.flagBind = 1;
		Map<Object, Object>	returnMap = new HashMap();   // initial returnMap
		// error 判断
		if (!box_bound.isEmpty()) {
			ThreadRecv.flagBind = 122;
			returnMap.put("condition_code", ThreadRecv.flagBind);
			return returnMap;
		}
		
		// initial and set protocolPachet
		ThreadRecv.gateway.initialProtocolGateway();  //重置protocol为gateway构造时的状态
		ThreadRecv.gateway.protocol.cmd = Cmd.BIND3;
		ThreadRecv.gateway.protocol.function = Function.CONFIG1;
		ThreadRecv.gateway.protocol.toMac = box2bindMac;
		// targetBoxIp
		InetSocketAddress targetBoxIp = (InetSocketAddress) ThreadRecv.box_mac_ip_map.get(box2bindMac);
		
		// set packetOut
		DatagramPacket packetOut = new DatagramPacket(BoxManager.BUF, BoxManager.BUF.length, targetBoxIp);
		byte[] byteOut = ThreadRecv.gateway.protocol.packet2Byte();    // byteOut
		packetOut.setData(byteOut);
		// send Packet
		int counter = 0;
		while(counter < BoxManager.BIND_LOOP_TIMES) {
			ThreadRecv.gateway.msGateway.send(packetOut);  //发送packetOut1
//			System.out.println("socketGateway ip addresss is: " + ThreadRecv.gateway.socketGateway.getLocalSocketAddress());
			Thread.sleep(60);  // 等box发包和Receive线程处理
			counter++;
		}
		// wait box and ThreadReceive process...
		Thread.sleep(50);
		// error code 121, box不返回包
		if (ThreadRecv.flagBind == 1) {
			ThreadRecv.flagBind = 121;
			returnMap.put("condition_code", ThreadRecv.flagBind);
		}
		// normal case
		returnMap.put("condition_code", ThreadRecv.flagBind);
		return returnMap;
	}
	
	// unbind
	public static Map<Object, Object> unbind() throws IOException, InterruptedException{
		ThreadRecv.flagUnbind = 1;
		Map<Object, Object>	returnMap = new HashMap();   // initial returnMap
		// error 判断
		if (box_bound.isEmpty()) {
			ThreadRecv.flagUnbind = 132;
			returnMap.put("condition_code", ThreadRecv.flagUnbind);
			return returnMap;
		}
		// initial and set protocolPachet
		ThreadRecv.gateway.initialProtocolGateway();  //重置protocol为gateway构造时的状态
		ThreadRecv.gateway.protocol.cmd = Cmd.UNBIND7;
		ThreadRecv.gateway.protocol.function = Function.CONFIG1;
		ThreadRecv.gateway.protocol.toMac = BoxManager.box_bound;
		box_bound = "";   // box_bound set 为空字符
		// targetBoxIp
		InetSocketAddress targetBoxIp = (InetSocketAddress) ThreadRecv.box_mac_ip_map.get(ThreadRecv.gateway.protocol.toMac);
		
		// set packetOut
		DatagramPacket packetOut = new DatagramPacket(BoxManager.BUF, BoxManager.BUF.length, targetBoxIp);
		byte[] byteOut = ThreadRecv.gateway.protocol.packet2Byte();    // byteOut
		packetOut.setData(byteOut);
		// send Packet
		int counter = 0;
		while(counter < BoxManager.BIND_LOOP_TIMES) {
			ThreadRecv.gateway.msGateway.send(packetOut);  //发送packetOut1
			Thread.sleep(60);  // 等box发包和Receive线程处理
			counter++;
		}
		
		Thread.sleep(100); //等box和ThreadRecv处理
		if(!BoxManager.box_bound.isEmpty()) {
			ThreadRecv.flagUnbind = 131;
			returnMap.put("condition_code", ThreadRecv.flagUnbind);
			return returnMap;
		}
		// normal case
		returnMap.put("condition_code", ThreadRecv.flagUnbind);
		return returnMap;
	}
	
	// find
	public static Map<Object, Object> find() throws IOException, InterruptedException{
		ThreadRecv.flagFind = 1;
		Map<Object, Object> returnMap = new HashMap();
		// error 判断
		if (box_bound.isEmpty()) {
			ThreadRecv.flagUnbind = 142;
			returnMap.put("condition_code", ThreadRecv.flagUnbind);
			return returnMap;
		}
		
		// initial and set protocolPachet
		ThreadRecv.gateway.initialProtocolGateway();  //重置protocol为gateway构造时的状态
		ThreadRecv.gateway.protocol.cmd = Cmd.FIND9;
		ThreadRecv.gateway.protocol.function = Function.CONFIG1;
		ThreadRecv.gateway.protocol.toMac = box_bound;
		// set packetOut
		DatagramPacket packetOut = new DatagramPacket(BoxManager.BUF, BoxManager.BUF.length, BoxManager.group, BoxManager.remotePort);
		byte[] byteOut = ThreadRecv.gateway.protocol.packet2Byte();    // byteOut
		packetOut.setData(byteOut);
		// send Packet
		int counter = 0;
		while(counter < BoxManager.SCAN_LOOP_TIMES) {
			ThreadRecv.gateway.msGateway.send(packetOut);  //发送packetOut1
			Thread.sleep(BoxManager.SCAN_LOOP_SLEEP_TIME);  //sleep, 等box发包和Receive线程处理
			counter++;
		}
		
		Thread.sleep(50);//等box和ThreadRecv处理
		// error 141
		if (ThreadRecv.flagFind == 1) {
			ThreadRecv.flagFind = 141;
			returnMap.put("condition_code", ThreadRecv.flagUnbind);
			return returnMap;
		}
		//normal case
		returnMap.put("condition_code", ThreadRecv.flagFind);
		return returnMap;
	}
	
	// status
	public static Map<Object, Object> status(){
		Map<Object, Object> returnMap = new HashMap();
		// error 153, statusContent为空
		int lenValid = 0;
		for (int i=0; i < ThreadRecv.statusContent.length; i++) {
			if (ThreadRecv.statusContent != null) {
				lenValid ++;
			}
		}
		if(lenValid==0) {
			returnMap.put("condition_code", 153);
			return returnMap;
		}
		// lastContent取值
		Content lastContent = new Content();
		if (ThreadRecv.statusPointer == 0) {
			lastContent = ThreadRecv.statusContent[4];
		}
		else {
			lastContent = ThreadRecv.statusContent[ThreadRecv.statusPointer-1];
		}
		
		// error 151, 超过STATUS_INTERVAL
		if(System.currentTimeMillis() - lastContent.time > BoxManager.STATUS_TIME_INTERVAL) {
			returnMap.put("condition_code", 151);
			returnMap.put("value", lastContent);
			return returnMap;
		}
		// normal case
		returnMap.put("condition_code", 0);
		returnMap.put("value", lastContent);
		
		return returnMap;
	}
	
	// status list, only for display in test
	public static Map<Object, Object> statusList(){
		Map<Object, Object> returnMap = new HashMap();
		returnMap.put("value", ThreadRecv.statusContent.length);
		returnMap.put("currentPointer", ThreadRecv.statusPointer);
		return returnMap;

	}
	
	
}
	

