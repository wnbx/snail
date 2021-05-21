package com.acgist.snail.gui.javafx;

import javafx.scene.Scene;
import javafx.scene.control.Tooltip;
import javafx.util.Duration;

/**
 * <p>工具提示助手</p>
 * 
 * @author acgist
 */
public final class Tooltips {
	
	/**
	 * <p>默认显示时间（毫秒）：{@value}</p>
	 */
	public static final int DEFAULT_SHOW_DELAY = 200;
	
	private Tooltips() {
	}
	
	/**
	 * <p>新建工具提示</p>
	 * 
	 * @param value 提示内容
	 * 
	 * @return 工具提示
	 * 
	 * @see #DEFAULT_SHOW_DELAY
	 */
	public static final Tooltip newTooltip(String value) {
		return newTooltip(value, DEFAULT_SHOW_DELAY);
	}
	
	/**
	 * <p>新建工具提示</p>
	 * 
	 * @param value 提示内容
	 * @param millis 显示时间：毫秒
	 * 
	 * @return 工具提示
	 */
	public static final Tooltip newTooltip(String value, int millis) {
		final Tooltip tooltip = new Tooltip(value);
		tooltip.setShowDelay(Duration.millis(millis));
		final Scene scene = tooltip.getScene();
		Themes.applyStyle(scene);
		return tooltip;
	}
	
}
