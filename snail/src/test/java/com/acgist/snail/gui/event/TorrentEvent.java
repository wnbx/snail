package com.acgist.snail.gui.event;

import com.acgist.snail.gui.event.adapter.TorrentEventAdapter;

/**
 * <p>GUI种子文件选择事件</p>
 * 
 * @author acgist
 */
public final class TorrentEvent extends TorrentEventAdapter {

	private static final TorrentEvent INSTANCE = new TorrentEvent();
	
	public static final TorrentEvent getInstance() {
		return INSTANCE;
	}
	
	private TorrentEvent() {
	}
	
}
