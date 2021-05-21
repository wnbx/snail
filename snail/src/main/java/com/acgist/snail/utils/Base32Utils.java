package com.acgist.snail.utils;

/**
 * <p>Base32编码工具</p>
 * 
 * @author acgist
 */
public final class Base32Utils {

	/**
	 * <p>编码字符</p>
	 */
	private static final char[] BASE_32_ENCODE = {
		'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M',
		'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z',
		'2', '3', '4', '5', '6', '7'
	};
	/**
	 * <p>解码字符</p>
	 */
	private static final byte[] BASE_32_DECODE;

	static {
		BASE_32_DECODE = new byte[128];
		for (int index = 0; index < BASE_32_DECODE.length; index++) {
			BASE_32_DECODE[index] = (byte) 0xFF;
		}
		for (int index = 0; index < BASE_32_ENCODE.length; index++) {
			BASE_32_DECODE[(int) BASE_32_ENCODE[index]] = (byte) index;
			if (index < 24) {
				BASE_32_DECODE[(int) Character.toLowerCase(BASE_32_ENCODE[index])] = (byte) index;
			}
		}
	}
	
	private Base32Utils() {
	}
	
	/**
	 * <p>数据编码</p>
	 * 
	 * @param content 原始数据
	 * 
	 * @return 编码数据
	 * 
	 * @see #encode(byte[])
	 */
	public static final String encode(String content) {
		if(content == null) {
			return null;
		}
		return encode(content.getBytes());
	}
	
	/**
	 * <p>数据编码</p>
	 * 
	 * @param content 原始数据
	 * 
	 * @return 编码数据
	 */
	public static final String encode(final byte[] content) {
		if(content == null) {
			return null;
		}
		int value;
		int index = 0;
		int contentIndex = 0;
		final int contentLength = content.length;
		final char[] chars = new char[((contentLength * 8) / 5) + ((contentLength % 5) != 0 ? 1 : 0)];
		final int charsLength = chars.length;
		for (int charsIndex = 0; charsIndex < charsLength; charsIndex++) {
			if (index > 3) {
				value = (content[contentIndex] & 0xFF) & (0xFF >> index);
				index = (index + 5) % 8;
				value <<= index;
				if (contentIndex < contentLength - 1) {
					value |= (content[contentIndex + 1] & 0xFF) >> (8 - index);
				}
				chars[charsIndex] = BASE_32_ENCODE[value];
				contentIndex++;
			} else {
				chars[charsIndex] = BASE_32_ENCODE[((content[contentIndex] >> (8 - (index + 5))) & 0x1F)];
				index = (index + 5) % 8;
				if (index == 0) {
					contentIndex++;
				}
			}
		}
		return new String(chars);
	}

	/**
	 * <p>数据解码</p>
	 * 
	 * @param content 编码数据
	 * 
	 * @return 原始数据
	 * 
	 * @see #decode(String)
	 */
	public static final String decodeToString(final String content) {
		if(content == null) {
			return null;
		}
		return new String(decode(content));
	}
	
	/**
	 * <p>数据解码</p>
	 * 
	 * @param content 编码数据
	 * 
	 * @return 原始数据
	 */
	public static final byte[] decode(final String content) {
		if(content == null) {
			return null;
		}
		int value;
		int index = 0;
		int bytesIndex = 0;
		final char[] chars = content.toUpperCase().toCharArray();
		final int charsLength = chars.length;
		final byte[] bytes = new byte[(charsLength * 5) / 8];
		final int bytesLength = bytes.length;
		for (int charsIndex = 0; charsIndex < charsLength; charsIndex++) {
			value = BASE_32_DECODE[chars[charsIndex]];
			if (index <= 3) {
				index = (index + 5) % 8;
				if (index == 0) {
					bytes[bytesIndex++] |= value;
				} else {
					bytes[bytesIndex] |= value << (8 - index);
				}
			} else {
				index = (index + 5) % 8;
				bytes[bytesIndex++] |= (value >> index);
				if (bytesIndex < bytesLength) {
					bytes[bytesIndex] |= value << (8 - index);
				}
			}
		}
		return bytes;
	}

}
