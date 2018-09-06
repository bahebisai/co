package com.xiaomi.emm.socket.packet;


import android.util.Log;

import com.xiaomi.emm.socket.utils.Formatter;
import com.xiaomi.emm.socket.utils.StringFormatter;

import java.nio.charset.Charset;


// 基础类
public class BasicPacket {
	final static String TAG ="BasicPacket";// "com.elife.sdk.packet.BasicPacket";
	private static final String CHARSET = "utf-8";
	private static final String GBK = "GBK";
	public static final int MAX_PACKET_DATA_LENGTH = 1024;

	public byte[] buff;

	// 起始头 '0xfa' , 标准包头 1+1+1+4+2+1 = 10 个字节
	public byte header = (byte) 0xfa;
	// 包类型
	public byte msg_type = (byte) 0x00;
	// 版本号
	public byte ver = (byte) 0x01;
	// 序列号
	public int seq = 0;
	// 数据包长度
	public short content_len = (byte) 0x00;
	// 返回值
	public byte ret = (byte) 0x00;

	// 内容
	public byte[] content;
	// 内容
	public String content_str;

	public String msg_type_str;

	public BasicPacket() {
	}

	// 传递 buff 参数，转成数据包
	public boolean setInfo(byte[] buf) {
		boolean b_ok = false;

		// 检查包长及 包头第一个字节及包头版本号　　
		if (buf == null || buf.length < 10) {
			return false;
		}
		if (buf[0] != (byte) 0xfa && buf[1] != 0x1) {
			return false;
		}

		try {
			this.buff = buf;
			// 检查 head
			msg_type = buff[2];
			msg_type_str = this.getMsgType(msg_type);

			byte[] seq_byte = new byte[4];
			System.arraycopy(buff, 3, seq_byte, 0, 4);
			seq = net_byte2int(seq_byte);

			byte[] len_byte = new byte[2];
			System.arraycopy(buff, 7, len_byte, 0, 2);
			content_len = net_byte2short(len_byte);
			Log.w(TAG, "setInfo() content_len = " + content_len);

			ret = buff[9];

			// 有 json 数组
			if (content_len > 0) {
				byte[] content_byte = new byte[content_len];
				System.arraycopy(buff, 10, content_byte, 0, content_len);
				content = new byte[content_len];
				System.arraycopy(buff, 10, content, 0, content_len);
				content_str = new String(content_byte, /*CHARSET*/"UTF-8");

			}
			b_ok = true;

		} catch (Exception e) {
			Log.w(TAG, e);
			b_ok = false;
		}
		return b_ok;
	}

	// 传递 参数，构建 包
	public byte[] _packet(byte msg_type, int seq, String content) {
		return _packet(msg_type, seq, 0, content);
	}

	// 传递 参数，构建 包
	public byte[] _packet(byte msg_type, int seq, int ret, String content) {
		// 变量赋值
		this.msg_type = msg_type;
		this.seq = seq;

		byte[] head_buf = new byte[1024];

		head_buf[0] = header;
		head_buf[1] = (byte) 0x01;

		// 消息类型
		head_buf[2] = (byte) msg_type;

		byte[] seq_byte = int2net_byte(seq);
		System.arraycopy(seq_byte, 0, head_buf, 3, 4);

		head_buf[9] = (byte) ret;

		// 是否有内容
		int len = 0;
		if (content != null && content.length() > 0) {
			// 内容转换
			try {
				byte[] bb = content.getBytes(CHARSET);

				// 长度字段
				len = bb.length;
				// Log.e(TAG, "_packet()  len = " + len);

				byte[] len_byte = short2net_byte((short) len);
				System.arraycopy(len_byte, 0, head_buf, 7, 2);

				// 生成结果 byte[]
				buff = new byte[len + 10];
				System.arraycopy(head_buf, 0, buff, 0, 10);
				System.arraycopy(bb, 0, buff, 10, len);
			} catch (Exception e) {
				e.printStackTrace();
				buff = new byte[10];
				System.arraycopy(buff, 0, head_buf, 0, 10);

			}
		} else {
			buff = new byte[10];
			System.arraycopy(head_buf, 0, buff, 0, 10);
		}

		 String buf_str = StringFormatter.FormatToString(buff, 10);
		 Log.e(TAG, "_packet()   buf_str = " + buf_str);

		return buff;
	}

