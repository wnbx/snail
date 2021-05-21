package com.acgist.snail.context.exception;

import com.acgist.snail.config.SystemConfig;

/**
 * <p>网络包大小异常</p>
 * 
 * @author acgist
 */
public final class PacketSizeException extends NetException {

	private static final long serialVersionUID = 1L;
	
	/**
	 * <p>验证网络包大小</p>
	 * 
	 * @param length 网络包大小
	 * 
	 * @throws PacketSizeException 网络包大小异常
	 * 
	 * @see SystemConfig#MAX_NET_BUFFER_LENGTH
	 */
	public static final void verify(short length) throws PacketSizeException {
		if(length < 0 || length > SystemConfig.MAX_NET_BUFFER_LENGTH) {
			throw new PacketSizeException(length);
		}
	}
	
	/**
	 * <p>验证网络包大小</p>
	 * 
	 * @param length 网络包大小
	 * 
	 * @throws PacketSizeException 网络包大小异常
	 * 
	 * @see SystemConfig#MAX_NET_BUFFER_LENGTH
	 */
	public static final void verify(int length) throws PacketSizeException {
		if(length < 0 || length > SystemConfig.MAX_NET_BUFFER_LENGTH) {
			throw new PacketSizeException(length);
		}
	}

	public PacketSizeException() {
		super("网络包大小异常");
	}
	
	/**
	 * <p>网络包大小异常</p>
	 * 
	 * @param size 网络包大小
	 */
	public PacketSizeException(int size) {
		super("网络包大小错误：" + size);
	}

	/**
	 * <p>网络包大小异常</p>
	 * 
	 * @param message 错误信息
	 */
	public PacketSizeException(String message) {
		super(message);
	}

	/**
	 * <p>网络包大小异常</p>
	 * 
	 * @param cause 原始异常
	 */
	public PacketSizeException(Throwable cause) {
		super(cause);
	}
	
	/**
	 * <p>网络包大小异常</p>
	 * 
	 * @param message 错误信息
	 * @param cause 原始异常
	 */
	public PacketSizeException(String message, Throwable cause) {
		super(message, cause);
	}
	
}
