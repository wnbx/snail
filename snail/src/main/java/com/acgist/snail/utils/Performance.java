package com.acgist.snail.utils;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.config.SystemConfig;
import com.acgist.snail.context.SystemThreadContext;

/**
 * <p>性能分析工具</p>
 * 
 * @author acgist
 */
public abstract class Performance {

	protected static final Logger LOGGER = LoggerFactory.getLogger(Performance.class);
	
	/**
	 * <p>跳过执行方法属性：{@value}</p>
	 */
	private static final String SKIP_COSTED_NAME = "skipCosted";
	/**
	 * <p>是否跳过执行方法：{@value}</p>
	 */
	private static final String SKIP_COSTED_SKIP = "skip";
	/**
	 * <p>是否跳过执行方法：{@value}</p>
	 */
	private static final String SKIP_COSTED_TRUE = "true";
	/**
	 * <p>是否跳过执行方法</p>
	 * <p>跳过执行方法：性能测试、测试时间过长</p>
	 */
	protected static final boolean SKIP_COSTED;
	
	static {
		final String skipCosted = System.getProperty(SKIP_COSTED_NAME);
		SKIP_COSTED = SKIP_COSTED_SKIP.equalsIgnoreCase(skipCosted) || SKIP_COSTED_TRUE.equalsIgnoreCase(skipCosted);
	}
	
	/**
	 * <p>消耗时间统计</p>
	 */
	protected final AtomicLong costTime = new AtomicLong();
	
	/**
	 * <p>记录日志</p>
	 * 
	 * @param message 日志信息
	 */
	protected final void log(Object message) {
		this.log(null, message);
	}
	
	/**
	 * <p>记录日志</p>
	 * 
	 * @param message 日志信息
	 * @param args 日志参数
	 */
	protected final void log(String message, Object ... args) {
		if(message == null) {
			message = "{}";
		}
		LOGGER.info(message, args);
	}
	
	/**
	 * <p>开始统计消耗时间</p>
	 */
	protected final void cost() {
		this.costTime.set(System.currentTimeMillis());
	}
	
	/**
	 * <p>结束统计消耗时间</p>
	 * <p>重置消耗时间统计</p>
	 * 
	 * @return 消耗时间
	 */
	protected final long costed() {
		final long time = System.currentTimeMillis();
		final long costed = time - this.costTime.getAndSet(time);
		if(LOGGER.isInfoEnabled()) {
			LOGGER.info("""
				消耗时间（毫秒）：{}
				消耗时间（秒）：{}""",
				costed,
				costed / SystemConfig.ONE_SECOND_MILLIS
			);
		}
		return costed;
	}
	
	/**
	 * <p>重复执行消耗任务</p>
	 * 
	 * @param count 执行次数
	 * @param coster 消耗任务
	 * 
	 * @return 消耗时间
	 */
	protected final long costed(int count, Coster coster) {
		if(SKIP_COSTED) {
			this.log("跳过消耗测试");
			return 0L;
		}
		this.cost();
		for (int index = 0; index < count; index++) {
			coster.execute();
		}
		return this.costed();
	}
	
	/**
	 * <p>重复执行消耗任务</p>
	 * 
	 * @param count 任务数量
	 * @param thread 线程数量
	 * @param coster 消耗任务
	 * 
	 * @return 消耗时间
	 */
	protected final long costed(int count, int thread, Coster coster) {
		if(SKIP_COSTED) {
			this.log("跳过消耗测试");
			return 0L;
		}
		final var latch = new CountDownLatch(count);
		final var executor = SystemThreadContext.newExecutor(thread, thread, count, 60L, SystemThreadContext.SNAIL_THREAD_COSTED);
		this.cost();
		for (int index = 0; index < count; index++) {
			executor.submit(() -> {
				try {
					coster.execute();
				} catch (Exception e) {
					LOGGER.error("执行任务异常", e);
				} finally {
					latch.countDown();
				}
			});
		}
		try {
			latch.await();
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			LOGGER.error("线程等待异常", e);
		}
		final long costed = this.costed();
		SystemThreadContext.shutdownNow(executor);
		return costed;
	}

	/**
	 * <p>执行任务接口</p>
	 * 
	 * @author acgist
	 */
	public interface Coster {

		/**
		 * <p>执行任务</p>
		 */
		public void execute();
		
	}
	
}
