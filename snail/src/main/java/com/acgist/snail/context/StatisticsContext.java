package com.acgist.snail.context;

import com.acgist.snail.IContext;
import com.acgist.snail.pojo.ISpeedGetter;
import com.acgist.snail.pojo.StatisticsGetter;
import com.acgist.snail.pojo.session.StatisticsSession;

/**
 * <p>系统统计上下文</p>
 * <p>系统统计：累计下载、累计上传、速度采样</p>
 * <p>系统只限制单个任务的速度，如果需要限制整个系统的速度可以打开{@linkplain #statistics 系统统计上下文}限速。</p>
 * 
 * @author acgist
 */
public final class StatisticsContext extends StatisticsGetter implements IContext, ISpeedGetter {
	
	private static final StatisticsContext INSTANCE = new StatisticsContext();
	
	public static final StatisticsContext getInstance() {
		return INSTANCE;
	}
	
	private StatisticsContext() {
		super(new StatisticsSession());
	}
	
	@Override
	public long uploadSpeed() {
		return this.statistics.uploadSpeed();
	}
	
	@Override
	public long downloadSpeed() {
		return this.statistics.downloadSpeed();
	}

	@Override
	public void resetUploadSpeed() {
		this.statistics.resetUploadSpeed();
	}

	@Override
	public void resetDownloadSpeed() {
		this.statistics.resetDownloadSpeed();
	}

}
