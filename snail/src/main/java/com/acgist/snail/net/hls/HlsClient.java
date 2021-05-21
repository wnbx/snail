package com.acgist.snail.net.hls;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.config.DownloadConfig;
import com.acgist.snail.config.SystemConfig;
import com.acgist.snail.context.exception.NetException;
import com.acgist.snail.downloader.Downloader;
import com.acgist.snail.net.http.HttpClient;
import com.acgist.snail.pojo.ITaskSession;
import com.acgist.snail.pojo.session.HlsSession;
import com.acgist.snail.utils.FileUtils;
import com.acgist.snail.utils.IoUtils;

/**
 * <p>HLS客户端</p>
 * 
 * @author acgist
 */
public final class HlsClient implements Runnable {

	private static final Logger LOGGER = LoggerFactory.getLogger(HlsClient.class);
	
	/**
	 * <p>下载路径</p>
	 */
	private final String link;
	/**
	 * <p>下载文件路径</p>
	 */
	private final String path;
	/**
	 * <p>文件大小</p>
	 */
	private long size;
	/**
	 * <p>是否支持断点续传</p>
	 */
	private boolean range;
	/**
	 * <p>是否下载完成</p>
	 */
	private volatile boolean completed;
	/**
	 * <p>HLS任务信息</p>
	 */
	private final HlsSession hlsSession;
	/**
	 * <p>输入流</p>
	 */
	protected ReadableByteChannel input;
	/**
	 * <p>输出流</p>
	 */
	protected WritableByteChannel output;
	
	/**
	 * @param link 下载路径
	 * @param taskSession 任务信息
	 * @param hlsSession HLS任务信息
	 */
	public HlsClient(String link, ITaskSession taskSession, HlsSession hlsSession) {
		this.link = link;
		final String fileName = FileUtils.fileName(link);
		this.path = FileUtils.file(taskSession.getFile(), fileName);
		this.range = false;
		this.completed = false;
		this.hlsSession = hlsSession;
	}

	@Override
	public void run() {
		if(!this.downloadable()) {
			LOGGER.debug("HLS任务不能下载：{}", this.link);
			return;
		}
		LOGGER.debug("HLS任务下载文件：{}", this.link);
		// 已经下载大小
		long downloadSize = FileUtils.fileSize(this.path);
		this.completed = this.checkCompleted(downloadSize);
		if(this.completed) {
			LOGGER.debug("HLS文件校验成功：{}", this.link);
		} else {
			int length = 0;
			final ByteBuffer buffer = ByteBuffer.allocateDirect(SystemConfig.DEFAULT_EXCHANGE_LENGTH);
			try {
				this.buildInput(downloadSize);
				this.buildOutput();
				// 不支持断点续传：重置已经下载大小
				if(!this.range) {
					downloadSize = 0L;
				}
				while(this.downloadable()) {
					length = this.input.read(buffer);
					if(length >= 0) {
						buffer.flip();
						this.output.write(buffer);
						buffer.clear();
						downloadSize += length;
						this.hlsSession.download(length);
					}
					if(Downloader.checkFinish(length, downloadSize, this.size)) {
						this.completed = true;
						break;
					}
				}
			} catch (Exception e) {
				LOGGER.error("HLS任务下载异常：{}", this.link, e);
			}
		}
		this.release();
		if(this.completed) {
			LOGGER.debug("HLS文件下载完成：{}", this.link);
			this.hlsSession.remove(this);
			this.hlsSession.downloadSize(downloadSize);
			this.hlsSession.checkCompletedAndDone();
		} else {
			LOGGER.debug("HLS文件下载失败：{}", this.link);
			// 下载失败重新添加下载
			this.hlsSession.download(this);
		}
	}

	/**
	 * <p>判断是否可以下载</p>
	 * 
	 * @return 是否可以下载
	 */
	private boolean downloadable() {
		return !this.completed && this.hlsSession.downloadable();
	}
	
	/**
	 * <p>校验是否下载完成</p>
	 * 
	 * @param downloadSize 已经下载大小
	 * 
	 * @return 是否下载完成
	 */
	private boolean checkCompleted(final long downloadSize) {
		// 如果文件已经下载完成直接返回
		if(this.completed) {
			return this.completed;
		}
		final File file = new File(this.path);
		if(!file.exists()) {
			return false;
		}
		try {
			final var header = HttpClient
				.newInstance(this.link)
				.head()
				.responseHeader();
			this.size = header.fileSize();
			return this.size == downloadSize;
		} catch (NetException e) {
			LOGGER.error("HLS文件校验异常：{}", this.link, e);
		}
		return false;
	}

	/**
	 * <p>新建{@linkplain #input 输入流}</p>
	 * 
	 * @param downloadSize 已经下载大小
	 * 
	 * @throws NetException 网络异常
	 */
	private void buildInput(final long downloadSize) throws NetException {
		final var client = HttpClient
			.newDownloader(this.link)
			.range(downloadSize)
			.get();
		if(client.downloadable()) {
			final var headers = client.responseHeader();
			this.range = headers.range();
			this.input = Channels.newChannel(client.response());
		} else {
			throw new NetException("HLS客户端输入流新建失败");
		}
	}
	
	/**
	 * <p>新建{@linkplain #output 输出流}</p>
	 * 
	 * @throws NetException 网络异常
	 */
	private void buildOutput() throws NetException {
		try {
			final int bufferSize = DownloadConfig.getMemoryBufferByte(this.size);
			OutputStream outputStream;
			if(this.range) {
				// 支持断点续传
				outputStream = new FileOutputStream(this.path, true);
			} else {
				// 不支持断点续传
				outputStream = new FileOutputStream(this.path);
			}
			final BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(outputStream, bufferSize);
			this.output = Channels.newChannel(bufferedOutputStream);
		} catch (FileNotFoundException e) {
			throw new NetException("HLS客户端输出流新建失败", e);
		}
	}

	/**
	 * <p>释放资源</p>
	 */
	public void release() {
		LOGGER.debug("HLS客户端释放：{}", this.link);
		IoUtils.close(this.input);
		IoUtils.close(this.output);
	}
	
}
