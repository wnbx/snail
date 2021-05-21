package com.acgist.snail.net.upnp;

import java.net.InetSocketAddress;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.context.NatContext;
import com.acgist.snail.context.exception.NetException;
import com.acgist.snail.net.UdpMessageHandler;
import com.acgist.snail.net.codec.IMessageDecoder;
import com.acgist.snail.net.codec.StringMessageCodec;
import com.acgist.snail.pojo.wrapper.HeaderWrapper;
import com.acgist.snail.utils.StringUtils;

/**
 * <p>UPNP消息代理</p>
 * <p>协议链接：https://www.rfc-editor.org/rfc/rfc6970.txt</p>
 * <p>协议链接：http://upnp.org/specs/arch/UPnP-arch-DeviceArchitecture-v1.0.pdf</p>
 * <p>注意：固定IP有时不能正确获取UPNP设置（需要设置自动获取IP地址）</p>
 * 
 * @author acgist
 */
public final class UpnpMessageHandler extends UdpMessageHandler implements IMessageDecoder<String> {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(UpnpMessageHandler.class);

	/**
	 * <p>描述文件地址响应头名称：{@value}</p>
	 */
	private static final String HEADER_LOCATION = "location";
	
	/**
	 * <p>服务端</p>
	 */
	public UpnpMessageHandler() {
		this(null);
	}
	
	/**
	 * <p>客户端</p>
	 * 
	 * @param socketAddress 地址
	 */
	public UpnpMessageHandler(InetSocketAddress socketAddress) {
		super(socketAddress);
		this.messageDecoder = new StringMessageCodec(this);
	}
	
	@Override
	public void onMessage(String message, InetSocketAddress address) {
		final HeaderWrapper headers = HeaderWrapper.newInstance(message);
		// 判断是否支持UPNP设置
		final boolean support = headers.allHeaders().values().stream()
			.anyMatch(list -> list.stream()
				.anyMatch(value -> StringUtils.startsWith(value, UpnpServer.UPNP_ROOT_DEVICE))
			);
		if(!support) {
			LOGGER.warn("UPNP设置失败（不支持的驱动）：{}", message);
			return;
		}
		final String location = headers.header(HEADER_LOCATION);
		if(StringUtils.isEmpty(location)) {
			LOGGER.warn("UPNP设置失败（没有描述文件地址）：{}", message);
			return;
		}
		final UpnpService upnpService = UpnpService.getInstance();
		try {
			upnpService.load(location).mapping();
		} catch (NetException e) {
			LOGGER.error("UPNP端口映射异常：{}", location, e);
		} finally {
			if(upnpService.useable()) {
				NatContext.getInstance().unlockUpnp();
			}
		}
	}
	
}
