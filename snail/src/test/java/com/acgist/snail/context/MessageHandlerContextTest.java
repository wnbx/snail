package com.acgist.snail.context;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import com.acgist.snail.net.UdpMessageHandler;
import com.acgist.snail.net.torrent.TorrentServer;
import com.acgist.snail.utils.Performance;
import com.acgist.snail.utils.ThreadUtils;

class MessageHandlerContextTest extends Performance {
	
	@Test
	void testMessageHandlerContext() {
		if(SKIP_COSTED) {
			this.log("跳过testMessageHandlerContext测试");
			return;
		}
		final var context = MessageHandlerContext.getInstance();
		final var handler = new UdpMessageHandler(null) {
			@Override
			public boolean useless() {
				return true;
			}
		};
		handler.handle(TorrentServer.getInstance().channel());
		assertTrue(handler.available());
		context.newInstance(handler);
		ThreadUtils.sleep(62000);
		assertFalse(handler.available());
	}
	
}
