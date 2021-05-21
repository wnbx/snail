package com.acgist.snail.net.torrent.tracker;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

import com.acgist.snail.context.TorrentContext;
import com.acgist.snail.context.TrackerContext;
import com.acgist.snail.context.exception.DownloadException;
import com.acgist.snail.context.exception.NetException;
import com.acgist.snail.pojo.session.TorrentSession;
import com.acgist.snail.pojo.session.TrackerSession;
import com.acgist.snail.utils.Performance;
import com.acgist.snail.utils.ThreadUtils;

class UdpTrackerSessionTest extends Performance {

	@Test
	void testAnnounce() throws NetException, DownloadException {
		final String path = "E:/snail/902FFAA29EE632C8DC966ED9AB573409BA9A518E.torrent";
		final String announceUrl = "udp://tracker.opentrackr.org:1337/announce";
//		final String announceUrl = "udp://[2001:19f0:6c01:1b7d:5400:1ff:fefc:3c2a]:6969/announce";
		final TorrentSession torrentSession = TorrentContext.getInstance().newTorrentSession(path);
		final var list = TrackerContext.getInstance().sessions(announceUrl);
		final TrackerSession session = list.stream()
			.filter(value -> value.equalsAnnounceUrl(announceUrl))
			.findFirst()
			.get();
		session.started(1000, torrentSession);
		session.scrape(1000, torrentSession);
		ThreadUtils.sleep(5000);
		assertNotNull(session);
	}
	
}
