
public class Test1 {
	
	public static void main(String[] args) {
//		long t = System.currentTimeMillis();
//		short x = 220;
//		byte[] b1 = new byte[8];
//		byte[] b2 = new byte[2];
//		
//		byte b;
//		
//		b2 = ProtocolPacket.short2byte(x);
//		System.out.println(b2[0]);
//		System.out.println(ProtocolPacket.byte2short(b2));
//		System.out.println("--------------");
//		b1 = ProtocolPacket.long2byte(t);
//		System.out.println(b1);
//		System.out.println(t);
//		System.out.println(ProtocolPacket.byte2long(b1));
//		
//		
//		b = (byte)(129 & 0xff);
//
//		System.out.println(b&0x01);
//		System.out.println(b&0x02);
//		System.out.println(b&0x04);
//		System.out.println(b&0x08);
//		System.out.println(b&0x10);
//		System.out.println(b&0x20);
//		System.out.println(b&0x40);
//		System.out.println(b&0x80);
		
		String x = "";
		byte[] testByte = x.getBytes();
		for(int i=0; i<testByte.length; i++) {
			System.out.println(testByte[i]);
		}
		System.out.println("finished");
	}
}
