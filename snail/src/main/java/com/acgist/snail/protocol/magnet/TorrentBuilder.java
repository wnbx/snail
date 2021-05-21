package com.acgist.snail.protocol.magnet;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.config.SystemConfig;
import com.acgist.snail.context.NodeContext;
import com.acgist.snail.context.exception.PacketSizeException;
import com.acgist.snail.format.BEncodeDecoder;
import com.acgist.snail.format.BEncodeEncoder;
import com.acgist.snail.pojo.bean.InfoHash;
import com.acgist.snail.pojo.bean.Torrent;
import com.acgist.snail.protocol.Protocol;
import com.acgist.snail.utils.CollectionUtils;
import com.acgist.snail.utils.DateUtils;
import com.acgist.snail.utils.FileUtils;
import com.acgist.snail.utils.NetUtils;

/**
 * <p>种子文件Builder</p>
 * 
 * @author acgist
 */
public final class TorrentBuilder {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(TorrentBuilder.class);
	
	/**
	 * <p>InfoHash</p>
	 */
	private final InfoHash infoHash;
	/**
	 * <p>Tracker服务器</p>
	 */
	private final List<String> trackers;
	
	/**
	 * @param infoHash InfoHash
	 * @param trackers Tracker服务器
	 */
	private TorrentBuilder(InfoHash infoHash, List<String> trackers) {
		this.infoHash = infoHash;
		this.trackers = trackers;
	}
	
	/**
	 * <p>新建种子文件Builder</p>
	 * 
	 * @param infoHash InfoHash
	 * @param trackers Tracker服务器
	 * 
	 * @return 种子文件Builder
	 */
	public static final TorrentBuilder newInstance(InfoHash infoHash, List<String> trackers) {
		return new TorrentBuilder(infoHash, trackers);
	}
	
	/**
	 * <p>新建种子文件</p>
	 * 
	 * @param path 保存目录
	 * 
	 * @return 文件路径
	 */
	public String buildFile(String path) {
		final String fileName = this.buildFileName();
		final String filePath = FileUtils.file(path, fileName);
		final Map<String, Object> fileInfo = this.buildFileInfo();
		this.buildFile(filePath, fileInfo);
		return filePath;
	}

	/**
	 * <p>新建种子信息</p>
	 * 
	 * @return 种子信息
	 */
	private Map<String, Object> buildFileInfo() {
		final Map<String, Object> data = new LinkedHashMap<>();
		data.put(Torrent.ATTR_COMMENT, SystemConfig.getSource());
		data.put(Torrent.ATTR_COMMENT_UTF8, SystemConfig.getSource());
		data.put(Torrent.ATTR_ENCODING, SystemConfig.DEFAULT_CHARSET);
		data.put(Torrent.ATTR_CREATED_BY, SystemConfig.getNameEnAndVersion());
		data.put(Torrent.ATTR_CREATION_DATE, DateUtils.unixTimestamp());
		this.buildAnnounce(data);
		this.buildInfo(data);
		this.buildNodes(data);
		return data;
	}

	/**
	 * <p>设置Tracker服务器</p>
	 * 
	 * @param data 种子信息
	 */
	private void buildAnnounce(Map<String, Object> data) {
		if(CollectionUtils.isEmpty(this.trackers)) {
			return;
		}
		data.put(Torrent.ATTR_ANNOUNCE, this.trackers.get(0));
		if(this.trackers.size() > 1) {
			data.put(
				Torrent.ATTR_ANNOUNCE_LIST,
				this.trackers.stream()
					.skip(1)
					.map(List::of)
					.collect(Collectors.toList())
			);
		}
	}
	
	/**
	 * <p>设置种子信息</p>
	 * 
	 * @param data 种子信息
	 */
	private void buildInfo(Map<String, Object> data) {
		try {
			final var decoder = BEncodeDecoder.newInstance(this.infoHash.info());
			data.put(Torrent.ATTR_INFO, decoder.nextMap());
		} catch (PacketSizeException e) {
			LOGGER.error("设置种子信息异常", e);
		}
	}

	/**
	 * <p>设置DHT节点</p>
	 * 
	 * @param data 种子信息
	 */
	private void buildNodes(Map<String, Object> data) {
		final var sessions = NodeContext.getInstance().findNode(this.infoHash.infoHash());
		if(CollectionUtils.isNotEmpty(sessions)) {
			final var nodes = sessions.stream()
				.filter(session -> NetUtils.ip(session.getHost()))
				.map(session -> List.of(session.getHost(), session.getPort()))
				.collect(Collectors.toList());
			if(CollectionUtils.isNotEmpty(nodes)) {
				data.put(Torrent.ATTR_NODES, nodes);
			}
		}
	}
	
	/**
	 * <p>获取文件名称</p>
	 * 
	 * @return 文件名称
	 */
	private String buildFileName() {
		return this.infoHash.infoHashHex() + Protocol.Type.TORRENT.defaultSuffix();
	}

	/**
	 * <p>保存种子文件</p>
	 * 
	 * @param filePath 文件路径
	 * @param fileInfo 文件数据
	 */
	private void buildFile(String filePath, Map<String, Object> fileInfo) {
		final File file = new File(filePath);
		if(file.exists()) {
			LOGGER.debug("种子文件已经存在：{}", filePath);
			return;
		}
		LOGGER.debug("保存种子文件：{}", filePath);
		final byte[] bytes = BEncodeEncoder.encodeMap(fileInfo);
		FileUtils.write(filePath, bytes);
	}

}