	// 传递 参数，构建 2.4G 通信包 ， content 为 byte[]
	public byte[] _packet_wireless(byte msg_type, int seq, byte[] content) {
		// 变量赋值
		this.msg_type = msg_type;
		this.seq = seq;

		byte[] head_buf = new byte[1024];

		head_buf[0] = header;
		head_buf[1] = (byte) 0x01;

		// 消息类型
		head_buf[2] = (byte) msg_type;

		// Log.e(TAG, "_packet_wireless() seq = " + seq);

		byte[] seq_byte = int2net_byte(seq);
		System.arraycopy(seq_byte, 0, head_buf, 3, 4);

		head_buf[9] = (byte) ret;

		// 是否有内容
		int len = 0;
		if (content != null && content.length > 0) {
			// 内容转换
			try {
				len = content.length;
				// Log.e(TAG, "_packet()  len = " + len);

				byte[] len_byte = short2net_byte((short) len);
				System.arraycopy(len_byte, 0, head_buf, 7, 2);

				// 生成结果 byte[]
				buff = new byte[len + 10];
				System.arraycopy(head_buf, 0, buff, 0, 10);
				// 发送的数据包
				System.arraycopy(content, 0, buff, 10, len);
			} catch (Exception e) {
				e.printStackTrace();
				buff = new byte[10];
				System.arraycopy(buff, 0, head_buf, 0, 10);
			}
		} else {
			buff = new byte[10];
			System.arraycopy(head_buf, 0, buff, 0, 10);
		}

		// String buf_str = Formatter.FormatToString(buff, 10);
		// Log.e(TAG, "_packet()   buf_str = " + buf_str);

		return buff;
	}

	public String headToString() {
		return "PacketInfo" + "(" + "| ver : " + ver + "| seq : " + seq +
				// msg_type_str
				"| cmd : " + Formatter.byte2str(msg_type) + "| ret : " + ret + "| len : " + content_len;
	}

	// 返回命令包类型
	public String getMsgType(int type) {
		String str = "unknown";
		if (type == PacketConstant.MSG_HEARTBEAT_REQ) {
			str = "heartbeat_req";
		} else if (type == PacketConstant.MSG_HEARTBEAT_RESP) {
			str = "heartbeat_resp";
		} else if (type == PacketConstant.MSG_MOBILE_REGISTE_REQ) {
			str = "mobile_registe_req";
		} else if (type == PacketConstant.MSG_MOBILE_REGISTE_RESP) {
			str = "mobile_registe_resp";
		} else if (type == PacketConstant.MSG_BOX_REGISTE_REQ) {
			str = "box_registe_req";
		} else if (type == PacketConstant.MSG_BOX_REGISTE_RESP) {
			str = "box_registe_resp";
		} else if (type == PacketConstant.MSG_UNREGISTE_REQ) {
			str = "unregiste_req";
		} else if (type == PacketConstant.MSG_UNREGISTE_RESP) {
			str = "unregiste_resp";
		} else if (type == PacketConstant.MSG_PUSH_MOBILE_REQ) {
			str = "push_mobile_req";
		} else if (type == PacketConstant.MSG_PUSH_MOBILE_RESP) {
			str = "push_mobile_resp";
		} else if (type == PacketConstant.MSG_PUSH_DEV_REQ) {
			str = "push_dev_req";
		} else if (type == PacketConstant.MSG_PUSH_DEV_RESP) {
			str = "push_dev_resp";
		} else if (type == PacketConstant.MSG_MOBILE_CMD_REQ) {
			str = "mobile_cmd_req";
		} else if (type == PacketConstant.MSG_MOBILE_CMD_RESP) {
			str = "mobile_cmd_resp";
		}
		return str;
	}

	public boolean isResponse() {
		return (msg_type & 0x0080) != 0;
	}

	public void setResponse(boolean response) {
		msg_type = (byte) (msg_type & (response ? 0x00ff : 0x007f));
	}

