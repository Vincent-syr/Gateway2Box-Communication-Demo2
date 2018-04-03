import java.sql.ResultSet;

import javax.print.attribute.standard.RequestingUserName;

enum Type {
	BOX, AIR_CONDITION;
}

enum Function {
	TEST0, CONFIG1, MSG2, CTRL3;
}

enum Role {
	TEST0, GATEWAY1, BOX2;
}

enum Cmd {
	TEST0, SCAN1, SCANACK2, BIND3, BINDACK4, BLINK5, BLINGACK6, UNBIND7, UNBINDACK8, FIND9, FINDACK10, STATUS11, STATUSACK12, REJECT13, CTRL14, CTRLACK15;
}

class Content {
	public short temperature;
	public short humidity;
	public short pm25;
	public short pm10;
	public short co2;
	public short formaldehyde; // 甲醛
	public short tvoc;
	public long time;

	public String toString() {
		String returnString = "";
		returnString += "temperature: " + temperature + ", humidity: " + humidity + ", pm25: " + pm25 + ", pm10: "
				+ pm10 + ", co2: " + co2 + ", formaldehyde: " + formaldehyde + ", tvoc: " + tvoc + ", time: " + time;
		return returnString;

	}

}

public class ProtocolPacket {
	// Attribute
	public Type type;
	public Function function;
	public Role role;
	public String fromMac;
	public String fromName;
	public String toMac;
	public Cmd cmd;
	public Content content;

	// Method
	// long to byte
	public static byte[] long2byte(long data) {
		byte[] bytes = new byte[8];
		bytes[0] = (byte) (data & 0xff);
		bytes[1] = (byte) ((data >> 8) & 0xff);
		bytes[2] = (byte) ((data >> 16) & 0xff);
		bytes[3] = (byte) ((data >> 24) & 0xff);
		bytes[4] = (byte) ((data >> 32) & 0xff);
		bytes[5] = (byte) ((data >> 40) & 0xff);
		bytes[6] = (byte) ((data >> 48) & 0xff);
		bytes[7] = (byte) ((data >> 56) & 0xff);
		return bytes;
	}

	// short to byte
	public static byte[] short2byte(short data) {
		byte[] bytes = new byte[4];
		bytes[0] = (byte) (data & 0xff);
		bytes[1] = (byte) ((data & 0xff00) >> 8);
		return bytes;
	}

	// byte[] to short
	public static short byte2short(byte[] bytes) {
		return (short) ((0xff & bytes[0]) | (0xff00 & (bytes[1] << 8)));
	}

	// byte to long
	public static long byte2long(byte[] bytes) {
		long returnLong = (0xffL & (long) bytes[0]) | (0xff00L & ((long) bytes[1] << 8))
				| (0xff0000L & ((long) bytes[2] << 16)) | (0xff000000L & ((long) bytes[3] << 24))
				| (0xff00000000L & ((long) bytes[4] << 32)) | (0xff0000000000L & ((long) bytes[5] << 40))
				| (0xff000000000000L & ((long) bytes[6] << 48)) | (0xff00000000000000L & ((long) bytes[7] << 56));
		return returnLong;
	}

	// reset protocolPacket
	public void resetProtocol() {
		type = null;
		function = null;
		role = null;
		fromMac = "";
		fromName = "";
		toMac = "";
		cmd = null;
		content = new Content();
	}

	public String toString() {
		String returnString = "";
		returnString += "type: " + type.name() + ", function: " + function.name() + ", role: " + role.name()
				+ ", fromMac: " + fromMac + ", fromName: " + fromName + ", toMac: " + toMac + ", cmd: " + cmd.name()
				+ "\n" + ", content{ " + content.toString() + "}";

		return returnString;
	}

	public ProtocolPacket() {
		content = new Content();
		// System.out.println("ProtocolPacket*** ProtocolPacket constructed defaultly");
	}

