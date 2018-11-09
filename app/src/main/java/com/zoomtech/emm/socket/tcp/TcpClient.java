package com.zoomtech.emm.socket.tcp;



import android.util.Log;

import com.zoomtech.emm.utils.LogUtil;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;
import java.util.Vector;

/**
 * 只负责连接、发送以及接收，不处理具体业务
 *
 * ByteBuffer需要看看使用方式是否正确
 */
public class TcpClient {
	private final String LOG_TAG = this.getClass().getSimpleName();

	// 连接结果回调对象
	private ITcpResponse m_response;

	private SocketChannel m_socketChannel;

	//Selector(选择器)是Java NIO中能够检测一到多个NIO通道，并能够知晓通道是否为诸如读写事件做好准备的组件。
	// 这样，一个单独的线程可以管理多个channel，从而管理多个网络连接。
	private Selector m_selector;

	private boolean m_bRun;
	private Boolean m_bReady;

	private int countMaX=0;
	// 连接相关
	private String m_host;
	private int m_port;

	private Boolean m_bWantConnect;

	// 读写相关
	private Vector<ByteBuffer> m_sendQueue = new Vector<ByteBuffer>();

	//创建一个容量为2048字节的ByteBuffer,如果发现创建的缓冲区容量太小,唯一的选择就是重新创建一个大小合适的缓冲区
	private ByteBuffer m_buff = ByteBuffer.allocate(32768);


	/**
	 * 连接结果处理接口
	 */
	public interface ITcpResponse {
		/** 连接成功 */
		public abstract void onConnectSucceed();
		/** 连接失败 */
		public abstract void onConnectFailed();
		/** 从连接中收到信息 */
		public abstract boolean onRecv(byte[] data, int count);
	}


	/**
	 * 构造方法
	 * @param response
	 */
	public TcpClient(ITcpResponse response) {
		m_response = response;

		m_socketChannel = null;
		m_selector = null;

		m_bRun = true;
		m_bWantConnect = false;
		m_bReady = false;

		// 启动连接线程
		Thread conn_thread = new Thread(new TcpTask());
		conn_thread.start();
	}

	public void start() {
		// 开启线程并且只维持一个线程
	}

	/**
	 * 关闭
	 */
	public void close() {
		m_bRun = false;

		// 唤醒阻塞的SELECTOR
		wakeup();
	}

	/**
	 * 发送数据-向队列中添加要
	 * @param data
	 */
	public void send(ByteBuffer data) {
		m_sendQueue.add(data);

		// 唤醒阻塞的SELECTOR
		wakeup();
	}

	/**
	 * 建立连接
	 * @param host
	 * @param port
	 */
	public void connect(String host, int port) {
		m_host = host;
		m_port = port;
		m_bWantConnect = true;
		wakeup();
	}

	/**
	 * 连接任务线程, 监听连接事件
	 */
	class TcpTask implements Runnable {
		public void run() {
			while (m_bRun) {

				// 建立连接
				if (m_bWantConnect) {
					connectProc();

					continue;
				}

				// 是否已经准备就绪
				if (!m_bReady) {
					try {
						Thread.sleep(1000);
					} catch (Exception e) {
						e.printStackTrace();
					}
					continue;
				}

				// 处理连接
				handleConnect();
			}

			// 关闭连接资源
			closeConnProc();
		}

		/**
		 * 处理连接
		 */
		private void handleConnect() {
			try {
				// 阻塞到至少有一个通道在注册的事件上就绪
				m_selector.select();

				// 访问"已选择键集（selected key set）"中的就绪通道，
				// 检测各个键所对应的通道的就绪事件
				Set<SelectionKey> keys = m_selector.selectedKeys();
				Iterator<SelectionKey> iterator = keys.iterator();

				while (iterator.hasNext()) {
					SelectionKey key = iterator.next();

					// 删除已选的key，以防重复处理
					iterator.remove();

					// 建立连接已就绪
					if (key.isConnectable()) {
						onConnectProc(key);
					}

					// 读取数据已就绪
					if (key.isReadable()) {
						onReadProc(key);
					}

					// 删除正在处理的SelectionKey
					keys.remove(key);
				}

				// 向连接CHANNEL中写数据
				onWriteProc();

			} catch (Exception e) {
				e.printStackTrace();
				// 管理连接，释放资源
				closeConnProc();
				if (m_response != null) {
					//	LogUtil.writeToFile(LOG_TAG, "m_response.onConnectFailed -1-连接失败"+ TheTang.getExceptionInfo(e));
					m_response.onConnectFailed();
				}

				Log.e(LOG_TAG, LogUtil.getExceptionInfo(e));
				LogUtil.writeToFile(LOG_TAG, LogUtil.getExceptionInfo(e));

			}
		}
	}

