package com.acgist.snail.gui.javafx.window;

import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.utils.CollectionUtils;

import javafx.fxml.Initializable;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;

/**
 * <p>窗口控制器</p>
 * 
 * @author acgist
 */
public abstract class Controller implements Initializable {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(Controller.class);

	/**
	 * <p>双击次数：{@value}</p>
	 */
	protected static final int DOUBLE_CLICK_COUNT = 2;
	
	/**
	 * <p>获取拖入面板数据</p>
	 * <p>支持数据：链接、文本、文件（路径）</p>
	 * 
	 * @param event 拖拽事件
	 * 
	 * @return 链接、文本、文件（路径）
	 */
	protected final String dragboard(DragEvent event) {
		final Dragboard dragboard = event.getDragboard();
		if (dragboard.hasFiles()) {
			// 优先判断文件
			final var files = dragboard.getFiles();
			if (CollectionUtils.isNotEmpty(files)) {
				for (File file : files) {
					if(file.isFile()) {
						return file.getAbsolutePath();
					}
				}
			}
		} else if (dragboard.hasUrl()) {
			return dragboard.getUrl();
		} else if (dragboard.hasString()) {
			return dragboard.getString();
		}
		return null;
	}

	/**
	 * <p>释放资源</p>
	 */
	public void release() {
		LOGGER.debug("释放控制器资源");
	}
	
}
