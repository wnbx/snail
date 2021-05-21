package com.acgist.snail.pojo.session;

import java.util.concurrent.atomic.AtomicInteger;

import com.acgist.snail.config.SystemConfig;

/**
 * <p>速度信息</p>
 * 
 * @author acgist
 */
public final class SpeedSession {
	
	/**
	 * <p>采样次数：{@value}</p>
	 */
	private static final byte SAMPLE_SIZE = 10;

	/**
	 * <p>速度</p>
	 */
	private long speed = 0L;
	/**
	 * <p>当前采样位置</p>
	 */
	private byte bufferSampleIndex = 0;
	/**
	 * <p>速度累计采样</p>
	 */
	private final AtomicInteger bufferSample = new AtomicInteger(0);
	/**
	 * <p>最后一次采样时间</p>
	 */
	private volatile long bufferSampleTime = System.currentTimeMillis();
	/**
	 * <p>速度采样集合</p>
	 * <p>每次计算速度时采样一次放入到集合</p>
	 */
	private final int[] bufferSamples = new int[SAMPLE_SIZE];
	/**
	 * <p>采样时间集合</p>
	 */
	private final long[] bufferSampleTimes = new long[SAMPLE_SIZE];
	
	/**
	 * <p>速度采样</p>
	 * 
	 * @param buffer 数据大小
	 */
	public void buffer(int buffer) {
		this.bufferSample.addAndGet(buffer);
	}

	/**
	 * <p>获取速度</p>
	 * <p>超过采样时间：重新计算速度</p>
	 * <p>小于采样时间：返回上次速度</p>
	 * 
	 * @return 速度
	 */
	public long speed() {
		final long time = System.currentTimeMillis();
		final long interval = time - this.bufferSampleTime;
		if(interval >= SystemConfig.REFRESH_INTERVAL_MILLIS) {
			synchronized (this) {
				if(time - this.bufferSampleTime == interval) {
					this.bufferSampleTime = time;
					this.speed = this.calculateSpeed(interval);
				}
			}
		}
		return this.speed;
	}

	/**
	 * <p>计算速度</p>
	 * 
	 * @param interval 时间间隔
	 * 
	 * @return 速度
	 */
	private long calculateSpeed(long interval) {
		this.bufferSamples[this.bufferSampleIndex] = this.bufferSample.getAndSet(0);
		this.bufferSampleTimes[this.bufferSampleIndex] = interval;
		if(++this.bufferSampleIndex >= SAMPLE_SIZE) {
			this.bufferSampleIndex = 0;
		}
		long buffer = 0L;
		long bufferTime = 0L;
		for (int index = 0; index < SAMPLE_SIZE; index++) {
			buffer += this.bufferSamples[index];
			bufferTime += this.bufferSampleTimes[index];
		}
		if(bufferTime <= 0L) {
			return 0L;
		} else {
			return buffer * SystemConfig.ONE_SECOND_MILLIS / bufferTime;
		}
	}

	/**
	 * <p>重置速度统计</p>
	 */
	public void reset() {
		for (int index = 0; index < SAMPLE_SIZE; index++) {
			this.bufferSamples[index] = 0;
			this.bufferSampleTimes[index] = 0;
		}
	}
	
}
