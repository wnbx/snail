package com.acgist.snail.pojo.session;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicLong;

import javax.crypto.Cipher;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.config.SystemConfig;
import com.acgist.snail.context.HlsContext;
import com.acgist.snail.context.SystemThreadContext;
import com.acgist.snail.net.hls.HlsClient;
import com.acgist.snail.pojo.IStatisticsSession;
import com.acgist.snail.pojo.ITaskSession;
import com.acgist.snail.pojo.bean.M3u8;

/**
 * <p>HSL任务信息</p>
 * 
 * @author acgist
 */
public final class HlsSession {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(HlsSession.class);

	/**
	 * <p>下载线程数量</p>
	 */
	private static final int POOL_SIZE = SystemConfig.getHlsThreadSize();
	
	/**
	 * <p>下载状态</p>
	 */
	private volatile boolean downloadable = false;
	/**
	 * <p>M3U8</p>
	 */
	private final M3u8 m3u8;
	/**
	 * <p>文件总数量</p>
	 */
	private final int fileSize;
	/**
	 * <p>累计下载大小</p>
	 */
	private final AtomicLong downloadSize;
	/**
	 * <p>任务信息</p>
	 */
	private final ITaskSession taskSession;
	/**
	 * <p>统计信息</p>
	 */
	private final IStatisticsSession statistics;
	/**
	 * <p>HLS客户端</p>
	 * <p>HLS客户端下载成功移除列表，否者重新添加继续下载。</p>
	 */
	private final List<HlsClient> clients;
	/**
	 * <p>线程池</p>
	 */
	private ExecutorService executor;
	
	/**
	 * @param m3u8 M3U8
	 * @param taskSession 任务信息
	 */
	private HlsSession(M3u8 m3u8, ITaskSession taskSession) {
		final var links = taskSession.multifileSelected();
		this.m3u8 = m3u8;
		this.fileSize = links.size();
		this.downloadSize = new AtomicLong();
		this.taskSession = taskSession;
		this.statistics = taskSession.statistics();
		this.clients = new ArrayList<>(this.fileSize);
		for (String link : links) {
			final var client = new HlsClient(link, taskSession, this);
			this.clients.add(client);
		}
	}
	
	/**
	 * <p>新建HLS任务信息</p>
	 * 
	 * @param m3u8 M3U8
	 * @param taskSession 任务信息
	 * 
	 * @return {@link HlsSession}
	 */
	public static final HlsSession newInstance(M3u8 m3u8, ITaskSession taskSession) {
		return new HlsSession(m3u8, taskSession);
	}
	
	/**
	 * <p>获取加密套件</p>
	 * 
	 * @return 加密套件
	 */
	public Cipher cipher() {
		if(this.m3u8 == null) {
			return null;
		}
		return this.m3u8.getCipher();
	}
	
	/**
	 * <p>开始下载</p>
	 * 
	 * @return 是否下载完成
	 */
	public boolean download() {
		if(this.downloadable) {
			LOGGER.debug("任务已经开始下载");
			return false;
		}
		// 修改开始下载：提交client需要判断
		this.downloadable = true;
		this.executor = SystemThreadContext.newExecutor(POOL_SIZE, POOL_SIZE, 10000, 60L, SystemThreadContext.SNAIL_THREAD_HLS);
		synchronized (this.clients) {
			this.clients.forEach(this::download);
		}
		return this.checkCompleted();
	}
	
	/**
	 * <p>添加下载客户端</p>
	 * 
	 * @param client 客户端
	 */
	public void download(HlsClient client) {
		if(this.downloadable) {
			this.executor.submit(client);
		}
	}
	
	/**
	 * <p>移除下载客户端</p>
	 * 
	 * @param client 客户端
	 */
	public void remove(HlsClient client) {
		synchronized (this.clients) {
			this.clients.remove(client);
		}
	}
	
	/**
	 * <p>统计下载数据</p>
	 * 
	 * @param buffer 下载大小
	 */
	public void download(int buffer) {
		this.statistics.download(buffer);
		this.statistics.downloadLimit(buffer);
	}
	
	/**
	 * <p>设置已经下载大小</p>
	 * <p>注意：下载大小通过计算预计得出</p>
	 * 
	 * @param size 下载文件大小
	 */
	public void downloadSize(long size) {
		// 设置已经下载大小
		final long newDownloadSize = this.downloadSize.addAndGet(size);
		this.taskSession.downloadSize(newDownloadSize);
		// 已经下载文件数量
		int downloadFileSize;
		synchronized (this.clients) {
			downloadFileSize = this.fileSize - this.clients.size();
		}
		// 预测文件总大小：存在误差
		final long taskFileSize = newDownloadSize * this.fileSize / downloadFileSize;
		this.taskSession.setSize(taskFileSize);
	}
	
	/**
	 * <p>判断是否可以下载</p>
	 * 
	 * @return 是否可以下载
	 */
	public boolean downloadable() {
		return this.downloadable;
	}

	/**
	 * <p>校验是否完成</p>
	 * 
	 * @return 是否完成
	 */
	public boolean checkCompleted() {
		synchronized (this.clients) {
			return this.clients.isEmpty();
		}
	}
	
	/**
	 * <p>校验是否完成</p>
	 * <p>注意：不要在该方法中实现释放资源等非幂等操作（可能会被多次调用）</p>
	 */
	public void checkCompletedAndDone() {
		if(this.checkCompleted()) {
			this.taskSession.unlockDownload();
		}
	}
	
	/**
	 * <p>释放资源</p>
	 */
	public void release() {
		LOGGER.debug("HLS任务释放资源：{}", this.taskSession.getName());
		this.downloadable = false;
		synchronized (this.clients) {
			this.clients.forEach(HlsClient::release);
		}
		SystemThreadContext.shutdownNow(this.executor);
	}

	/**
	 * <p>删除任务信息</p>
	 */
	public void delete() {
		HlsContext.getInstance().remove(this.taskSession);
	}
	
}