	/**
	 * 建立连接
	 */
	public void connectProc() {
		m_bWantConnect = false;

		// 关闭连接(如果已经连接)
		closeConnProc();

		try {
			// 获得一个Socket通道
			m_socketChannel = SocketChannel.open();
			// 设置通道为非阻塞
			m_socketChannel.configureBlocking(false);
			// 获得一个通道管理器
			m_selector = Selector.open();

			// 将通道管理器和该通道绑定，并为该通道注册SelectionKey.OP_CONNECT事件。
			m_socketChannel.register(m_selector, SelectionKey.OP_CONNECT);

			// 客户端连接服务器,其实方法执行并没有实现连接
			// 需要调用channel.finishConnect();才能完成连接
			InetSocketAddress isa = new InetSocketAddress(m_host, m_port);
			m_socketChannel.connect(isa);


			m_bReady = true;

		} catch (IOException e) {
			e.printStackTrace();
			Log.e(LOG_TAG, e.toString());
			LogUtil.writeToFile(LOG_TAG, LogUtil.getExceptionInfo(e));
			if (m_response != null) {
				//	LogUtil.writeToFile(LOG_TAG, "m_response.onConnectFailed -2-连接失败"+ TheTang.getExceptionInfo(e));
				m_response.onConnectFailed();
			}
		}
	}

	/**
	 * 关闭连接
	 */
	public void closeConnProc() {
		m_bReady = false;

		// 分开关闭，以确保两个都有执行
		try {
			if (m_selector != null) {
				m_selector.close();
			}
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		try {
			if (m_socketChannel != null) {
				m_socketChannel.close();
			}
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		m_selector = null;
		m_socketChannel = null;

		Log.w(LOG_TAG, "closeConnProc() 关闭连接m_selector=" + m_selector+ ", m_socketChannel=" + m_socketChannel);
		//	LogUtil.writeToFile(LOG_TAG, "closeConnProc() 关闭连接m_selector=" + m_selector+ ", m_socketChannel=" + m_socketChannel);
	}

	/**
	 * 处理连接成功
	 * @param key
	 * @throws IOException
	 */
	private void onConnectProc(SelectionKey key) throws IOException {
		SocketChannel sc = (SocketChannel) key.channel();
		// 如果正在连接，则完成连接
		if (sc.isConnectionPending()) {
			sc.finishConnect();
			if (m_response != null) {
				m_response.onConnectSucceed();
			}
		}

		// 在和服务端连接成功之后，为了可以接收到服务端的信息，需要给通道设置读的权限。
		sc.register(m_selector, SelectionKey.OP_READ);
	}

	/**
	 * 读取连接中发来的数据
	 * @param key
	 * @throws IOException
	 */
	private void onReadProc(SelectionKey key) throws IOException {
		SocketChannel sc = (SocketChannel) key.channel();
		m_buff.clear();

		int count = sc.read(m_buff);
		if (count < 0) {
			Log.e("TcpClient","read count < 0,count="+count);
			LogUtil.writeToFile("TcpClient","read count < 0，count="+count);
			throw new IOException("read count < 0");
		}

		if (m_response != null) {
			byte[] data = m_buff.array();
			countMaX=countMaX+count;
			try{
				boolean recv = m_response.onRecv(data, countMaX);
				if (recv){
					//如果是正常返回则把临时记录记为0(初始值)
					countMaX=0;
					m_buff.flip();
					m_buff.clear();
				}
			} catch(Exception e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * 把数据队列中数据项写到连接中
	 *
	 * @throws IOException
	 */
	private void onWriteProc() throws IOException {
		Iterator<ByteBuffer> iter = m_sendQueue.iterator();

		while (iter.hasNext()) {
			ByteBuffer data = iter.next();

			// 删除已选的key,以防重复处理
			iter.remove();

			while (data.hasRemaining()) {
				m_socketChannel.write(data);
			}
		}
	}

	/**
	 * 唤醒阻塞的Selector
	 */
	private void wakeup() {
		try {
			if (m_selector != null && m_selector.isOpen()) {
				m_selector.wakeup();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 获取Socket连接地址
	 * @return
	 */
	public InetAddress getLocalAddress() {
		if (m_socketChannel != null) {
			return m_socketChannel.socket().getLocalAddress();
		}

		return null;
	}



}

