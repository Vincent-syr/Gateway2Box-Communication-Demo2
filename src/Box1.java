import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MulticastSocket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;
import java.util.jar.Attributes.Name;




public class Box1 {
	public String localMac = "005056C00008";
	public DatagramSocket socketBox = null;
	public MulticastSocket msBox = null;
	public ProtocolPacket protocol = null;
	public String name = null;
	
	public static Map<Object, Object> gateway_mac_ip_map = null;
	public static String gatewayBound = "";
	public static InetAddress group2 = null;

	
	public void initialProtocolBox() {
		protocol.resetProtocol();  // 重置protocalMap
		protocol.role = Role.BOX2;
		protocol.fromMac = this.localMac;
		protocol.type = Type.BOX;
		protocol.fromName = this.name;
		System.out.println("Box.initialProtocolBox*** initialProtocolBox");
	}
	// construct box1
	public Box1(String name, int localPort) throws IOException {
		protocol = new ProtocolPacket();
		initialProtocolBox();  // 初始化protocol
		this.name = name;
		group2 = InetAddress.getByName("224.0.0.3");
//		socketBox = new DatagramSocket(localPort);
		msBox = new MulticastSocket(localPort);
		msBox.joinGroup(group2);
		gateway_mac_ip_map = new HashMap();
		System.out.println("Box.Box1*** BoxManager constructed");
	}
	
	// scanack
	public void scanack() throws IOException {
		System.out.println("Box.scanack***scanack is running!");
		// initial and set protocolPachet
		initialProtocolBox(); 
		protocol.cmd = Cmd.SCANACK2;
		protocol.function = Function.CONFIG1;
		// set byte and packetOut
		byte[] byteOut = protocol.packet2Byte();
		DatagramPacket packetOut = new DatagramPacket(BoxManager.BUF, BoxManager.BUF.length, InetAddress.getByName("255.255.255.255"), BoxManager.gatewayPort);
		packetOut.setData(byteOut);
		// send packet
		socketBox.send(packetOut);
		System.out.println("Box.scanack*** scanack finished");
		
	}
	
	// bindack
	public void bindack() throws IOException {
		System.out.println("Box.bindack***bindack is running!");
		// initial and set protocolPachet
		initialProtocolBox(); 
		protocol.cmd = Cmd.BINDACK4;
		protocol.function = Function.CONFIG1;
		// set byte and packetOut
		byte[] byteOut = protocol.packet2Byte();
		DatagramPacket packetOut = new DatagramPacket(BoxManager.BUF, BoxManager.BUF.length, InetAddress.getByName("255.255.255.255"), BoxManager.gatewayPort);
		packetOut.setData(byteOut);
		// send packet
		socketBox.send(packetOut);
		System.out.println("Box.bindack*** bindack finished");
		
	}
	
	// unbindack
	public void unbindack() throws IOException {
		System.out.println("Box.unbindack***unbindack is running!");
		// initial and set protocolPachet
		initialProtocolBox(); 
		protocol.cmd = Cmd.UNBINDACK8;
		protocol.function = Function.CONFIG1;
		protocol.toMac = gatewayBound;
		
		gatewayBound = "";  // gatewayBound = ""
		// set byte and packetOut
		byte[] byteOut = protocol.packet2Byte();
		DatagramPacket packetOut = new DatagramPacket(BoxManager.BUF, BoxManager.BUF.length, InetAddress.getByName("255.255.255.255"), BoxManager.gatewayPort);
		packetOut.setData(byteOut);
		// send packet
		socketBox.send(packetOut);
		System.out.println("Box.unbindack*** unbindack finished");
	}
	
	// find
	public void findack() throws IOException {
		System.out.println("Box.unbindack***find is running!");
		// initial and set protocolPachet
		initialProtocolBox(); 
		protocol.cmd = Cmd.FINDACK10;
		protocol.function = Function.CONFIG1;
		protocol.toMac = gatewayBound;
		// set byte and packetOut
		byte[] byteOut = protocol.packet2Byte();
		DatagramPacket packetOut = new DatagramPacket(BoxManager.BUF, BoxManager.BUF.length, InetAddress.getByName("255.255.255.255"), BoxManager.gatewayPort);
		packetOut.setData(byteOut);
		// send packet
		socketBox.send(packetOut);
		System.out.println("Box.findack*** findack finished");
	}
	
	
	
	
	
	public void work() throws IOException, ClassNotFoundException, InterruptedException {
		System.out.println("Box.work*** box start to work!");
		while(true) {
			DatagramPacket packetIn = new DatagramPacket(BoxManager.BUF, BoxManager.BUF.length);
			System.out.println("Box.work***" + this.name + " waiting for connection..");
//			socketBox.receive(packetIn);
			msBox.receive(packetIn);
			byte[] byteIn = packetIn.getData();
			
			ProtocolPacket protocolIn = new ProtocolPacket(byteIn);
			System.out.println("received from BoxManager");
			System.out.println(protocolIn.toString());
			
			// 每次收到都update gateway_mac_ip_map
			gateway_mac_ip_map.put(protocolIn.fromMac, packetIn.getSocketAddress());

			// 判断是否执行scanack
			if (protocolIn.cmd.equals(Cmd.SCAN1)) {
				scanack();
			}
			// bindack
			else if (protocolIn.cmd.equals(Cmd.BIND3)) {
				gatewayBound = protocolIn.fromMac;  // update gatewayBound
				bindack();
			}
			//unbindack
			else if (protocolIn.cmd.equals(Cmd.UNBIND7)){
				unbindack();
			}
			
			// find
			else if(protocolIn.cmd.equals(Cmd.FIND9)) {
				if (protocolIn.toMac.equals(this.localMac)) {
					findack();					
				}
			}
			
		}
	}
	
	public static void main(String[] args) throws ClassNotFoundException, IOException, InterruptedException {
		Box1 box1 = new Box1("box1", 20162);
		Thread threadSendStatus = new ThreadSendStatus(box1);
		threadSendStatus.start();
		box1.work();
	}
	
	
}