	@Override
	public String toString() {
		return "Packet" + "(ver : " + ver + "| seq : " + seq + "| cmd : " + Formatter.byte2str(msg_type)
				+ "| ret : " + ret + "| len : " + content_len;
	}

	public String toAllString() {
		return "Packet " + "(ver : " + ver + "| seq : " + seq + "| cmd : " + Formatter.byte2str(msg_type)
				+ "| ret : " + ret + "| len : " + content_len + "| data: " + content_str + " )";
	}

	// int 转 byte[]
	public static byte[] int2byte(int x) {
		byte[] bb = new byte[4];
		int index = 0;
		bb[index + 3] = (byte) (x >> 24);
		bb[index + 2] = (byte) (x >> 16);
		bb[index + 1] = (byte) (x >> 8);
		bb[index + 0] = (byte) (x >> 0);
		return bb;
	}

	// int 转 网络 byte[]
	public static byte[] int2net_byte(int x) {
		byte[] bb = new byte[4];
		int index = 0;
		bb[index + 0] = (byte) (x >> 24);
		bb[index + 1] = (byte) (x >> 16);
		bb[index + 2] = (byte) (x >> 8);
		bb[index + 3] = (byte) (x >> 0);
		return bb;
	}

	public static int byte2int(byte[] bb) {
		int i = (int) ((((bb[3] & 0xff) << 24) | ((bb[2] & 0xff) << 16) | ((bb[1] & 0xff) << 8) | ((bb[0] & 0xff) << 0)));
		return i;
	}

	public static int net_byte2int(byte[] bb) {
		int i = (int) ((((bb[0] & 0xff) << 24) | ((bb[1] & 0xff) << 16) | ((bb[2] & 0xff) << 8) | ((bb[3] & 0xff) << 0)));
		return i;
	}

	// short 转 byte[]
	public static byte[] short2byte(short x) {
		byte[] bb = new byte[2];
		int index = 0;
		bb[index + 1] = (byte) (x >> 8);
		bb[index + 0] = (byte) (x >> 0);
		return bb;
	}

	// short 转 网络 byte[]
	public static byte[] short2net_byte(short x) {
		byte[] bb = new byte[2];
		int index = 0;
		bb[index + 0] = (byte) (x >> 8);
		bb[index + 1] = (byte) (x >> 0);
		return bb;
	}

	public static short byte2short(byte[] bb) {
		int i = (int) (((bb[1] & 0xff) << 8) | ((bb[0] & 0xff) << 0));
		return (short) i;
	}

	public static short net_byte2short(byte[] bb) {
		int i = (int) (((bb[0] & 0xff) << 8) | ((bb[1] & 0xff) << 0));
		return (short) i;
	}


	public static boolean IsTextUTF8( byte[] inputStream)
	{
		int encodingBytesCount = 0;
		boolean allTextsAreASCIIChars = true;

		for (int i = 0; i < inputStream.length; i++)
		{
			byte current = inputStream[i];

			if ((current & 0x80) == 0x80)
			{
				allTextsAreASCIIChars = false;
			}
			// First byte
			if (encodingBytesCount == 0)
			{
				if ((current & 0x80) == 0)
				{
					// ASCII chars, from 0x00-0x7F
					continue;
				}

				if ((current & 0xC0) == 0xC0)
				{
					encodingBytesCount = 1;
					current <<= 2;

					// More than two bytes used to encoding a unicode char.
					// Calculate the real length.
					while ((current & 0x80) == 0x80)
					{
						current <<= 1;
						encodingBytesCount++;
					}
				}
				else
				{
					// Invalid bits structure for UTF8 encoding rule.
					return false;
				}
			}
			else
			{
				// Following bytes, must start with 10.
				if ((current & 0xC0) == 0x80)
				{
					encodingBytesCount--;
				}
				else
				{
					// Invalid bits structure for UTF8 encoding rule.
					return false;
				}
			}
		}

		if (encodingBytesCount != 0)
		{
			// Invalid bits structure for UTF8 encoding rule.
			// Wrong following bytes count.
			return false;
		}

		// Although UTF8 supports encoding for ASCII chars, we regard as a input stream, whose contents are all ASCII as default encoding.
		return !allTextsAreASCIIChars;
	}
}
