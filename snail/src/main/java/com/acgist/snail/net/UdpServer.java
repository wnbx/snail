package com.acgist.snail.net;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.StandardSocketOptions;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ExecutorService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.config.SystemConfig;
import com.acgist.snail.context.SystemThreadContext;
import com.acgist.snail.utils.IoUtils;
import com.acgist.snail.utils.NetUtils;

/**
 * <p>UDP服务端</p>
 * <p>全部使用单例：初始化时立即开始监听（客户端和服务端使用同一个通道）</p>
 * 
 * @param <T> UDP消息接收代理类型
 * 
 * @author acgist
 */
public abstract class UdpServer<T extends UdpAcceptHandler> extends Server<DatagramChannel> {

	private static final Logger LOGGER = LoggerFactory.getLogger(UdpServer.class);

	/**
	 * <p>服务端线程池</p>
	 */
	private static final ExecutorService EXECUTOR;
	
	static {
		EXECUTOR = SystemThreadContext.newCacheExecutor(0, 60L, SystemThreadContext.SNAIL_THREAD_UDP_SERVER);
	}
	
	/**
	 * <p>消息接收代理</p>
	 */
	private final T handler;
	/**
	 * <p>Selector：每个服务端独立</p>
	 */
	private Selector selector;

	/**
	 * <p>UDP服务端</p>
	 * 
	 * @param name 服务端名称
	 * @param handler 消息接收代理
	 * 
	 * @see Server#PORT_AUTO
	 * @see Server#ADDR_LOCAL
	 * @see Server#ADDR_UNREUSE
	 */
	protected UdpServer(String name, T handler) {
		this(ADDR_LOCAL, PORT_AUTO, ADDR_UNREUSE, name, handler);
	}
	
	/**
	 * <p>UDP服务端</p>
	 * 
	 * @param port 端口
	 * @param name 服务端名称
	 * @param handler 消息接收代理
	 * 
	 * @see Server#ADDR_LOCAL
	 * @see Server#ADDR_UNREUSE
	 */
	protected UdpServer(int port, String name, T handler) {
		this(ADDR_LOCAL, port, ADDR_UNREUSE, name, handler);
	}
	
	/**
	 * <p>UDP服务端</p>
	 * 
	 * @param port 端口
	 * @param reuse 是否重用地址
	 * @param name 服务端名称
	 * @param handler 消息接收代理
	 * 
	 * @see Server#ADDR_LOCAL
	 */
	protected UdpServer(int port, boolean reuse, String name, T handler) {
		this(ADDR_LOCAL, port, reuse, name, handler);
	}
	
	/**
	 * <p>UDP服务端</p>
	 * 
	 * @param host 地址
	 * @param port 端口
	 * @param reuse 是否重用地址
	 * @param name 服务端名称
	 * @param handler 消息接收代理
	 */
	protected UdpServer(String host, int port, boolean reuse, String name, T handler) {
		super(name);
		this.handler = handler;
		this.listen(host, port, reuse);
	}
	
	@Override
	protected boolean listen(String host, int port, boolean reuse) {
		LOGGER.debug("启动UDP服务端：{}", this.name);
		boolean success = true;
		try {
			this.channel = DatagramChannel.open(NetUtils.LOCAL_PROTOCOL_FAMILY);
			// 不要阻塞
			this.channel.configureBlocking(false);
			if(reuse) {
				this.channel.setOption(StandardSocketOptions.SO_REUSEADDR, true);
			}
			this.channel.bind(NetUtils.buildSocketAddress(host, port));
		} catch (IOException e) {
			LOGGER.error("启动UDP服务端异常：{}", this.name, e);
			success = false;
		} finally {
			if(success) {
				LOGGER.debug("启动UDP服务端成功：{}", this.name);
			} else {
				IoUtils.close(this.channel);
				this.close();
			}
		}
		return success;
	}
	
	/**
	 * <p>多播（组播）</p>
	 * 
	 * @param ttl TTL
	 * @param group 分组
	 */
	protected void join(int ttl, String group) {
		if(!this.available()) {
			LOGGER.warn("UDP多播失败：{}-{}-{}", this.name, group, this.channel);
			return;
		}
		try {
			this.channel.setOption(StandardSocketOptions.IP_MULTICAST_TTL, ttl);
			this.channel.setOption(StandardSocketOptions.IP_MULTICAST_LOOP, true);
			this.channel.join(InetAddress.getByName(group), NetUtils.DEFAULT_NETWORK_INTERFACE);
		} catch (IOException e) {
			LOGGER.debug("UDP多播异常：{}-{}", this.name, group, e);
		}
	}
	
	/**
	 * <p>消息代理</p>
	 */
	protected void handle() {
		if(!this.available()) {
			LOGGER.warn("UDP消息代理失败：{}-{}", this.name, this.channel);
			return;
		}
		this.handler.handle(this.channel);
		this.selector();
		EXECUTOR.submit(this::loopMessage);
	}
	
	/**
	 * <p>注册消息读取事件</p>
	 */
	private void selector() {
		try {
			this.selector = Selector.open();
			this.channel.register(this.selector, SelectionKey.OP_READ);
		} catch (IOException e) {
			LOGGER.error("注册消息读取事件异常：{}", this.name, e);
		}
	}
	
	/**
	 * <p>消息轮询</p>
	 */
	private void loopMessage() {
		while (this.available()) {
			try {
				this.receive();
			} catch (Exception e) {
				LOGGER.error("UDP Server消息轮询异常：{}", this.name, e);
			}
		}
		LOGGER.debug("UDP Server退出消息轮询：{}", this.name);
	}
	
	/**
	 * <p>消息接收</p>
	 * 
	 * @throws IOException IO异常
	 */
	private void receive() throws IOException {
		if(this.selector.select() > 0) {
			final Set<SelectionKey> selectionKeys = this.selector.selectedKeys();
			final Iterator<SelectionKey> iterator = selectionKeys.iterator();
			while (iterator.hasNext()) {
				final SelectionKey selectionKey = iterator.next();
				// 移除已经取出来的信息
				iterator.remove();
				if (selectionKey.isValid() && selectionKey.isReadable()) {
					final ByteBuffer buffer = ByteBuffer.allocateDirect(SystemConfig.UDP_BUFFER_LENGTH);
					// 服务器多例：selectionKey.channel()
					// 服务端单例：客户端通道=服务端通道
					final InetSocketAddress socketAddress = (InetSocketAddress) this.channel.receive(buffer);
					this.handler.receive(buffer, socketAddress);
				}
			}
		}
	}
	
	/**
	 * <p>关闭UDP Server</p>
	 */
	public void close() {
		LOGGER.debug("关闭UDP Server：{}", this.name);
		IoUtils.close(this.channel);
		IoUtils.close(this.selector);
	}
	
	/**
	 * <p>关闭UDP Server线程池</p>
	 */
	public static final void shutdown() {
		LOGGER.debug("关闭UDP Server线程池");
		SystemThreadContext.shutdown(EXECUTOR);
	}

}