	// 通过byteIn构造packet
	public ProtocolPacket(byte[] byteIn) {
		content = new Content();
		byte[] temp = new byte[512];
		String stype, sfunction, srole, sfromMac, sfromName, stoMac, scmd, scontent;
		int validLen = 0;
		int index = 0;
		// 0-7 位
		validLen = 0;
		for (int i = 0; i < 8; i++) {
			if (byteIn[i] == 0) {
				break;
			}
			validLen++;
		}
		stype = new String(byteIn, 0, validLen);
		// 根据stype得到type枚举值
		for (Type t : Type.values()) {
			if (t.name().equals(stype)) {
				type = t;
			}
		}
		// System.out.println("type: " + type.name());

		// 8 位 function
		index = byteIn[8];
		for (Function f : Function.values()) {
			if (f.ordinal() == index) {
				function = f;
			}
		}
		// System.out.println("function" + function.name());

		// 9位 role
		index = byteIn[9];
		for (Role r : Role.values()) {
			if (r.ordinal() == index) {
				role = r;
			}
		}
		// System.out.println("role: " + role.name());

		// 10-34 fromMac
		validLen = 0;
		for (int i = 10; i < 35; i++) {
			if (byteIn[i] == 0) {
				break;
			}
			validLen++;
		}
		fromMac = new String(byteIn, 10, validLen);
		// System.out.println(validLen);
		// System.out.println("fromMac: " + fromMac);

		// 35-36 fromName
		validLen = 0;
		for (int i = 35; i < 67; i++) {
			if (byteIn[i] == 0) {
				break;
			}
			validLen++;
		}
		fromName = new String(byteIn, 35, validLen);
		// System.out.println(validLen);
		// System.out.println("fromName: " + fromName);

		// 67-91
		validLen = 0;
		for (int i = 67; i < 92; i++) {
			if (byteIn[i] == 0) {
				break;
			}
			validLen++;
		}
		toMac = new String(byteIn, 67, validLen);
		// System.out.println(validLen);
		// System.out.println("toMac: " + toMac);

		// 92-93 cmd
		index = byteIn[92];
		for (Cmd c : Cmd.values()) {
			if (c.ordinal() == index) {
				cmd = c;
			}
		}
		// System.out.println("cmd: " + cmd.name());

		// 94-157 content
		int tempIndex = 94;
		byte[] tempByte = new byte[2];
		// temperature
		tempByte[0] = byteIn[tempIndex];
		tempIndex++;
		tempByte[1] = byteIn[tempIndex];
		tempIndex++;
		content.temperature = ProtocolPacket.byte2short(tempByte);
		// System.out.println("received temperature: " + content.temperature);
		// humidity
		tempByte[0] = byteIn[tempIndex];
		tempIndex++;
		tempByte[1] = byteIn[tempIndex];
		tempIndex++;
		content.humidity = ProtocolPacket.byte2short(tempByte);
		// System.out.println("received humidity: " + content.humidity);
		// pm25
		tempByte[0] = byteIn[tempIndex];
		tempIndex++;
		tempByte[1] = byteIn[tempIndex];
		tempIndex++;
		content.pm25 = ProtocolPacket.byte2short(tempByte);
		// System.out.println("received pm25: " + content.pm25);
		// pm10
		tempByte[0] = byteIn[tempIndex];
		tempIndex++;
		tempByte[1] = byteIn[tempIndex];
		tempIndex++;
		content.pm10 = ProtocolPacket.byte2short(tempByte);
		// System.out.println("received pm10: " + content.pm10);
		// co2
		tempByte[0] = byteIn[tempIndex];
		tempIndex++;
		tempByte[1] = byteIn[tempIndex];
		tempIndex++;
		content.co2 = ProtocolPacket.byte2short(tempByte);
		// System.out.println("received co2: " + content.co2);
		// 甲醛
		tempByte[0] = byteIn[tempIndex];
		tempIndex++;
		tempByte[1] = byteIn[tempIndex];
		tempIndex++;
		content.formaldehyde = ProtocolPacket.byte2short(tempByte);
		// System.out.println("received formaldehyde: " + content.formaldehyde);
		// tvoc
		tempByte[0] = byteIn[tempIndex];
		tempIndex++;
		tempByte[1] = byteIn[tempIndex];
		tempIndex++;
		content.tvoc = ProtocolPacket.byte2short(tempByte);
		// System.out.println("received tvoc: " + content.tvoc);
		// time
		tempByte = new byte[8];
		for (int i = 0; i < 8; i++) {
			tempByte[i] = byteIn[i + 114];
		}
		content.time = ProtocolPacket.byte2long(tempByte);
		// System.out.println("received time: " + content.time);

	}
	// public void resetProtocol() {
	//
	// }

	public void putValue() {
		type = Type.BOX;
		function = Function.CONFIG1;
		role = Role.GATEWAY1;
		cmd = Cmd.SCAN1;
		fromMac = "005056C00008";
		fromName = "this.name";
		toMac = "005056C00008";
		// content set
		content.temperature = (short) ((24.5 + 50) * 10);
		content.humidity = (short) (52.3 * 10);
		content.pm25 = (short) (32.5 * 10);
		content.pm10 = (short) (301.3 * 10);
		content.co2 = 479;
		content.formaldehyde = (short) (0.082 * 1000);
		content.tvoc = (short) (0.275 * 1000);
		content.time = System.currentTimeMillis();
		System.out.println("putValue*** time: " + content.time);

	}

