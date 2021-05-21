package com.acgist.snail.net.torrent.peer;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import org.junit.jupiter.api.Test;

import com.acgist.snail.config.DownloadConfig;
import com.acgist.snail.context.TorrentContext;
import com.acgist.snail.context.exception.DownloadException;
import com.acgist.snail.net.torrent.TorrentServer;
import com.acgist.snail.pojo.bean.TorrentFile;
import com.acgist.snail.pojo.entity.TaskEntity;
import com.acgist.snail.pojo.session.PeerSession;
import com.acgist.snail.pojo.session.StatisticsSession;
import com.acgist.snail.pojo.session.TaskSession;
import com.acgist.snail.pojo.wrapper.DescriptionWrapper;
import com.acgist.snail.protocol.Protocol.Type;
import com.acgist.snail.utils.Performance;
import com.acgist.snail.utils.ThreadUtils;

/**
 * <p>软件本身充当下载的客户端和服务端测试</p>
 */
class PeerServerTest extends Performance {
	
	@Test
	void testServer() throws DownloadException {
		final var path = "E:/snail/902FFAA29EE632C8DC966ED9AB573409BA9A518E.torrent";
		final var torrentSession = TorrentContext.getInstance().newTorrentSession(path);
		DownloadConfig.setBuffer(40 * 1024);
		final List<String> list = new ArrayList<>();
		final AtomicLong size = new AtomicLong();
		torrentSession.torrent().getInfo().files().stream()
			.filter(TorrentFile::notPaddingFile)
			.forEach(file -> {
				if(file.path().contains("Scans/Vol.1")) {
					list.add(file.path());
				}
				size.addAndGet(file.getLength());
			});
		final var entity = new TaskEntity();
		entity.setFile("E:\\snail\\server");
		entity.setType(Type.TORRENT);
		entity.setSize(size.get());
		entity.setDescription(DescriptionWrapper.newEncoder(list).serialize());
		torrentSession.upload(TaskSession.newInstance(entity));
		torrentSession.verify();
		PeerServer.getInstance();
		TorrentServer.getInstance();
		ThreadUtils.sleep(100000);
	}

	@Test
	void testClient() throws DownloadException {
		final var path = "E:/snail/902FFAA29EE632C8DC966ED9AB573409BA9A518E.torrent";
		final var torrentSession = TorrentContext.getInstance().newTorrentSession(path);
		DownloadConfig.setBuffer(40 * 1024);
		final List<String> list = new ArrayList<>();
		// 选择下载文件
		torrentSession.torrent().getInfo().files().stream()
			.filter(TorrentFile::notPaddingFile)
			.forEach(file -> {
				if(file.path().contains("Scans/Vol.1")) {
					list.add(file.path());
				}
			});
		final var entity = new TaskEntity();
		entity.setFile("E://snail/tmp/client");
		entity.setType(Type.TORRENT);
		entity.setDescription(DescriptionWrapper.newEncoder(list).serialize());
		// 禁止自动加载Peer
		torrentSession.upload(TaskSession.newInstance(entity)).download(false);
		torrentSession.verify();
		final String host = "127.0.0.1";
		final Integer port = 18888;
		final var statisticsSession = new StatisticsSession(); // 统计
		final var peerSession = PeerSession.newInstance(statisticsSession, host, port);
		// UTP支持：需要启动服务端后修改端口配置
//		peerSession.flags(PeerConfig.PEX_UTP);
		final var launcher = PeerDownloader.newInstance(peerSession, torrentSession);
		launcher.handshake();
		new Thread(() -> {
			while(true) {
				LOGGER.debug("下载速度：{}", statisticsSession.downloadSpeed());
				ThreadUtils.sleep(1000);
				torrentSession.torrentStreamGroup().flush();
			}
		}).start();
		ThreadUtils.sleep(100000);
	}

}
