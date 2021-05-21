package com.acgist.snail.net;

import java.nio.channels.Channel;

/**
 * <p>通道代理</p>
 * 
 * @param <T> 通道代理类型
 * 
 * @author acgist
 */
public interface IChannelHandler<T extends Channel> {

	/**
	 * <p>通道代理</p>
	 * 
	 * @param channel 通道
	 */
	void handle(T channel);
	
}
