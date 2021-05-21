package com.acgist.snail.net.torrent.lsd;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import com.acgist.snail.context.PeerContext;
import com.acgist.snail.context.TorrentContext;
import com.acgist.snail.context.exception.DownloadException;
import com.acgist.snail.context.exception.NetException;
import com.acgist.snail.pojo.entity.TaskEntity;
import com.acgist.snail.pojo.session.TaskSession;
import com.acgist.snail.utils.Performance;
import com.acgist.snail.utils.ThreadUtils;

class LocalServiceDiscoveryTest extends Performance {

	private String hashA = "28b5e72737f183cb36182fcc8991d5cbf7ce627c";
	private String hashB = "28b5e72737f183cb36182fcc8991d5cbf7ce6271";
	private String hashC = "28b5e72737f183cb36182fcc8991d5cbf7ce6272";
	
	@Test
	void testServer() throws DownloadException {
		if(SKIP_COSTED) {
			this.log("跳过testServer测试");
			return;
		}
		final var server = LocalServiceDiscoveryServer.getInstance();
		final var entity = new TaskEntity();
		entity.setUrl(this.hashA);
		entity.setSize(0L);
		TorrentContext.getInstance().newTorrentSession(this.hashA, null).magnet(TaskSession.newInstance(entity));
		assertNotNull(server);
		while(!PeerContext.getInstance().isNotEmpty(this.hashA)) {
			ThreadUtils.sleep(1000);
		}
		assertTrue(PeerContext.getInstance().isNotEmpty(this.hashA));
	}
	
	@Test
	void testClient() throws NetException {
		final var client = LocalServiceDiscoveryClient.newInstance();
		client.localSearch(this.hashC);
		client.localSearch(this.hashA, this.hashB);
		assertNotNull(client);
	}
	
}
