package com.acgist.snail.downloader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.context.exception.DownloadException;
import com.acgist.snail.context.exception.NetException;
import com.acgist.snail.pojo.ITaskSession;
import com.acgist.snail.utils.FileUtils;

/**
 * <p>多文件任务下载器</p>
 * 
 * @author acgist
 */
public abstract class MultifileDownloader extends Downloader {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(MultifileDownloader.class);

	/**
	 * <p>下载锁</p>
	 * <p>下载时阻塞下载任务线程</p>
	 */
	protected final Object downloadLock = new Object();
	
	/**
	 * @param taskSession 任务信息
	 */
	protected MultifileDownloader(ITaskSession taskSession) {
		super(taskSession);
	}
	
	@Override
	public void open() throws NetException, DownloadException {
		// 新建文件目录：防止删除目录导致任务下载失败
		FileUtils.buildFolder(this.taskSession.getFile());
		this.loadDownload();
	}

	@Override
	public void download() throws DownloadException {
		while(this.downloadable()) {
			synchronized (this.downloadLock) {
				try {
					// 防止过长时间下载（失败时间等待）：验证下载数据是否变化判断任务是否失败
					this.downloadLock.wait(Long.MAX_VALUE);
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
					LOGGER.debug("线程等待异常", e);
				}
				this.completed = this.checkCompleted();
			}
		}
	}
	
	@Override
	public void unlockDownload() {
		super.unlockDownload();
		synchronized (this.downloadLock) {
			this.downloadLock.notifyAll();
		}
	}
	
	/**
	 * <p>加载下载</p>
	 * 
	 * @throws DownloadException 下载异常
	 */
	protected abstract void loadDownload() throws DownloadException;

	/**
	 * <p>判断是否下载完成</p>
	 * 
	 * @return 是否下载完成
	 */
	protected abstract boolean checkCompleted();
	
}
