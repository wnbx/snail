package com.acgist.snail.gui.javafx;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.context.SystemContext.SystemType;
import com.acgist.snail.gui.javafx.theme.WindowsTheme;

import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.paint.Color;

/**
 * <p>主题助手</p>
 * <p>使用CMD命令获取（可以使用JNA调用系统接口获取）</p>
 * 
 * @author acgist
 */
public final class Themes {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(Themes.class);

	/**
	 * <p>FXML样式路径：{@value}</p>
	 */
	public static final String FXML_STYLE = "/style/fxml.css";
	/**
	 * <p>图标文件路径（16PX）：{@value}</p>
	 */
	public static final String LOGO_ICON_16 = "/image/16/logo.png";
	/**
	 * <p>图标文件路径（200PX）：{@value}</p>
	 */
	public static final String LOGO_ICON_200 = "/image/logo.png";
	/**
	 * <p>红色：禁用</p>
	 */
	public static final Color COLOR_RED = Color.rgb(0xDD, 0x33, 0x55);
	/**
	 * <p>灰色：禁用</p>
	 */
	public static final Color COLOR_GRAY = Color.rgb(0xCC, 0xCC, 0xCC);
	/**
	 * <p>灰色：可用</p>
	 */
	public static final Color COLOR_BLUD = Color.rgb(0, 153, 204);
	/**
	 * <p>绿色：可用</p>
	 */
	public static final Color COLOR_GREEN = Color.rgb(0x22, 0xAA, 0x22);
	/**
	 * <p>黄色：警告</p>
	 */
	public static final Color COLOR_YELLOW = Color.rgb(0xFF, 0xEE, 0x99);
	/**
	 * <p>托盘样式</p>
	 */
	public static final String CLASS_TRAY = "tray";
	/**
	 * <p>图标样式：{@value}</p>
	 */
	public static final String CLASS_SNAIL_ICON = "snail-icon";
	/**
	 * <p>没有任务样式</p>
	 */
	public static final String CLASS_TASK_EMPTY = "placeholder";
	/**
	 * <p>系统信息样式</p>
	 */
	public static final String CLASS_SYSTEM_INFO = "system-info";
	/**
	 * <p>画图信息样式</p>
	 */
	public static final String CLASS_PAINTER_INFO = "painter-info";
	/**
	 * <p>统计信息样式</p>
	 */
	public static final String CLASS_STATISTICS_INFO = "statistics-info";
	/**
	 * <p>默认主题颜色</p>
	 */
	public static final Color DEFAULT_THEME_COLOR = Themes.COLOR_BLUD;
	/**
	 * <p>系统主题颜色</p>
	 */
	private static final Color SYSTEM_THEME_COLOR;
	/**
	 * <p>系统主题样式</p>
	 */
	private static final String SYSTEM_THEME_STYLE;

	static {
		ITheme themeHandler = null;
		final SystemType systemType = SystemType.local();
		if(systemType == SystemType.WINDOWS) {
			themeHandler = WindowsTheme.newInstance();
		} else {
			LOGGER.info("没有适配系统主题类型：{}", systemType);
		}
		Color color;
		if(themeHandler != null) {
			color = themeHandler.systemThemeColor();
		} else {
			color = DEFAULT_THEME_COLOR;
		}
		// 系统主题颜色
		SYSTEM_THEME_COLOR = color;
		// 十六进制颜色：0x + RRGGBB + OPACITY
		final String colorHex = SYSTEM_THEME_COLOR.toString();
		final StringBuilder themeStyle = new StringBuilder();
		// 设置主题颜色
		themeStyle
			.append("-fx-snail-main-color:#")
			.append(colorHex, 2, colorHex.length() - 2)
			.append(";");
		// 系统主题样式
		SYSTEM_THEME_STYLE = themeStyle.toString();
	}
	
	private Themes() {
	}
	
	/**
	 * <p>获取系统主题样式</p>
	 * 
	 * @return 系统主题样式
	 */
	public static final String getThemeStyle() {
		return SYSTEM_THEME_STYLE;
	}
	
	/**
	 * <p>设置控件主题样式</p>
	 * 
	 * @param scene 场景
	 */
	public static final void applyTheme(Scene scene) {
		final Parent root = scene.getRoot();
		// 设置主题样式
		root.setStyle(Themes.getThemeStyle());
		// 设置样式文件
		root.getStylesheets().add(Themes.FXML_STYLE);
	}
	
}
