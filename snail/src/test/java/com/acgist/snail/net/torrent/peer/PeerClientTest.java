package com.acgist.snail.net.torrent.peer;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.acgist.snail.config.PeerConfig;
import com.acgist.snail.context.TorrentContext;
import com.acgist.snail.context.exception.DownloadException;
import com.acgist.snail.pojo.bean.InfoHash;
import com.acgist.snail.pojo.entity.TaskEntity;
import com.acgist.snail.pojo.session.PeerSession;
import com.acgist.snail.pojo.session.StatisticsSession;
import com.acgist.snail.pojo.session.TaskSession;
import com.acgist.snail.pojo.session.TorrentSession;
import com.acgist.snail.pojo.wrapper.DescriptionWrapper;
import com.acgist.snail.protocol.Protocol.Type;
import com.acgist.snail.utils.Performance;
import com.acgist.snail.utils.ThreadUtils;

/**
 * <p>结合本地迅雷或者其他BT软件测试</p>
 */
class PeerClientTest extends Performance {
	
	@Test
	void testDownload() throws DownloadException {
		final var path = "E:/snail/1f4f28a6df2ea7899328cbef1dfaaeec9920cdb3.torrent";
		final var torrentSession = TorrentContext.getInstance().newTorrentSession(path);
		final List<String> list = new ArrayList<>();
		// 选择下载文件
		torrentSession.torrent().getInfo().files().forEach(file -> {
			if(file.notPaddingFile()) {
				list.add(file.path());
			}
		});
		final var entity = new TaskEntity();
		entity.setFile("E:/snail/tmp/download/");
		entity.setType(Type.TORRENT);
		// 设置下载文件
		entity.setDescription(DescriptionWrapper.newEncoder(list).serialize());
		// 禁止自动加载Peer
		torrentSession.upload(TaskSession.newInstance(entity)).download(false);
		final String host = "127.0.0.1";
//		final Integer port = 15000; // 迅雷测试端口
//		final Integer port = 18888; // 蜗牛测试端口
		final Integer port = 49160; // FDM测试端口
		this.log("已经下载Piece：" + torrentSession.pieces());
		final var statisticsSession = new StatisticsSession();
		final var peerSession = PeerSession.newInstance(statisticsSession, host, port);
		// UTP支持
		peerSession.flags(PeerConfig.PEX_UTP);
		final var downloader = PeerDownloader.newInstance(peerSession, torrentSession);
		downloader.handshake();
		new Thread(() -> {
			while(true) {
				this.log("下载速度：" + statisticsSession.downloadSpeed());
				ThreadUtils.sleep(1000);
			}
		}).start();
		ThreadUtils.sleep(100000);
		assertNotNull(downloader);
	}

	@Test
	void testMagnet() throws DownloadException {
		final var hash = "1f4f28a6df2ea7899328cbef1dfaaeec9920cdb3";
		final var torrentSession = TorrentSession.newInstance(InfoHash.newInstance(hash), null);
		final var entity = new TaskEntity();
		entity.setFile("E:/snail/tmp/torrent/");
		entity.setType(Type.TORRENT);
		entity.setUrl(hash);
		torrentSession.magnet(TaskSession.newInstance(entity));
		final String host = "127.0.0.1";
//		final Integer port = 15000; // 迅雷测试端口
//		final Integer port = 18888; // 蜗牛测试端口
		final Integer port = 49160; // FDM测试端口
		final var peerSession = PeerSession.newInstance(new StatisticsSession(), host, port);
		final var downloader = PeerDownloader.newInstance(peerSession, torrentSession);
		downloader.handshake();
		ThreadUtils.sleep(100000);
		assertNotNull(downloader);
	}

}