	public byte[] packet2Byte() {
		byte[] out = new byte[512];
		byte[] temp = new byte[512];
		int refer = 0;

		// 0-7 位 type
		temp = type.name().getBytes();
		for (int i = 0; i < 8 && i < temp.length; i++) {
			out[i] = temp[i];
		}

		// 8位 function
		out[8] = (byte) function.ordinal();

		// 9位 role
		out[9] = (byte) role.ordinal();

		// 10-34 fromMac
		temp = fromMac.getBytes();
		refer = 0;
		for (int i = 10; i < 35 && (i < temp.length + 10); i++) {
			out[i] = temp[i - 10];
			refer++;
		}
		// System.out.println("refer: " +refer);
		// System.out.println("temp.len: " + temp.length);
		try {
			if (refer < temp.length) {
				throw new Exception("fromMac out of length");
			}
		} catch (Exception e) {
			System.out.println(e);
		}

		// 35-66 fromName
		refer = 0;
		temp = fromName.getBytes();
		for (int i = 35; i < 67 && (i < temp.length + 35); i++) {
			out[i] = temp[i - 35];
			refer++;
		}
		// System.out.println("refer: " +refer);
		// System.out.println("temp.len: " + temp.length);
		try {
			if (refer < temp.length) {
				throw new Exception("fromName out of length");
			}
		} catch (Exception e) {
			System.out.println(e);
		}

		// 67-91 toMac
		// null 值不处理,所有bit默认0
		if (toMac != null) {
			refer = 0;
			temp = toMac.getBytes();
			for (int i = 67; i < 92 && (i < temp.length + 67); i++) {
				out[i] = temp[i - 67];
				refer++;
			}
			// System.out.println("refer: " +refer);
			// System.out.println("temp.len: " + temp.length);
			try {
				if (refer < temp.length) {
					throw new Exception("toMac out of length");
				}
			} catch (Exception e) {
				System.out.println(e);
			}
		}

		// 92-93 cmd
		if (cmd != null) {
			out[92] = (byte) cmd.ordinal();

		}

		// 94-157 content
		if (content != null) {
			int tempIndex = 94;
			temp = ProtocolPacket.short2byte(content.temperature); // 温度
			out[tempIndex] = temp[0];
			tempIndex++;
			out[tempIndex] = temp[1];
			tempIndex++;
			temp = ProtocolPacket.short2byte(content.humidity); // 湿度
			out[tempIndex] = temp[0];
			tempIndex++;
			out[tempIndex] = temp[1];
			tempIndex++;
			temp = ProtocolPacket.short2byte(content.pm25); // pm25
			out[tempIndex] = temp[0];
			tempIndex++;
			out[tempIndex] = temp[1];
			tempIndex++;
			temp = ProtocolPacket.short2byte(content.pm10); // pm10
			out[tempIndex] = temp[0];
			tempIndex++;
			out[tempIndex] = temp[1];
			tempIndex++;
			temp = ProtocolPacket.short2byte(content.co2); // co2
			out[tempIndex] = temp[0];
			tempIndex++;
			out[tempIndex] = temp[1];
			tempIndex++;
			temp = ProtocolPacket.short2byte(content.formaldehyde); // 甲醛
			out[tempIndex] = temp[0];
			tempIndex++;
			out[tempIndex] = temp[1];
			tempIndex++;
			temp = ProtocolPacket.short2byte(content.tvoc); // tvoc
			out[tempIndex] = temp[0];
			tempIndex++;
			out[tempIndex] = temp[1];
			tempIndex++;
			System.out.println("the last tempIndex should be 108, and the test value is: " + tempIndex);
			temp = ProtocolPacket.long2byte(content.time); // time
			for (int i = 114; i < 122; i++) {
				out[i] = temp[i - 114];
			}
		}

		// // output display
		// for(int i=0; i<148; i++) {
		// System.out.println(i + "th: byteValue: " + out[i] + "; charValue: " +
		// (char)out[i]);
		// }
		// return
		return out;
	}

	public void test() {
		ProtocolPacket packetOut = new ProtocolPacket();
		packetOut.putValue();
		byte[] byteOut = new byte[512];
		byteOut = packetOut.packet2Byte();
		ProtocolPacket packetIn = new ProtocolPacket(byteOut);
	}

	public static void main(String[] args) {
		new ProtocolPacket().test();

	}
}
